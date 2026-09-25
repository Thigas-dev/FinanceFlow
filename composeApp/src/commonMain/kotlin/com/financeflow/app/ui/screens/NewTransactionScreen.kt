package com.financeflow.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.financeflow.app.data.FinanceData
import com.financeflow.app.data.FinanceRules
import com.financeflow.app.data.Frequency
import com.financeflow.app.data.NewEntry
import com.financeflow.app.data.PaymentMethod
import com.financeflow.app.data.TxType
import com.financeflow.app.ui.MainViewModel
import com.financeflow.app.ui.components.BigMoneyInput
import com.financeflow.app.ui.components.DateField
import com.financeflow.app.ui.components.FFCard
import com.financeflow.app.ui.components.FFChip
import com.financeflow.app.ui.components.FFTextField
import com.financeflow.app.ui.components.Pill
import com.financeflow.app.ui.components.PrimaryButton
import com.financeflow.app.ui.components.Segmented
import com.financeflow.app.ui.components.SelectField
import com.financeflow.app.ui.components.ToggleRow
import com.financeflow.app.ui.components.categoryIcon
import com.financeflow.app.ui.theme.FF
import com.financeflow.app.util.Dates
import com.financeflow.app.util.Money
import com.financeflow.app.util.epochDay

private data class Source(val accountId: Long?, val cardId: Long?, val label: String)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewTransactionScreen(vm: MainViewModel, d: FinanceData, initialType: String, editId: Long?, onClose: () -> Unit) {
    val editing = editId?.let { id -> d.transactions.firstOrNull { it.id == id } }

    var type by rememberSaveable { mutableStateOf(when (initialType) { "INCOME" -> 1; "TRANSFER" -> 2; else -> 0 }) }
    var mode by rememberSaveable { mutableStateOf(0) }
    var amount by rememberSaveable { mutableStateOf(0L) }
    var count by rememberSaveable { mutableStateOf(2) }
    var frequency by rememberSaveable { mutableStateOf(Frequency.MONTHLY) }
    var endDate by rememberSaveable { mutableStateOf<Long?>(null) }
    var description by rememberSaveable { mutableStateOf("") }
    var categoryId by rememberSaveable { mutableStateOf<Long?>(null) }
    var date by rememberSaveable { mutableStateOf(Dates.todayEpoch()) }
    var accountId by rememberSaveable { mutableStateOf<Long?>(null) }
    var cardId by rememberSaveable { mutableStateOf<Long?>(null) }
    var toAccountId by rememberSaveable { mutableStateOf<Long?>(null) }
    var method by rememberSaveable { mutableStateOf<PaymentMethod?>(PaymentMethod.PIX) }
    var person by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var done by rememberSaveable { mutableStateOf(true) }
    var showAllCategories by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var prefilled by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(editing, d.loaded) {
        if (editing != null && !prefilled) {
            type = if (editing.type == TxType.INCOME) 1 else 0
            amount = editing.amount
            description = editing.description
            categoryId = editing.categoryId
            date = editing.date
            accountId = editing.accountId
            cardId = editing.cardId
            method = editing.paymentMethod
            person = editing.person.orEmpty()
            notes = editing.notes.orEmpty()
            prefilled = true
        }
        if (editing == null && accountId == null && cardId == null) accountId = d.activeAccounts.firstOrNull()?.id
    }

    val txType = when (type) { 1 -> TxType.INCOME; 2 -> TxType.TRANSFER; else -> TxType.EXPENSE }
    val color = when (txType) { TxType.INCOME -> FF.EmeraldLight; TxType.TRANSFER -> FF.Sky; else -> FF.CrimsonLight }
    val categories = d.categories.filter { it.type == txType }
    val sources = d.activeAccounts.map { Source(it.id, null, "${it.name} (${Money.format(d.balances[it.id] ?: 0)})") } +
        if (txType == TxType.EXPENSE) d.activeCards.map {
            Source(null, it.id, "Cartão ${it.name} (Limite disp. ${Money.format(it.limit - d.cardUsed(it.id))})")
        } else emptyList()
    val source = sources.firstOrNull { (cardId != null && it.cardId == cardId) || (cardId == null && it.accountId == accountId && it.accountId != null) }
    val onCard = cardId != null && txType == TxType.EXPENSE

    fun save() {
        error = null
        if (amount <= 0) { error = "Informe um valor"; return }
        if (txType == TxType.TRANSFER) {
            val from = accountId
            val to = toAccountId
            if (from == null || to == null) { error = "Selecione as contas de origem e destino"; return }
            if (from == to) { error = "Origem e destino devem ser diferentes"; return }
            vm.addTransfer(from, to, amount, date, notes.ifBlank { null })
            onClose()
            return
        }
        if (source == null) { error = "Selecione a conta ou cartão"; return }
        val desc = description.trim().ifEmpty { categories.firstOrNull { it.id == categoryId }?.name ?: "" }
        if (desc.isEmpty()) { error = "Informe uma descrição"; return }
        if (editing != null) {
            vm.updateTx(
                editing.copy(
                    type = txType, description = desc, amount = amount, date = date, categoryId = categoryId,
                    accountId = source.accountId ?: editing.accountId.takeIf { onCard }, cardId = source.cardId,
                    paymentMethod = method, person = person.ifBlank { null }, notes = notes.ifBlank { null },
                ),
            )
            onClose()
            return
        }
        val entry = vm.newEntry { uid ->
            NewEntry(
                userId = uid, type = txType, description = desc, amount = amount, date = date,
                categoryId = categoryId, accountId = source.accountId, cardId = source.cardId,
                paymentMethod = if (onCard) PaymentMethod.CREDIT else method,
                person = person.ifBlank { null }, notes = notes.ifBlank { null }, done = done,
            )
        }
        when (mode) {
            1 -> vm.addInstallments(entry, count)
            2 -> vm.addRecurrence(entry, frequency, endDate)
            else -> vm.addSingle(entry)
        }
        onClose()
    }

    Column(Modifier.fillMaxSize().imePadding()) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) { Icon(Icons.Outlined.Close, "Fechar", tint = FF.TextPrimary) }
            Text(
                if (editing != null) "Editar Movimentação" else "Nova Movimentação",
                style = MaterialTheme.typography.titleLarge, color = FF.TextPrimary,
                textAlign = TextAlign.Center, modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.padding(24.dp))
        }
        HorizontalDivider(color = FF.Border)

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (editing == null) {
                Segmented(
                    listOf("Despesa", "Receita", "Transferência"), type,
                    { type = it; categoryId = null; if (it != 0) cardId = null },
                    colors = listOf(FF.CrimsonLight, FF.EmeraldLight, FF.Sky),
                )
            }

            FFCard(background = FF.Surface1) {
                Text(
                    "VALOR ${if (mode == 1) "TOTAL" else "DA TRANSAÇÃO"}", color = FF.TextSecondary,
                    style = MaterialTheme.typography.labelLarge, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(4.dp))
                BigMoneyInput(amount, { amount = it }, color)
                Text(
                    "Toque para alterar via teclado numérico", color = FF.TextTertiary, style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center,
                )
            }

            if (txType == TxType.TRANSFER) {
                val accs = d.activeAccounts
                SelectField("Conta de origem", accs.firstOrNull { it.id == accountId }, accs, { "${it.name} (${Money.format(d.balances[it.id] ?: 0)})" }, { accountId = it.id }, leadingIcon = Icons.Outlined.AccountBalanceWallet)
                SelectField("Conta de destino", accs.firstOrNull { it.id == toAccountId }, accs, { "${it.name} (${Money.format(d.balances[it.id] ?: 0)})" }, { toAccountId = it.id }, leadingIcon = Icons.Outlined.AccountBalanceWallet)
                DateField("Data", date, { date = it })
                FFTextField(notes, { notes = it }, "Observação", leadingIcon = Icons.Outlined.EditNote)
                Text(
                    "Transferências entre contas próprias não contam como receita nem despesa.",
                    color = FF.TextSecondary, style = MaterialTheme.typography.bodySmall,
                )
            } else {
                if (editing == null) {
                    Column {
                        Text("Modalidade", color = FF.TextSecondary, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 6.dp))
                        Segmented(listOf("À Vista", "Parcelado", "Recorrente"), mode, { mode = it }, colors = listOf(FF.Sky, FF.Sky, FF.Sky))
                    }
                }
                if (mode == 1) {
                    FFCard(background = FF.Surface1, borderColor = FF.Sky.copy(alpha = 0.5f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.CreditCard, null, tint = FF.Sky)
                            Text(" Condições", color = FF.TextPrimary, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                            Pill("${count}x", FF.Sky)
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            SelectField("Parcelas", count, (2..48).toList(), { "${it}x" }, { count = it }, Modifier.weight(1f))
                            Column(Modifier.weight(1f)) {
                                Text("Valor da parcela", color = FF.TextSecondary, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 6.dp))
                                val per = FinanceRules.splitInstallments(amount, count).last()
                                Text("${count}x de ${Money.format(per)}", color = FF.TextPrimary, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(vertical = 15.dp))
                            }
                        }
                        Text(
                            if (onCard) "As parcelas serão lançadas nas próximas faturas do cartão."
                            else "A data abaixo é o vencimento da 1ª parcela; as demais vencem mensalmente.",
                            color = FF.TextSecondary, style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                if (mode == 2) {
                    FFCard(background = FF.Surface1, borderColor = FF.Sky.copy(alpha = 0.5f)) {
                        SelectField("Frequência", frequency, Frequency.entries, { it.label }, { frequency = it })
                        Spacer(Modifier.height(10.dp))
                        DateField("Data final (opcional)", endDate, { endDate = it }, placeholder = "Sem data final")
                        Spacer(Modifier.height(6.dp))
                        Text("Os próximos lançamentos são gerados automaticamente (60 dias à frente).", color = FF.TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                }

                FFTextField(description, { description = it }, "Descrição", leadingIcon = Icons.Outlined.EditNote, placeholder = "Ex.: Compras do mês")

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Categoria", color = FF.TextSecondary, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                        Text(
                            if (showAllCategories) "Ver menos" else "Ver todas", color = FF.EmeraldLight, style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.clickable { showAllCategories = !showAllCategories }.padding(4.dp),
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    val shown = if (showAllCategories) categories else {
                        val first = categories.take(4)
                        val sel = categories.firstOrNull { it.id == categoryId }
                        if (sel != null && sel !in first) first + sel else first
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        shown.forEach { c ->
                            FFChip(c.name, c.id == categoryId, icon = categoryIcon(c.icon)) {
                                categoryId = if (categoryId == c.id) null else c.id
                            }
                        }
                    }
                }

                DateField(
                    when {
                        onCard -> "Data da compra"
                        mode == 1 -> "Vencimento da 1ª parcela"
                        mode == 2 -> "Primeira ocorrência"
                        txType == TxType.INCOME -> "Data de recebimento"
                        else -> "Data de vencimento / pagamento"
                    },
                    date, { date = it },
                )
                if (onCard && editing == null) {
                    val card = d.cardMap[cardId]
                    if (card != null) {
                        val due = FinanceRules.invoiceDueDate(Dates.of(date), card.closingDay, card.dueDay)
                        Text("Entra na fatura com vencimento em ${Dates.full(due.epochDay())}.", color = FF.Sky, style = MaterialTheme.typography.bodySmall)
                    }
                }

                SelectField(
                    if (txType == TxType.INCOME) "Conta de destino" else "Conta / Cartão",
                    source, sources, { it.label },
                    {
                        accountId = it.accountId; cardId = it.cardId
                        if (it.cardId != null) method = PaymentMethod.CREDIT else if (method == PaymentMethod.CREDIT) method = PaymentMethod.PIX
                    },
                    leadingIcon = if (onCard) Icons.Outlined.CreditCard else Icons.Outlined.AccountBalanceWallet,
                )
                if (!onCard) {
                    SelectField("Forma de pagamento", method, PaymentMethod.entries.filter { it != PaymentMethod.CREDIT }, { it.label }, { method = it })
                }
                FFTextField(
                    person, { person = it },
                    if (txType == TxType.INCOME) "Pessoa / pagador (opcional)" else "Pessoa / favorecido (opcional)",
                    leadingIcon = Icons.Outlined.Person,
                )
                FFTextField(notes, { notes = it }, "Observação (opcional)", singleLine = false)

                if (editing == null && !onCard) {
                    ToggleRow(
                        title = when {
                            mode == 1 -> "1ª parcela já ${if (txType == TxType.INCOME) "recebida" else "paga"}"
                            mode == 2 -> "1ª ocorrência já efetivada"
                            txType == TxType.INCOME -> "Marcar como já recebido"
                            else -> "Marcar como já pago / efetivado"
                        },
                        subtitle = if (done) "Valor será ${if (txType == TxType.INCOME) "creditado no" else "debitado do"} saldo imediatamente."
                        else "Ficará pendente e entrará no saldo projetado.",
                        checked = done, onChange = { done = it }, icon = Icons.Outlined.CheckCircle,
                    )
                }
            }
            error?.let { Text(it, color = FF.Crimson, style = MaterialTheme.typography.bodyMedium) }
        }

        HorizontalDivider(color = FF.Border)
        PrimaryButton(
            if (editing != null) "Salvar Alterações" else "Salvar Movimentação", ::save,
            Modifier.fillMaxWidth().padding(16.dp).height(52.dp), icon = Icons.Outlined.CheckCircle,
        )
    }
}
