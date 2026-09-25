package com.financeflow.app.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.ConstructedBy
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import kotlinx.coroutines.Dispatchers
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User): Long

    @Update
    suspend fun update(user: User)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun get(id: Long): User?

    @Query("SELECT * FROM users WHERE id = :id")
    fun observe(id: Long): Flow<User?>
}

@Dao
interface AccountDao {
    @Insert
    suspend fun insert(account: Account): Long

    @Update
    suspend fun update(account: Account)

    @Delete
    suspend fun delete(account: Account)

    @Query("SELECT * FROM accounts WHERE userId = :userId ORDER BY active DESC, name")
    fun observe(userId: Long): Flow<List<Account>>

    @Query("SELECT * FROM accounts WHERE userId = :userId")
    suspend fun list(userId: Long): List<Account>
}

@Dao
interface CategoryDao {
    @Insert
    suspend fun insert(category: Category): Long

    @Insert
    suspend fun insertAll(categories: List<Category>)

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY type, name")
    fun observe(userId: Long): Flow<List<Category>>
}

@Dao
interface CardDao {
    @Insert
    suspend fun insert(card: CreditCard): Long

    @Update
    suspend fun update(card: CreditCard)

    @Delete
    suspend fun delete(card: CreditCard)

    @Query("SELECT * FROM credit_cards WHERE userId = :userId ORDER BY name")
    fun observe(userId: Long): Flow<List<CreditCard>>

    @Query("SELECT * FROM credit_cards WHERE userId = :userId")
    suspend fun list(userId: Long): List<CreditCard>

    @Query("SELECT * FROM credit_cards WHERE id = :id")
    suspend fun get(id: Long): CreditCard?
}

@Dao
interface TransactionDao {
    @Insert
    suspend fun insert(tx: Transaction): Long

    @Insert
    suspend fun insertAll(txs: List<Transaction>)

    @Update
    suspend fun update(tx: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM transactions WHERE planId = :planId")
    suspend fun deleteByPlan(planId: Long)

    @Query("DELETE FROM transactions WHERE recurrenceId = :recurrenceId AND status = 'PENDING' AND date >= :fromDate")
    suspend fun deletePendingByRecurrence(recurrenceId: Long, fromDate: Long)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun get(id: Long): Transaction?

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC, id DESC")
    fun observe(userId: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE userId = :userId")
    suspend fun list(userId: Long): List<Transaction>

    @Query("SELECT * FROM transactions WHERE planId = :planId ORDER BY installmentNumber")
    suspend fun byPlan(planId: Long): List<Transaction>

    @Query("SELECT COUNT(*) FROM transactions WHERE planId = :planId")
    suspend fun countByPlan(planId: Long): Int

    @Query("UPDATE transactions SET accountId = NULL WHERE accountId = :accountId AND status != 'DONE'")
    suspend fun detachPendingFromAccount(accountId: Long)

    @Query("SELECT COUNT(*) FROM transactions WHERE (accountId = :accountId OR toAccountId = :accountId) AND status = 'DONE'")
    suspend fun countDoneForAccount(accountId: Long): Int

    @Query("UPDATE transactions SET categoryId = NULL WHERE categoryId = :categoryId")
    suspend fun detachCategory(categoryId: Long)
}

@Dao
interface PlanDao {
    @Insert
    suspend fun insert(plan: InstallmentPlan): Long

    @Query("DELETE FROM installment_plans WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM installment_plans WHERE userId = :userId ORDER BY createdAt DESC")
    fun observe(userId: Long): Flow<List<InstallmentPlan>>
}

@Dao
interface RecurrenceDao {
    @Insert
    suspend fun insert(r: Recurrence): Long

    @Update
    suspend fun update(r: Recurrence)

    @Query("DELETE FROM recurrences WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM recurrences WHERE userId = :userId ORDER BY active DESC, description")
    fun observe(userId: Long): Flow<List<Recurrence>>

    @Query("SELECT * FROM recurrences WHERE userId = :userId AND active = 1")
    suspend fun listActive(userId: Long): List<Recurrence>

    @Query("SELECT * FROM recurrences WHERE id = :id")
    suspend fun get(id: Long): Recurrence?
}

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(n: AppNotification): Long

    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY date DESC LIMIT 200")
    fun observe(userId: Long): Flow<List<AppNotification>>

    @Query("UPDATE notifications SET read = 1 WHERE userId = :userId")
    suspend fun markAllRead(userId: Long)

    @Query("DELETE FROM notifications WHERE userId = :userId")
    suspend fun clear(userId: Long)
}

@Database(
    entities = [
        User::class, Account::class, Category::class, CreditCard::class, Transaction::class,
        InstallmentPlan::class, Recurrence::class, AppNotification::class,
    ],
    version = 1,
    exportSchema = false,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun users(): UserDao
    abstract fun accounts(): AccountDao
    abstract fun categories(): CategoryDao
    abstract fun cards(): CardDao
    abstract fun transactions(): TransactionDao
    abstract fun plans(): PlanDao
    abstract fun recurrences(): RecurrenceDao
    abstract fun notifications(): NotificationDao

    companion object {
        /**
         * Cada plataforma cria o builder (caminho do arquivo e driver: SQLite do sistema no Android,
         * SQLite embutido no iOS); aqui fica a configuração comum.
         */
        fun build(builder: Builder<AppDatabase>): AppDatabase =
            builder.setQueryCoroutineContext(Dispatchers.Default).build()
    }
}

// Implementação gerada pelo compilador do Room (KSP) para cada plataforma.
@Suppress("NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
