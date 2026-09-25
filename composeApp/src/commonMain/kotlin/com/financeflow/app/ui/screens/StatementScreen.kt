package com.financeflow.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.financeflow.app.data.DisplayStatus
import com.financeflow.app.data.FinanceData
import com.financeflow.app.data.FinanceRules
import com.financeflow.app.data.PaymentMethod
import com.financeflow.app.data.Transaction
import com.financeflow.app.data.TxStatus
import com.financeflow.app.data.TxType
import com.financeflow.app.ui.MainViewModel
import com.financeflow.app.ui.components.ChipRow
import com.financeflow.app.ui.components.DateField
import com.financeflow.app.ui.components.EmptyState
import com.financeflow.app.ui.components.FFCard
import com.financeflow.app.ui.components.FFTextField
import com.financeflow.app.ui.components.MoneyField
import com.financeflow.app.ui.components.PrimaryButton
import com.financeflow.app.ui.components.ScreenHeader
import com.financeflow.app.ui.components.SecondaryButton
import com.financeflow.app.ui.components.SelectField
import com.financeflow.app.ui.components.TxList
import com.financeflow.app.ui.components.ToggleRow
import com.financeflow.app.ui.theme.FF
import com.financeflow.app.util.Dates
import com.financeflow.app.util.Money
import com.financeflow.app.util.YearMonth
import com.financeflow.app.util.epochDay

data class AdvancedFilter(
    val categoryId: Long? = null,
    val accountId: Long? = null,
    val cardId: Long? = null,
    val method: PaymentMethod? = null,
    val status: DisplayStatus? = null,
    val person: String = "",
    val minAmount: Long = 0,
    val maxAmount: Long = 0,
    val from: Long? = null,
    val to: Long? = null,
) {
    val active: Boolean
        get() = categoryId != null || accountId != null || cardId != null || method != null || status != null ||
            person.isNotBlank() || minAmount > 0 || maxAmount > 0 || from != null || to != null

    fun matches(t: Transaction): Boolean {
        if (categoryId != null && t.categoryId != categoryId) return false
        if (accountId != null && t.accountId != accountId && t.toAccountId != accountId) return false
        if (cardId != null && t.cardId != cardId) return false
        if (method != null && t.paymentMethod != method) return false
        if (status != null && FinanceRules.displayStatus(t) != status) return false
        if (person.isNotBlank() && t.person?.contains(person.trim(), ignoreCase = true) != true) return false
        if (minAmount > 0 && t.amount < minAmount) return false
        if (maxAmount > 0 && t.amount > maxAmount) return false
        if (from != null && t.date < from) return false
        if (to != null && t.date > to) return false
        return true
    }
}

private val quickFilters = listOf("all" to "Todas", "income" to "Receitas", "expense" to "Despesas", "pending" to "Pendentes", "late" to "Atrasadas", "transfer" to "Transferências")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatementScreen(vm: MainViewModel, d: FinanceData, initialFilter: String, onTx: (Transaction) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var quick by rememberSaveable { mutableStateOf(quickFilters.indexOfFirst { it.first == initialFilter }.coerceAtLeast(0)) }
    var monthOffset by rememberSaveable { mutableStateOf(0) }
    var allPeriods by rememberSaveable { mutableStateOf(false) }
    var advanced by remember { mutableStateOf(AdvancedFilter()) }
    var showFilters by remember { mutableStateOf(false) }
    val month = YearMonth.now().plusMonths(monthOffset.toLong())
    val today = Dates.todayEpoch()

    val filtered = remember(d, query, quick, month, allPeriods, advanced) {
        val from = month.atDay(1).epochDay()
        val to = month.atEndOfMonth().epochDay()
        d.transactions.filter { t ->
            val periodOk = allPeriods || advanced.from != null || advanced.to != null || t.date in from..to
            val quickOk = when (quickFilters[quick].first) {
                "income" -> t.type == TxType.INCOME
                "expense" -> t.type == TxType.EXPENSE
                "pending" -> t.status == TxStatus.PENDING && t.type != TxType.TRANSFER
                "late" -> FinanceRules.isLate(t, today) && t.type != TxType.TRANSFER
                "transfer" -> t.type == TxType.TRANSFER
                else -> true
            }
            val q = query.trim()
            val queryOk = q.isEmpty() || t.description.contains(q, true) || t.person?.contains(q, true) == true ||
                t.notes?.contains(q, true) == true || Money.format(t.amount).contains(q) ||
                (t.categoryId?.let { d.categoryMap[it]?.name?.contains(q, true) } == true)
            periodOk && quickOk && queryOk && advanced.matches(t)
        }.sortedWith(compareByDescending<Transaction> { it.date }.thenByDescending { it.id })
    }
    val incomes = filtered.filter { it.type == TxType.INCOME && it.status != TxStatus.CANCELLED }.sumOf { it.amount }
    val expenses = filtered.filter { it.type == TxType.EXPENSE && it.status != TxStatus.CANCELLED }.sumOf { it.amount }
    val groups = filtered.groupBy { it.date }.toList()

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Extrato & Histórico", subtitle = "FinanceFlow")
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                FFTextField(
                    query, { query = it }, "Buscar descrição, valor ou pessoa",
                    leadingIcon = Icons.Outlined.Search,
                    trailing = {
                        IconButton(onClick = { showFilters = true }) {
                            Icon(Icons.Outlined.Tune, "Filtros", tint = if (advanced.active) FF.Emerald else FF.TextSecondary)
                        }
                    },
                )
            }
            item { ChipRow(quickFilters.map { it.second }, quick, { quick = it }) }
            item {
                FFCard(accent = FF.Emerald, background = FF.Surface1, padding = 12.dp) {
                    if (advanced.from != null || advanced.to != null) {
                        Text(
                            "Período: ${advanced.from?.let { Dates.full(it) } ?: "início"} a ${advanced.to?.let { Dates.full(it) } ?: "hoje"}",
                            color = FF.TextPrimary, style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { monthOffset--; allPeriods = false }) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Mês anterior", tint = FF.TextSecondary)
                            }
                            Text(
                                if (allPeriods) "Todos os períodos" else Dates.monthYear(month),
                                color = FF.TextPrimary, style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            )
                            IconButton(onClick = { monthOffset++; allPeriods = false }) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Próximo mês", tint = FF.TextSecondary)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MonthCell("Receitas", "+ " + Money.format(incomes), FF.EmeraldLight, Modifier.weight(1f))
                        MonthCell("Despesas", "- " + Money.format(expenses), FF.CrimsonLight, Modifier.weight(1f))
                        val bal = incomes - expenses
                        MonthCell("Saldo", Money.signed(bal, bal >= 0), if (bal >= 0) FF.EmeraldLight else FF.Crimson, Modifier.weight(1f))
                    }
                }
            }
            if (groups.isEmpty()) item { EmptyState("Nenhuma movimentação encontrada") }
            items(groups, key = { it.first }) { (date, list) ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row {
                        val diff = date - today
                        val label = when (diff) {
                            0L -> "HOJE - "
                            -1L -> "ONTEM - "
                            1L -> "AMANHÃ - "
                            else -> ""
                        } + Dates.dayMonthUpper(date)
                        Text(label, color = FF.TextSecondary, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                        Text("${list.size} transaç${if (list.size == 1) "ão" else "ões"}", color = FF.TextTertiary, style = MaterialTheme.typography.labelMedium)
                    }
                    TxList(list, d, onTx)
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }

    if (showFilters) {
        val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var f by remember { mutableStateOf(advanced) }
        ModalBottomSheet(onDismissRequest = { showFilters = false }, sheetState = state, containerColor = FF.Surface1) {
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 24.dp).navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Filtros", style = MaterialTheme.typography.headlineSmall, color = FF.TextPrimary, modifier = Modifier.weight(1f))
                    IconButton(onClick = { showFilters = false }) { Icon(Icons.Outlined.Close, "Fechar", tint = FF.TextSecondary) }
                }
                ToggleRow("Todos os períodos", "Ignora o mês selecionado", allPeriods, { allPeriods = it })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DateField("De", f.from, { f = f.copy(from = it) }, Modifier.weight(1f), placeholder = "Início")
                    DateField("Até", f.to, { f = f.copy(to = it) }, Modifier.weight(1f), placeholder = "Fim")
                }
                val cats = listOf<Pair<Long?, String>>(null to "Todas") + d.categories.map { it.id to "${it.name} (${if (it.type == TxType.INCOME) "receita" else "despesa"})" }
                SelectField("Categoria", cats.first { it.first == f.categoryId }, cats, { it.second }, { f = f.copy(categoryId = it.first) })
                val accs = listOf<Pair<Long?, String>>(null to "Todas") + d.accounts.map { it.id to it.name }
                SelectField("Conta", accs.firstOrNull { it.first == f.accountId } ?: accs[0], accs, { it.second }, { f = f.copy(accountId = it.first) })
                val cards = listOf<Pair<Long?, String>>(null to "Todos") + d.cards.map { it.id to it.name }
                SelectField("Cartão", cards.firstOrNull { it.first == f.cardId } ?: cards[0], cards, { it.second }, { f = f.copy(cardId = it.first) })
                val methods = listOf<Pair<PaymentMethod?, String>>(null to "Todas") + PaymentMethod.entries.map { it to it.label }
                SelectField("Forma de pagamento", methods.first { it.first == f.method }, methods, { it.second }, { f = f.copy(method = it.first) })
                val statuses = listOf<Pair<DisplayStatus?, String>>(null to "Todos") + DisplayStatus.entries.map { it to it.label }
                SelectField("Status", statuses.first { it.first == f.status }, statuses, { it.second }, { f = f.copy(status = it.first) })
                FFTextField(f.person, { f = f.copy(person = it) }, "Pessoa")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MoneyField(f.minAmount, { f = f.copy(minAmount = it) }, "Valor mínimo", Modifier.weight(1f))
                    MoneyField(f.maxAmount, { f = f.copy(maxAmount = it) }, "Valor máximo", Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SecondaryButton("Limpar", { advanced = AdvancedFilter(); showFilters = false }, Modifier.weight(1f))
                    PrimaryButton("Aplicar", { advanced = f; showFilters = false }, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MonthCell(label: String, value: String, color: Color, modifier: Modifier) {
    FFCard(modifier, padding = 10.dp) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(label, color = FF.TextSecondary, style = MaterialTheme.typography.labelMedium)
                Text(value, color = color, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
