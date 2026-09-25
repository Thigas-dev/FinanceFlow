package com.financeflow.app.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.financeflow.app.util.nowMillis

enum class TxType { INCOME, EXPENSE, TRANSFER }

/** DONE = pago (despesa) / recebido (receita) / efetivada (transferência). Atrasado é derivado (RN08). */
enum class TxStatus { PENDING, DONE, CANCELLED }

enum class PaymentMethod(val label: String) {
    PIX("Pix"),
    DEBIT("Débito"),
    CREDIT("Cartão de crédito"),
    CASH("Dinheiro"),
    BOLETO("Boleto"),
    TRANSFER("Transferência"),
    OTHER("Outro"),
}

enum class AccountType(val label: String) {
    CHECKING("Conta corrente"),
    DIGITAL("Conta digital"),
    SAVINGS("Poupança"),
    WALLET("Carteira"),
    CASH("Dinheiro físico"),
    OTHER("Outro"),
}

enum class Frequency(val label: String) {
    WEEKLY("Semanal"),
    BIWEEKLY("Quinzenal"),
    MONTHLY("Mensal"),
    YEARLY("Anual"),
}

@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val passwordHash: String,
    val salt: String,
    val recoveryHash: String,
    val recoverySalt: String,
    val createdAt: Long = nowMillis(),
)

@Entity(tableName = "accounts", indices = [Index("userId")])
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val name: String,
    val type: AccountType,
    val institution: String = "",
    /** Centavos. */
    val initialBalance: Long = 0,
    val color: Long = 0xFF10B981,
    val active: Boolean = true,
)

@Entity(tableName = "categories", indices = [Index("userId")])
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val name: String,
    /** INCOME ou EXPENSE. */
    val type: TxType,
    val icon: String = "more",
    val isDefault: Boolean = false,
)

@Entity(tableName = "credit_cards", indices = [Index("userId")])
data class CreditCard(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val name: String,
    val bank: String = "",
    /** Centavos. */
    val limit: Long,
    val closingDay: Int,
    val dueDay: Int,
    /** Conta usada para pagar a fatura. */
    val accountId: Long?,
    val color: Long = 0xFF8B5CF6,
    val active: Boolean = true,
)

/**
 * Representa tanto movimentações efetivadas (status DONE) quanto compromissos futuros (PENDING).
 * Parcelas são transações ligadas a um [InstallmentPlan]; lançamentos recorrentes a uma [Recurrence].
 */
@Entity(
    tableName = "transactions",
    indices = [Index("userId"), Index("planId"), Index("recurrenceId"), Index("date")],
)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val type: TxType,
    val description: String,
    /** Centavos, sempre positivo. */
    val amount: Long,
    /** Data da movimentação / vencimento (epochDay). */
    val date: Long,
    val status: TxStatus,
    /** Data em que foi paga/recebida (epochDay). */
    val doneDate: Long? = null,
    val categoryId: Long? = null,
    /** Conta de origem (despesa/transferência) ou destino (receita). */
    val accountId: Long? = null,
    /** Conta de destino em transferências. */
    val toAccountId: Long? = null,
    val cardId: Long? = null,
    val paymentMethod: PaymentMethod? = null,
    /** Pessoa/empresa (valores a receber, empréstimos). */
    val person: String? = null,
    val notes: String? = null,
    val planId: Long? = null,
    val installmentNumber: Int? = null,
    val totalInstallments: Int? = null,
    val recurrenceId: Long? = null,
    val createdAt: Long = nowMillis(),
)

@Entity(tableName = "installment_plans", indices = [Index("userId")])
data class InstallmentPlan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val type: TxType,
    val description: String,
    val totalAmount: Long,
    val count: Int,
    val firstDueDate: Long,
    val categoryId: Long?,
    val accountId: Long?,
    val cardId: Long?,
    val createdAt: Long = nowMillis(),
)

@Entity(tableName = "recurrences", indices = [Index("userId")])
data class Recurrence(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val type: TxType,
    val description: String,
    val amount: Long,
    val frequency: Frequency,
    val startDate: Long,
    val endDate: Long? = null,
    val categoryId: Long?,
    val accountId: Long?,
    val cardId: Long? = null,
    val paymentMethod: PaymentMethod? = null,
    val active: Boolean = true,
    /** Última ocorrência já gerada (epochDay). */
    val lastGenerated: Long? = null,
)

@Entity(
    tableName = "notifications",
    indices = [Index(value = ["userId", "key"], unique = true)],
)
data class AppNotification(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    /** Chave de deduplicação: evita repetir o mesmo alerta. */
    val key: String,
    val type: String,
    val title: String,
    val message: String,
    val date: Long = nowMillis(),
    val read: Boolean = false,
)
