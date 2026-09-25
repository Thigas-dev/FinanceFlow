package com.financeflow.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.financeflow.app.data.Account
import com.financeflow.app.data.DisplayStatus
import com.financeflow.app.data.FinanceData
import com.financeflow.app.data.FinanceRules
import com.financeflow.app.data.Transaction
import com.financeflow.app.data.TxStatus
import com.financeflow.app.data.TxType
import com.financeflow.app.ui.MainViewModel
import com.financeflow.app.ui.theme.FF
import com.financeflow.app.util.Dates
import com.financeflow.app.util.Money

fun txSubtitle(t: Transaction, d: FinanceData): String {
    if (t.type == TxType.TRANSFER) {
        return "${d.accountMap[t.accountId]?.name ?: "?"} → ${d.accountMap[t.toAccountId]?.name ?: "?"}"
    }
    val parts = mutableListOf<String>()
    t.categoryId?.let { d.categoryMap[it]?.name }?.let { parts += it }
    t.person?.takeIf { it.isNotBlank() }?.let { parts += it }
    val where = t.cardId?.let { d.cardMap[it]?.let { c -> "Cartão ${c.name}" } } ?: t.accountId?.let { d.accountMap[it]?.name }
    where?.let { parts += it }
    if (parts.isEmpty()) t.paymentMethod?.let { parts += it.label }
    return parts.joinToString(" • ")
}

@Composable
fun TxRow(t: Transaction, d: FinanceData, onClick: () -> Unit, showDate: Boolean = false) {
    val status = FinanceRules.displayStatus(t)
    val cat = t.categoryId?.let { d.categoryMap[it] }
    val icon = when (t.type) {
        TxType.TRANSFER -> categoryIcon("transfer")
        else -> categoryIcon(cat?.icon ?: if (t.type == TxType.INCOME) "income" else "expense")
    }
    val tint = when {
        status == DisplayStatus.LATE -> FF.Crimson
        t.type == TxType.INCOME -> FF.Emerald
        t.type == TxType.TRANSFER -> FF.Sky
        else -> FF.TextSecondary
    }
    val amountColor = when {
        status == DisplayStatus.CANCELLED -> FF.TextTertiary
        t.type == TxType.INCOME -> FF.EmeraldLight
        t.type == TxType.TRANSFER -> FF.Sky
        status == DisplayStatus.LATE -> FF.Crimson
        else -> FF.TextPrimary
    }
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconBadge(icon, tint, bg = if (status == DisplayStatus.LATE) FF.Crimson.copy(alpha = 0.12f) else FF.Surface1)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    t.description, color = FF.TextPrimary, style = MaterialTheme.typography.titleSmall,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false),
                )
                if (t.installmentNumber != null) {
                    Spacer(Modifier.width(6.dp))
                    Pill("${t.installmentNumber}/${t.totalInstallments}", FF.Sky)
                }
            }
            val sub = txSubtitle(t, d).let { if (showDate) "${Dates.short(t.date)} • $it" else it }
            Text(sub, color = FF.TextSecondary, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            val prefix = when (t.type) {
                TxType.INCOME -> "+ "
                TxType.EXPENSE -> "- "
                TxType.TRANSFER -> "⇄ "
            }
            Text(prefix + Money.format(t.amount), color = amountColor, style = MaterialTheme.typography.titleSmall, maxLines = 1)
            Spacer(Modifier.height(4.dp))
            val extra = if (status == DisplayStatus.PENDING) {
                val days = Dates.daysUntil(t.date)
                when {
                    days == 0L -> "hoje"
                    days in 1..30 -> "$days dias"
                    else -> null
                }
            } else null
            if (t.type != TxType.TRANSFER) StatusPill(status, extra)
        }
    }
}

@Composable
fun TxList(items: List<Transaction>, d: FinanceData, onClick: (Transaction) -> Unit, showDate: Boolean = false) {
    FFCard(padding = 0.dp) {
        items.forEachIndexed { i, t ->
            if (i > 0) HorizontalDivider(color = FF.Border.copy(alpha = 0.5f))
            TxRow(t, d, { onClick(t) }, showDate)
        }
    }
}

/** Fluxo de pagamento (seção 25): confirma a conta utilizada e a data antes de efetivar. */
@Composable
fun MarkDoneDialog(tx: Transaction, d: FinanceData, onConfirm: (Long?, Long) -> Unit, onDismiss: () -> Unit) {
    val defaultAccount = tx.accountId?.let { d.accountMap[it] }
        ?: tx.cardId?.let { d.cardMap[it]?.accountId }?.let { d.accountMap[it] }
        ?: d.activeAccounts.firstOrNull()
    var account by remember { mutableStateOf(defaultAccount) }
    var date by remember { mutableStateOf(Dates.todayEpoch()) }
    val income = tx.type == TxType.INCOME
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FF.Surface2,
        title = { Text(if (income) "Marcar como recebido" else "Marcar como pago", color = FF.TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("${tx.description} — ${Money.format(tx.amount)}", color = FF.TextSecondary)
                SelectField(
                    label = if (income) "Conta de destino" else "Conta utilizada",
                    selected = account, options = d.activeAccounts, optionLabel = { accountLabel(it, d) },
                    onSelect = { account = it },
                )
                DateField(if (income) "Data do recebimento" else "Data do pagamento", date, { date = it })
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(account?.id, date); onDismiss() }, enabled = account != null) {
                Text("Confirmar", color = FF.Emerald)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = FF.TextSecondary) } },
    )
}

fun accountLabel(a: Account, d: FinanceData) = "${a.name} (${Money.format(d.balances[a.id] ?: 0)})"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TxDetailSheet(tx: Transaction, d: FinanceData, vm: MainViewModel, onEdit: (Transaction) -> Unit, onDismiss: () -> Unit) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var markDone by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var confirmDeletePlan by remember { mutableStateOf(false) }
    var confirmCancel by remember { mutableStateOf(false) }
    val status = FinanceRules.displayStatus(tx)
    val income = tx.type == TxType.INCOME

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = state, containerColor = FF.Surface1) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 24.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(tx.description, style = MaterialTheme.typography.headlineSmall, color = FF.TextPrimary)
                    if (tx.installmentNumber != null) {
                        Text("Parcela ${tx.installmentNumber}/${tx.totalInstallments}", color = FF.Sky, style = MaterialTheme.typography.labelLarge)
                    }
                }
                if (tx.type != TxType.TRANSFER) StatusPill(status)
            }
            Text(
                (if (income) "+ " else if (tx.type == TxType.EXPENSE) "- " else "") + Money.format(tx.amount),
                style = MaterialTheme.typography.displaySmall,
                color = if (income) FF.EmeraldLight else if (tx.type == TxType.TRANSFER) FF.Sky else FF.TextPrimary,
            )
            FFCard(background = FF.Surface2) {
                DetailLine(if (tx.status == TxStatus.PENDING) "Vencimento" else "Data", Dates.full(tx.date))
                tx.doneDate?.let { DetailLine(if (income) "Recebido em" else "Efetivado em", Dates.full(it)) }
                tx.categoryId?.let { d.categoryMap[it] }?.let { DetailLine("Categoria", it.name) }
                if (tx.type == TxType.TRANSFER) {
                    DetailLine("Origem", d.accountMap[tx.accountId]?.name ?: "-")
                    DetailLine("Destino", d.accountMap[tx.toAccountId]?.name ?: "-")
                } else {
                    tx.accountId?.let { d.accountMap[it] }?.let { DetailLine("Conta", it.name) }
                }
                tx.cardId?.let { d.cardMap[it] }?.let { DetailLine("Cartão", it.name) }
                tx.paymentMethod?.let { DetailLine("Forma de pagamento", it.label) }
                tx.person?.takeIf { it.isNotBlank() }?.let { DetailLine("Pessoa", it) }
                tx.recurrenceId?.let { DetailLine("Origem", "Lançamento recorrente") }
                tx.notes?.takeIf { it.isNotBlank() }?.let { DetailLine("Observação", it) }
            }

            if (tx.status == TxStatus.PENDING) {
                PrimaryButton(
                    if (income) "Marcar como recebido" else "Marcar como pago", { markDone = true },
                    Modifier.fillMaxWidth(), icon = Icons.Outlined.CheckCircle,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (tx.status == TxStatus.DONE && tx.type != TxType.TRANSFER) {
                    SecondaryButton("Voltar a pendente", { vm.markPending(tx); onDismiss() }, Modifier.weight(1f), icon = Icons.Outlined.Schedule)
                }
                if (tx.status == TxStatus.CANCELLED) {
                    SecondaryButton("Reativar", { vm.markPending(tx); onDismiss() }, Modifier.weight(1f), icon = Icons.Outlined.Schedule)
                }
                if (tx.type != TxType.TRANSFER) {
                    SecondaryButton("Editar", { onEdit(tx); onDismiss() }, Modifier.weight(1f), icon = Icons.Outlined.Edit)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (tx.status == TxStatus.PENDING) {
                    SecondaryButton("Cancelar", { confirmCancel = true }, Modifier.weight(1f), color = FF.Amber, icon = Icons.Outlined.Block)
                }
                SecondaryButton("Excluir", { confirmDelete = true }, Modifier.weight(1f), color = FF.Crimson, icon = Icons.Outlined.Delete)
            }
            if (tx.planId != null) {
                SecondaryButton("Excluir parcelamento inteiro", { confirmDeletePlan = true }, Modifier.fillMaxWidth(), color = FF.Crimson)
            }
        }
    }

    if (markDone) {
        MarkDoneDialog(tx, d, { acc, date -> vm.markDone(tx, acc, date); onDismiss() }, { markDone = false })
    }
    if (confirmCancel) {
        ConfirmDialog(
            "Cancelar movimentação", "Movimentações canceladas não entram no cálculo de saldo.",
            confirm = "Cancelar movimentação", destructive = true,
            onConfirm = { vm.cancel(tx); onDismiss() }, onDismiss = { confirmCancel = false },
        )
    }
    if (confirmDelete) {
        ConfirmDialog(
            "Excluir movimentação", "Esta ação não pode ser desfeita. O saldo será recalculado.",
            confirm = "Excluir", destructive = true,
            onConfirm = { vm.delete(tx); onDismiss() }, onDismiss = { confirmDelete = false },
        )
    }
    if (confirmDeletePlan) {
        ConfirmDialog(
            "Excluir parcelamento", "Todas as ${tx.totalInstallments} parcelas (inclusive as pagas) serão excluídas e o saldo recalculado.",
            confirm = "Excluir tudo", destructive = true,
            onConfirm = { tx.planId?.let { vm.deletePlan(it) }; onDismiss() }, onDismiss = { confirmDeletePlan = false },
        )
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        Text(label, color = FF.TextSecondary, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(value, color = FF.TextPrimary, style = MaterialTheme.typography.bodyMedium)
    }
}
