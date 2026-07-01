package com.motoapp.pro.data

import androidx.room.*
import com.motoapp.pro.model.Caixinha
import kotlinx.coroutines.flow.Flow

@Dao
interface CaixinhaDao {
    @Query("SELECT * FROM caixinhas ORDER BY prioridade ASC")
    fun getAllFlow(): Flow<List<Caixinha>>

    @Query("SELECT * FROM caixinhas ORDER BY prioridade ASC")
    suspend fun getAll(): List<Caixinha>

    @Query("SELECT * FROM caixinhas WHERE id = :id")
    suspend fun getById(id: String): Caixinha?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<Caixinha>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(caixinha: Caixinha)

    @Update
    suspend fun update(caixinha: Caixinha)

    @Delete
    suspend fun delete(caixinha: Caixinha)

    @Query("DELETE FROM caixinhas WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE caixinhas SET prioridade = :prioridade WHERE id = :id")
    suspend fun updatePrioridade(id: String, prioridade: Int)

    @Query("UPDATE caixinhas SET saldoAtual = :saldo WHERE id = :id")
    suspend fun updateSaldo(id: String, saldo: Long)

    @Query("SELECT COUNT(*) FROM caixinhas")
    suspend fun count(): Int

    @Query("DELETE FROM caixinhas")
    suspend fun deleteAll()
}
