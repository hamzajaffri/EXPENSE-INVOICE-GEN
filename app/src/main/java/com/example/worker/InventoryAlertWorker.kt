package com.example.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.AppDatabase
import kotlinx.coroutines.flow.first

class InventoryAlertWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        try {
            val database = AppDatabase.getDatabase(applicationContext)
            // Query low stock items once
            val lowStockProducts = database.productDao().getLowStockProducts().first()

            if (lowStockProducts.isNotEmpty()) {
                showNotification(lowStockProducts.size)
            }
        } catch (e: Exception) {
            // Log to Firebase Crashlytics if available, else standard stacktrace
            try {
                com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance().recordException(e)
            } catch (ignored: Exception) {}
            return Result.failure()
        }
        return Result.success()
    }

    private fun showNotification(count: Int) {
        val channelId = "low_stock_alerts"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Inventory Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifies when inventory items are running low"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Low Stock Alert")
            .setContentText("You have $count product(s) running below the minimum warning threshold.")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(101, notification)
    }
}
