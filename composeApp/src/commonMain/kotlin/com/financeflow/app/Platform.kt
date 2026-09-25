package com.financeflow.app

import com.financeflow.app.data.AppNotification
import com.financeflow.app.data.Repository

/** Preferências simples. Android: SharedPreferences; iOS: UserDefaults (implementado em Swift). */
interface KeyValueStore {
    fun getLong(key: String, fallback: Long): Long
    fun putLong(key: String, value: Long)
    fun getBoolean(key: String, fallback: Boolean): Boolean
    fun putBoolean(key: String, value: Boolean)
    fun getInt(key: String, fallback: Int): Int
    fun putInt(key: String, value: Int)
}

/** Lembrete agendado para uma data futura (usado no iOS, onde não há trabalho periódico em segundo plano). */
data class Reminder(val id: String, val title: String, val body: String, val epochDay: Long, val hour: Int)

/** Notificações do sistema. Android: NotificationManager; iOS: UNUserNotificationCenter (Swift). */
interface Notifier {
    /** Mostra imediatamente os alertas recém-gerados. */
    fun post(items: List<AppNotification>)

    /** Substitui os lembretes futuros agendados. */
    fun schedule(reminders: List<Reminder>)
}

class Session(private val store: KeyValueStore) {
    var userId: Long?
        get() = store.getLong("userId", -1L).takeIf { it > 0 }
        set(value) = store.putLong("userId", value ?: -1L)

    var notificationsEnabled: Boolean
        get() = store.getBoolean("notifications", true)
        set(value) = store.putBoolean("notifications", value)

    var hideBalance: Boolean
        get() = store.getBoolean("hideBalance", false)
        set(value) = store.putBoolean("hideBalance", value)

    var alertDaysBefore: Int
        get() = store.getInt("alertDays", 3)
        set(value) = store.putInt("alertDays", value)
}

class AppContainer(val repository: Repository, val session: Session, val notifier: Notifier)
