package com.axesproductivite.hub

import android.content.Context
import android.net.Uri
import android.provider.Settings
import android.util.Log

class SmsSync(private val ctx: Context) {

    private val prefs    = ctx.getSharedPreferences("hub_sync", Context.MODE_PRIVATE)
    private val deviceId = Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"

    suspend fun sync() {
        try {
            val lastId = prefs.getLong("last_sms_id", 0L)
            val cursor = ctx.contentResolver.query(
                Uri.parse("content://sms/inbox"),
                arrayOf("_id", "address", "body", "date"),
                "_id > ?",
                arrayOf(lastId.toString()),
                "_id ASC"
            ) ?: return

            var maxId = lastId
            cursor.use {
                while (it.moveToNext()) {
                    val id      = it.getLong(it.getColumnIndexOrThrow("_id"))
                    val address = it.getString(it.getColumnIndexOrThrow("address")) ?: "inconnu"
                    val body    = it.getString(it.getColumnIndexOrThrow("body"))    ?: ""
                    val date    = it.getLong(it.getColumnIndexOrThrow("date"))

                    SupabaseClient.insertMessage("sms", address, body, deviceId, date)
                    if (id > maxId) maxId = id
                }
            }

            if (maxId > lastId) {
                prefs.edit().putLong("last_sms_id", maxId).apply()
                Log.d("SmsSync", "SMS synchronisés jusqu'à id=$maxId")
            }
        } catch (e: SecurityException) {
            Log.w("SmsSync", "Permission READ_SMS manquante")
        } catch (e: Exception) {
            Log.e("SmsSync", "Erreur: ${e.message}")
        }
    }
}
