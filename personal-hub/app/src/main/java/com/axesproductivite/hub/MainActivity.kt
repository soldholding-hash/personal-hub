package com.axesproductivite.hub

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var tvNotifStatus : TextView
    private lateinit var tvStatus      : TextView
    private lateinit var btnNotifAccess: Button
    private lateinit var btnSync       : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvNotifStatus  = findViewById(R.id.tvNotifStatus)
        tvStatus       = findViewById(R.id.tvStatus)
        btnNotifAccess = findViewById(R.id.btnNotifAccess)
        btnSync        = findViewById(R.id.btnSync)

        // Permissions runtime
        requestPermissions()

        // Ouvrir le paramètre d'accès aux notifications
        btnNotifAccess.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        // Sync manuelle
        btnSync.setOnClickListener {
            tvStatus.text = "⏳ Synchronisation en cours…"
            WorkManager.getInstance(this)
                .enqueue(OneTimeWorkRequestBuilder<SyncWorker>().build())
        }

        // Sync automatique toutes les 15 minutes
        schedulePeriodic()
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun updateUI() {
        val notifOk = isNotificationListenerEnabled()
        tvNotifStatus.text = if (notifOk)
            "✅ Accès notifications : ACTIF\n(WhatsApp, Messenger, Telegram…)"
        else
            "❌ Accès notifications INACTIF\n→ Appuie sur le bouton ci-dessous"

        btnNotifAccess.isEnabled = !notifOk
        tvStatus.text = if (notifOk)
            "Personal Hub actif\nServeur : ${SupabaseClient.SUPABASE_URL}\n\nSync auto toutes les 15 min."
        else
            "En attente de l'autorisation de notification…"
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat?.contains(packageName) == true
    }

    private fun requestPermissions() {
        val base = arrayOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_PHONE_STATE
        )
        val media = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
        else
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

        val missing = (base + media).filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty())
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), 100)
    }

    private fun schedulePeriodic() {
        val req = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "periodic_sync", ExistingPeriodicWorkPolicy.KEEP, req
        )
    }
}
