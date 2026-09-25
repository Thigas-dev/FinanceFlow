package com.financeflow.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.financeflow.app.data.AppNotification
import com.financeflow.app.work.DueCheckWorker

class SharedPrefsStore(context: Context) : KeyValueStore {
    private val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)
    override fun getLong(key: String, fallback: Long) = prefs.getLong(key, fallback)
    override fun putLong(key: String, value: Long) = prefs.edit().putLong(key, value).apply()
    override fun getBoolean(key: String, fallback: Boolean) = prefs.getBoolean(key, fallback)
    override fun putBoolean(key: String, value: Boolean) = prefs.edit().putBoolean(key, value).apply()
    override fun getInt(key: String, fallback: Int) = prefs.getInt(key, fallback)
    override fun putInt(key: String, value: Int) = prefs.edit().putInt(key, value).apply()
}

/** No Android os alertas futuros vêm do WorkManager (DueCheckWorker), então [schedule] não faz nada. */
class AndroidNotifier(private val context: Context) : Notifier {
    override fun post(items: List<AppNotification>) = DueCheckWorker.post(context, items)
    override fun schedule(reminders: List<Reminder>) = Unit
}

@Composable
fun NotificationPermission(enabled: Boolean) {
    if (!enabled || Build.VERSION.SDK_INT < 33) return
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
