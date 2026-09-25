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

    suspend fun sync() {
        try {
            val baseFolder = File("/storage/emulated/0/Music/PhoneRecord")
            if (!baseFolder.exists()) return

            val lastSynced = prefs.getLong("last_record_ts", 0L)

            val files = baseFolder.walkTopDown()
                .filter { it.isFile && (it.name.endsWith(".aac") || it.name.endsWith(".mp3") || it.name.endsWith(".m4a")) }
                
                .sortedBy { it.lastModified() }
                .take(5)

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
