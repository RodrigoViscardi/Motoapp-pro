package com.motoapp.pro.data

import androidx.room.*
import com.motoapp.pro.model.Config

@Dao
interface ConfigDao {
    @Query("SELECT * FROM config WHERE chave = :chave")
    suspend fun get(chave: String): Config?

    @Query("SELECT valor FROM config WHERE chave = :chave")
    suspend fun getValor(chave: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(config: Config)

    @Query("DELETE FROM config WHERE chave = :chave")
    suspend fun delete(chave: String)

    @Query("SELECT * FROM config")
    suspend fun getAll(): List<Config>

    @Query("DELETE FROM config")
    suspend fun deleteAll()
}
