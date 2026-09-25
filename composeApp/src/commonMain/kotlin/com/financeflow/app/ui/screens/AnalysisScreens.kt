package com.financeflow.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.financeflow.app.data.FinanceData
import com.financeflow.app.data.Transaction
import com.financeflow.app.data.TxStatus
import com.financeflow.app.data.TxType
import com.financeflow.app.ui.components.BalanceLineChart
import com.financeflow.app.ui.components.BarGroup
import com.financeflow.app.ui.components.ChipRow
import com.financeflow.app.ui.components.DateField
import com.financeflow.app.ui.components.DonutChart
import com.financeflow.app.ui.components.EmptyState
import com.financeflow.app.ui.components.FFCard
import com.financeflow.app.ui.components.IncomeExpenseChart
import com.financeflow.app.ui.components.ProgressBar
import com.financeflow.app.ui.components.ScreenHeader
import com.financeflow.app.ui.components.TxList
import com.financeflow.app.ui.theme.FF
import com.financeflow.app.util.Dates
import com.financeflow.app.util.Money
import kotlinx.datetime.isoDayNumber
import com.financeflow.app.util.YearMonth
import com.financeflow.app.util.epochDay

@Composable
private fun MonthSwitcher(month: YearMonth, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { onChange(-1) }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Anterior", tint = FF.TextSecondary) }
        Text(Dates.monthYear(month), color = FF.TextPrimary, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        IconButton(onClick = { onChange(1) }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Próximo", tint = FF.TextSecondary) }
    }
}

@Composable
private fun StatLine(label: String, value: String, color: Color = FF.TextPrimary) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(label, color = FF.TextSecondary, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(value, color = color, style = MaterialTheme.typography.titleSmall)
    }
}

// ---------------- Relatórios ----------------

@Composable
fun ReportsScreen(d: FinanceData, onBack: () -> Unit) {
    var offset by rememberSaveable { mutableStateOf(0) }
    val month = YearMonth.now().plusMonths(offset.toLong())
    val s = remember(d, month) { d.monthSummary(month) }
    val chart = remember(d, month) {
        (5 downTo 0).map { month.minusMonths(it.toLong()) }.map {
            val m = d.monthSummary(it)
            BarGroup(Dates.monthShort(it), m.incomeTotal, m.expenseTotal)
        }
    }
    val progresses = remember(d) { d.plans.filter { it.type == TxType.EXPENSE }.map { d.planProgress(it) } }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Relatórios", onBack = onBack)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FFCard(background = FF.Surface1, padding = 6.dp) { MonthSwitcher(month) { offset += it } }

            FFCard(accent = FF.Emerald) {
                Text("Relatório mensal", color = FF.TextPrimary, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                StatLine("Total recebido", Money.format(s.received), FF.EmeraldLight)
                StatLine("Total gasto", Money.format(s.spent), FF.CrimsonLight)
                StatLine("Pendente a receber", Money.format(s.pendingIncome), FF.Sky)
                StatLine("Pendente a pagar", Money.format(s.pendingExpense), FF.Amber)
                HorizontalDivider(color = FF.Border, modifier = Modifier.padding(vertical = 6.dp))
                StatLine("Resultado previsto (receitas − despesas)", Money.format(s.result), if (s.result >= 0) FF.EmeraldLight else FF.Crimson)
                StatLine("Maior categoria de despesa", s.byCategory.firstOrNull()?.let { "${it.first} (${Money.format(it.second)})" } ?: "-")
                StatLine("Quantidade de transações", s.count.toString())
            }

            FFCard {
                Text("Receitas x Despesas", color = FF.TextPrimary, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(12.dp))
                IncomeExpenseChart(chart)
            }

            FFCard {
                Text("Despesas por categoria", color = FF.TextPrimary, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(12.dp))
                if (s.byCategory.isEmpty()) {
                    EmptyState("Sem despesas neste mês")
                } else {
                    DonutChart(s.byCategory)
                    Spacer(Modifier.height(12.dp))
                    val max = s.byCategory.first().second.coerceAtLeast(1)
                    s.byCategory.forEachIndexed { i, (name, value) ->
                        Column(Modifier.padding(vertical = 6.dp)) {
                            Row {
                                Text(name, color = FF.TextPrimary, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                Text(Money.format(value), color = FF.TextPrimary, style = MaterialTheme.typography.titleSmall)
                            }
                            Spacer(Modifier.height(4.dp))
                            ProgressBar(value.toFloat() / max, Color(FF.palette[i % FF.palette.size]))
                        }
                    }
                }
            }

            FFCard {
                Text("Relatório de parcelas", color = FF.TextPrimary, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                StatLine("Total parcelado", Money.format(progresses.sumOf { it.plan.totalAmount }))
                StatLine("Total já pago", Money.format(progresses.sumOf { it.paidAmount }), FF.EmeraldLight)
                StatLine("Total restante", Money.format(progresses.sumOf { it.remainingAmount }), FF.CrimsonLight)
                StatLine("Parcelas futuras", progresses.sumOf { it.remainingCount }.toString())
                StatLine("Contratos ativos", progresses.count { it.remainingCount > 0 }.toString())
            }
        }
    }
}

// ---------------- Calendário ----------------

@Composable
fun CalendarScreen(d: FinanceData, onBack: () -> Unit, onTx: (Transaction) -> Unit) {
    var offset by rememberSaveable { mutableStateOf(0) }
    var selected by rememberSaveable { mutableStateOf(Dates.todayEpoch()) }
    val month = YearMonth.now().plusMonths(offset.toLong())
    val byDay = remember(d, month) {
        val from = month.atDay(1).epochDay()
        val to = month.atEndOfMonth().epochDay()
        d.transactions.filter { it.date in from..to && it.status != TxStatus.CANCELLED }.groupBy { it.date }
    }
    val today = Dates.todayEpoch()

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Calendário financeiro", onBack = onBack)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FFCard(background = FF.Surface1, padding = 8.dp) {
                MonthSwitcher(month) { offset += it }
                Row(Modifier.fillMaxWidth()) {
                    listOf("D", "S", "T", "Q", "Q", "S", "S").forEach {
                        Text(it, color = FF.TextTertiary, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    }
                }
                Spacer(Modifier.height(4.dp))
                val first = month.atDay(1)
                val lead = first.dayOfWeek.isoDayNumber % 7 // domingo = 0
                val cells = lead + month.lengthOfMonth()
                val rows = (cells + 6) / 7
                for (r in 0 until rows) {
                    Row(Modifier.fillMaxWidth()) {
                        for (c in 0 until 7) {
                            val dayNum = r * 7 + c - lead + 1
                            Box(Modifier.weight(1f).aspectRatio(0.85f).padding(2.dp)) {
                                if (dayNum in 1..month.lengthOfMonth()) {
                                    val epoch = month.atDay(dayNum).epochDay()
                                    val items = byDay[epoch].orEmpty()
                                    val isSel = epoch == selected
                                    val hasLate = items.any { it.status == TxStatus.PENDING && it.date < today && it.type != TxType.TRANSFER }
                                    Column(
                                        Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))
                                            .background(if (isSel) FF.Emerald.copy(alpha = 0.18f) else if (items.isNotEmpty()) FF.Surface2 else Color.Transparent)
                                            .border(1.dp, if (isSel) FF.Emerald else if (epoch == today) FF.Sky else Color.Transparent, RoundedCornerShape(8.dp))
                                            .clickable { selected = epoch },
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                    ) {
                                        Text("$dayNum", color = if (isSel) FF.EmeraldLight else FF.TextPrimary, style = MaterialTheme.typography.labelLarge)
                                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                            if (items.any { it.type == TxType.INCOME }) Dot(FF.Emerald)
                                            if (items.any { it.type == TxType.EXPENSE }) Dot(if (hasLate) FF.Crimson else FF.Amber)
                                            if (items.any { it.type == TxType.TRANSFER }) Dot(FF.Sky)
                                        }
                                        if (items.size > 2) Text("${items.size}", color = FF.TextTertiary, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            val dayItems = d.transactions.filter { it.date == selected && it.status != TxStatus.CANCELLED }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(Dates.friendly(selected), color = FF.TextPrimary, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                val net = dayItems.filter { it.type == TxType.INCOME }.sumOf { it.amount } - dayItems.filter { it.type == TxType.EXPENSE }.sumOf { it.amount }
                if (dayItems.isNotEmpty()) Text(Money.signed(net, net >= 0), color = if (net >= 0) FF.EmeraldLight else FF.CrimsonLight, style = MaterialTheme.typography.titleSmall)
            }
            if (dayItems.isEmpty()) FFCard { EmptyState("Nenhum compromisso neste dia") } else TxList(dayItems, d, onTx)
        }
    }
}

@Composable
private fun Dot(color: Color) = Box(Modifier.size(5.dp).clip(CircleShape).background(color))

// ---------------- Previsão ----------------

@Composable
fun ForecastScreen(d: FinanceData, onBack: () -> Unit, onTx: (Transaction) -> Unit) {
    val options = listOf(7, 30, 60, 90)
    var selected by rememberSaveable { mutableStateOf(1) }
    var custom by rememberSaveable { mutableStateOf<Long?>(null) }
    val today = Dates.todayEpoch()
    val until = if (selected == 4) custom ?: (today + 30) else today + options[selected]
    val pending = d.pending.filter { it.date <= until }.sortedBy { it.date }
    val incomes = pending.filter { it.type == TxType.INCOME }
    val expenses = pending.filter { it.type == TxType.EXPENSE }
    val projected = d.projectedUntil(until)
    val points = remember(d, until) {
        val overdue = d.pending.filter { it.date < today }
        var bal = d.currentBalance + overdue.sumOf { if (it.type == TxType.INCOME) it.amount else -it.amount }
        val byDay = d.pending.filter { it.date in today..until }.groupBy { it.date }
        val step = ((until - today) / 60).coerceAtLeast(1)
        val list = mutableListOf(bal)
        var day = today
        while (day <= until) {
            byDay[day]?.forEach { bal += if (it.type == TxType.INCOME) it.amount else -it.amount }
            if ((day - today) % step == 0L || day == until) list += bal
            day++
        }
        list
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Previsão financeira", onBack = onBack)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ChipRow(listOf("7 dias", "30 dias", "60 dias", "90 dias", "Personalizado"), selected, { selected = it })
            if (selected == 4) DateField("Projetar até", custom, { custom = it })

            FFCard(accent = FF.Sky, background = FF.Surface1) {
                Row {
                    Column(Modifier.weight(1f)) {
                        Text("Hoje", color = FF.TextSecondary, style = MaterialTheme.typography.labelLarge)
                        Text(Money.format(d.currentBalance), color = FF.TextPrimary, style = MaterialTheme.typography.titleMedium)
                    }
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text("Projetado em ${Dates.full(until)}", color = FF.Sky, style = MaterialTheme.typography.labelLarge)
                        Text(Money.format(projected), color = if (projected >= 0) FF.Sky else FF.Crimson, style = MaterialTheme.typography.headlineMedium)
                    }
                }
                Spacer(Modifier.height(12.dp))
                BalanceLineChart(points)
                Spacer(Modifier.height(8.dp))
                Row {
                    Text("+ ${Money.format(incomes.sumOf { it.amount })} entradas", color = FF.EmeraldLight, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                    Text("- ${Money.format(expenses.sumOf { it.amount })} saídas", color = FF.CrimsonLight, style = MaterialTheme.typography.labelLarge)
                }
                if (pending.any { it.date < today }) {
                    Text("Inclui compromissos atrasados ainda pendentes.", color = FF.TextTertiary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp))
                }
            }

            Text("Próximas entradas", color = FF.TextPrimary, style = MaterialTheme.typography.headlineSmall)
            if (incomes.isEmpty()) FFCard { EmptyState("Nenhuma entrada prevista") } else TxList(incomes, d, onTx, showDate = true)
            Text("Próximas saídas", color = FF.TextPrimary, style = MaterialTheme.typography.headlineSmall)
            if (expenses.isEmpty()) FFCard { EmptyState("Nenhuma saída prevista") } else TxList(expenses, d, onTx, showDate = true)
            Spacer(Modifier.height(8.dp))
        }
    }
}
