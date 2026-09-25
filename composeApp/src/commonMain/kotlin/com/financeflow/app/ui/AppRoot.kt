package com.financeflow.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.financeflow.app.AppContainer
import com.financeflow.app.ui.components.TxDetailSheet
import com.financeflow.app.ui.screens.AccountsScreen
import com.financeflow.app.ui.screens.CalendarScreen
import com.financeflow.app.ui.screens.CardsScreen
import com.financeflow.app.ui.screens.CategoriesScreen
import com.financeflow.app.ui.screens.DashboardScreen
import com.financeflow.app.ui.screens.ForecastScreen
import com.financeflow.app.ui.screens.InstallmentsScreen
import com.financeflow.app.ui.screens.LoginScreen
import com.financeflow.app.ui.screens.MoreScreen
import com.financeflow.app.ui.screens.NewTransactionScreen
import com.financeflow.app.ui.screens.NotificationsScreen
import com.financeflow.app.ui.screens.RecoverScreen
import com.financeflow.app.ui.screens.RecurrencesScreen
import com.financeflow.app.ui.screens.RegisterScreen
import com.financeflow.app.ui.screens.ReportsScreen
import com.financeflow.app.ui.screens.SettingsScreen
import com.financeflow.app.ui.screens.StatementScreen
import com.financeflow.app.ui.theme.FF

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val RECOVER = "recover"
    const val HOME = "home"
    const val STATEMENT = "extrato"
    const val INSTALLMENTS = "parcelas"
    const val MORE = "mais"
    const val NEW = "new"
    const val ACCOUNTS = "accounts"
    const val CARDS = "cards"
    const val REPORTS = "reports"
    const val CALENDAR = "calendar"
    const val FORECAST = "forecast"
    const val RECURRENCES = "recurrences"
    const val CATEGORIES = "categories"
    const val SETTINGS = "settings"
    const val NOTIFICATIONS = "notifications"

    fun newTx(type: String = "EXPENSE") = "$NEW?type=$type"
    fun editTx(id: Long) = "$NEW?edit=$id"
    fun installments(tab: Int) = "$INSTALLMENTS?tab=$tab"
    fun statement(filter: String) = "$STATEMENT?filter=$filter"
}

private val tabs = listOf(Routes.HOME, Routes.STATEMENT, Routes.INSTALLMENTS, Routes.MORE)

/**
 * Raiz do app, compartilhada entre Android e iOS.
 * [permissionRequest] permite à plataforma pedir permissão de notificação após o login.
 */
@Composable
fun AppRoot(container: AppContainer, permissionRequest: @Composable (Boolean) -> Unit = {}) {
    val vm: MainViewModel = viewModel { MainViewModel(container) }
    val userId by vm.userId.collectAsStateWithLifecycle()
    val data by vm.data.collectAsStateWithLifecycle()
    val nav = rememberNavController()
    val snackbar = remember { SnackbarHostState() }
    var selectedTxId by rememberSaveable { mutableStateOf<Long?>(null) }
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route.orEmpty()
    val currentTab = tabs.firstOrNull { route.startsWith(it) }
    val start = remember { if (userId == null) Routes.LOGIN else Routes.HOME }

    LaunchedEffect(Unit) { vm.messages.collect { snackbar.showSnackbar(it) } }
    // Evita que o aviso da tela anterior cubra botões da nova (ex.: "Salvar" do formulário).
    LaunchedEffect(route) { snackbar.currentSnackbarData?.dismiss() }
    LaunchedEffect(userId) {
        if (userId == null && route.isNotEmpty() && route !in listOf(Routes.LOGIN, Routes.REGISTER, Routes.RECOVER)) {
            nav.navigate(Routes.LOGIN) { popUpTo(nav.graph.id) { inclusive = true } }
        }
    }
    permissionRequest(userId != null)

    val onTx: (com.financeflow.app.data.Transaction) -> Unit = { selectedTxId = it.id }

    Scaffold(
        containerColor = FF.Ink,
        snackbarHost = {
            SnackbarHost(snackbar) { Snackbar(it, containerColor = FF.Surface2, contentColor = FF.TextPrimary, actionColor = FF.Emerald) }
        },
        bottomBar = {
            if (currentTab != null) {
                BottomBar(
                    current = currentTab,
                    onSelect = { tab ->
                        nav.navigate(tab) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNew = { nav.navigate(Routes.newTx()) },
                )
            }
        },
    ) { padding ->
        NavHost(nav, startDestination = start, modifier = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) {
            composable(Routes.LOGIN) {
                LoginScreen(vm, onLogged = { nav.goHome() }, onRegister = { nav.navigate(Routes.REGISTER) }, onRecover = { nav.navigate(Routes.RECOVER) })
            }
            composable(Routes.REGISTER) { RegisterScreen(vm, onDone = { nav.goHome() }, onBack = { nav.popBackStack() }) }
            composable(Routes.RECOVER) { RecoverScreen(vm, onBack = { nav.popBackStack() }) }
            composable(Routes.HOME) { DashboardScreen(vm, data, nav, onTx) }
            composable(
                "${Routes.STATEMENT}?filter={filter}",
                arguments = listOf(navArgument("filter") { type = NavType.StringType; defaultValue = "all" }),
            ) { StatementScreen(vm, data, it.savedStateHandle.get<String>("filter") ?: "all", onTx) }
            composable(
                "${Routes.INSTALLMENTS}?tab={tab}",
                arguments = listOf(navArgument("tab") { type = NavType.IntType; defaultValue = 0 }),
            ) { InstallmentsScreen(vm, data, it.savedStateHandle.get<Int>("tab") ?: 0, nav, onTx) }
            composable(Routes.MORE) { MoreScreen(vm, data, nav) }
            composable(
                "${Routes.NEW}?type={type}&edit={edit}",
                arguments = listOf(
                    navArgument("type") { type = NavType.StringType; defaultValue = "EXPENSE" },
                    navArgument("edit") { type = NavType.LongType; defaultValue = 0L },
                ),
            ) {
                NewTransactionScreen(
                    vm, data,
                    initialType = it.savedStateHandle.get<String>("type") ?: "EXPENSE",
                    editId = it.savedStateHandle.get<Long>("edit")?.takeIf { id -> id > 0 },
                    onClose = { nav.popBackStack() },
                )
            }
            composable(Routes.ACCOUNTS) { AccountsScreen(vm, data, onBack = { nav.popBackStack() }, onTransfer = { nav.navigate(Routes.newTx("TRANSFER")) }) }
            composable(Routes.CARDS) { CardsScreen(vm, data, onBack = { nav.popBackStack() }, onTx = onTx) }
            composable(Routes.REPORTS) { ReportsScreen(data, onBack = { nav.popBackStack() }) }
            composable(Routes.CALENDAR) { CalendarScreen(data, onBack = { nav.popBackStack() }, onTx = onTx) }
            composable(Routes.FORECAST) { ForecastScreen(data, onBack = { nav.popBackStack() }, onTx = onTx) }
            composable(Routes.RECURRENCES) { RecurrencesScreen(vm, data, onBack = { nav.popBackStack() }, onNew = { nav.navigate(Routes.newTx()) }) }
            composable(Routes.CATEGORIES) { CategoriesScreen(vm, data, onBack = { nav.popBackStack() }) }
            composable(Routes.SETTINGS) { SettingsScreen(vm, nav, onBack = { nav.popBackStack() }) }
            composable(Routes.NOTIFICATIONS) { NotificationsScreen(vm, data, onBack = { nav.popBackStack() }) }
        }
    }

    selectedTxId?.let { id ->
        val tx = data.transactions.firstOrNull { it.id == id }
        if (tx == null) {
            if (data.loaded) selectedTxId = null
        } else {
            TxDetailSheet(tx, data, vm, onEdit = { nav.navigate(Routes.editTx(it.id)) }, onDismiss = { selectedTxId = null })
        }
    }
}

private fun NavHostController.goHome() = navigate(Routes.HOME) { popUpTo(graph.id) { inclusive = true } }

@Composable
private fun BottomBar(current: String, onSelect: (String) -> Unit, onNew: () -> Unit) {
    Column(Modifier.background(FF.Surface1)) {
        HorizontalDivider(color = FF.Border)
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().height(64.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            BarItem("Início", Icons.Outlined.GridView, current == Routes.HOME) { onSelect(Routes.HOME) }
            BarItem("Extrato", Icons.AutoMirrored.Filled.ReceiptLong, current == Routes.STATEMENT) { onSelect(Routes.STATEMENT) }
            Column(
                Modifier.weight(1f).clickable(onClick = onNew),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier.offset(y = (-10).dp).size(52.dp).clip(CircleShape).background(FF.Emerald)
                        .border(4.dp, FF.Ink, CircleShape),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Filled.Add, "Novo", tint = FF.Ink, modifier = Modifier.size(30.dp)) }
                Text("Novo", color = FF.TextSecondary, style = MaterialTheme.typography.labelMedium, modifier = Modifier.offset(y = (-8).dp))
            }
            BarItem("Parcelas", Icons.Outlined.CreditCard, current == Routes.INSTALLMENTS) { onSelect(Routes.INSTALLMENTS) }
            BarItem("Mais", Icons.Outlined.MoreHoriz, current == Routes.MORE) { onSelect(Routes.MORE) }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.BarItem(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    Column(
        Modifier.weight(1f).clickable(onClick = onClick).padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val color = if (selected) FF.EmeraldLight else FF.TextSecondary
        Icon(icon, label, tint = color)
        Text(label, color = color, style = MaterialTheme.typography.labelMedium)
    }
}
