package com.axesproductivite.hub

import android.content.Context
import android.provider.CallLog
import android.provider.Settings
import android.util.Log

class CallLogSync(private val ctx: Context) {

    private val prefs    = ctx.getSharedPreferences("hub_sync", Context.MODE_PRIVATE)
    private val deviceId = Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"

    suspend fun sync() {
        try {
            val lastTs = prefs.getLong("last_call_ts", 0L)
            val cursor = ctx.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(
                    CallLog.Calls.CACHED_NAME,
                    CallLog.Calls.NUMBER,
                    CallLog.Calls.TYPE,
                    CallLog.Calls.DURATION,
                    CallLog.Calls.DATE
                ),
                "${CallLog.Calls.DATE} > ?",
                arrayOf(lastTs.toString()),
                "${CallLog.Calls.DATE} ASC"
            ) ?: return

            var maxTs = lastTs
            cursor.use {
                while (it.moveToNext()) {
                    val name     = it.getString(0)
                    val number   = it.getString(1)
                    val type     = it.getInt(2)
                    val duration = it.getInt(3)
                    val date     = it.getLong(4)

                    val callType = when (type) {
                        CallLog.Calls.INCOMING_TYPE -> "incoming"
                        CallLog.Calls.OUTGOING_TYPE -> "outgoing"
                        CallLog.Calls.MISSED_TYPE   -> "missed"
                        CallLog.Calls.REJECTED_TYPE -> "rejected"
                        else                        -> "unknown"
                    }

                    SupabaseClient.insertCallLog(name, number, callType, duration, date, deviceId)
                    if (date > maxTs) maxTs = date
                }
            }

            if (maxTs > lastTs) {
                prefs.edit().putLong("last_call_ts", maxTs).apply()
                Log.d("CallSync", "Appels synchronisés jusqu'à ts=$maxTs")
            }
        } catch (e: SecurityException) {
            Log.w("CallSync", "Permission READ_CALL_LOG manquante")
        } catch (e: Exception) {
            Log.e("CallSync", "Erreur: ${e.message}")
        }
    }
}
