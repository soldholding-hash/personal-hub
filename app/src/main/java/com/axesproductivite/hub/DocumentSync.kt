package com.axesproductivite.hub

import android.content.Context
import android.os.Environment
import android.provider.Settings
import android.util.Log
import java.io.File

class DocumentSync(private val ctx: Context) {

    private val prefs = ctx.getSharedPreferences("hub_sync", Context.MODE_PRIVATE)
    private val deviceId = Settings.Secure.getString(
        ctx.contentResolver, Settings.Secure.ANDROID_ID
    ) ?: "unknown"

    private val extensions = listOf("pdf", "docx", "doc", "xlsx", "xls", "pptx", "txt")

    suspend fun sync() {
        try {
            val lastSynced = prefs.getLong("last_doc_ts", 0L)
            val baseDir = Environment.getExternalStorageDirectory()

            val files = baseDir.walkTopDown()
                .filter { it.isFile }
                .filter { extensions.contains(it.extension.lowercase()) }
                .filter { it.lastModified() > lastSynced }
                .filter { it.length() < 20_000_000 }
                .sortedBy { it.lastModified() }
                .take(10)
                .toList()

            var maxTs = lastSynced
            for (file in files) {
                val safeName = file.name.replace(" ", "_")
                val contentType = when (file.extension.lowercase()) {
                    "pdf" -> "application/pdf"
                    "docx", "doc" -> "application/msword"
                    "xlsx", "xls" -> "application/vnd.ms-excel"
                    "pptx" -> "application/vnd.ms-powerpoint"
                    else -> "text/plain"
                }
                Log.d("DocSync", "Upload: $safeName")
                SupabaseClient.uploadFile(file, "Documents", safeName, contentType)
                if (file.lastModified() > maxTs) maxTs = file.lastModified()
            }

            if (maxTs > lastSynced) {
                prefs.edit().putLong("last_doc_ts", maxTs).apply()
            }
        } catch (e: Exception) {
            Log.e("DocSync", "Erreur: ${e.message}")
        }
    }
}
