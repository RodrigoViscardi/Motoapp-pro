package com.motoapp.pro.data

import androidx.room.*
import com.motoapp.pro.model.Transacao
import kotlinx.coroutines.flow.Flow

@Dao
interface TransacaoDao {
    @Query("SELECT * FROM transacoes ORDER BY timestamp DESC")
    fun getAllFlow(): Flow<List<Transacao>>

    @Query("SELECT * FROM transacoes ORDER BY timestamp DESC")
    suspend fun getAll(): List<Transacao>

    @Query("SELECT * FROM transacoes WHERE timestamp >= :desde ORDER BY timestamp DESC")
    suspend fun getFrom(desde: Long): List<Transacao>

    @Query("SELECT * FROM transacoes WHERE data = :data ORDER BY timestamp DESC")
    suspend fun getByDate(data: String): List<Transacao>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transacao: Transacao)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<Transacao>)

    @Query("DELETE FROM transacoes")
    suspend fun deleteAll()
}
