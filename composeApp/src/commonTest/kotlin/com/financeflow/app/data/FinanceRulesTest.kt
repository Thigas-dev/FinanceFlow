package com.financeflow.app.data

import com.financeflow.app.util.Dates
import com.financeflow.app.util.Money
import com.financeflow.app.util.YearMonth
import com.financeflow.app.util.epochDay
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FinanceRulesTest {
    private val today = Dates.todayEpoch()

    private fun tx(
        id: Long, type: TxType, amount: Long, status: TxStatus, date: Long = today,
        account: Long? = 1, to: Long? = null, card: Long? = null, plan: Long? = null,
    ) = Transaction(
        id = id, userId = 1, type = type, description = "t$id", amount = amount, date = date, status = status,
        accountId = account, toAccountId = to, cardId = card, planId = plan,
    )

    private val accounts = listOf(
        Account(id = 1, userId = 1, name = "Nubank", type = AccountType.DIGITAL, initialBalance = 250_000),
        Account(id = 2, userId = 1, name = "Itaú", type = AccountType.CHECKING, initialBalance = 120_000),
    )

    @Test
    fun saldoConsideraApenasMovimentacoesEfetivadasRn01Rn02Rn07() {
        val d = FinanceData(
            accounts = accounts,
            transactions = listOf(
                tx(1, TxType.INCOME, 300_000, TxStatus.DONE),
                tx(2, TxType.EXPENSE, 12_000, TxStatus.DONE),
                tx(3, TxType.EXPENSE, 50_000, TxStatus.PENDING, today + 5),
                tx(4, TxType.EXPENSE, 99_999, TxStatus.CANCELLED),
                tx(5, TxType.INCOME, 80_000, TxStatus.PENDING, today + 10),
            ),
        )
        assertEquals(250_000 + 300_000 - 12_000L, d.balances[1])
        assertEquals(250_000 + 300_000 - 12_000 + 120_000L, d.currentBalance)
        assertEquals(80_000L, d.toReceive)
        assertEquals(50_000L, d.toPay)
        assertEquals(d.currentBalance + 80_000 - 50_000, d.projected)
        assertEquals(d.currentBalance - 50_000, d.projectedUntil(today + 7))
    }

    @Test
    fun transferenciaMoveSaldoSemAfetarResultadoRn06() {
        val d = FinanceData(
            accounts = accounts,
            transactions = listOf(tx(1, TxType.TRANSFER, 50_000, TxStatus.DONE, account = 1, to = 2)),
        )
        assertEquals(200_000L, d.balances[1])
        assertEquals(170_000L, d.balances[2])
        assertEquals(370_000L, d.currentBalance)
        val s = d.monthSummary(YearMonth.now())
        assertEquals(0L, s.incomeTotal)
        assertEquals(0L, s.expenseTotal)
    }

    @Test
    fun pendenteVencidoEAtrasadoRn08() {
        assertEquals(DisplayStatus.LATE, FinanceRules.displayStatus(tx(1, TxType.EXPENSE, 1, TxStatus.PENDING, today - 1)))
        assertEquals(DisplayStatus.PENDING, FinanceRules.displayStatus(tx(1, TxType.EXPENSE, 1, TxStatus.PENDING, today)))
        assertEquals(DisplayStatus.PAID, FinanceRules.displayStatus(tx(1, TxType.EXPENSE, 1, TxStatus.DONE, today - 9)))
        assertEquals(DisplayStatus.RECEIVED, FinanceRules.displayStatus(tx(1, TxType.INCOME, 1, TxStatus.DONE)))
    }

    @Test
    fun parcelasSomamOTotalRn05() {
        assertEquals(List(12) { 10_000L }, FinanceRules.splitInstallments(120_000, 12))
        val odd = FinanceRules.splitInstallments(10_000, 3)
        assertEquals(listOf(3_334L, 3_333L, 3_333L), odd)
        assertEquals(10_000L, odd.sum())
    }

    @Test
    fun progressoDoParcelamento() {
        val plan = InstallmentPlan(id = 7, userId = 1, type = TxType.EXPENSE, description = "Notebook", totalAmount = 120_000, count = 12, firstDueDate = today, categoryId = null, accountId = 1, cardId = null)
        val items = (1..12).map { i ->
            tx(i.toLong(), TxType.EXPENSE, 10_000, if (i <= 3) TxStatus.DONE else TxStatus.PENDING, Dates.plusMonths(today, i - 1L), plan = 7)
                .copy(installmentNumber = i, totalInstallments = 12)
        }
        val p = FinanceData(accounts = accounts, plans = listOf(plan), transactions = items).planProgress(plan)
        assertEquals(3, p.paidCount)
        assertEquals(9, p.remainingCount)
        assertEquals(90_000L, p.remainingAmount)
        assertEquals(4, p.next?.installmentNumber)
    }

    @Test
    fun faturaDoCartaoRespeitaFechamentoEVencimento() {
        // Fecha dia 3, vence dia 10: compra antes do fechamento cai no dia 10 do mesmo mês.
        assertEquals(LocalDate(2026, 10, 10), FinanceRules.invoiceDueDate(LocalDate(2026, 10, 2), 3, 10))
        // Compra no dia/apos o fechamento vai para a fatura seguinte.
        assertEquals(LocalDate(2026, 11, 10), FinanceRules.invoiceDueDate(LocalDate(2026, 10, 3), 3, 10))
        // Fecha dia 25, vence dia 5 do mês seguinte.
        assertEquals(LocalDate(2026, 11, 5), FinanceRules.invoiceDueDate(LocalDate(2026, 10, 20), 25, 5))
        assertEquals(LocalDate(2026, 12, 5), FinanceRules.invoiceDueDate(LocalDate(2026, 10, 26), 25, 5))
    }

    @Test
    fun contasAPagarAgrupamComprasDoCartaoEmFatura() {
        val card = CreditCard(id = 1, userId = 1, name = "Nubank", limit = 500_000, closingDay = 3, dueDay = 10, accountId = 1)
        val d = FinanceData(
            accounts = accounts, cards = listOf(card),
            transactions = listOf(
                tx(1, TxType.EXPENSE, 10_000, TxStatus.PENDING, today + 5, account = null, card = 1),
                tx(2, TxType.EXPENSE, 20_000, TxStatus.PENDING, today + 5, account = null, card = 1),
                tx(3, TxType.EXPENSE, 9_990, TxStatus.PENDING, today + 2),
            ),
        )
        val payables = d.payables()
        assertEquals(2, payables.size)
        assertTrue(payables[0] is Payable.Single)
        assertEquals(30_000L, (payables[1] as Payable.CardBill).amount)
        assertEquals(30_000L, d.cardUsed(1))
    }

    @Test
    fun recorrenciaMensalPreservaODiaDeVencimento() {
        val start = LocalDate(2026, 1, 31).epochDay()
        val r = Recurrence(id = 1, userId = 1, type = TxType.EXPENSE, description = "Aluguel", amount = 1, frequency = Frequency.MONTHLY, startDate = start, categoryId = null, accountId = 1)
        val feb = FinanceRules.nextOccurrence(start, r)
        assertEquals(LocalDate(2026, 2, 28), Dates.of(feb))
        assertEquals(LocalDate(2026, 3, 31), Dates.of(FinanceRules.nextOccurrence(feb, r)))
    }

    @Test
    fun formatacaoEmReais() {
        assertEquals("R$ 3.250,00", Money.format(325_000))
        assertEquals(15_000L, Money.fromDigits("R$ 150,00"))
    }
}
