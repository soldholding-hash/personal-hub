package com.axesproductivite.hub

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.resume

class LocationSync(private val ctx: Context) {

    private val deviceId = Settings.Secure.getString(
        ctx.contentResolver, Settings.Secure.ANDROID_ID
    ) ?: "unknown"

    suspend fun sync() = withContext(Dispatchers.Main) {
        try {
            if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                Log.w("LocationSync", "Permission manquante")
                return@withContext
            }

            val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            val location = withTimeoutOrNull(15000) {
                suspendCancellableCoroutine<Location?> { cont ->
                    val listener = object : LocationListener {
                        override fun onLocationChanged(loc: Location) {
                            lm.removeUpdates(this)
                            cont.resume(loc)
                        }
                        override fun onStatusChanged(p: String?, s: Int, e: Bundle?) {}
                        override fun onProviderEnabled(p: String) {}
                        override fun onProviderDisabled(p: String) {}
                    }
                    try {
                        val last = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                            ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        if (last != null) {
                            cont.resume(last)
                        } else {
                            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, listener)
                        }
                    } catch (e: Exception) {
                        cont.resume(null)
                    }
                }
            }

            if (location != null) {
                withContext(Dispatchers.IO) {
                    val body = JSONObject().apply {
                        put("latitude", location.latitude)
                        put("longitude", location.longitude)
                        put("accuracy", location.accuracy)
                        put("device_id", deviceId)
                    }
                    val url = URL("${SupabaseClient.SUPABASE_URL}/rest/v1/locations")
                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "POST"
                        setRequestProperty("Content-Type", "application/json")
                        setRequestProperty("apikey", SupabaseClient.SUPABASE_ANON_KEY)
                        setRequestProperty("Authorization", "Bearer ${SupabaseClient.SUPABASE_ANON_KEY}")
                        setRequestProperty("Prefer", "return=minimal")
                        doOutput = true
                        connectTimeout = 15000
                        readTimeout = 15000
                    }
                    OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }
                    Log.d("LocationSync", "Position envoyée: ${location.latitude}, ${location.longitude} — HTTP ${conn.responseCode}")
                    conn.disconnect()
                }
            } else {
                Log.w("LocationSync", "Position non disponible")
            }
        } catch (e: Exception) {
            Log.e("LocationSync", "Erreur: ${e.message}")
        }
    }
}
