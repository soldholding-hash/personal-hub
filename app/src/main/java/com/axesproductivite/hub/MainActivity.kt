package com.axesproductivite.hub

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var btnSync: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        btnSync = findViewById(R.id.btnSync)

        btnSync.setOnClickListener {
            tvStatus.text = "Synchronisation en cours..."
            
            CoroutineScope(Dispatchers.IO).launch {
                // Appel direct des fonctions de synchronisation
                // au lieu d'instancier SyncWorker qui est un Worker Android
                MediaSync(applicationContext).sync() 
                CallRecordSync(applicationContext).sync()
                
                runOnUiThread {
                    tvStatus.text = "Synchronisation terminée ✅"
                }
            }
            
            // Envoyer la localisation
            sendLocation()
        }
    }

    private fun sendLocation() {
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            tvStatus.text = "Permission GPS manquante"
            return
        }
        
        try {
            val loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                
            if (loc != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    LocationSync(applicationContext).sync()
                }
                tvStatus.text = "Position envoyée ✅\n${loc.latitude}, ${loc.longitude}"
            } else {
                tvStatus.text = "GPS non disponible — Active la localisation"
            }
        } catch (e: Exception) {
            tvStatus.text = "Erreur localisation: ${e.message}"
        }
    }
}

