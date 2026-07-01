package com.motoapp.pro.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transacoes")
data class Transacao(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val tipo: String,
    val valor: Long,
    val origem: String? = null,
    val destino: String? = null,
    val nota: String? = null,
    val data: String
) {
    companion object {
        const val ENTRADA = "ENTRADA"
        const val SAIDA = "SAIDA"
        const val DISTRIB = "DISTRIB"
        const val TRANSF = "TRANSF"
    }
}
