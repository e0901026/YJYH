package com.yjyh.phoneloan.core.analytics

import android.content.Context
import android.os.Build
import android.util.Log
import com.yjyh.phoneloan.BuildConfig
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.util.UUID
import java.util.concurrent.Executors
import org.json.JSONObject

object AnalyticsLogger {
    private const val TAG = "PhoneLoanAnalytics"
    private const val EVENT_FILE = "analytics-events.jsonl"
    private const val MAX_QUEUE_LINES = 500

    private val executor = Executors.newSingleThreadExecutor()
    private val sessionId = UUID.randomUUID().toString()
    private var appContext: Context? = null
    private var currentUserId: String? = null
    private var installedCrashHandler = false

    fun initialize(context: Context) {
        appContext = context.applicationContext
        installCrashHandler()
        trackAction(
            name = "app_open",
            screen = "app",
            payload = mapOf("versionName" to BuildConfig.VERSION_NAME)
        )
    }

    fun identifyUser(userId: String, employeeNo: String) {
        currentUserId = userId
        trackAction(
            name = "user_identified",
            screen = "auth",
            payload = mapOf("employeeNo" to employeeNo)
        )
    }

    fun trackScreen(route: String) {
        trackEvent(
            eventName = "screen_view",
            eventType = "behavior",
            severity = "info",
            screen = route,
            action = "view",
            payload = mapOf("route" to route)
        )
    }

    fun trackAction(
        name: String,
        screen: String,
        action: String = name,
        payload: Map<String, Any?> = emptyMap()
    ) {
        trackEvent(
            eventName = name,
            eventType = "behavior",
            severity = "info",
            screen = screen,
            action = action,
            payload = payload
        )
    }

    fun trackError(
        name: String,
        screen: String,
        throwable: Throwable? = null,
        payload: Map<String, Any?> = emptyMap()
    ) {
        val errorPayload = payload + mapOf(
            "message" to throwable?.message,
            "exception" to throwable?.javaClass?.simpleName
        )
        trackEvent(
            eventName = name,
            eventType = "error",
            severity = "error",
            screen = screen,
            action = name,
            payload = errorPayload
        )
    }

    private fun trackEvent(
        eventName: String,
        eventType: String,
        severity: String,
        screen: String,
        action: String,
        payload: Map<String, Any?>
    ) {
        val context = appContext ?: return
        val event = JSONObject().apply {
            put("eventName", eventName)
            put("eventType", eventType)
            put("result", if (eventType == "error") "FAILURE" else "SUCCESS")
            put("severity", severity.uppercase())
            put("appVersion", BuildConfig.VERSION_NAME)
            put("platform", "android")
            put("screen", screen)
            put("action", action)
            put("deviceModel", "${Build.MANUFACTURER} ${Build.MODEL}")
            put("osVersion", Build.VERSION.RELEASE ?: "")
            put("sessionId", sessionId)
            currentUserId?.let { put("userId", it) }
            put("context", JSONObject(sanitize(payload + mapOf("clientTime" to Instant.now().toString()))))
        }
        executor.execute {
            appendEvent(context, event)
            flushPending(context)
        }
    }

    private fun appendEvent(context: Context, event: JSONObject) {
        val file = File(context.filesDir, EVENT_FILE)
        val existing = if (file.exists()) file.readLines() else emptyList()
        val retained = (existing + event.toString()).takeLast(MAX_QUEUE_LINES)
        file.writeText(retained.joinToString(separator = "\n", postfix = "\n"))
    }

    private fun flushPending(context: Context) {
        val file = File(context.filesDir, EVENT_FILE)
        if (!file.exists()) return
        val lines = file.readLines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return

        val remaining = mutableListOf<String>()
        lines.forEach { line ->
            val posted = runCatching { postEvent(normalizeEvent(line)) }.getOrElse {
                Log.w(TAG, "event upload failed", it)
                false
            }
            if (!posted) remaining += line
        }
        file.writeText(remaining.takeLast(MAX_QUEUE_LINES).joinToString(separator = "\n", postfix = if (remaining.isEmpty()) "" else "\n"))
    }

    private fun normalizeEvent(line: String): String {
        val event = JSONObject(line)
        if (!event.has("context") && event.has("payload")) {
            event.put("context", event.getJSONObject("payload"))
            event.remove("payload")
        }
        if (!event.has("result")) {
            event.put("result", if (event.optString("eventType") == "error") "FAILURE" else "SUCCESS")
        }
        event.put("severity", event.optString("severity", "INFO").uppercase())
        return event.toString()
    }

    private fun postEvent(json: String): Boolean {
        val endpoint = URL("${BuildConfig.API_BASE_URL.trimEnd('/')}/api/events")
        val connection = endpoint.openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 1200
            connection.readTimeout = 1200
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.outputStream.use { it.write(json.toByteArray(Charsets.UTF_8)) }
            connection.responseCode in 200..299
        } finally {
            connection.disconnect()
        }
    }

    private fun sanitize(payload: Map<String, Any?>): Map<String, Any?> {
        return payload
            .filterKeys { key ->
                val lower = key.lowercase()
                lower !in setOf("password", "token", "authorization")
            }
            .mapValues { (_, value) ->
                when (value) {
                    null -> JSONObject.NULL
                    is Number, is Boolean, is String -> value
                    else -> value.toString()
                }
            }
    }

    private fun installCrashHandler() {
        if (installedCrashHandler) return
        installedCrashHandler = true
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            trackError(
                name = "app_crash",
                screen = "runtime",
                throwable = throwable,
                payload = mapOf("thread" to thread.name)
            )
            executor.shutdown()
            previous?.uncaughtException(thread, throwable)
        }
    }
}
