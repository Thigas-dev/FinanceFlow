package com.financeflow.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.financeflow.app.data.Account
import com.financeflow.app.data.AccountType
import com.financeflow.app.data.Category
import com.financeflow.app.data.CreditCard
import com.financeflow.app.data.FinanceData
import com.financeflow.app.data.Transaction
import com.financeflow.app.data.TxType
import com.financeflow.app.ui.MainViewModel
import com.financeflow.app.ui.components.ColorPicker
import com.financeflow.app.ui.components.ConfirmDialog
import com.financeflow.app.ui.components.EmptyState
import com.financeflow.app.ui.components.FFCard
import com.financeflow.app.ui.components.FFTextField
import com.financeflow.app.ui.components.IconBadge
import com.financeflow.app.ui.components.InvoicePayDialog
import com.financeflow.app.ui.components.MoneyField
import com.financeflow.app.ui.components.Pill
import com.financeflow.app.ui.components.PrimaryButton
import com.financeflow.app.ui.components.ProgressBar
import com.financeflow.app.ui.components.ScreenHeader
import com.financeflow.app.ui.components.SecondaryButton
import com.financeflow.app.ui.components.Segmented
import com.financeflow.app.ui.components.SelectField
import com.financeflow.app.ui.components.SmallActionButton
import com.financeflow.app.ui.components.ToggleRow
import com.financeflow.app.ui.components.TxList
import com.financeflow.app.ui.components.accountIcon
import com.financeflow.app.ui.components.categoryIcon
import com.financeflow.app.ui.components.categoryIconKeys
import com.financeflow.app.ui.theme.FF
import com.financeflow.app.util.Dates
import com.financeflow.app.util.Money

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormSheet(title: String, onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = state, containerColor = FF.Surface1) {
        Column(
            Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp).padding(bottom = 24.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(title, style = MaterialTheme.typography.headlineSmall, color = FF.TextPrimary)
            content()
        }
    }
}

// ---------------- Contas ----------------

@Composable
fun AccountsScreen(vm: MainViewModel, d: FinanceData, onBack: () -> Unit, onTransfer: () -> Unit) {
    var editing by remember { mutableStateOf<Account?>(null) }
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Contas", subtitle = "Bancos, carteiras e dinheiro", onBack = onBack)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FFCard(accent = FF.Emerald, background = FF.Surface1) {
                Text("Saldo total em contas ativas", color = FF.TextSecondary, style = MaterialTheme.typography.labelLarge)
                Text(Money.format(d.currentBalance), color = FF.TextPrimary, style = MaterialTheme.typography.displaySmall)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PrimaryButton("Nova conta", { editing = Account(userId = 0, name = "", type = AccountType.CHECKING) }, Modifier.weight(1f), icon = Icons.Outlined.Add)
                    SecondaryButton("Transferir", onTransfer, Modifier.weight(1f), icon = Icons.Outlined.SwapHoriz)
                }
            }
            if (d.accounts.isEmpty()) EmptyState("Nenhuma conta cadastrada")
            d.accounts.forEach { a ->
                FFCard(Modifier.alpha(if (a.active) 1f else 0.5f), onClick = { editing = a }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBadge(accountIcon(a.type), Color(a.color), bg = Color(a.color).copy(alpha = 0.15f))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(a.name, color = FF.TextPrimary, style = MaterialTheme.typography.titleMedium)
                            Text(
                                listOf(a.type.label, a.institution).filter { it.isNotBlank() }.joinToString(" • ") + if (!a.active) " • inativa" else "",
                                color = FF.TextSecondary, style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        val bal = d.balances[a.id] ?: 0
                        Text(Money.format(bal), color = if (bal < 0) FF.Crimson else FF.TextPrimary, style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
        }
    }
    editing?.let { a -> AccountForm(a, vm) { editing = null } }
}

@Composable
private fun AccountForm(initial: Account, vm: MainViewModel, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(initial.name) }
    var type by remember { mutableStateOf(initial.type) }
    var institution by remember { mutableStateOf(initial.institution) }
    var balance by remember { mutableStateOf(initial.initialBalance) }
    var negative by remember { mutableStateOf(initial.initialBalance < 0) }
    var color by remember { mutableStateOf(initial.color) }
    var active by remember { mutableStateOf(initial.active) }
    var confirmDelete by remember { mutableStateOf(false) }
    FormSheet(if (initial.id == 0L) "Nova conta" else "Editar conta", onDismiss) {
        FFTextField(name, { name = it }, "Nome (ex.: Nubank)")
        SelectField("Tipo", type, AccountType.entries, { it.label }, { type = it })
        FFTextField(institution, { institution = it }, "Instituição (opcional)")
        MoneyField(kotlin.math.abs(balance), { balance = it }, "Saldo inicial")
        ToggleRow("Saldo inicial negativo", null, negative, { negative = it })
        Text("Cor / identificação", color = FF.TextSecondary, style = MaterialTheme.typography.labelMedium)
        ColorPicker(color) { color = it }
        ToggleRow("Conta ativa", "Contas inativas não entram no saldo atual", active, { active = it })
        PrimaryButton("Salvar", {
            if (name.isNotBlank()) {
                val value = kotlin.math.abs(balance) * if (negative) -1 else 1
                vm.saveAccount(initial.copy(name = name.trim(), type = type, institution = institution.trim(), initialBalance = value, color = color, active = active))
                onDismiss()
            }
        }, Modifier.fillMaxWidth(), enabled = name.isNotBlank())
        if (initial.id != 0L) SecondaryButton("Excluir conta", { confirmDelete = true }, Modifier.fillMaxWidth(), color = FF.Crimson, icon = Icons.Outlined.Delete)
    }
    if (confirmDelete) {
        ConfirmDialog(
            "Excluir conta", "Se a conta tiver movimentações efetivadas ela será apenas inativada para preservar o histórico.",
            confirm = "Excluir", destructive = true, onConfirm = { vm.deleteAccount(initial); onDismiss() }, onDismiss = { confirmDelete = false },
        )
    }
}

// ---------------- Cartões ----------------

@Composable
fun CardsScreen(vm: MainViewModel, d: FinanceData, onBack: () -> Unit, onTx: (Transaction) -> Unit) {
    var editing by remember { mutableStateOf<CreditCard?>(null) }
    var paying by remember { mutableStateOf<Pair<CreditCard, List<Transaction>>?>(null) }
    var expanded by rememberSaveable { mutableStateOf<Long?>(null) }
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Cartões", subtitle = "Limites e faturas", onBack = onBack) {
            IconButton(onClick = { editing = CreditCard(userId = 0, name = "", limit = 0, closingDay = 1, dueDay = 10, accountId = d.activeAccounts.firstOrNull()?.id) }) {
                Icon(Icons.Outlined.Add, "Novo cartão", tint = FF.EmeraldLight)
            }
        }
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (d.activeCards.isEmpty()) {
                FFCard {
                    EmptyState("Nenhum cartão cadastrado")
                    PrimaryButton("Cadastrar cartão", {
                        editing = CreditCard(userId = 0, name = "", limit = 0, closingDay = 1, dueDay = 10, accountId = d.activeAccounts.firstOrNull()?.id)
                    }, Modifier.fillMaxWidth(), icon = Icons.Outlined.Add)
                }
            }
            d.activeCards.forEach { c ->
                val used = d.cardUsed(c.id)
                val available = c.limit - used
                val invoices = d.cardInvoices(c.id)
                val open = invoices.filter { !it.isPaid }
                FFCard(accent = Color(c.color)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBadge(categoryIcon("card"), Color(c.color), bg = Color(c.color).copy(alpha = 0.15f))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(c.name, color = FF.TextPrimary, style = MaterialTheme.typography.titleMedium)
                            Text(
                                listOf(c.bank, "Fecha dia ${c.closingDay}", "Vence dia ${c.dueDay}").filter { it.isNotBlank() }.joinToString(" • "),
                                color = FF.TextSecondary, style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        IconButton(onClick = { editing = c }) { Icon(Icons.Outlined.Edit, "Editar", tint = FF.TextSecondary) }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row {
                        Column(Modifier.weight(1f)) {
                            Text("Limite", color = FF.TextSecondary, style = MaterialTheme.typography.labelMedium)
                            Text(Money.format(c.limit), color = FF.TextPrimary, style = MaterialTheme.typography.titleSmall)
                        }
                        Column(Modifier.weight(1f)) {
                            Text("Utilizado", color = FF.TextSecondary, style = MaterialTheme.typography.labelMedium)
                            Text(Money.format(used), color = FF.CrimsonLight, style = MaterialTheme.typography.titleSmall)
                        }
                        Column(Modifier.weight(1f)) {
                            Text("Disponível", color = FF.TextSecondary, style = MaterialTheme.typography.labelMedium)
                            Text(Money.format(available), color = if (available < 0) FF.Crimson else FF.EmeraldLight, style = MaterialTheme.typography.titleSmall)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    ProgressBar(if (c.limit > 0) used.toFloat() / c.limit else 0f, if (available < 0) FF.Crimson else Color(c.color))
                    Spacer(Modifier.height(12.dp))
                    Text("Próximas faturas", color = FF.TextSecondary, style = MaterialTheme.typography.labelLarge)
                    if (open.isEmpty()) Text("Nenhuma fatura em aberto", color = FF.TextTertiary, style = MaterialTheme.typography.bodySmall)
                    open.take(4).forEach { inv ->
                        val late = inv.dueDate < Dates.todayEpoch()
                        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "${Dates.monthName(com.financeflow.app.util.YearMonth.from(Dates.of(inv.dueDate)))} • vence ${Dates.short(inv.dueDate)}",
                                    color = if (late) FF.Crimson else FF.TextPrimary, style = MaterialTheme.typography.bodyMedium,
                                )
                                Text("${inv.items.size} lançamento(s)", color = FF.TextTertiary, style = MaterialTheme.typography.bodySmall)
                            }
                            Text(Money.format(inv.pendingTotal), color = FF.TextPrimary, style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.width(8.dp))
                            SmallActionButton("Pagar", { paying = c to inv.items.filter { it.status == com.financeflow.app.data.TxStatus.PENDING } })
                        }
                    }
                    Text(
                        if (expanded == c.id) "Ocultar lançamentos" else "Ver lançamentos",
                        color = FF.Sky, style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.clickable { expanded = if (expanded == c.id) null else c.id }.padding(vertical = 8.dp),
                    )
                    if (expanded == c.id) {
                        val items = invoices.flatMap { it.items }.sortedByDescending { it.date }
                        if (items.isEmpty()) Text("Sem lançamentos", color = FF.TextTertiary) else TxList(items.take(60), d, onTx, showDate = true)
                    }
                }
            }
        }
    }
    editing?.let { c -> CardForm(c, d, vm) { editing = null } }
    paying?.let { (c, items) -> InvoicePayDialog(items, c.accountId, c.name, d, vm) { paying = null } }
}

@Composable
private fun CardForm(initial: CreditCard, d: FinanceData, vm: MainViewModel, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(initial.name) }
    var bank by remember { mutableStateOf(initial.bank) }
    var limit by remember { mutableStateOf(initial.limit) }
    var closing by remember { mutableStateOf(initial.closingDay) }
    var due by remember { mutableStateOf(initial.dueDay) }
    var account by remember { mutableStateOf(initial.accountId?.let { d.accountMap[it] }) }
    var color by remember { mutableStateOf(initial.color) }
    var confirmDelete by remember { mutableStateOf(false) }
    FormSheet(if (initial.id == 0L) "Novo cartão" else "Editar cartão", onDismiss) {
        FFTextField(name, { name = it }, "Nome do cartão")
        FFTextField(bank, { bank = it }, "Banco")
        MoneyField(limit, { limit = it }, "Limite")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SelectField("Dia de fechamento", closing, (1..31).toList(), { "Dia $it" }, { closing = it }, Modifier.weight(1f))
            SelectField("Dia de vencimento", due, (1..31).toList(), { "Dia $it" }, { due = it }, Modifier.weight(1f))
        }
        SelectField("Conta para pagamento", account, d.activeAccounts, { it.name }, { account = it })
        Text("Cor", color = FF.TextSecondary, style = MaterialTheme.typography.labelMedium)
        ColorPicker(color) { color = it }
        PrimaryButton("Salvar", {
            vm.saveCard(initial.copy(name = name.trim(), bank = bank.trim(), limit = limit, closingDay = closing, dueDay = due, accountId = account?.id, color = color))
            onDismiss()
        }, Modifier.fillMaxWidth(), enabled = name.isNotBlank() && limit > 0)
        if (initial.id != 0L) SecondaryButton("Remover cartão", { confirmDelete = true }, Modifier.fillMaxWidth(), color = FF.Crimson, icon = Icons.Outlined.Delete)
    }
    if (confirmDelete) {
        ConfirmDialog(
            "Remover cartão", "O cartão será inativado; o histórico de lançamentos é mantido.",
            confirm = "Remover", destructive = true, onConfirm = { vm.deleteCard(initial); onDismiss() }, onDismiss = { confirmDelete = false },
        )
    }
}

// ---------------- Categorias ----------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoriesScreen(vm: MainViewModel, d: FinanceData, onBack: () -> Unit) {
    var tab by rememberSaveable { mutableStateOf(0) }
    var editing by remember { mutableStateOf<Category?>(null) }
    var deleting by remember { mutableStateOf<Category?>(null) }
    val type = if (tab == 0) TxType.EXPENSE else TxType.INCOME
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Categorias", onBack = onBack) {
            IconButton(onClick = { editing = Category(userId = 0, name = "", type = type) }) { Icon(Icons.Outlined.Add, "Nova", tint = FF.EmeraldLight) }
        }
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Segmented(listOf("Despesas", "Receitas"), tab, { tab = it }, colors = listOf(FF.CrimsonLight, FF.EmeraldLight))
            FFCard(padding = 0.dp) {
                val list = d.categories.filter { it.type == type }
                if (list.isEmpty()) EmptyState("Nenhuma categoria")
                list.forEachIndexed { i, c ->
                    if (i > 0) HorizontalDivider(color = FF.Border.copy(alpha = 0.5f))
                    Row(Modifier.fillMaxWidth().clickable { editing = c }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconBadge(categoryIcon(c.icon), if (type == TxType.INCOME) FF.Emerald else FF.Sky)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(c.name, color = FF.TextPrimary, style = MaterialTheme.typography.titleSmall)
                            val count = d.transactions.count { it.categoryId == c.id }
                            Text("${if (c.isDefault) "Padrão" else "Personalizada"} • $count lançamento(s)", color = FF.TextSecondary, style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { deleting = c }) { Icon(Icons.Outlined.Delete, "Excluir", tint = FF.TextTertiary) }
                    }
                }
            }
        }
    }
    editing?.let { c ->
        var name by remember(c) { mutableStateOf(c.name) }
        var icon by remember(c) { mutableStateOf(c.icon) }
        FormSheet(if (c.id == 0L) "Nova categoria" else "Editar categoria", { editing = null }) {
            FFTextField(name, { name = it }, "Nome")
            Text("Ícone", color = FF.TextSecondary, style = MaterialTheme.typography.labelMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                categoryIconKeys.forEach { key ->
                    Box(
                        Modifier.size(44.dp).clip(RoundedCornerShape(10.dp))
                            .background(if (key == icon) FF.Emerald.copy(alpha = 0.2f) else FF.Surface2)
                            .border(1.dp, if (key == icon) FF.Emerald else FF.Border, RoundedCornerShape(10.dp))
                            .clickable { icon = key },
                        contentAlignment = Alignment.Center,
                    ) { Icon(categoryIcon(key), key, tint = if (key == icon) FF.Emerald else FF.TextSecondary) }
                }
            }
            PrimaryButton("Salvar", {
                vm.saveCategory(c.copy(name = name.trim(), icon = icon, type = if (c.id == 0L) type else c.type))
                editing = null
            }, Modifier.fillMaxWidth(), enabled = name.isNotBlank())
        }
    }
    deleting?.let { c ->
        ConfirmDialog(
            "Excluir categoria", "As movimentações de \"${c.name}\" ficarão sem categoria.",
            confirm = "Excluir", destructive = true, onConfirm = { vm.deleteCategory(c) }, onDismiss = { deleting = null },
        )
    }
}

// ---------------- Recorrências ----------------

@Composable
fun RecurrencesScreen(vm: MainViewModel, d: FinanceData, onBack: () -> Unit, onNew: () -> Unit) {
    var deleting by remember { mutableStateOf<com.financeflow.app.data.Recurrence?>(null) }
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Contas recorrentes", subtitle = "Aluguel, assinaturas, salário...", onBack = onBack) {
            IconButton(onClick = onNew) { Icon(Icons.Outlined.Add, "Nova", tint = FF.EmeraldLight) }
        }
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val monthlyIn = d.recurrences.filter { it.active && it.type == TxType.INCOME }.sumOf { monthlyEquivalent(it) }
            val monthlyOut = d.recurrences.filter { it.active && it.type == TxType.EXPENSE }.sumOf { monthlyEquivalent(it) }
            FFCard(accent = FF.Sky, background = FF.Surface1) {
                Text("Impacto mensal estimado", color = FF.TextSecondary, style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(6.dp))
                Row {
                    Column(Modifier.weight(1f)) {
                        Text("Entradas", color = FF.TextSecondary, style = MaterialTheme.typography.labelMedium)
                        Text("+ " + Money.format(monthlyIn), color = FF.EmeraldLight, style = MaterialTheme.typography.titleSmall)
                    }
                    Column(Modifier.weight(1f)) {
                        Text("Saídas", color = FF.TextSecondary, style = MaterialTheme.typography.labelMedium)
                        Text("- " + Money.format(monthlyOut), color = FF.CrimsonLight, style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
            if (d.recurrences.isEmpty()) {
                FFCard {
                    EmptyState("Nenhuma recorrência.\nUse + Novo → Recorrente.")
                    PrimaryButton("Nova recorrência", onNew, Modifier.fillMaxWidth(), icon = Icons.Outlined.Add)
                }
            }
            d.recurrences.forEach { r ->
                val next = d.transactions.filter { it.recurrenceId == r.id && it.status == com.financeflow.app.data.TxStatus.PENDING }.minByOrNull { it.date }
                FFCard(Modifier.alpha(if (r.active) 1f else 0.55f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val cat = r.categoryId?.let { d.categoryMap[it] }
                        IconBadge(categoryIcon(cat?.icon ?: "more"), if (r.type == TxType.INCOME) FF.Emerald else FF.Sky)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(r.description, color = FF.TextPrimary, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                "${r.frequency.label} • desde ${Dates.full(r.startDate)}" + (r.endDate?.let { " até ${Dates.full(it)}" } ?: ""),
                                color = FF.TextSecondary, style = MaterialTheme.typography.bodySmall,
                            )
                            next?.let { Text("Próximo: ${Dates.full(it.date)}", color = FF.Sky, style = MaterialTheme.typography.bodySmall) }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                (if (r.type == TxType.INCOME) "+ " else "- ") + Money.format(r.amount),
                                color = if (r.type == TxType.INCOME) FF.EmeraldLight else FF.TextPrimary, style = MaterialTheme.typography.titleSmall,
                            )
                            Switch(
                                checked = r.active, onCheckedChange = { vm.setRecurrenceActive(r, it) },
                                colors = SwitchDefaults.colors(checkedTrackColor = FF.Emerald, checkedThumbColor = FF.TextPrimary, uncheckedTrackColor = FF.Surface1),
                            )
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Pill(if (r.active) "Ativa" else "Inativa", if (r.active) FF.Emerald else FF.TextTertiary)
                        Spacer(Modifier.weight(1f))
                        Text("Excluir", color = FF.Crimson, style = MaterialTheme.typography.labelLarge, modifier = Modifier.clickable { deleting = r }.padding(4.dp))
                    }
                }
            }
        }
    }
    deleting?.let { r ->
        ConfirmDialog(
            "Excluir recorrência", "Os lançamentos futuros pendentes serão removidos. O histórico já efetivado é mantido.",
            confirm = "Excluir", destructive = true, onConfirm = { vm.deleteRecurrence(r) }, onDismiss = { deleting = null },
        )
    }
}

private fun monthlyEquivalent(r: com.financeflow.app.data.Recurrence): Long = when (r.frequency) {
    com.financeflow.app.data.Frequency.WEEKLY -> r.amount * 52 / 12
    com.financeflow.app.data.Frequency.BIWEEKLY -> r.amount * 26 / 12
    com.financeflow.app.data.Frequency.MONTHLY -> r.amount
    com.financeflow.app.data.Frequency.YEARLY -> r.amount / 12
}
