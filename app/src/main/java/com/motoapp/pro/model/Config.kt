package com.motoapp.pro.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "config")
data class Config(
    @PrimaryKey val chave: String,
    val valor: String
) {
    companion object {
        const val CARREIRA = "carteira"
        const val EXTRA = "extra"
        const val CUSTO_MENSAL = "custoMensal"
        const val EMERGENCIA_ID = "emgId"
        const val LOADED = "loaded"
    }
}
