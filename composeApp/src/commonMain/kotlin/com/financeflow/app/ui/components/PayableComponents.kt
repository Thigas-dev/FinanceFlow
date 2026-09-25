package com.financeflow.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.financeflow.app.data.FinanceData
import com.financeflow.app.data.Payable
import com.financeflow.app.data.Transaction
import com.financeflow.app.data.TxType
import com.financeflow.app.ui.MainViewModel
import com.financeflow.app.ui.theme.FF
import com.financeflow.app.util.Dates
import com.financeflow.app.util.Money

@Composable
fun SmallActionButton(text: String, onClick: () -> Unit, color: Color = FF.Emerald) {
    Button(
        onClick = onClick, shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = FF.Ink),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
        modifier = Modifier.heightIn(min = 34.dp),
    ) { Text(text, style = MaterialTheme.typography.labelLarge) }
}

@Composable
fun PayableRow(p: Payable, d: FinanceData, onOpenTx: (Transaction) -> Unit, onPay: (Payable) -> Unit) {
    val (title, icon) = when (p) {
        is Payable.Single -> p.tx.description to categoryIcon(p.tx.categoryId?.let { d.categoryMap[it]?.icon } ?: "expense")
        is Payable.CardBill -> "Fatura ${p.card?.name ?: "cartão"}" to categoryIcon("card")
    }
    val late = p.date < Dates.todayEpoch()
    val soon = Dates.daysUntil(p.date) in 0..3
    Row(
        Modifier.fillMaxWidth()
            .clickable { if (p is Payable.Single) onOpenTx(p.tx) }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconBadge(icon, if (late) FF.Crimson else FF.Sky, bg = if (late) FF.Crimson.copy(alpha = 0.12f) else FF.Surface1)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = FF.TextPrimary, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            val sub = when (p) {
                is Payable.Single -> p.tx.installmentNumber?.let { "Parcela $it/${p.tx.totalInstallments} • " } ?: ""
                is Payable.CardBill -> "${p.items.size} lançamento(s) • "
            } + Dates.dueText(p.date)
            Text(sub, color = if (late || soon) FF.CrimsonLight else FF.TextSecondary, style = MaterialTheme.typography.bodySmall, maxLines = 1)
        }
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(Money.format(p.amount), color = if (late) FF.Crimson else FF.TextPrimary, style = MaterialTheme.typography.titleSmall)
            SmallActionButton("Pagar", { onPay(p) })
        }
    }
}

/** Abre o diálogo adequado para quitar uma conta ou a fatura inteira do cartão. */
@Composable
fun PayDialogHost(payable: Payable?, d: FinanceData, vm: MainViewModel, onDismiss: () -> Unit) {
    when (payable) {
        null -> Unit
        is Payable.Single -> MarkDoneDialog(payable.tx, d, { acc, date -> vm.markDone(payable.tx, acc, date) }, onDismiss)
        is Payable.CardBill -> InvoicePayDialog(payable.items, payable.card?.accountId, payable.card?.name ?: "cartão", d, vm, onDismiss)
    }
}

@Composable
fun InvoicePayDialog(items: List<Transaction>, defaultAccountId: Long?, cardName: String, d: FinanceData, vm: MainViewModel, onDismiss: () -> Unit) {
    var account by remember { mutableStateOf(defaultAccountId?.let { d.accountMap[it] } ?: d.activeAccounts.firstOrNull()) }
    val total = items.filter { it.type == TxType.EXPENSE }.sumOf { it.amount }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FF.Surface2,
        title = { Text("Pagar fatura $cardName", color = FF.TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("${items.size} lançamento(s) — total ${Money.format(total)}", color = FF.TextSecondary)
                SelectField("Conta utilizada", account, d.activeAccounts, { accountLabel(it, d) }, { account = it })
            }
        },
        confirmButton = {
            TextButton(onClick = { vm.payInvoice(items, account?.id); onDismiss() }, enabled = account != null) {
                Text("Pagar", color = FF.Emerald)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = FF.TextSecondary) } },
    )
}
