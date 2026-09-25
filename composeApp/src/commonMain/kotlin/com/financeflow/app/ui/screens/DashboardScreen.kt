package com.financeflow.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.NorthEast
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.SouthWest
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.financeflow.app.data.FinanceData
import com.financeflow.app.data.Payable
import com.financeflow.app.data.Transaction
import com.financeflow.app.data.TxStatus
import com.financeflow.app.data.TxType
import com.financeflow.app.ui.MainViewModel
import com.financeflow.app.ui.Routes
import com.financeflow.app.ui.components.BarGroup
import com.financeflow.app.ui.components.EmptyState
import com.financeflow.app.ui.components.FFCard
import com.financeflow.app.ui.components.IconBadge
import com.financeflow.app.ui.components.IncomeExpenseChart
import com.financeflow.app.ui.components.MarkDoneDialog
import com.financeflow.app.ui.components.PayDialogHost
import com.financeflow.app.ui.components.PayableRow
import com.financeflow.app.ui.components.Pill
import com.financeflow.app.ui.components.SectionHeader
import com.financeflow.app.ui.components.SmallActionButton
import com.financeflow.app.ui.components.TxList
import com.financeflow.app.ui.components.accountIcon
import com.financeflow.app.ui.theme.FF
import com.financeflow.app.util.Dates
import com.financeflow.app.util.Money
import com.financeflow.app.util.YearMonth

fun hidden(value: String, hide: Boolean) = if (hide) "R$ •••••" else value

@Composable
fun DashboardScreen(vm: MainViewModel, d: FinanceData, nav: NavController, onTx: (Transaction) -> Unit) {
    val user by vm.user.collectAsStateWithLifecycle()
    val hide by vm.hideBalance.collectAsStateWithLifecycle()
    var paying by remember { mutableStateOf<Payable?>(null) }
    var receiving by remember { mutableStateOf<Transaction?>(null) }
    val month = YearMonth.now()
    val summary = remember(d) { d.monthSummary(month) }
    val payables = remember(d) { d.payables() }
    val receivables = remember(d) { d.pending.filter { it.type == TxType.INCOME }.sortedBy { it.date } }
    val latest = remember(d) {
        d.transactions.filter { it.status == TxStatus.DONE }
            .sortedWith(compareByDescending<Transaction> { it.doneDate ?: it.date }.thenByDescending { it.id }).take(5)
    }
    val chart = remember(d) {
        (5 downTo 0).map { month.minusMonths(it.toLong()) }.map {
            val s = d.monthSummary(it)
            BarGroup(Dates.monthShort(it), s.incomeTotal, s.expenseTotal)
        }
    }
    val unread = d.notifications.count { !it.read }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // Cabeçalho
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(42.dp).clip(CircleShape).background(FF.Emerald.copy(alpha = 0.2f)).border(1.dp, FF.Emerald, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(user?.name?.firstOrNull()?.uppercase() ?: "?", color = FF.EmeraldLight, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Olá, ${user?.name?.substringBefore(' ') ?: ""}", color = FF.TextSecondary, style = MaterialTheme.typography.bodySmall)
                Text("${Dates.monthName(month)}/${month.year}", color = FF.TextPrimary, style = MaterialTheme.typography.titleMedium)
            }
            Box {
                IconButton(onClick = { nav.navigate(Routes.NOTIFICATIONS) }) {
                    Icon(Icons.Outlined.Notifications, "Notificações", tint = FF.TextPrimary)
                }
                if (unread > 0) Box(Modifier.align(Alignment.TopEnd).padding(10.dp).size(9.dp).clip(CircleShape).background(FF.Crimson))
            }
        }

        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Saldo atual
            FFCard(accent = FF.Emerald, background = FF.Surface1, padding = 20.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.AccountBalanceWallet, null, tint = FF.TextSecondary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Saldo Atual", color = FF.TextSecondary, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    IconButton(onClick = { vm.toggleHideBalance() }) {
                        Icon(if (hide) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, "Ocultar saldo", tint = FF.TextSecondary)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        hidden(Money.format(d.currentBalance), hide),
                        style = MaterialTheme.typography.displaySmall,
                        color = if (d.currentBalance < 0) FF.Crimson else FF.TextPrimary,
                        modifier = Modifier.weight(1f), maxLines = 1,
                    )
                    val res = summary.result
                    if (!hide && (summary.incomeTotal > 0 || summary.expenseTotal > 0)) {
                        Pill(Money.signed(res, res >= 0), if (res >= 0) FF.Emerald else FF.Crimson)
                    }
                }
                Text(
                    "Disponível em ${d.activeAccounts.size} conta(s) ativa(s)",
                    color = FF.TextSecondary, style = MaterialTheme.typography.bodySmall,
                )
            }

            // Indicadores
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MiniStat("A Receber", hidden("+ " + Money.format(d.toReceive), hide), FF.EmeraldLight, Icons.Outlined.SouthWest, "Entrada", Modifier.weight(1f)) {
                    nav.navigate(Routes.installments(2))
                }
                MiniStat("A Pagar", hidden("- " + Money.format(d.toPay), hide), FF.CrimsonLight, Icons.Outlined.NorthEast, "Saída", Modifier.weight(1f)) {
                    nav.navigate(Routes.installments(1))
                }
                MiniStat("Projetado", hidden("= " + Money.format(d.projected), hide), FF.Sky, Icons.Outlined.AutoGraph, "Previsão", Modifier.weight(1f), highlight = true) {
                    nav.navigate(Routes.FORECAST)
                }
            }

            // Resumo do mês + gráfico
            FFCard {
                SectionHeader("Resumo de ${Dates.monthName(month)}", action = "Relatórios", onAction = { nav.navigate(Routes.REPORTS) })
                Spacer(Modifier.height(8.dp))
                Row {
                    SummaryCell("Receitas", Money.format(summary.incomeTotal), FF.EmeraldLight, Modifier.weight(1f))
                    SummaryCell("Despesas", Money.format(summary.expenseTotal), FF.CrimsonLight, Modifier.weight(1f))
                    SummaryCell("Resultado", Money.format(summary.result), if (summary.result >= 0) FF.EmeraldLight else FF.Crimson, Modifier.weight(1f))
                }
                Spacer(Modifier.height(16.dp))
                Text("Receitas x Despesas", color = FF.TextSecondary, style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                IncomeExpenseChart(chart)
            }

            // Contas
            SectionHeader("Minhas Contas", action = "Ver todas", onAction = { nav.navigate(Routes.ACCOUNTS) })
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                d.activeAccounts.forEach { a ->
                    FFCard(Modifier.width(150.dp), onClick = { nav.navigate(Routes.ACCOUNTS) }, padding = 12.dp) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconBadge(accountIcon(a.type), Color(a.color), size = 32.dp, bg = Color(a.color).copy(alpha = 0.15f))
                            Spacer(Modifier.weight(1f))
                            Text(a.type.label.substringBefore(' '), color = FF.TextSecondary, style = MaterialTheme.typography.labelSmall)
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(a.name, color = FF.TextSecondary, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(hidden(Money.format(d.balances[a.id] ?: 0), hide), color = FF.TextPrimary, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                    }
                }
                FFCard(Modifier.width(120.dp), onClick = { nav.navigate(Routes.ACCOUNTS) }, padding = 12.dp, background = FF.Surface1) {
                    Text("+ Nova conta", color = FF.EmeraldLight, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(vertical = 22.dp))
                }
            }

            // Próximos vencimentos
            FFCard(padding = 0.dp) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Próximos Vencimentos", style = MaterialTheme.typography.headlineSmall, color = FF.TextPrimary, modifier = Modifier.weight(1f))
                    if (payables.isNotEmpty()) Pill("${payables.size} pendentes", FF.Crimson)
                }
                if (payables.isEmpty()) EmptyState("Nenhuma conta pendente 🎉")
                payables.take(4).forEachIndexed { i, p ->
                    if (i > 0) HorizontalDivider(color = FF.Border.copy(alpha = 0.5f))
                    PayableRow(p, d, onTx) { paying = it }
                }
                if (payables.size > 4) {
                    Text(
                        "Ver todas as contas a pagar", color = FF.Sky, style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.fillMaxWidth().clickable { nav.navigate(Routes.installments(1)) }.padding(16.dp),
                    )
                }
            }

            // Próximos recebimentos
            if (receivables.isNotEmpty()) {
                FFCard(padding = 0.dp) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Próximos Recebimentos", style = MaterialTheme.typography.headlineSmall, color = FF.TextPrimary, modifier = Modifier.weight(1f))
                        Text("Ver todos", color = FF.Sky, style = MaterialTheme.typography.labelLarge, modifier = Modifier.clickable { nav.navigate(Routes.installments(2)) })
                    }
                    receivables.take(3).forEachIndexed { i, t ->
                        if (i > 0) HorizontalDivider(color = FF.Border.copy(alpha = 0.5f))
                        Row(Modifier.fillMaxWidth().clickable { onTx(t) }.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(t.description, color = FF.TextPrimary, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    (t.person?.takeIf { it.isNotBlank() }?.let { "$it • " } ?: "") + Dates.short(t.date),
                                    color = if (t.date < Dates.todayEpoch()) FF.CrimsonLight else FF.TextSecondary, style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("+ " + Money.format(t.amount), color = FF.EmeraldLight, style = MaterialTheme.typography.titleSmall)
                                SmallActionButton("Receber", { receiving = t })
                            }
                        }
                    }
                }
            }

            // Últimas movimentações
            SectionHeader("Últimas Movimentações", action = "Ver extrato", onAction = { nav.navigate(Routes.STATEMENT) })
            if (latest.isEmpty()) {
                FFCard { EmptyState("Nenhuma movimentação ainda.\nToque em + Novo para registrar.") }
            } else {
                TxList(latest, d, onTx, showDate = true)
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    PayDialogHost(paying, d, vm) { paying = null }
    receiving?.let { t -> MarkDoneDialog(t, d, { acc, date -> vm.markDone(t, acc, date) }, { receiving = null }) }
}

@Composable
private fun MiniStat(
    title: String, value: String, color: Color, icon: ImageVector, tag: String,
    modifier: Modifier, highlight: Boolean = false, onClick: () -> Unit,
) {
    Column(
        modifier.clip(RoundedCornerShape(12.dp))
            .background(if (highlight) FF.Sky.copy(alpha = 0.08f) else FF.Surface2)
            .border(1.dp, if (highlight) FF.Sky.copy(alpha = 0.6f) else FF.Border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick).padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = if (highlight) FF.Sky else FF.TextSecondary, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f), maxLines = 1)
            Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
        }
        Text(value, color = color, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Pill(tag, color)
    }
}

@Composable
private fun SummaryCell(label: String, value: String, color: Color, modifier: Modifier) {
    Column(modifier) {
        Text(label, color = FF.TextSecondary, style = MaterialTheme.typography.labelMedium)
        Text(value, color = color, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
