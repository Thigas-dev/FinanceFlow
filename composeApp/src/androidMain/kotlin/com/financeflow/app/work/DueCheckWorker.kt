package com.financeflow.app.work

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.financeflow.app.FinanceFlowApp
import com.financeflow.app.MainActivity
import com.financeflow.app.R
import com.financeflow.app.data.AppNotification

class DueCheckWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val c = (applicationContext as FinanceFlowApp).container
        val userId = c.session.userId ?: return Result.success()
        c.repository.generateRecurrences(userId)
        val created = c.repository.runDueCheck(userId, c.session.alertDaysBefore)
        if (c.session.notificationsEnabled) post(applicationContext, created)
        return Result.success()
    }

    companion object {
        const val CHANNEL_ID = "due"

        fun post(context: Context, items: List<AppNotification>) {
            if (items.isEmpty()) return
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED &&
                android.os.Build.VERSION.SDK_INT >= 33
            ) return
            val intent = PendingIntent.getActivity(
                context, 0,
                Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
                PendingIntent.FLAG_IMMUTABLE,
            )
            val manager = NotificationManagerCompat.from(context)
            items.take(5).forEach { n ->
                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(n.title)
                    .setContentText(n.message)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(n.message))
                    .setContentIntent(intent)
                    .setAutoCancel(true)
                    .build()
                try {
                    manager.notify(n.key.hashCode(), notification)
                } catch (_: SecurityException) {
                }
            }
        }
    }
}
