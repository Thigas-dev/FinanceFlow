package com.financeflow.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.financeflow.app.data.FinanceData
import com.financeflow.app.ui.MainViewModel
import com.financeflow.app.ui.Routes
import com.financeflow.app.ui.components.ConfirmDialog
import com.financeflow.app.ui.components.EmptyState
import com.financeflow.app.ui.components.FFCard
import com.financeflow.app.ui.components.FFTextField
import com.financeflow.app.ui.components.IconBadge
import com.financeflow.app.ui.components.PrimaryButton
import com.financeflow.app.ui.components.ScreenHeader
import com.financeflow.app.ui.components.SelectField
import com.financeflow.app.ui.components.ToggleRow
import com.financeflow.app.ui.theme.FF
import com.financeflow.app.util.Money
import com.financeflow.app.util.Dates

@Composable
fun MoreScreen(vm: MainViewModel, d: FinanceData, nav: NavController) {
    val user by vm.user.collectAsStateWithLifecycle()
    var confirmLogout by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Mais", subtitle = user?.email)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            FFCard(padding = 0.dp) {
                MenuItem(Icons.Outlined.AccountBalance, "Contas", "${d.activeAccounts.size} ativas • ${Money.format(d.currentBalance)}", FF.Emerald) { nav.navigate(Routes.ACCOUNTS) }
                Divider()
                MenuItem(Icons.Outlined.CreditCard, "Cartões de crédito", "Limites e faturas", FF.Violet) { nav.navigate(Routes.CARDS) }
                Divider()
                MenuItem(Icons.Outlined.Repeat, "Contas recorrentes", "${d.recurrences.count { it.active }} ativas", FF.Sky) { nav.navigate(Routes.RECURRENCES) }
            }
            FFCard(padding = 0.dp) {
                MenuItem(Icons.Outlined.AutoGraph, "Previsão de saldo", "7, 30, 60, 90 dias ou personalizado", FF.Sky) { nav.navigate(Routes.FORECAST) }
                Divider()
                MenuItem(Icons.Outlined.CalendarMonth, "Calendário financeiro", "Compromissos por data", FF.Amber) { nav.navigate(Routes.CALENDAR) }
                Divider()
                MenuItem(Icons.Outlined.BarChart, "Relatórios", "Mensal, categorias e parcelas", FF.Emerald) { nav.navigate(Routes.REPORTS) }
            }
            FFCard(padding = 0.dp) {
                MenuItem(Icons.Outlined.Category, "Categorias", "${d.categories.size} categorias", FF.TextSecondary) { nav.navigate(Routes.CATEGORIES) }
                Divider()
                MenuItem(Icons.Outlined.Notifications, "Notificações", "${d.notifications.count { !it.read }} não lidas", FF.TextSecondary) { nav.navigate(Routes.NOTIFICATIONS) }
                Divider()
                MenuItem(Icons.Outlined.Settings, "Configurações", "Perfil, segurança e preferências", FF.TextSecondary) { nav.navigate(Routes.SETTINGS) }
            }
            FFCard(padding = 0.dp) {
                MenuItem(Icons.AutoMirrored.Filled.Logout, "Sair", null, FF.Crimson) { confirmLogout = true }
            }
            Text("FinanceFlow 1.0 • dados armazenados localmente", color = FF.TextTertiary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.fillMaxWidth())
        }
    }
    if (confirmLogout) {
        ConfirmDialog("Sair", "Deseja encerrar a sessão?", confirm = "Sair", destructive = true, onConfirm = { vm.logout() }, onDismiss = { confirmLogout = false })
    }
}

@Composable
private fun Divider() = HorizontalDivider(color = FF.Border.copy(alpha = 0.5f))

@Composable
fun MenuItem(icon: ImageVector, title: String, subtitle: String?, color: Color, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        IconBadge(icon, color)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = FF.TextPrimary, style = MaterialTheme.typography.titleSmall)
            if (subtitle != null) Text(subtitle, color = FF.TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = FF.TextTertiary)
    }
}

@Composable
fun SettingsScreen(vm: MainViewModel, nav: NavController, onBack: () -> Unit) {
    val user by vm.user.collectAsStateWithLifecycle()
    val hide by vm.hideBalance.collectAsStateWithLifecycle()
    var name by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(user?.id) { if (name.isEmpty()) name = user?.name.orEmpty() }
    var notifications by remember { mutableStateOf(vm.session.notificationsEnabled) }
    var alertDays by remember { mutableStateOf(vm.session.alertDaysBefore) }
    var current by rememberSaveable { mutableStateOf("") }
    var newPass by rememberSaveable { mutableStateOf("") }
    var passMsg by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    Column(Modifier.fillMaxSize().imePadding()) {
        ScreenHeader("Configurações", onBack = onBack)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SettingsSection("Perfil", Icons.Outlined.Person) {
                FFTextField(name, { name = it }, "Nome")
                Text(user?.email.orEmpty(), color = FF.TextSecondary, style = MaterialTheme.typography.bodySmall)
                PrimaryButton("Salvar perfil", { if (name.isNotBlank()) vm.updateName(name) }, Modifier.fillMaxWidth())
            }
            SettingsSection("Categorias", Icons.Outlined.Category) {
                TextButton(onClick = { nav.navigate(Routes.CATEGORIES) }) { Text("Gerenciar categorias", color = FF.Sky) }
            }
            SettingsSection("Notificações", Icons.Outlined.NotificationsActive) {
                ToggleRow("Alertas de vencimento", "Antes, no dia e após o vencimento", notifications, {
                    notifications = it; vm.session.notificationsEnabled = it
                })
                SelectField("Avisar com antecedência de", alertDays, listOf(1, 2, 3, 5, 7), { "$it dia(s)" }, {
                    alertDays = it; vm.session.alertDaysBefore = it
                })
            }
            SettingsSection("Segurança", Icons.Outlined.Lock) {
                FFTextField(current, { current = it }, "Senha atual", password = true)
                FFTextField(newPass, { newPass = it }, "Nova senha (mín. 6)", password = true)
                passMsg?.let { Text(it.second, color = if (it.first) FF.EmeraldLight else FF.Crimson, style = MaterialTheme.typography.bodySmall) }
                PrimaryButton("Alterar senha", {
                    if (newPass.length < 6) passMsg = false to "A nova senha deve ter ao menos 6 caracteres"
                    else vm.changePassword(current, newPass) { ok ->
                        passMsg = if (ok) true to "Senha alterada" else false to "Senha atual incorreta"
                        if (ok) { current = ""; newPass = "" }
                    }
                }, Modifier.fillMaxWidth())
            }
            SettingsSection("Preferências", Icons.Outlined.Visibility) {
                ToggleRow("Ocultar saldos", "Esconde valores no Início", hide, { vm.toggleHideBalance() })
                Text(
                    "Backup: os dados participam do backup automático do Android (conta Google do aparelho).",
                    color = FF.TextSecondary, style = MaterialTheme.typography.bodySmall,
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SettingsSection(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    FFCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = FF.Emerald)
            Spacer(Modifier.width(8.dp))
            Text(title, color = FF.TextPrimary, style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { content() }
    }
}

@Composable
fun NotificationsScreen(vm: MainViewModel, d: FinanceData, onBack: () -> Unit) {
    LaunchedEffect(Unit) { vm.markNotificationsRead() }
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Notificações", onBack = onBack) {
            if (d.notifications.isNotEmpty()) TextButton(onClick = { vm.clearNotifications() }) { Text("Limpar", color = FF.Sky) }
        }
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (d.notifications.isEmpty()) EmptyState("Nenhuma notificação")
            d.notifications.forEach { n ->
                val color = when (n.type) {
                    "late" -> FF.Crimson
                    "today" -> FF.Amber
                    "income" -> FF.Emerald
                    else -> FF.Sky
                }
                FFCard(borderColor = if (!n.read) color.copy(alpha = 0.6f) else FF.Border) {
                    Row(verticalAlignment = Alignment.Top) {
                        IconBadge(if (n.type == "late") Icons.Outlined.ErrorOutline else Icons.Outlined.Notifications, color)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(n.title, color = FF.TextPrimary, style = MaterialTheme.typography.titleSmall)
                            Text(n.message, color = FF.TextSecondary, style = MaterialTheme.typography.bodyMedium)
                            Text(Dates.dateTime(n.date), color = FF.TextTertiary, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
