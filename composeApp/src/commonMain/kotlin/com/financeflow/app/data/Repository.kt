package com.financeflow.app.data

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import com.financeflow.app.util.epochDay
import com.financeflow.app.util.Dates
import com.financeflow.app.util.Money
import com.financeflow.app.util.Passwords
import kotlinx.coroutines.flow.Flow

/** Dados de um novo lançamento vindos do formulário. */
data class NewEntry(
    val userId: Long,
    val type: TxType,
    val description: String,
    val amount: Long,
    val date: Long,
    val categoryId: Long?,
    val accountId: Long?,
    val cardId: Long?,
    val paymentMethod: PaymentMethod?,
    val person: String?,
    val notes: String?,
    val done: Boolean,
)

/** Transação de escrita (substitui o withTransaction, que só existe no Android). */
private suspend fun <R> AppDatabase.tx(block: suspend () -> R): R =
    useWriterConnection { transactor -> transactor.immediateTransaction { block() } }

class Repository(private val db: AppDatabase) {

    // ---------- Autenticação ----------

    suspend fun register(name: String, email: String, password: String, recovery: String): Result<Long> {
        val normalized = email.trim().lowercase()
        if (db.users().findByEmail(normalized) != null) return Result.failure(IllegalStateException("E-mail já cadastrado"))
        val salt = Passwords.newSalt()
        val rSalt = Passwords.newSalt()
        val user = User(
            name = name.trim(),
            email = normalized,
            passwordHash = Passwords.hash(password, salt),
            salt = salt,
            recoveryHash = Passwords.hash(recovery.trim().lowercase(), rSalt),
            recoverySalt = rSalt,
        )
        val id = db.tx {
            val id = db.users().insert(user)
            seedDefaults(id)
            id
        }
        return Result.success(id)
    }

    suspend fun login(email: String, password: String): Long? {
        val user = db.users().findByEmail(email.trim().lowercase()) ?: return null
        return if (Passwords.verify(password, user.salt, user.passwordHash)) user.id else null
    }

    suspend fun resetPassword(email: String, recovery: String, newPassword: String): Boolean {
        val user = db.users().findByEmail(email.trim().lowercase()) ?: return false
        if (!Passwords.verify(recovery.trim().lowercase(), user.recoverySalt, user.recoveryHash)) return false
        val salt = Passwords.newSalt()
        db.users().update(user.copy(salt = salt, passwordHash = Passwords.hash(newPassword, salt)))
        return true
    }

    suspend fun changePassword(userId: Long, current: String, newPassword: String): Boolean {
        val user = db.users().get(userId) ?: return false
        if (!Passwords.verify(current, user.salt, user.passwordHash)) return false
        val salt = Passwords.newSalt()
        db.users().update(user.copy(salt = salt, passwordHash = Passwords.hash(newPassword, salt)))
        return true
    }

    suspend fun updateName(userId: Long, name: String) {
        val user = db.users().get(userId) ?: return
        db.users().update(user.copy(name = name.trim()))
    }

    private suspend fun seedDefaults(userId: Long) {
        val income = listOf(
            "Salário" to "work", "Freelance" to "laptop", "Investimentos" to "trending",
            "Venda" to "sell", "Reembolso" to "undo", "Outros" to "more",
        )
        val expense = listOf(
            "Moradia" to "home", "Alimentação" to "restaurant", "Transporte" to "car",
            "Educação" to "school", "Saúde" to "health", "Lazer" to "movie",
            "Assinaturas" to "subscriptions", "Compras" to "shopping", "Impostos" to "receipt",
            "Outros" to "more",
        )
        db.categories().insertAll(
            income.map { Category(userId = userId, name = it.first, type = TxType.INCOME, icon = it.second, isDefault = true) } +
                expense.map { Category(userId = userId, name = it.first, type = TxType.EXPENSE, icon = it.second, isDefault = true) },
        )
        db.accounts().insert(Account(userId = userId, name = "Carteira", type = AccountType.CASH, color = 0xFF10B981))
    }

    // ---------- Observação ----------

    fun user(userId: Long) = db.users().observe(userId)
    fun accounts(userId: Long): Flow<List<Account>> = db.accounts().observe(userId)
    fun categories(userId: Long) = db.categories().observe(userId)
    fun cards(userId: Long) = db.cards().observe(userId)
    fun transactions(userId: Long) = db.transactions().observe(userId)
    fun plans(userId: Long) = db.plans().observe(userId)
    fun recurrences(userId: Long) = db.recurrences().observe(userId)
    fun notifications(userId: Long) = db.notifications().observe(userId)

    // ---------- Cadastros ----------

    suspend fun saveAccount(a: Account) = if (a.id == 0L) db.accounts().insert(a) else db.accounts().update(a).let { a.id }

    /** Exclui a conta; se já houver movimentações efetivadas, apenas inativa (preserva histórico). */
    suspend fun deleteAccount(a: Account): String = db.tx {
        if (db.transactions().countDoneForAccount(a.id) > 0) {
            db.accounts().update(a.copy(active = false))
            "Conta possui histórico e foi inativada"
        } else {
            db.transactions().detachPendingFromAccount(a.id)
            db.accounts().delete(a)
            "Conta excluída"
        }
    }

    suspend fun saveCategory(c: Category) = if (c.id == 0L) db.categories().insert(c) else db.categories().update(c).let { c.id }

    suspend fun deleteCategory(c: Category) = db.tx {
        db.transactions().detachCategory(c.id)
        db.categories().delete(c)
    }

    suspend fun saveCard(c: CreditCard) = if (c.id == 0L) db.cards().insert(c) else db.cards().update(c).let { c.id }

    suspend fun deleteCard(c: CreditCard) = db.cards().update(c.copy(active = false))

    // ---------- Lançamentos ----------

    suspend fun addSingle(e: NewEntry) {
        val card = e.cardId?.let { db.cards().get(it) }
        val date = if (card != null) {
            FinanceRules.invoiceDueDate(Dates.of(e.date), card.closingDay, card.dueDay).epochDay()
        } else e.date
        val done = e.done && card == null
        db.transactions().insert(
            Transaction(
                userId = e.userId, type = e.type, description = e.description, amount = e.amount,
                date = date, status = if (done) TxStatus.DONE else TxStatus.PENDING,
                doneDate = if (done) Dates.todayEpoch() else null,
                categoryId = e.categoryId, accountId = e.accountId, cardId = e.cardId,
                paymentMethod = e.paymentMethod, person = e.person, notes = e.notes,
            ),
        )
    }

    suspend fun updateTransaction(tx: Transaction) = db.transactions().update(tx)

    /** Gera automaticamente as N parcelas (CA05/CA06). [NewEntry.amount] é o valor total. */
    suspend fun addInstallments(e: NewEntry, count: Int) = db.tx {
        val card = e.cardId?.let { db.cards().get(it) }
        val first = if (card != null) {
            FinanceRules.invoiceDueDate(Dates.of(e.date), card.closingDay, card.dueDay).epochDay()
        } else e.date
        val planId = db.plans().insert(
            InstallmentPlan(
                userId = e.userId, type = e.type, description = e.description, totalAmount = e.amount,
                count = count, firstDueDate = first, categoryId = e.categoryId,
                accountId = e.accountId, cardId = e.cardId,
            ),
        )
        val values = FinanceRules.splitInstallments(e.amount, count)
        val preferredDay = Dates.of(first).dayOfMonth
        val txs = values.mapIndexed { i, value ->
            val firstDone = i == 0 && e.done && card == null
            Transaction(
                userId = e.userId, type = e.type, description = e.description, amount = value,
                date = Dates.plusMonths(first, i.toLong(), preferredDay),
                status = if (firstDone) TxStatus.DONE else TxStatus.PENDING,
                doneDate = if (firstDone) Dates.todayEpoch() else null,
                categoryId = e.categoryId, accountId = e.accountId, cardId = e.cardId,
                paymentMethod = e.paymentMethod, person = e.person, notes = e.notes,
                planId = planId, installmentNumber = i + 1, totalInstallments = count,
            )
        }
        db.transactions().insertAll(txs)
    }

    suspend fun addRecurrence(e: NewEntry, frequency: Frequency, endDate: Long?) {
        val id = db.recurrences().insert(
            Recurrence(
                userId = e.userId, type = e.type, description = e.description, amount = e.amount,
                frequency = frequency, startDate = e.date, endDate = endDate, categoryId = e.categoryId,
                accountId = e.accountId, cardId = e.cardId, paymentMethod = e.paymentMethod,
            ),
        )
        generateRecurrences(e.userId)
        if (e.done && e.cardId == null) {
            db.transactions().list(e.userId).firstOrNull { it.recurrenceId == id && it.date == e.date }?.let {
                markDone(it, it.accountId, Dates.todayEpoch())
            }
        }
    }

    suspend fun addTransfer(userId: Long, from: Long, to: Long, amount: Long, date: Long, notes: String?) {
        db.transactions().insert(
            Transaction(
                userId = userId, type = TxType.TRANSFER, description = "Transferência", amount = amount,
                date = date, status = TxStatus.DONE, doneDate = date, accountId = from, toAccountId = to,
                paymentMethod = PaymentMethod.TRANSFER, notes = notes,
            ),
        )
    }

    /** RN03/RN04: ao efetivar, o valor passa a compor o saldo da conta escolhida. */
    suspend fun markDone(tx: Transaction, accountId: Long?, date: Long) =
        db.transactions().update(tx.copy(status = TxStatus.DONE, accountId = accountId ?: tx.accountId, doneDate = date))

    suspend fun markPending(tx: Transaction) =
        db.transactions().update(tx.copy(status = TxStatus.PENDING, doneDate = null))

    suspend fun cancel(tx: Transaction) =
        db.transactions().update(tx.copy(status = TxStatus.CANCELLED, doneDate = null))

    /** RN10: o saldo é derivado das movimentações, então excluir já recalcula. */
    suspend fun delete(tx: Transaction) = db.tx {
        db.transactions().delete(tx.id)
        tx.planId?.let { if (db.transactions().countByPlan(it) == 0) db.plans().delete(it) }
    }

    suspend fun deletePlan(planId: Long) = db.tx {
        db.transactions().deleteByPlan(planId)
        db.plans().delete(planId)
    }

    suspend fun payInvoice(items: List<Transaction>, accountId: Long?) = db.tx {
        val today = Dates.todayEpoch()
        items.filter { it.status == TxStatus.PENDING }.forEach { markDone(it, accountId, today) }
    }

    suspend fun setRecurrenceActive(r: Recurrence, active: Boolean) = db.tx {
        db.recurrences().update(r.copy(active = active))
        if (!active) db.transactions().deletePendingByRecurrence(r.id, Dates.todayEpoch())
        else generateRecurrences(r.userId)
    }

    suspend fun deleteRecurrence(r: Recurrence) = db.tx {
        db.transactions().deletePendingByRecurrence(r.id, Dates.todayEpoch())
        db.recurrences().delete(r.id)
    }

    /** RN09: gera os lançamentos recorrentes até 60 dias à frente. */
    suspend fun generateRecurrences(userId: Long) = db.tx {
        val horizon = Dates.todayEpoch() + 60
        val cards = mutableMapOf<Long, CreditCard?>()
        for (r in db.recurrences().listActive(userId)) {
            val limit = r.endDate?.let { minOf(it, horizon) } ?: horizon
            var next = r.lastGenerated?.let { FinanceRules.nextOccurrence(it, r) } ?: r.startDate
            var last = r.lastGenerated
            val card = r.cardId?.let { id -> cards.getOrPut(id) { db.cards().get(id) } }
            val txs = mutableListOf<Transaction>()
            while (next <= limit) {
                val due = if (card != null) {
                    FinanceRules.invoiceDueDate(Dates.of(next), card.closingDay, card.dueDay).epochDay()
                } else next
                txs += Transaction(
                    userId = userId, type = r.type, description = r.description, amount = r.amount,
                    date = due, status = TxStatus.PENDING, categoryId = r.categoryId, accountId = r.accountId,
                    cardId = r.cardId, paymentMethod = r.paymentMethod, recurrenceId = r.id,
                )
                last = next
                next = FinanceRules.nextOccurrence(next, r)
            }
            if (txs.isNotEmpty()) {
                db.transactions().insertAll(txs)
                db.recurrences().update(r.copy(lastGenerated = last))
            }
        }
    }

    suspend fun snapshot(userId: Long) = FinanceData(
        cards = db.cards().list(userId),
        transactions = db.transactions().list(userId),
    )

    // ---------- Notificações ----------

    suspend fun markNotificationsRead(userId: Long) = db.notifications().markAllRead(userId)
    suspend fun clearNotifications(userId: Long) = db.notifications().clear(userId)

    /** Gera alertas de vencimento (3 dias antes, no dia e após o vencimento). Retorna apenas os novos. */
    suspend fun runDueCheck(userId: Long, daysBefore: Int): List<AppNotification> {
        val today = Dates.todayEpoch()
        val data = FinanceData(
            cards = db.cards().list(userId),
            transactions = db.transactions().list(userId),
        )
        val out = mutableListOf<AppNotification>()
        suspend fun add(key: String, type: String, title: String, message: String) {
            val n = AppNotification(userId = userId, key = key, type = type, title = title, message = message)
            if (db.notifications().insert(n) != -1L) out += n
        }

        val payables = data.payables()
        val soon = payables.filter { it.date in (today + 1)..(today + daysBefore) }
        if (soon.isNotEmpty()) {
            val n = soon.size
            add(
                "soon-$today", "soon", "Vencimentos próximos",
                if (n == 1) "Você possui 1 conta vencendo nos próximos $daysBefore dias."
                else "Você possui $n contas vencendo nos próximos $daysBefore dias.",
            )
        }
        for (p in payables) {
            val (key, name) = when (p) {
                is Payable.Single -> "tx-${p.tx.id}" to p.tx.description
                is Payable.CardBill -> "card-${p.cardId}-${p.date}" to "Fatura ${p.card?.name ?: "cartão"}"
            }
            val value = Money.format(p.amount)
            if (p.date == today) add("today-$key", "today", "Vence hoje", "A conta de $name de $value vence hoje.")
            if (p.date < today) add("late-$key", "late", "Conta atrasada", "A conta de $name de $value está atrasada.")
        }
        for (t in data.pending.filter { it.type == TxType.INCOME }) {
            val who = t.person?.takeIf { it.isNotBlank() }?.let { " de $it" } ?: ""
            val value = Money.format(t.amount)
            if (t.date == today) add("in-today-${t.id}", "income", "Recebimento previsto", "${t.description}$who: $value previsto para hoje.")
            if (t.date < today) add("in-late-${t.id}", "late", "Recebimento atrasado", "${t.description}$who de $value está atrasado.")
        }
        return out
    }
}
