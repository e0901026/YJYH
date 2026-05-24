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

    override fun currentUser(): User = me

    override fun devices(): List<Device> = if (remoteDevices.isNotEmpty()) remoteDevices.toList() else fallback.devices()

    override fun activeLoans(): List<LoanRecord> = if (online) remoteLoans.toList() else fallback.activeLoans()

    override fun ownerUsers(): List<OwnerUserRow> = if (remoteOwnerUsers.isNotEmpty()) remoteOwnerUsers.toList() else fallback.ownerUsers()

    override fun inviteCodes(): List<InviteCode> = if (remoteInvites.isNotEmpty()) remoteInvites.toList() else fallback.inviteCodes()

    override fun latestActivity(): String = activity.ifBlank { fallback.latestActivity() }

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

    private fun refreshFromBackend(reason: String) {
        executor.execute {
            runCatching {
                ensureLogin()
                val token = requireToken()
                val devices = api.getArray(token, "/api/devices").mapJson { it.toDevice(me) }
                val loans = api.getArray(token, "/api/loans/active").mapJson { it.toLoan(me) }
                val users = api.getArray(token, "/api/owner/users").mapJson {
                    OwnerUserRow(
                        employeeNo = it.getString("employeeNo"),
                        name = it.getString("name"),
                        registeredAt = "后端用户",
                        inviter = it.optString("invitedByUserId", "系统")
                    )
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
        val auth = api.login("10086", "password123")
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

    fun createDevice(token: String, name: String, imei: String): JSONObject {
        return post(
            token = token,
            path = "/api/devices",
            body = JSONObject()
                .put("name", name)
                .put("imei1", imei)
        )
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
            if (status !in 200..299) error("HTTP $status $text")
            text
        } finally {
            connection.disconnect()
        }
    }
}

private fun <T> JSONArray.mapJson(mapper: (JSONObject) -> T): List<T> {
    return (0 until length()).map { mapper(getJSONObject(it)) }
}

private fun <T> MutableList<T>.replaceWith(values: List<T>) {
    clear()
    addAll(values)
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

private fun JSONObject.toDevice(me: User): Device {
    val owner = getJSONObject("owner").toSummary()
    val holder = optJSONObject("currentHolder")?.toSummary()
    return Device(
        id = getString("id"),
        name = getString("name"),
        imei1 = getString("imei1"),
        imei2 = optString("imei2").ifBlank { null },
        owner = owner,
        currentHolder = holder,
        status = toDeviceStatus(owner, holder, me, optString("status"))
    )
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
