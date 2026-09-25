package com.financeflow.app.data

import com.financeflow.app.util.Dates
import com.financeflow.app.util.YearMonth
import com.financeflow.app.util.epochDay
import kotlinx.datetime.LocalDate

enum class DisplayStatus(val label: String) {
    PENDING("Pendente"),
    PAID("Pago"),
    RECEIVED("Recebido"),
    DONE("Efetivada"),
    LATE("Atrasado"),
    CANCELLED("Cancelado"),
}

/** Snapshot dos dados do usuário logado, com os cálculos derivados das regras de negócio. */
data class FinanceData(
    val accounts: List<Account> = emptyList(),
    val categories: List<Category> = emptyList(),
    val cards: List<CreditCard> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val plans: List<InstallmentPlan> = emptyList(),
    val recurrences: List<Recurrence> = emptyList(),
    val notifications: List<AppNotification> = emptyList(),
    val loaded: Boolean = false,
) {
    val accountMap: Map<Long, Account> = accounts.associateBy { it.id }
    val categoryMap: Map<Long, Category> = categories.associateBy { it.id }
    val cardMap: Map<Long, CreditCard> = cards.associateBy { it.id }
    val activeAccounts: List<Account> = accounts.filter { it.active }
    val activeCards: List<CreditCard> = cards.filter { it.active }

    /** RN01/RN06/RN07: saldo = inicial + movimentações efetivadas; canceladas e pendentes não entram. */
    val balances: Map<Long, Long> = run {
        val map = accounts.associate { it.id to it.initialBalance }.toMutableMap()
        for (t in transactions) {
            if (t.status != TxStatus.DONE) continue
            when (t.type) {
                TxType.INCOME -> t.accountId?.let { map[it] = (map[it] ?: 0) + t.amount }
                TxType.EXPENSE -> t.accountId?.let { map[it] = (map[it] ?: 0) - t.amount }
                TxType.TRANSFER -> {
                    t.accountId?.let { map[it] = (map[it] ?: 0) - t.amount }
                    t.toAccountId?.let { map[it] = (map[it] ?: 0) + t.amount }
                }
            }
        }
        map
    }

    val currentBalance: Long = activeAccounts.sumOf { balances[it.id] ?: 0 }

    val pending: List<Transaction> = transactions.filter { it.status == TxStatus.PENDING && it.type != TxType.TRANSFER }

    val toReceive: Long = pending.filter { it.type == TxType.INCOME }.sumOf { it.amount }
    val toPay: Long = pending.filter { it.type == TxType.EXPENSE }.sumOf { it.amount }

    /** RN02: compromissos futuros compõem o saldo projetado. */
    val projected: Long = currentBalance + toReceive - toPay

    fun projectedUntil(epochDay: Long): Long {
        val inRange = pending.filter { it.date <= epochDay }
        return currentBalance + inRange.filter { it.type == TxType.INCOME }.sumOf { it.amount } -
            inRange.filter { it.type == TxType.EXPENSE }.sumOf { it.amount }
    }

    fun cardUsed(cardId: Long): Long =
        transactions.filter { it.cardId == cardId && it.status == TxStatus.PENDING && it.type == TxType.EXPENSE }
            .sumOf { it.amount }

    fun cardInvoices(cardId: Long): List<Invoice> =
        transactions.filter { it.cardId == cardId && it.type == TxType.EXPENSE && it.status != TxStatus.CANCELLED }
            .groupBy { it.date }
            .map { (date, items) -> Invoice(cardId, date, items.sortedBy { it.description }) }
            .sortedBy { it.dueDate }

    /** Contas a pagar: despesas pendentes, com compras no cartão agrupadas em fatura. */
    fun payables(): List<Payable> {
        val list = mutableListOf<Payable>()
        pending.filter { it.type == TxType.EXPENSE && it.cardId == null }.forEach { list += Payable.Single(it) }
        pending.filter { it.type == TxType.EXPENSE && it.cardId != null }
            .groupBy { it.cardId!! to it.date }
            .forEach { (key, items) -> list += Payable.CardBill(cardMap[key.first], key.first, key.second, items) }
        return list.sortedBy { it.date }
    }

    fun monthSummary(ym: YearMonth): MonthSummary {
        val from = ym.atDay(1).epochDay()
        val to = ym.atEndOfMonth().epochDay()
        val inMonth = transactions.filter { it.date in from..to && it.type != TxType.TRANSFER && it.status != TxStatus.CANCELLED }
        val incomes = inMonth.filter { it.type == TxType.INCOME }
        val expenses = inMonth.filter { it.type == TxType.EXPENSE }
        val byCategory = expenses.groupBy { it.categoryId }
            .map { (cid, list) -> (cid?.let { categoryMap[it]?.name } ?: "Sem categoria") to list.sumOf { it.amount } }
            .sortedByDescending { it.second }
        return MonthSummary(
            incomeTotal = incomes.sumOf { it.amount },
            expenseTotal = expenses.sumOf { it.amount },
            received = incomes.filter { it.status == TxStatus.DONE }.sumOf { it.amount },
            spent = expenses.filter { it.status == TxStatus.DONE }.sumOf { it.amount },
            pendingIncome = incomes.filter { it.status == TxStatus.PENDING }.sumOf { it.amount },
            pendingExpense = expenses.filter { it.status == TxStatus.PENDING }.sumOf { it.amount },
            count = inMonth.size,
            byCategory = byCategory,
        )
    }

    fun planProgress(plan: InstallmentPlan): PlanProgress {
        val items = transactions.filter { it.planId == plan.id }.sortedBy { it.installmentNumber }
        val active = items.filter { it.status != TxStatus.CANCELLED }
        val done = active.filter { it.status == TxStatus.DONE }
        val remaining = active.filter { it.status == TxStatus.PENDING }
        return PlanProgress(
            plan = plan,
            items = items,
            paidCount = done.size,
            totalCount = plan.count,
            paidAmount = done.sumOf { it.amount },
            remainingAmount = remaining.sumOf { it.amount },
            remainingCount = remaining.size,
            next = remaining.minByOrNull { it.date },
            last = items.maxByOrNull { it.date },
        )
    }
}

data class Invoice(val cardId: Long, val dueDate: Long, val items: List<Transaction>) {
    val total: Long = items.sumOf { it.amount }
    val pendingTotal: Long = items.filter { it.status == TxStatus.PENDING }.sumOf { it.amount }
    val isPaid: Boolean = items.none { it.status == TxStatus.PENDING }
}

sealed class Payable {
    abstract val date: Long
    abstract val amount: Long

    data class Single(val tx: Transaction) : Payable() {
        override val date = tx.date
        override val amount = tx.amount
    }

    data class CardBill(val card: CreditCard?, val cardId: Long, override val date: Long, val items: List<Transaction>) : Payable() {
        override val amount = items.sumOf { it.amount }
    }
}

data class MonthSummary(
    val incomeTotal: Long,
    val expenseTotal: Long,
    val received: Long,
    val spent: Long,
    val pendingIncome: Long,
    val pendingExpense: Long,
    val count: Int,
    val byCategory: List<Pair<String, Long>>,
) {
    val result: Long = incomeTotal - expenseTotal
}

data class PlanProgress(
    val plan: InstallmentPlan,
    val items: List<Transaction>,
    val paidCount: Int,
    val totalCount: Int,
    val paidAmount: Long,
    val remainingAmount: Long,
    val remainingCount: Int,
    val next: Transaction?,
    val last: Transaction?,
) {
    val fraction: Float = if (totalCount == 0) 0f else paidCount.toFloat() / totalCount
}

object FinanceRules {
    /** RN08: pendente com vencimento anterior a hoje é atrasado. */
    fun isLate(t: Transaction, today: Long = Dates.todayEpoch()) =
        t.status == TxStatus.PENDING && t.date < today

    fun displayStatus(t: Transaction, today: Long = Dates.todayEpoch()): DisplayStatus = when (t.status) {
        TxStatus.CANCELLED -> DisplayStatus.CANCELLED
        TxStatus.PENDING -> if (isLate(t, today)) DisplayStatus.LATE else DisplayStatus.PENDING
        TxStatus.DONE -> when (t.type) {
            TxType.INCOME -> DisplayStatus.RECEIVED
            TxType.EXPENSE -> DisplayStatus.PAID
            TxType.TRANSFER -> DisplayStatus.DONE
        }
    }

    /** RN05: divide o total entre as parcelas; a diferença de centavos vai na primeira. */
    fun splitInstallments(total: Long, count: Int): List<Long> {
        val base = total / count
        val rest = total - base * count
        return List(count) { i -> if (i == 0) base + rest else base }
    }

    /** Data de vencimento da fatura em que uma compra no cartão cai. */
    fun invoiceDueDate(purchase: LocalDate, closingDay: Int, dueDay: Int): LocalDate {
        var closingMonth = YearMonth.from(purchase)
        val closing = closingMonth.atDay(minOf(closingDay, closingMonth.lengthOfMonth()))
        if (purchase >= closing) closingMonth = closingMonth.plusMonths(1)
        val dueMonth = if (dueDay > closingDay) closingMonth else closingMonth.plusMonths(1)
        return dueMonth.atDay(minOf(dueDay, dueMonth.lengthOfMonth()))
    }

    fun nextOccurrence(from: Long, r: Recurrence): Long = when (r.frequency) {
        Frequency.WEEKLY -> from + 7
        Frequency.BIWEEKLY -> from + 14
        Frequency.MONTHLY -> Dates.plusMonths(from, 1, Dates.of(r.startDate).dayOfMonth)
        Frequency.YEARLY -> Dates.plusYears(from, 1)
    }
}
