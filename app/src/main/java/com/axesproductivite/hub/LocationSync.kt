package com.axesproductivite.hub

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import kotlin.coroutines.resume

class LocationSync(private val ctx: Context) {

    private val deviceId = Settings.Secure.getString(
        ctx.contentResolver, Settings.Secure.ANDROID_ID
    ) ?: "unknown"

    suspend fun sync() = withContext(Dispatchers.IO) {
        try {
            if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                Log.w("LocationSync", "Permission localisation manquante")
                return@withContext
            }

            val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val location = withTimeoutOrNull(10000) {
                suspendCancellableCoroutine { cont ->
                    try {
                        val loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                            ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        cont.resume(loc)
                    } catch (e: Exception) {
                        cont.resume(null)
                    }
                }
            }

            if (location != null) {
                val body = JSONObject().apply {
                    put("latitude", location.latitude)
                    put("longitude", location.longitude)
                    put("accuracy", location.accuracy)
                    put("device_id", deviceId)
                }
                val url = java.net.URL("${SupabaseClient.SUPABASE_URL}/rest/v1/locations")
                val conn = (url.openConnection() as java.net.HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("apikey", SupabaseClient.SUPABASE_ANON_KEY)
                    setRequestProperty("Authorization", "Bearer ${SupabaseClient.SUPABASE_ANON_KEY}")
                    setRequestProperty("Prefer", "return=minimal")
                    doOutput = true
                    connectTimeout = 15000
                    readTimeout = 15000
                }
                java.io.OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }
                val code = conn.responseCode
                conn.disconnect()
                Log.d("LocationSync", "Position envoyée: ${location.latitude}, ${location.longitude} — HTTP $code")
            } else {
                Log.w("LocationSync", "Position non disponible")
            }
        } catch (e: Exception) {
            Log.e("LocationSync", "Erreur: ${e.message}")
        }
    }
}
