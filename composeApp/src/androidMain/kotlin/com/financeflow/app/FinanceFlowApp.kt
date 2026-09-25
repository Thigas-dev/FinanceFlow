package com.financeflow.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.room.Room
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.financeflow.app.data.AppDatabase
import com.financeflow.app.data.Repository
import com.financeflow.app.work.DueCheckWorker
import java.util.concurrent.TimeUnit

class FinanceFlowApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.build(
            Room.databaseBuilder(this, AppDatabase::class.java, getDatabasePath("financeflow.db").absolutePath),
        )
        container = AppContainer(Repository(db), Session(SharedPrefsStore(this)), AndroidNotifier(this))

        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(DueCheckWorker.CHANNEL_ID, "Vencimentos", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Alertas de contas a pagar e valores a receber"
            },
        )

        runCatching {
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "due-check",
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<DueCheckWorker>(12, TimeUnit.HOURS).build(),
            )
        }
    }
}
