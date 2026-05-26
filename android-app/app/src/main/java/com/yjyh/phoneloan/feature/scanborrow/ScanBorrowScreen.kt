package com.yjyh.phoneloan.feature.scanborrow

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CenterFocusWeak
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.yjyh.phoneloan.core.analytics.AnalyticsLogger
import com.yjyh.phoneloan.core.data.PhoneLoanData
import com.yjyh.phoneloan.core.design.AppCard
import com.yjyh.phoneloan.core.design.AppColors
import com.yjyh.phoneloan.core.design.InteractiveField
import com.yjyh.phoneloan.core.design.MutedText
import com.yjyh.phoneloan.core.design.Page
import com.yjyh.phoneloan.core.design.PrimaryButton
import com.yjyh.phoneloan.core.design.SecondaryButton
import com.yjyh.phoneloan.core.model.DeviceStatus
import com.yjyh.phoneloan.core.model.UserSummary
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ScanBorrowScreen(
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onRegisterDevice: (imei: String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val repository = PhoneLoanData.repository
    val user = repository.currentUser()
    val meSummary = remember { UserSummary(user.id, user.employeeNo, user.name) }

    var permissionGranted by remember { mutableStateOf(hasCameraPermission(context)) }
    var permissionDenied by remember { mutableStateOf(false) }
    var manualImeiInput by remember { mutableStateOf("") }
    var scannedImei by remember { mutableStateOf("") }
    var scanState by remember { mutableStateOf(ScanState.SCANNING) }
    var scanSession by remember { mutableIntStateOf(0) }
    var foundDeviceName by remember { mutableStateOf("") }
    var foundDeviceId by remember { mutableStateOf("") }
    var foundOwnerName by remember { mutableStateOf("") }
    var foundOwnerNo by remember { mutableStateOf("") }
    var foundHolderName by remember { mutableStateOf("") }
    var foundHolderNo by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var borrowSuccess by remember { mutableStateOf(false) }
    var borrowLoading by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGranted = granted
        permissionDenied = !granted
        AnalyticsLogger.trackAction(
            name = "camera_permission_result",
            screen = "scan_borrow",
            payload = mapOf("granted" to granted)
        )
        if (granted) {
            scanState = ScanState.SCANNING
            scanSession += 1
        }
    }

    fun resetResultState() {
        scannedImei = ""
        foundDeviceName = ""
        foundDeviceId = ""
        foundOwnerName = ""
        foundOwnerNo = ""
        foundHolderName = ""
        foundHolderNo = ""
        errorMessage = ""
        borrowSuccess = false
        borrowLoading = false
    }

    fun resolveImei(imei: String) {
        resetResultState()
        scannedImei = imei
        val device = repository.findDeviceByImei(imei)
        if (device != null) {
            AnalyticsLogger.trackAction(
                name = "imei_resolved_existing_device",
                screen = "scan_borrow",
                payload = mapOf("imei" to imei, "deviceId" to device.id)
            )
            scanState = ScanState.FOUND
            foundDeviceName = device.name
            foundDeviceId = device.id
            foundOwnerName = device.owner.name
            foundOwnerNo = device.owner.employeeNo
            foundHolderName = device.currentHolder?.name ?: "暂无"
            foundHolderNo = device.currentHolder?.employeeNo ?: ""
        } else {
            AnalyticsLogger.trackAction(
                name = "imei_resolved_unregistered_device",
                screen = "scan_borrow",
                payload = mapOf("imei" to imei)
            )
            scanState = ScanState.NOT_FOUND
        }
    }

    fun retryScan() {
        resetResultState()
        manualImeiInput = ""
        scanState = ScanState.SCANNING
        scanSession += 1
    }

    fun submitManualImei() {
        val trimmed = manualImeiInput.trim()
        if (!ImeiParser.isValid(trimmed)) {
            errorMessage = "请输入 15 位纯数字 IMEI 编码"
            AnalyticsLogger.trackError(
                name = "manual_imei_invalid",
                screen = "scan_borrow",
                payload = mapOf("length" to trimmed.length)
            )
            return
        }
        AnalyticsLogger.trackAction("manual_imei_submit", screen = "scan_borrow")
        resolveImei(trimmed)
    }

    Page(title = pageTitle(scanState), contentPadding = contentPadding, topLink = "‹ 返回", onTopLink = onBack) {
        when (scanState) {
            ScanState.MANUAL_INPUT -> {
                ManualInputState(
                    imeiInput = manualImeiInput,
                    errorMessage = errorMessage,
                    onImeiInputChange = {
                        manualImeiInput = it
                        errorMessage = ""
                    },
                    onSubmit = { submitManualImei() },
                    onBackToScan = { retryScan() }
                )
            }

            ScanState.SCANNING -> {
                when {
                    !permissionGranted && permissionDenied -> {
                        PermissionDeniedState(
                            onOpenPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            onManualInput = {
                                AnalyticsLogger.trackAction("manual_imei_entry_click", screen = "scan_borrow")
                                resetResultState()
                                scanState = ScanState.MANUAL_INPUT
                            }
                        )
                    }

                    !permissionGranted -> {
                        PermissionIntroState(
                            onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            onManualInput = {
                                AnalyticsLogger.trackAction("manual_imei_entry_click", screen = "scan_borrow")
                                resetResultState()
                                scanState = ScanState.MANUAL_INPUT
                            }
                        )
                    }

                    else -> {
                        CameraScanningState(
                            scanSession = scanSession,
                            onImeiDetected = { resolveImei(it) },
                            onInvalidBarcode = {
                                errorMessage = "未从条码 / 二维码中解析到 15 位 IMEI"
                                AnalyticsLogger.trackError("barcode_parse_failed", screen = "scan_borrow")
                                scanState = ScanState.SCAN_FAILED
                            }
                        )
                        SecondaryButton("手动输入 IMEI", onClick = {
                            AnalyticsLogger.trackAction("manual_imei_entry_click", screen = "scan_borrow")
                            resetResultState()
                            scanState = ScanState.MANUAL_INPUT
                        })
                    }
                }
            }

            ScanState.SCAN_FAILED -> {
                ScanFailedState(
                    message = errorMessage.ifBlank { "未从条码 / 二维码中解析到 15 位 IMEI" },
                    onRetry = { retryScan() },
                    onManualInput = {
                        AnalyticsLogger.trackAction("manual_imei_entry_click", screen = "scan_borrow")
                        resetResultState()
                        scanState = ScanState.MANUAL_INPUT
                    }
                )
            }

            ScanState.FOUND -> {
                FoundDeviceState(
                    scannedImei = scannedImei,
                    deviceName = foundDeviceName,
                    holderName = foundHolderName,
                    holderNo = foundHolderNo,
                    ownerName = foundOwnerName,
                    ownerNo = foundOwnerNo,
                    borrowSuccess = borrowSuccess,
                    borrowLoading = borrowLoading,
                    errorMessage = errorMessage,
                    onConfirmBorrow = {
                        if (borrowLoading) {
                            return@FoundDeviceState
                        }
                        AnalyticsLogger.trackAction(
                            name = "confirm_borrow_click",
                            screen = "scan_borrow",
                            payload = mapOf("deviceId" to foundDeviceId)
                        )
                        borrowLoading = true
                        errorMessage = ""
                        scope.launch {
                            val result = withContext(Dispatchers.IO) {
                                repository.borrowDeviceResult(
                                    deviceId = foundDeviceId,
                                    newHolder = meSummary,
                                    newStatus = DeviceStatus.HELD_BY_ME
                                )
                            }
                            borrowLoading = false
                            result
                                .onSuccess { borrowSuccess = true }
                                .onFailure {
                                    errorMessage = it.message ?: "借走失败，请稍后再试"
                                    AnalyticsLogger.trackError("borrow_visible_error", screen = "scan_borrow", throwable = it)
                                }
                        }
                    },
                    onRetry = { retryScan() }
                )
            }

            ScanState.NOT_FOUND -> {
                NotFoundState(
                    scannedImei = scannedImei,
                    onRegisterDevice = { onRegisterDevice(scannedImei) },
                    onRetry = { retryScan() }
                )
            }
        }
    }
}

@Composable
private fun PermissionIntroState(
    onRequestPermission: () -> Unit,
    onManualInput: () -> Unit
) {
    DarkCameraFrame(title = "等待相机权限", subtitle = "授权后用于扫描手机 IMEI 条码") {
        Icon(
            imageVector = Icons.Outlined.CameraAlt,
            contentDescription = null,
            tint = Color.White
        )
    }
    AppCard {
        Text("需要相机权限", fontWeight = FontWeight.Bold, color = AppColors.Text)
        MutedText("扫码借由借机人发起。权限仅用于识别要借走手机的 IMEI，不会保存相机画面。")
        MutedText("识别成功后按已建档 / 未建档进入确认借走或手机注册。")
    }
    PrimaryButton("允许相机权限", onClick = onRequestPermission)
    SecondaryButton("手动输入 IMEI", onClick = onManualInput)
}

@Composable
private fun PermissionDeniedState(
    onOpenPermission: () -> Unit,
    onManualInput: () -> Unit
) {
    AppCard {
        Icon(
            imageVector = Icons.Outlined.WarningAmber,
            contentDescription = null,
            tint = AppColors.Warning
        )
        Text("相机权限未开启", fontWeight = FontWeight.Bold, color = AppColors.Text)
        MutedText("无法打开相机扫码。可以再次请求权限，或先手动输入 15 位 IMEI 继续借机。")
    }
    PrimaryButton("去开启权限", onClick = onOpenPermission)
    SecondaryButton("手动输入 IMEI", onClick = onManualInput)
    AppCard(modifier = Modifier.background(AppColors.PrimarySoft)) {
        Text("业务规则", fontWeight = FontWeight.Bold, color = AppColors.Primary)
        MutedText("权限拒绝不阻断借机流程，手动输入兜底仍需进入同一套已建档 / 未建档分支。")
    }
}

@Composable
private fun CameraScanningState(
    scanSession: Int,
    onImeiDetected: (String) -> Unit,
    onInvalidBarcode: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(256.dp)
            .background(Color(0xFF17212B), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        BarcodeCameraPreview(
            scanSession = scanSession,
            onImeiDetected = onImeiDetected,
            onInvalidBarcode = onInvalidBarcode
        )
        Icon(
            imageVector = Icons.Outlined.CenterFocusWeak,
            contentDescription = null,
            tint = Color.White
        )
    }
    AppCard {
        Text("正在扫描 IMEI", fontWeight = FontWeight.Bold, color = AppColors.Text)
        MutedText("请对准手机工程界面或条码中的 IMEI。识别到 15 位 IMEI 后会自动进入下一步。")
    }
}

@Composable
private fun ScanFailedState(
    message: String,
    onRetry: () -> Unit,
    onManualInput: () -> Unit
) {
    DarkCameraFrame(title = "未识别到有效 IMEI", subtitle = "请对准手机条码或工程信息页") {
        Icon(
            imageVector = Icons.Outlined.CenterFocusWeak,
            contentDescription = null,
            tint = Color.White
        )
    }
    AppCard {
        Text("扫码失败", fontWeight = FontWeight.Bold, color = AppColors.Error)
        MutedText(message)
        MutedText("可以重新扫描，或手动输入 IMEI 继续流程。")
    }
    PrimaryButton("重新扫描", onClick = onRetry)
    SecondaryButton("手动输入 IMEI", onClick = onManualInput)
}

@Composable
private fun ManualInputState(
    imeiInput: String,
    errorMessage: String,
    onImeiInputChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBackToScan: () -> Unit
) {
    AppCard {
        InteractiveField(
            label = "IMEI",
            value = imeiInput,
            onValueChange = onImeiInputChange,
            placeholder = "请输入 15 位 IMEI",
            keyboardType = KeyboardType.Number
        )
        MutedText("手动输入只作为扫码失败或权限不可用时的兜底入口，提交后仍按仓库是否已有该 IMEI 分流。")
    }
    AppCard {
        Text("提交后的系统判断", fontWeight = FontWeight.Bold, color = AppColors.Primary)
        MutedText("已建档：展示被借手机详情，点击确认借走。")
        MutedText("未建档：进入手机注册页，填写手机名后建档。")
    }
    if (errorMessage.isNotEmpty()) {
        Text(errorMessage, color = AppColors.Error, fontWeight = FontWeight.Bold)
    }
    PrimaryButton("继续", onClick = onSubmit)
    SecondaryButton("返回扫码", onClick = onBackToScan)
}

@Composable
private fun FoundDeviceState(
    scannedImei: String,
    deviceName: String,
    holderName: String,
    holderNo: String,
    ownerName: String,
    ownerNo: String,
    borrowSuccess: Boolean,
    borrowLoading: Boolean,
    errorMessage: String,
    onConfirmBorrow: () -> Unit,
    onRetry: () -> Unit
) {
    AppCard {
        MutedText("识别到完整 IMEI")
        Text(scannedImei, fontWeight = FontWeight.Bold, color = AppColors.Text)
        MutedText("被借手机")
        Text("$deviceName · 已建档", fontWeight = FontWeight.Bold)
    }
    AppCard {
        Text("当前记录", fontWeight = FontWeight.Bold)
        MutedText("上一位持有人：$holderName · $holderNo")
        MutedText("绑定 owner：$ownerName · $ownerNo")
        MutedText("状态：可被借走，确认后系统自动更新持有人")
    }
    if (!borrowSuccess) {
        if (errorMessage.isNotEmpty()) {
            AppCard {
                Text(errorMessage, color = AppColors.Error, fontWeight = FontWeight.Bold)
                MutedText("设备状态可能已变化，请重新扫描或稍后重试。")
            }
        }
        PrimaryButton(if (borrowLoading) "正在确认" else "确认借走", onClick = onConfirmBorrow)
    } else {
        AppCard {
            Text(
                "成功：已记录你为最新持有人，并通知上一位持有人和绑定 owner。",
                color = AppColors.Success
            )
        }
    }
    SecondaryButton("重新扫描", onClick = onRetry)
}

@Composable
private fun NotFoundState(
    scannedImei: String,
    onRegisterDevice: () -> Unit,
    onRetry: () -> Unit
) {
    AppCard {
        MutedText("完整 IMEI")
        Text(scannedImei, fontWeight = FontWeight.Bold)
        MutedText("该 IMEI 尚未建档，请先注册设备。")
    }
    PrimaryButton("注册新设备", onClick = onRegisterDevice)
    SecondaryButton("重新扫描", onClick = onRetry)
}

@Composable
private fun DarkCameraFrame(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(256.dp)
            .background(Color(0xFF17212B), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            icon()
            Spacer(Modifier.height(12.dp))
            Text(title, color = Color.White, fontWeight = FontWeight.Bold)
            MutedText(subtitle)
        }
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
private fun BarcodeCameraPreview(
    scanSession: Int,
    onImeiDetected: (String) -> Unit,
    onInvalidBarcode: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mainExecutor = remember(context) { ContextCompat.getMainExecutor(context) }
    val analyzerExecutor = remember(scanSession) { Executors.newSingleThreadExecutor() }
    val scanner = remember(scanSession) { BarcodeScanning.getClient() }
    val hasEmitted = remember(scanSession) { AtomicBoolean(false) }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { viewContext ->
            val previewView = PreviewView(viewContext).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            val cameraProviderFuture = ProcessCameraProvider.getInstance(viewContext)
            cameraProviderFuture.addListener(
                {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(analyzerExecutor) { imageProxy ->
                                processImageProxy(
                                    imageProxy = imageProxy,
                                    scanner = scanner,
                                    hasEmitted = hasEmitted,
                                    onImeiDetected = onImeiDetected,
                                    onInvalidBarcode = onInvalidBarcode
                                )
                            }
                        }
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                },
                mainExecutor
            )
            previewView
        }
    )

    DisposableEffect(scanSession) {
        onDispose {
            scanner.close()
            analyzerExecutor.shutdown()
            val providerFuture = ProcessCameraProvider.getInstance(context)
            providerFuture.addListener(
                { providerFuture.get().unbindAll() },
                mainExecutor
            )
        }
    }
}

@ExperimentalGetImage
private fun processImageProxy(
    imageProxy: ImageProxy,
    scanner: BarcodeScanner,
    hasEmitted: AtomicBoolean,
    onImeiDetected: (String) -> Unit,
    onInvalidBarcode: () -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage == null) {
        imageProxy.close()
        return
    }
    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
    scanner.process(image)
        .addOnSuccessListener { barcodes ->
            if (barcodes.isEmpty()) return@addOnSuccessListener
            val imei = ImeiParser.extractFirst(barcodes.mapNotNull { it.rawValue })
            if (imei != null && hasEmitted.compareAndSet(false, true)) {
                onImeiDetected(imei)
            } else if (imei == null && hasEmitted.compareAndSet(false, true)) {
                onInvalidBarcode()
            }
        }
        .addOnFailureListener {
            if (hasEmitted.compareAndSet(false, true)) {
                onInvalidBarcode()
            }
        }
        .addOnCompleteListener {
            imageProxy.close()
        }
}

private fun hasCameraPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
}

private fun pageTitle(scanState: ScanState): String {
    return if (scanState == ScanState.MANUAL_INPUT) "手动输入 IMEI" else "扫码识别手机"
}

private enum class ScanState {
    SCANNING,
    SCAN_FAILED,
    MANUAL_INPUT,
    FOUND,
    NOT_FOUND
}
