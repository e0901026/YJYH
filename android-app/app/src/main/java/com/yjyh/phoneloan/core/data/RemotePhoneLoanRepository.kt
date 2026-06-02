package com.yjyh.phoneloan.core.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.yjyh.phoneloan.BuildConfig
import com.yjyh.phoneloan.core.analytics.AnalyticsLogger
import com.yjyh.phoneloan.core.model.Device
import com.yjyh.phoneloan.core.model.DeviceStatus
import com.yjyh.phoneloan.core.model.InviteCode
import com.yjyh.phoneloan.core.model.InviteStatus
import com.yjyh.phoneloan.core.model.LoanRecord
import com.yjyh.phoneloan.core.model.OwnerUserRow
import com.yjyh.phoneloan.core.model.User
import com.yjyh.phoneloan.core.model.UserRole
import com.yjyh.phoneloan.core.model.UserSummary
import java.net.HttpURLConnection
import java.net.URL
import java.time.Duration
import java.time.Instant
import java.util.concurrent.Executors
import org.json.JSONArray
import org.json.JSONObject

class RemotePhoneLoanRepository(
    private val fallback: PhoneLoanRepository
) : PhoneLoanRepository {
    private val executor = Executors.newSingleThreadExecutor()
    private val api = PhoneLoanApi(BuildConfig.API_BASE_URL)

    private var accessToken: String? = null
    private var me by mutableStateOf(fallback.currentUser())
    private var activity by mutableStateOf("正在连接本地后端，离线时使用演示数据。")
    private val remoteDevices = mutableStateListOf<Device>()
    private val remoteLoans = mutableStateListOf<LoanRecord>()
    private val remoteOwnerUsers = mutableStateListOf<OwnerUserRow>()
    private val remoteInvites = mutableStateListOf<InviteCode>()
    private var online by mutableStateOf(false)

    init {
        refreshFromBackend(reason = "app_start")
    }

    override fun login(employeeNo: String, password: String): Result<Unit> {
        return runCatching {
            val auth = api.login(employeeNo, password)
            applyAuth(auth)
            refreshFromBackend(reason = "login")
            AnalyticsLogger.trackAction("login_success", screen = "login", payload = mapOf("employeeNo" to employeeNo))
        }.onFailure {
            AnalyticsLogger.trackError("login_failed", screen = "login", throwable = it, payload = mapOf("employeeNo" to employeeNo))
        }.map { Unit }
    }

    override fun register(employeeNo: String, name: String, password: String, inviteCode: String): Result<Unit> {
        return runCatching {
            val auth = api.register(employeeNo, name, password, inviteCode)
            applyAuth(auth)
            refreshFromBackend(reason = "register")
            AnalyticsLogger.trackAction("register_success", screen = "register", payload = mapOf("employeeNo" to employeeNo))
        }.onFailure {
            AnalyticsLogger.trackError("register_failed", screen = "register", throwable = it, payload = mapOf("employeeNo" to employeeNo))
        }.map { Unit }
    }

    override fun currentUser(): User = me

    override fun devices(): List<Device> = if (remoteDevices.isNotEmpty()) remoteDevices.toList() else fallback.devices()

    override fun activeLoans(): List<LoanRecord> = if (online) remoteLoans.toList() else fallback.activeLoans()

    override fun ownerUsers(): List<OwnerUserRow> = if (remoteOwnerUsers.isNotEmpty()) remoteOwnerUsers.toList() else fallback.ownerUsers()

    override fun inviteCodes(): List<InviteCode> = if (remoteInvites.isNotEmpty()) remoteInvites.toList() else fallback.inviteCodes()

    override fun latestActivity(): String = activity.ifBlank { fallback.latestActivity() }

    override fun ownerCreateUser(employeeNo: String, name: String, password: String, role: UserRole): Result<OwnerUserRow> {
        return runCatching {
            ensureLogin()
            val row = api.ownerCreateUser(requireToken(), employeeNo, name, password, role).toOwnerUserRow()
            remoteOwnerUsers.add(row)
            refreshFromBackend(reason = "owner_user_created")
            AnalyticsLogger.trackAction("owner_user_create_success", screen = "owner_users", payload = mapOf("employeeNo" to employeeNo))
            row
        }.onFailure {
            handleFailure("owner_user_create_failed", "owner_users", it)
        }
    }

    override fun ownerUpdateUser(userId: String, name: String, password: String, role: UserRole): Result<OwnerUserRow> {
        return runCatching {
            ensureLogin()
            val row = api.ownerUpdateUser(requireToken(), userId, name, password, role).toOwnerUserRow()
            remoteOwnerUsers.replaceRow(row)
            refreshFromBackend(reason = "owner_user_updated")
            AnalyticsLogger.trackAction("owner_user_update_success", screen = "owner_users", payload = mapOf("userId" to userId))
            row
        }.onFailure {
            handleFailure("owner_user_update_failed", "owner_users", it)
        }
    }

    override fun ownerDisableUser(userId: String): Result<OwnerUserRow> {
        return runCatching {
            ensureLogin()
            val row = api.ownerDisableUser(requireToken(), userId).toOwnerUserRow()
            remoteOwnerUsers.replaceRow(row)
            refreshFromBackend(reason = "owner_user_disabled")
            AnalyticsLogger.trackAction("owner_user_disable_success", screen = "owner_users", payload = mapOf("userId" to userId))
            row
        }.onFailure {
            handleFailure("owner_user_disable_failed", "owner_users", it)
        }
    }

    override fun ownerCreateInviteCode(): Result<InviteCode> {
        return runCatching {
            ensureLogin()
            val code = api.ownerCreateInviteCode(requireToken()).toInviteCode()
            remoteInvites.add(0, code)
            refreshFromBackend(reason = "owner_invite_created")
            AnalyticsLogger.trackAction("owner_invite_generate_success", screen = "owner_invites", payload = mapOf("inviteId" to code.id))
            code
        }.onFailure {
            handleFailure("owner_invite_generate_failed", "owner_invites", it)
        }
    }

    override fun findDeviceByImei(imei: String): Device? {
        refreshFromBackend(reason = "find_device")
        return devices().find { it.imei1 == imei || it.imei2 == imei }
    }

    override fun addDevice(name: String, imei: String): Device {
        val optimistic = fallback.addDevice(name, imei)
        executor.execute {
            runCatching {
                ensureLogin()
                api.createDevice(requireToken(), name, imei).toDevice(me)
            }.onSuccess {
                activity = "${it.name} 已通过后端建档。"
                refreshFromBackend(reason = "device_created")
            }.onFailure {
                handleFailure("remote_device_create_failed", "register_device", it)
            }
        }
        return optimistic
    }

    override fun addDeviceResult(name: String, imei: String): Result<Device> {
        return runCatching {
            ensureLogin()
            val device = api.createDevice(requireToken(), name, imei).toDevice(me)
            activity = "${device.name} 已通过后端建档。"
            refreshFromBackend(reason = "device_created")
            AnalyticsLogger.trackAction(
                name = "device_register_success",
                screen = "register_device",
                payload = mapOf("deviceId" to device.id, "imei" to imei)
            )
            device
        }.onFailure {
            handleFailure("remote_device_create_failed", "register_device", it)
        }
    }

    override fun updateDeviceHolder(deviceId: String, newHolder: UserSummary, newStatus: DeviceStatus) {
        val device = devices().find { it.id == deviceId }
        fallback.updateDeviceHolder(deviceId, newHolder, newStatus)
        if (device == null) {
            AnalyticsLogger.trackError("remote_borrow_device_missing", screen = "scan_borrow", payload = mapOf("deviceId" to deviceId))
            return
        }
        executor.execute {
            runCatching {
                ensureLogin()
                api.borrowByImei(requireToken(), device.imei1).toLoan(me)
            }.onSuccess {
                activity = "${it.device.name} 已通过后端记录借走。"
                refreshFromBackend(reason = "device_borrowed")
            }.onFailure {
                handleFailure("remote_borrow_failed", "scan_borrow", it)
            }
        }
    }

    override fun borrowDeviceResult(deviceId: String, newHolder: UserSummary, newStatus: DeviceStatus): Result<Unit> {
        val device = devices().find { it.id == deviceId }
            ?: return Result.failure(IllegalArgumentException("设备不存在或数据未同步"))
        return runCatching {
            ensureLogin()
            val loan = api.borrowByImei(requireToken(), device.imei1).toLoan(me)
            activity = "${loan.device.name} 已通过后端记录借走。"
            refreshFromBackend(reason = "device_borrowed")
            AnalyticsLogger.trackAction(
                name = "borrow_success",
                screen = "scan_borrow",
                payload = mapOf("deviceId" to deviceId, "imei" to device.imei1)
            )
        }.onFailure {
            handleFailure("remote_borrow_failed", "scan_borrow", it)
        }
    }

    override fun returnLoan(deviceId: String) {
        val loan = activeLoans().find { it.device.id == deviceId && it.statusText == "我借入的" }
        fallback.returnLoan(deviceId)
        if (loan == null) {
            AnalyticsLogger.trackError("remote_return_loan_missing", screen = "return_loan", payload = mapOf("deviceId" to deviceId))
            return
        }
        executor.execute {
            runCatching {
                ensureLogin()
                api.post(requireToken(), "/api/loans/${loan.id}/return", null)
            }.onSuccess {
                activity = "${loan.device.name} 已通过后端归还。"
                refreshFromBackend(reason = "device_returned")
            }.onFailure {
                handleFailure("remote_return_failed", "return_loan", it)
            }
        }
    }

    override fun returnLoanResult(deviceId: String): Result<Unit> {
        val loan = activeLoans().find { it.device.id == deviceId && it.statusText == "我借入的" }
            ?: return Result.failure(IllegalArgumentException("没有找到可归还的借入记录"))
        return runCatching {
            ensureLogin()
            api.post(requireToken(), "/api/loans/${loan.id}/return", null)
            activity = "${loan.device.name} 已通过后端归还。"
            refreshFromBackend(reason = "device_returned")
            AnalyticsLogger.trackAction(
                name = "return_success",
                screen = "return_loan",
                payload = mapOf("deviceId" to deviceId, "loanId" to loan.id)
            )
        }.onFailure {
            handleFailure("remote_return_failed", "return_loan", it)
        }
    }

    override fun urgeReturn(deviceId: String) {
        val loan = activeLoans().find { it.device.id == deviceId && it.statusText == "我借出去的" }
        fallback.urgeReturn(deviceId)
        if (loan == null) {
            AnalyticsLogger.trackError("remote_urge_loan_missing", screen = "return_loan", payload = mapOf("deviceId" to deviceId))
            return
        }
        executor.execute {
            runCatching {
                ensureLogin()
                api.post(requireToken(), "/api/loans/${loan.id}/urge-return", null)
            }.onSuccess {
                activity = "${loan.device.name} 已通过后端发送催还消息。"
                refreshFromBackend(reason = "device_urged")
            }.onFailure {
                handleFailure("remote_urge_failed", "return_loan", it)
            }
        }
    }

    override fun urgeReturnResult(deviceId: String): Result<Unit> {
        val loan = activeLoans().find { it.device.id == deviceId && it.statusText == "我借出去的" }
            ?: return Result.failure(IllegalArgumentException("没有找到可催还的借出记录"))
        return runCatching {
            ensureLogin()
            api.post(requireToken(), "/api/loans/${loan.id}/urge-return", null)
            activity = "${loan.device.name} 已通过后端发送催还消息。"
            refreshFromBackend(reason = "device_urged")
            AnalyticsLogger.trackAction(
                name = "urge_return_success",
                screen = "return_loan",
                payload = mapOf("deviceId" to deviceId, "loanId" to loan.id)
            )
        }.onFailure {
            handleFailure("remote_urge_failed", "return_loan", it)
        }
    }

    private fun refreshFromBackend(reason: String) {
        executor.execute {
            runCatching {
                ensureLogin()
                val token = requireToken()
                val devices = api.getArray(token, "/api/devices").mapJson { it.toDevice(me) }
                val loans = api.getArray(token, "/api/loans/active").mapJson { it.toLoan(me) }
                val users = api.getArray(token, "/api/owner/users").mapJson {
                    it.toOwnerUserRow()
                }
                val invites = api.getArray(token, "/api/owner/invite-codes").mapJson { it.toInviteCode() }
                BackendSnapshot(devices, loans, users, invites)
            }.onSuccess { snapshot ->
                online = true
                remoteDevices.replaceWith(snapshot.devices)
                remoteLoans.replaceWith(snapshot.loans)
                remoteOwnerUsers.replaceWith(snapshot.users)
                remoteInvites.replaceWith(snapshot.invites)
                activity = "已连接本地后端，数据来自真实 API。"
                AnalyticsLogger.trackAction("remote_refresh_success", screen = "data", payload = mapOf("reason" to reason))
            }.onFailure {
                online = false
                activity = "本地后端暂不可用，当前使用离线演示数据。"
                handleFailure("remote_refresh_failed", "data", it)
            }
        }
    }

    private fun ensureLogin() {
        if (accessToken != null) return
        applyAuth(api.login("10086", "password123"))
    }

    private fun applyAuth(auth: JSONObject) {
        accessToken = auth.getString("accessToken")
        me = auth.getJSONObject("user").toUser()
        AnalyticsLogger.identifyUser(me.id, me.employeeNo)
    }

    private fun requireToken() = accessToken ?: error("missing access token")

    private fun handleFailure(name: String, screen: String, throwable: Throwable) {
        AnalyticsLogger.trackError(name, screen = screen, throwable = throwable)
    }

    private data class BackendSnapshot(
        val devices: List<Device>,
        val loans: List<LoanRecord>,
        val users: List<OwnerUserRow>,
        val invites: List<InviteCode>
    )
}

private class PhoneLoanApi(baseUrl: String) {
    private val root = baseUrl.trimEnd('/')

    fun login(employeeNo: String, password: String): JSONObject {
        return post(
            token = null,
            path = "/api/auth/login",
            body = JSONObject()
                .put("employeeNo", employeeNo)
                .put("password", password)
        )
    }

    fun register(employeeNo: String, name: String, password: String, inviteCode: String): JSONObject {
        return post(
            token = null,
            path = "/api/auth/register",
            body = JSONObject()
                .put("employeeNo", employeeNo)
                .put("name", name)
                .put("password", password)
                .put("inviteCode", inviteCode)
        )
    }

    fun createDevice(token: String, name: String, imei: String): JSONObject {
        return post(
            token = token,
            path = "/api/devices",
            body = JSONObject()
                .put("name", name)
                .put("imei1", imei)
        )
    }

    fun ownerCreateUser(token: String, employeeNo: String, name: String, password: String, role: UserRole): JSONObject {
        return post(
            token = token,
            path = "/api/owner/users",
            body = JSONObject()
                .put("employeeNo", employeeNo)
                .put("name", name)
                .put("password", password)
                .put("role", role.name)
        )
    }

    fun ownerUpdateUser(token: String, userId: String, name: String, password: String, role: UserRole): JSONObject {
        return put(
            token = token,
            path = "/api/owner/users/$userId",
            body = JSONObject()
                .put("name", name)
                .put("password", password)
                .put("role", role.name)
        )
    }

    fun ownerDisableUser(token: String, userId: String): JSONObject {
        val text = request("DELETE", token, "/api/owner/users/$userId", null)
        return JSONObject(text)
    }

    fun ownerCreateInviteCode(token: String): JSONObject {
        return post(token = token, path = "/api/owner/invite-codes", body = null)
    }

    fun borrowByImei(token: String, imei: String): JSONObject {
        return post(
            token = token,
            path = "/api/loans/borrow-by-imei",
            body = JSONObject().put("imei", imei)
        )
    }

    fun getArray(token: String, path: String): JSONArray {
        val text = request("GET", token, path, null)
        return JSONArray(text)
    }

    fun post(token: String?, path: String, body: JSONObject?): JSONObject {
        val text = request("POST", token, path, body)
        return JSONObject(text)
    }

    fun put(token: String?, path: String, body: JSONObject?): JSONObject {
        val text = request("PUT", token, path, body)
        return JSONObject(text)
    }

    private fun request(method: String, token: String?, path: String, body: JSONObject?): String {
        val connection = (URL("$root$path").openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 1500
            readTimeout = 1500
            setRequestProperty("Accept", "application/json")
            if (token != null) setRequestProperty("Authorization", "Bearer $token")
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
        }
        return try {
            if (body != null) {
                connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (status !in 200..299) throw ApiClientException(status, extractErrorMessage(text))
            text
        } finally {
            connection.disconnect()
        }
    }

    private fun extractErrorMessage(text: String): String {
        return runCatching { JSONObject(text).optString("message") }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: "服务请求失败"
    }
}

private class ApiClientException(status: Int, message: String) : RuntimeException("$message（$status）")

private fun <T> JSONArray.mapJson(mapper: (JSONObject) -> T): List<T> {
    return (0 until length()).map { mapper(getJSONObject(it)) }
}

private fun <T> MutableList<T>.replaceWith(values: List<T>) {
    clear()
    addAll(values)
}

private fun MutableList<OwnerUserRow>.replaceRow(row: OwnerUserRow) {
    val index = indexOfFirst { it.id == row.id }
    if (index >= 0) {
        this[index] = row
    } else {
        add(row)
    }
}

private fun JSONObject.toUser(): User {
    return User(
        id = getString("id"),
        employeeNo = getString("employeeNo"),
        name = getString("name"),
        role = if (optString("role") == "OWNER") UserRole.OWNER else UserRole.USER,
        inviteUsed = optInt("inviteQuotaUsed", 0)
    )
}

private fun JSONObject.toSummary(): UserSummary {
    return UserSummary(
        id = getString("id"),
        employeeNo = getString("employeeNo"),
        name = getString("name")
    )
}

private fun JSONObject.toOwnerUserRow(): OwnerUserRow {
    val inviterId = optNullableString("invitedByUserId")
    return OwnerUserRow(
        id = getString("id"),
        employeeNo = getString("employeeNo"),
        name = getString("name"),
        registeredAt = compactInstantText(optString("createdAt")).ifBlank { "后端用户" },
        inviter = if (inviterId.isNullOrBlank()) "系统" else "后端记录",
        role = if (optString("role") == "OWNER") UserRole.OWNER else UserRole.USER,
        enabled = optBoolean("enabled", true)
    )
}

private fun JSONObject.toDevice(me: User): Device {
    val owner = getJSONObject("owner").toSummary()
    val holder = optJSONObject("currentHolder")?.toSummary()
    val createdAt = optString("createdAt")
    val updatedAt = optString("updatedAt")
    return Device(
        id = getString("id"),
        name = getString("name"),
        imei1 = getString("imei1"),
        imei2 = optNullableString("imei2"),
        owner = owner,
        currentHolder = holder,
        status = toDeviceStatus(owner, holder, me, optString("status")),
        latestEventLabel = latestDeviceEventLabel(createdAt, updatedAt),
        latestEventOrder = instantOrder(updatedAt.ifBlank { createdAt })
    )
}

private fun JSONObject.optNullableString(name: String): String? {
    return if (isNull(name)) null else optString(name).ifBlank { null }
}

private fun JSONObject.toLoan(me: User): LoanRecord {
    val device = getJSONObject("device").toDevice(me)
    val holder = device.currentHolder
    val borrowedIn = holder?.id == me.id && device.owner.id != me.id
    val counterpart = if (borrowedIn) device.owner else holder ?: device.owner
    return LoanRecord(
        id = getString("id"),
        device = device,
        counterpart = counterpart,
        startedAt = holdDaysText(optString("startedAt")),
        statusText = if (borrowedIn) "我借入的" else "我借出去的"
    )
}

private fun JSONObject.toInviteCode(): InviteCode {
    val status = when (optString("status")) {
        "USED" -> InviteStatus.USED
        "EXPIRED" -> InviteStatus.EXPIRED
        else -> InviteStatus.UNUSED
    }
    val detail = when (status) {
        InviteStatus.UNUSED -> "有效期：${optString("expiresAt", "后端记录")}"
        InviteStatus.USED -> "使用者：${optString("usedByUserId", "后端记录")}"
        InviteStatus.EXPIRED -> "已过期"
    }
    return InviteCode(getString("id"), getString("code"), status, detail)
}

private fun toDeviceStatus(owner: UserSummary, holder: UserSummary?, me: User, remoteStatus: String): DeviceStatus {
    return when {
        holder?.id == me.id -> {
            if (owner.id == me.id) DeviceStatus.HELD_BY_ME else DeviceStatus.PENDING_RETURN
        }
        owner.id == me.id && holder != null -> DeviceStatus.BORROWED_OUT
        remoteStatus == "AVAILABLE" -> DeviceStatus.AVAILABLE
        else -> DeviceStatus.AVAILABLE
    }
}

private fun holdDaysText(startedAt: String): String {
    return runCatching {
        val days = Duration.between(Instant.parse(startedAt), Instant.now()).toDays().coerceAtLeast(0)
        if (days == 0L) "今天" else "$days 天"
    }.getOrDefault("后端记录")
}

private fun latestDeviceEventLabel(createdAt: String, updatedAt: String): String {
    val label = if (createdAt.isNotBlank() && createdAt == updatedAt) "录入时间" else "最新转手"
    val time = compactInstantText(updatedAt.ifBlank { createdAt })
    return if (time.isBlank()) "最新状态：后端记录" else "$label：$time"
}

private fun instantOrder(value: String): Long {
    return runCatching { Instant.parse(value).toEpochMilli() }.getOrDefault(0L)
}

private fun compactInstantText(value: String): String {
    return runCatching {
        val instant = Instant.parse(value)
        val minutes = Duration.between(instant, Instant.now()).toMinutes()
        when {
            minutes < 1 -> "刚刚"
            minutes < 60 -> "${minutes}分钟前"
            minutes < 60 * 24 -> "${minutes / 60}小时前"
            else -> "${minutes / (60 * 24)}天前"
        }
    }.getOrDefault("")
}
