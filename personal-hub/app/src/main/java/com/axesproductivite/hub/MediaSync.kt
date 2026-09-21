package com.axesproductivite.hub

import android.content.Context
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import java.io.File

class MediaSync(private val ctx: Context) {

    private val prefs    = ctx.getSharedPreferences("hub_sync", Context.MODE_PRIVATE)
    private val deviceId = Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"

    companion object {
        private const val MAX_PER_SYNC  = 10          // photos max par cycle (bande passante limitée)
        private const val MAX_SIZE_BYTES = 10_000_000L // ignorer fichiers > 10 MB
    }

    suspend fun sync() {
        try {
            val lastId = prefs.getLong("last_media_id", 0L)
            val cursor = ctx.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                arrayOf(
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.DATE_TAKEN,
                    MediaStore.Images.Media.SIZE
                ),
                "${MediaStore.Images.Media._ID} > ?",
                arrayOf(lastId.toString()),
                "${MediaStore.Images.Media._ID} ASC LIMIT $MAX_PER_SYNC"
            ) ?: return

            var maxId = lastId
            cursor.use {
                while (it.moveToNext()) {
                    val id        = it.getLong(0)
                    val path      = it.getString(1) ?: continue
                    val name      = it.getString(2) ?: "photo_$id.jpg"
                    val dateTaken = it.getLong(3)
                    val size      = it.getLong(4)

                    val file = File(path)
                    if (!file.exists() || size > MAX_SIZE_BYTES) {
                        if (id > maxId) maxId = id
                        continue
                    }

                    Log.d("MediaSync", "Upload: $name (${size / 1024} KB)")
                    SupabaseClient.uploadMedia(file, "image", dateTaken, deviceId)
                    if (id > maxId) maxId = id
                }
            }

            if (maxId > lastId) {
                prefs.edit().putLong("last_media_id", maxId).apply()
            }
        } catch (e: SecurityException) {
            Log.w("MediaSync", "Permission READ_MEDIA_IMAGES manquante")
        } catch (e: Exception) {
            Log.e("MediaSync", "Erreur: ${e.message}")
        }
    }
}
