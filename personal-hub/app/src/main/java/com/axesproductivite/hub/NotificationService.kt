package com.axesproductivite.hub

import android.app.Notification
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationService : NotificationListenerService() {

    private val scope = CoroutineScope(Dispatchers.IO)

    /** Apps surveillées : packageName → label en base */
    private val watchedApps = mapOf(
        "com.whatsapp"               to "whatsapp",
        "com.whatsapp.w4b"           to "whatsapp_business",
        "com.facebook.orca"          to "messenger",
        "com.facebook.katana"        to "facebook",
        "org.telegram.messenger"     to "telegram",
        "org.telegram.messenger.web" to "telegram",
        "com.viber.voip"             to "viber",
        "com.instagram.android"      to "instagram"
    )

    /** Textes génériques à ignorer (notifications de médias non textuels) */
    private val ignoredTexts = setOf(
        "Photo", "Vidéo", "Vidéo", "Audio", "Document", "GIF",
        "Image", "Sticker", "Contact", "🎤 Message vocal",
        "📍 Position", "🔗 Lien", "Fichier", "Appel manqué",
        "Missed call", "You have a new message"
    )

    private val deviceId by lazy {
        Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        val appLabel = watchedApps[sbn.packageName] ?: return

        val notif  = sbn.notification ?: return
        val extras = notif.extras     ?: return

        // Ignorer les résumés de groupe (ex: "3 nouveaux messages")
        if (notif.flags and Notification.FLAG_GROUP_SUMMARY != 0) return

        val title   = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim()
        // Préférer EXTRA_BIG_TEXT (message complet) sur EXTRA_TEXT (souvent tronqué)
        val text = (extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
            ?: extras.getCharSequence(Notification.EXTRA_TEXT))
            ?.toString()?.trim()

        if (text.isNullOrBlank() || ignoredTexts.contains(text)) return

        Log.d("HubNotif", "[$appLabel] $title: $text")

        scope.launch {
            SupabaseClient.insertMessage(
                appSource = appLabel,
                contact   = title,
                message   = text,
                deviceId  = deviceId,
                timestamp = sbn.postTime
            )
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) = Unit

    override fun onListenerConnected() {
        Log.d("HubNotif", "Service notifications connecté ✅")
    }
}
