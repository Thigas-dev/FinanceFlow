package com.financeflow.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financeflow.app.AppContainer
import com.financeflow.app.Reminder
import com.financeflow.app.data.Payable
import com.financeflow.app.data.TxType
import com.financeflow.app.util.Dates
import com.financeflow.app.util.Money
import com.financeflow.app.data.Account
import com.financeflow.app.data.Category
import com.financeflow.app.data.CreditCard
import com.financeflow.app.data.FinanceData
import com.financeflow.app.data.Frequency
import com.financeflow.app.data.NewEntry
import com.financeflow.app.data.Recurrence
import com.financeflow.app.data.Transaction
import com.financeflow.app.data.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(private val container: AppContainer) : ViewModel() {
    private val repo = container.repository
    val session = container.session

    private val _userId = MutableStateFlow(session.userId)
    val userId: StateFlow<Long?> = _userId.asStateFlow()

    private val _hideBalance = MutableStateFlow(session.hideBalance)
    val hideBalance: StateFlow<Boolean> = _hideBalance.asStateFlow()

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    val user: StateFlow<User?> = _userId.flatMapLatest { id -> if (id == null) flowOf(null) else repo.user(id) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val data: StateFlow<FinanceData> = _userId.flatMapLatest { id ->
        if (id == null) {
            flowOf(FinanceData(loaded = true))
        } else {
            val base = combine(repo.accounts(id), repo.categories(id), repo.cards(id), repo.transactions(id)) { a, c, k, t ->
                FinanceData(accounts = a, categories = c, cards = k, transactions = t)
            }
            combine(base, repo.plans(id), repo.recurrences(id), repo.notifications(id)) { d, p, r, n ->
                d.copy(plans = p, recurrences = r, notifications = n, loaded = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, FinanceData())

    init {
        _userId.value?.let { refresh(it) }
    }

    private fun refresh(userId: Long) = viewModelScope.launch {
        repo.generateRecurrences(userId)
        val created = repo.runDueCheck(userId, session.alertDaysBefore)
        if (session.notificationsEnabled) {
            container.notifier.post(created)
            container.notifier.schedule(reminders(repo.snapshot(userId)))
        } else {
            container.notifier.schedule(emptyList())
        }
    }

    /** Lembretes futuros: N dias antes e no dia de cada vencimento/recebimento pendente. */
    private fun reminders(d: FinanceData): List<Reminder> {
        val today = Dates.todayEpoch()
        val days = session.alertDaysBefore
        val out = mutableListOf<Reminder>()
        for (p in d.payables().filter { it.date >= today }) {
            val (id, name) = when (p) {
                is Payable.Single -> "tx${p.tx.id}" to p.tx.description
                is Payable.CardBill -> "card${p.cardId}-${p.date}" to "Fatura ${p.card?.name ?: "cartão"}"
            }
            val value = Money.format(p.amount)
            out += Reminder("$id-due", "Vence hoje", "A conta de $name de $value vence hoje.", p.date, 9)
            if (p.date - days > today) out += Reminder("$id-soon", "Vencimento próximo", "A conta de $name de $value vence em $days dias.", p.date - days, 9)
        }
        d.pending.filter { it.type == TxType.INCOME && it.date >= today }.forEach {
            out += Reminder("in${it.id}", "Recebimento previsto", "${it.description}: ${Money.format(it.amount)} previsto para hoje.", it.date, 9)
        }
        return out.sortedBy { it.epochDay }.take(60)
    }

    fun toast(msg: String) {
        _messages.trySend(msg)
    }

    private fun act(msg: String? = null, block: suspend () -> Unit) = viewModelScope.launch {
        try {
            block()
            msg?.let { toast(it) }
            _userId.value?.let { id ->
                container.notifier.schedule(if (session.notificationsEnabled) reminders(repo.snapshot(id)) else emptyList())
            }
        } catch (e: Exception) {
            toast("Erro: ${e.message ?: "falha inesperada"}")
        }
    }

    private val uid: Long get() = _userId.value ?: error("Sessão expirada")

    // ---------- Sessão ----------

    fun login(email: String, password: String, onResult: (Boolean) -> Unit) = viewModelScope.launch {
        val id = repo.login(email, password)
        if (id != null) setUser(id)
        onResult(id != null)
    }

    fun register(name: String, email: String, password: String, recovery: String, onResult: (String?) -> Unit) =
        viewModelScope.launch {
            repo.register(name, email, password, recovery)
                .onSuccess { setUser(it); onResult(null) }
                .onFailure { onResult(it.message ?: "Não foi possível criar a conta") }
        }

    fun resetPassword(email: String, recovery: String, newPassword: String, onResult: (Boolean) -> Unit) =
        viewModelScope.launch { onResult(repo.resetPassword(email, recovery, newPassword)) }

    fun changePassword(current: String, newPassword: String, onResult: (Boolean) -> Unit) =
        viewModelScope.launch { onResult(repo.changePassword(uid, current, newPassword)) }

    fun updateName(name: String) = act("Perfil atualizado") { repo.updateName(uid, name) }

    private fun setUser(id: Long) {
        session.userId = id
        _userId.value = id
        refresh(id)
    }

    fun logout() {
        container.notifier.schedule(emptyList())
        session.userId = null
        _userId.value = null
    }

    fun toggleHideBalance() {
        _hideBalance.value = !_hideBalance.value
        session.hideBalance = _hideBalance.value
    }

    // ---------- Cadastros ----------

    fun saveAccount(a: Account) = act("Conta salva") { repo.saveAccount(a.copy(userId = uid)) }
    fun deleteAccount(a: Account) = viewModelScope.launch { toast(repo.deleteAccount(a)) }
    fun saveCategory(c: Category) = act("Categoria salva") { repo.saveCategory(c.copy(userId = uid)) }
    fun deleteCategory(c: Category) = act("Categoria excluída") { repo.deleteCategory(c) }
    fun saveCard(c: CreditCard) = act("Cartão salvo") { repo.saveCard(c.copy(userId = uid)) }
    fun deleteCard(c: CreditCard) = act("Cartão inativado") { repo.deleteCard(c) }

    // ---------- Lançamentos ----------

    fun newEntry(build: (Long) -> NewEntry) = build(uid)

    fun addSingle(e: NewEntry) = act("Movimentação salva") { repo.addSingle(e) }
    fun addInstallments(e: NewEntry, count: Int) = act("$count parcelas geradas") { repo.addInstallments(e, count) }
    fun addRecurrence(e: NewEntry, f: Frequency, end: Long?) = act("Recorrência criada") { repo.addRecurrence(e, f, end) }
    fun addTransfer(from: Long, to: Long, amount: Long, date: Long, notes: String?) =
        act("Transferência realizada") { repo.addTransfer(uid, from, to, amount, date, notes) }

    fun updateTx(tx: Transaction) = act("Movimentação atualizada") { repo.updateTransaction(tx) }

    fun markDone(tx: Transaction, accountId: Long?, date: Long) = act(
        if (tx.type == com.financeflow.app.data.TxType.INCOME) "Marcado como recebido" else "Marcado como pago",
    ) { repo.markDone(tx, accountId, date) }

    fun markPending(tx: Transaction) = act("Voltou para pendente") { repo.markPending(tx) }
    fun cancel(tx: Transaction) = act("Movimentação cancelada") { repo.cancel(tx) }
    fun delete(tx: Transaction) = act("Movimentação excluída") { repo.delete(tx) }
    fun deletePlan(planId: Long) = act("Parcelamento excluído") { repo.deletePlan(planId) }
    fun payInvoice(items: List<Transaction>, accountId: Long?) = act("Fatura paga") { repo.payInvoice(items, accountId) }

    fun setRecurrenceActive(r: Recurrence, active: Boolean) =
        act(if (active) "Recorrência ativada" else "Recorrência pausada") { repo.setRecurrenceActive(r, active) }

    fun deleteRecurrence(r: Recurrence) = act("Recorrência excluída") { repo.deleteRecurrence(r) }

    fun markNotificationsRead() = act { repo.markNotificationsRead(uid) }
    fun clearNotifications() = act("Notificações limpas") { repo.clearNotifications(uid) }
}
