package com.financeflow.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.PendingActions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.financeflow.app.data.FinanceData
import com.financeflow.app.data.FinanceRules
import com.financeflow.app.data.Payable
import com.financeflow.app.data.PlanProgress
import com.financeflow.app.data.Transaction
import com.financeflow.app.data.TxStatus
import com.financeflow.app.data.TxType
import com.financeflow.app.ui.MainViewModel
import com.financeflow.app.ui.Routes
import com.financeflow.app.ui.components.ChipRow
import com.financeflow.app.ui.components.EmptyState
import com.financeflow.app.ui.components.FFCard
import com.financeflow.app.ui.components.IconBadge
import com.financeflow.app.ui.components.MarkDoneDialog
import com.financeflow.app.ui.components.PayDialogHost
import com.financeflow.app.ui.components.PayableRow
import com.financeflow.app.ui.components.Pill
import com.financeflow.app.ui.components.PrimaryButton
import com.financeflow.app.ui.components.ProgressBar
import com.financeflow.app.ui.components.ScreenHeader
import com.financeflow.app.ui.components.Segmented
import com.financeflow.app.ui.components.SmallActionButton
import com.financeflow.app.ui.components.TxList
import com.financeflow.app.ui.components.categoryIcon
import com.financeflow.app.ui.theme.FF
import com.financeflow.app.util.Dates
import com.financeflow.app.util.Money
import com.financeflow.app.util.YearMonth
import com.financeflow.app.util.epochDay

@Composable
fun InstallmentsScreen(vm: MainViewModel, d: FinanceData, initialTab: Int, nav: NavController, onTx: (Transaction) -> Unit) {
    var tab by rememberSaveable { mutableStateOf(initialTab) }
    var paying by remember { mutableStateOf<Payable?>(null) }
    var settle by remember { mutableStateOf<Transaction?>(null) }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            when (tab) { 1 -> "Contas a Pagar"; 2 -> "A Receber"; else -> "Parcelamentos" },
            subtitle = when (tab) {
                1 -> "Compromissos financeiros"
                2 -> "Valores pendentes de recebimento"
                else -> "Controle de compras parceladas e prazos"
            },
        )
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Segmented(listOf("Parceladas", "Contas a Pagar", "A Receber"), tab, { tab = it })
            }
            when (tab) {
                0 -> plansTab(d, onTx, onPay = { settle = it })
                1 -> payablesTab(d, onTx, onPay = { paying = it })
                else -> receivablesTab(d, onTx, onReceive = { settle = it }, onNew = { nav.navigate(Routes.newTx("INCOME")) })
            }
        }
    }

    PayDialogHost(paying, d, vm) { paying = null }
    settle?.let { t ->
        if (t.cardId != null && t.type == TxType.EXPENSE) {
            // Parcela no cartão: quita a fatura inteira daquele mês.
            val bill = d.payables().filterIsInstance<Payable.CardBill>().firstOrNull { b -> b.items.any { it.id == t.id } }
            PayDialogHost(bill ?: Payable.Single(t), d, vm) { settle = null }
        } else {
            MarkDoneDialog(t, d, { acc, date -> vm.markDone(t, acc, date) }, { settle = null })
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.plansTab(d: FinanceData, onTx: (Transaction) -> Unit, onPay: (Transaction) -> Unit) {
    val progresses = d.plans.map { d.planProgress(it) }
    val active = progresses.filter { it.remainingCount > 0 }
    val expenses = active.filter { it.plan.type == TxType.EXPENSE }
    val total = expenses.sumOf { it.paidAmount + it.remainingAmount }
    val paid = expenses.sumOf { it.paidAmount }
    val remaining = expenses.sumOf { it.remainingAmount }
    val futureCount = expenses.sumOf { it.remainingCount }
    val next = YearMonth.now().plusMonths(1)
    val nextMonthImpact = d.transactions.filter {
        it.planId != null && it.type == TxType.EXPENSE && it.status == TxStatus.PENDING &&
            it.date in next.atDay(1).epochDay()..next.atEndOfMonth().epochDay()
    }.sumOf { it.amount }

    item {
        FFCard(accent = FF.Sky, background = FF.Surface1) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Total Comprometido em Parcelas", color = FF.TextSecondary, style = MaterialTheme.typography.labelLarge)
                    Text(Money.format(total), color = FF.TextPrimary, style = MaterialTheme.typography.headlineLarge)
                }
                Pill("${expenses.size} ativas", FF.Sky)
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = FF.Border)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FFCard(Modifier.weight(1f), padding = 12.dp) {
                    Text("Já Pago", color = FF.EmeraldLight, style = MaterialTheme.typography.labelLarge)
                    Text(Money.format(paid), color = FF.EmeraldLight, style = MaterialTheme.typography.titleSmall)
                    val permille = if (total > 0) paid * 1000 / total else 0
                    Text("${permille / 10},${permille % 10}% liquidado", color = FF.TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
                FFCard(Modifier.weight(1f), padding = 12.dp) {
                    Text("Restante", color = FF.CrimsonLight, style = MaterialTheme.typography.labelLarge)
                    Text(Money.format(remaining), color = FF.TextPrimary, style = MaterialTheme.typography.titleSmall)
                    Text("$futureCount parcelas futuras", color = FF.TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
    item {
        Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Contratos", style = MaterialTheme.typography.headlineSmall, color = FF.TextPrimary, modifier = Modifier.weight(1f))
            Text("${progresses.size} contratos", color = FF.TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
    if (progresses.isEmpty()) item { FFCard { EmptyState("Nenhuma compra parcelada.\nUse + Novo → Parcelado.") } }
    val sorted = progresses.sortedWith(compareBy<PlanProgress> { it.remainingCount == 0 }.thenBy { it.next?.date ?: Long.MAX_VALUE })
    items(sorted, key = { it.plan.id }) { p -> PlanCard(p, d, onTx, onPay) }
    if (nextMonthImpact > 0) {
        item {
            FFCard(background = FF.Surface1) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(Icons.AutoMirrored.Filled.TrendingDown, FF.Sky, bg = FF.Sky.copy(alpha = 0.12f))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Impacto no próximo mês:", color = FF.TextSecondary, style = MaterialTheme.typography.bodySmall)
                        Text("${Money.format(nextMonthImpact)} em parcelas", color = FF.TextPrimary, style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanCard(p: PlanProgress, d: FinanceData, onTx: (Transaction) -> Unit, onPay: (Transaction) -> Unit) {
    var expanded by rememberSaveable(p.plan.id) { mutableStateOf(false) }
    val income = p.plan.type == TxType.INCOME
    val finished = p.remainingCount == 0
    val cat = p.plan.categoryId?.let { d.categoryMap[it] }
    val per = p.items.lastOrNull()?.amount ?: 0
    FFCard(onClick = { expanded = !expanded }) {
        Row(verticalAlignment = Alignment.Top) {
            IconBadge(categoryIcon(cat?.icon ?: if (income) "income" else "shopping"), if (income) FF.Emerald else FF.Sky, size = 44.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(p.plan.description, color = FF.TextPrimary, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("${p.totalCount}x de ${Money.format(per)}", color = FF.TextSecondary, style = MaterialTheme.typography.bodySmall)
                p.plan.cardId?.let { d.cardMap[it] }?.let { Text("Cartão ${it.name}", color = FF.TextTertiary, style = MaterialTheme.typography.bodySmall) }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(Money.format(p.plan.totalAmount), color = FF.TextPrimary, style = MaterialTheme.typography.titleSmall)
                Text(if (income) "A receber" else "Total", color = FF.TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(12.dp))
        Row {
            Text(
                "${p.paidCount}/${p.totalCount} ${if (income) "recebidas" else "pagas"} (${(p.fraction * 100).toInt()}%)",
                color = FF.EmeraldLight, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f),
            )
            Text(
                if (finished) "Quitado" else "Restante: ${Money.format(p.remainingAmount)} (${p.remainingCount})",
                color = if (p.remainingCount == 1) FF.CrimsonLight else FF.TextSecondary, style = MaterialTheme.typography.labelMedium,
            )
        }
        Spacer(Modifier.height(8.dp))
        ProgressBar(p.fraction, if (finished) FF.Sky else FF.Emerald)
        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = FF.Border.copy(alpha = 0.6f))
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.CalendarMonth, null, tint = FF.TextSecondary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                if (p.next != null) {
                    Text("Próximo vencimento:", color = FF.TextSecondary, style = MaterialTheme.typography.bodySmall)
                    Text(
                        Dates.full(p.next.date), style = MaterialTheme.typography.titleSmall,
                        color = if (FinanceRules.isLate(p.next)) FF.Crimson else FF.TextPrimary,
                    )
                } else {
                    Text("Finalizado em ${p.last?.let { Dates.full(it.date) } ?: "-"}", color = FF.TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
            when {
                p.next != null && p.remainingCount == 1 -> Pill("Reta final", FF.Crimson)
                else -> Unit
            }
            if (p.next != null) {
                Spacer(Modifier.width(8.dp))
                SmallActionButton("${if (income) "Receber" else "Pagar"} ${p.next.installmentNumber}", { onPay(p.next) })
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.Center) {
            Icon(if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore, "Parcelas", tint = FF.TextTertiary)
        }
        if (expanded) {
            TxList(p.items, d, onTx, showDate = true)
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.payablesTab(d: FinanceData, onTx: (Transaction) -> Unit, onPay: (Payable) -> Unit) {
    item(key = "payables") { PayablesContent(d, onTx, onPay) }
}

@Composable
private fun PayablesContent(d: FinanceData, onTx: (Transaction) -> Unit, onPay: (Payable) -> Unit) {
    var filter by rememberSaveable { mutableStateOf(0) }
    val today = Dates.todayEpoch()
    val payables = d.payables()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TotalBox("Pendente", Money.format(payables.filter { it.date >= today }.sumOf { it.amount }), FF.Sky, Modifier.weight(1f))
            TotalBox("Atrasado", Money.format(payables.filter { it.date < today }.sumOf { it.amount }), FF.Crimson, Modifier.weight(1f))
        }
        ChipRow(listOf("A pagar", "Atrasadas", "Pagas", "Canceladas"), filter, { filter = it })
        when (filter) {
            0, 1 -> {
                val list = if (filter == 1) payables.filter { it.date < today } else payables
                if (list.isEmpty()) FFCard { EmptyState("Nada por aqui 🎉") } else FFCard(padding = 0.dp) {
                    list.forEachIndexed { i, p ->
                        if (i > 0) HorizontalDivider(color = FF.Border.copy(alpha = 0.5f))
                        PayableRow(p, d, onTx, onPay)
                    }
                }
            }
            else -> {
                val status = if (filter == 2) TxStatus.DONE else TxStatus.CANCELLED
                val list = d.transactions.filter { it.type == TxType.EXPENSE && it.status == status }.sortedByDescending { it.date }.take(100)
                if (list.isEmpty()) FFCard { EmptyState("Nenhuma conta") } else TxList(list, d, onTx, showDate = true)
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.receivablesTab(
    d: FinanceData, onTx: (Transaction) -> Unit, onReceive: (Transaction) -> Unit, onNew: () -> Unit,
) {
    item(key = "receivables") { ReceivablesContent(d, onTx, onReceive, onNew) }
}

@Composable
private fun ReceivablesContent(d: FinanceData, onTx: (Transaction) -> Unit, onReceive: (Transaction) -> Unit, onNew: () -> Unit) {
    var filter by rememberSaveable { mutableStateOf(0) }
    val today = Dates.todayEpoch()
    val incomes = d.transactions.filter { it.type == TxType.INCOME }
    val pending = incomes.filter { it.status == TxStatus.PENDING }.sortedBy { it.date }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TotalBox("A receber", Money.format(pending.filter { it.date >= today }.sumOf { it.amount }), FF.Emerald, Modifier.weight(1f))
            TotalBox("Atrasado", Money.format(pending.filter { it.date < today }.sumOf { it.amount }), FF.Crimson, Modifier.weight(1f))
        }
        PrimaryButton("Novo valor a receber", onNew, Modifier.fillMaxWidth(), icon = Icons.Outlined.PendingActions)
        ChipRow(listOf("Pendentes", "Atrasados", "Recebidos", "Cancelados"), filter, { filter = it })
        val list = when (filter) {
            0 -> pending
            1 -> pending.filter { it.date < today }
            2 -> incomes.filter { it.status == TxStatus.DONE }.sortedByDescending { it.date }.take(100)
            else -> incomes.filter { it.status == TxStatus.CANCELLED }
        }
        if (list.isEmpty()) {
            FFCard { EmptyState("Nenhum valor") }
        } else if (filter <= 1) {
            FFCard(padding = 0.dp) {
                list.forEachIndexed { i, t ->
                    if (i > 0) HorizontalDivider(color = FF.Border.copy(alpha = 0.5f))
                    ReceivableRow(t, d, onTx, onReceive)
                }
            }
        } else {
            TxList(list, d, onTx, showDate = true)
        }
    }
}

@Composable
private fun ReceivableRow(t: Transaction, d: FinanceData, onTx: (Transaction) -> Unit, onReceive: (Transaction) -> Unit) {
    val late = FinanceRules.isLate(t)
    Row(
        Modifier.fillMaxWidth().then(Modifier.padding(0.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            com.financeflow.app.ui.components.TxRow(t, d, { onTx(t) })
        }
    }
    Row(Modifier.fillMaxWidth().padding(start = 66.dp, end = 14.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            if (late) "Previsto para ${Dates.full(t.date)} — atrasado" else "Previsto para ${Dates.full(t.date)}",
            color = if (late) FF.CrimsonLight else FF.TextSecondary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f),
        )
        SmallActionButton("Receber", { onReceive(t) })
    }
}

@Composable
private fun TotalBox(label: String, value: String, color: Color, modifier: Modifier) {
    FFCard(modifier, padding = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.CheckCircle, null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, color = color, style = MaterialTheme.typography.labelLarge)
        }
        Text(value, color = FF.TextPrimary, style = MaterialTheme.typography.titleSmall)
    }
}
