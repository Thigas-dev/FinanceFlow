package com.financeflow.app

import androidx.compose.ui.window.ComposeUIViewController
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.financeflow.app.data.AppDatabase
import com.financeflow.app.data.Repository
import com.financeflow.app.ui.AppRoot
import com.financeflow.app.ui.theme.FinanceFlowTheme
import platform.UIKit.UIViewController

/**
 * Ponto de entrada chamado pelo Swift (iosApp/iosApp/ContentView.swift).
 * O Swift fornece o caminho do banco, as preferências (UserDefaults) e as notificações.
 */
fun MainViewController(dbPath: String, store: KeyValueStore, notifier: Notifier): UIViewController {
    val db = AppDatabase.build(Room.databaseBuilder<AppDatabase>(name = dbPath).setDriver(BundledSQLiteDriver()))
    val container = AppContainer(Repository(db), Session(store), notifier)
    return ComposeUIViewController {
        FinanceFlowTheme { AppRoot(container) }
    }
}
