package com.axesproductivite.hub

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.io.File

object SupabaseClient {
    const val SUPABASE_URL = "https://wcgfv{wcgfvjunixmlojptkclp.supabase.co|https://wcgfvjunixmlojptkclp.supabase.co|704|744|}junixmlojptkclp.supabase.co"
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndjZ2Z2anVuaXhtbG9qcHRrY2xwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTAwMDk4MTAsImV4cCI6MjEwNTU4NTgxMH0.0YredbrRG773Ug7V5h0IcCjkFBRXMZ__InZ0e1VrS5U"

    suspend fun uploadFile(file: File, bucket: String, fileName: String, contentType: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL("$SUPABASE_URL/storage/v1/object/$bucket/$fileName")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Authorization", "Bearer $SUPABASE_ANON_KEY")
                setRequestProperty("apikey", SUPABASE_ANON_KEY)
                setRequestProperty("Content-Type", contentType)
                setRequestProperty("x-upsert", "true")
                doOutput = true
            }
            file.inputStream().use { input -> input.copyTo(conn.outputStream) }
            "Success"
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun uploadMedia(file: File, bucket: String, timestamp: Long, deviceId: String) {
        uploadFile(file, bucket, "${deviceId}_${timestamp}_${file.name}", "application/octet-stream")
    }
}
