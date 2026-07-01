package com.motoapp.pro.data

import androidx.room.*
import com.motoapp.pro.model.Jornada
import kotlinx.coroutines.flow.Flow

@Dao
interface JornadaDao {
    @Query("SELECT * FROM jornadas ORDER BY inicio DESC")
    fun getAllFlow(): Flow<List<Jornada>>

    @Query("SELECT * FROM jornadas WHERE fim IS NULL LIMIT 1")
    suspend fun getAtiva(): Jornada?

    @Query("SELECT * FROM jornadas WHERE id = :id")
    suspend fun getById(id: String): Jornada?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(jornada: Jornada)

    @Update
    suspend fun update(jornada: Jornada)

    @Query("UPDATE jornadas SET kmFinal = :km, fim = :fim WHERE id = :id")
    suspend fun finalizar(id: String, km: Int, fim: Long)

    @Query("DELETE FROM jornadas WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM jornadas")
    suspend fun deleteAll()
}
