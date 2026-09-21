package com.axesproductivite.hub

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class SyncWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        return try {
            Log.d("SyncWorker", "⏳ Synchronisation démarrée…")
            SmsSync(applicationContext).sync()
            CallLogSync(applicationContext).sync()
            MediaSync(applicationContext).sync()
            Log.d("SyncWorker", "✅ Synchronisation terminée")
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "❌ Erreur: ${e.message}")
            Result.retry()
        }
    }
}
