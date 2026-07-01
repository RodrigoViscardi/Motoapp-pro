package com.motoapp.pro.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "caixinhas")
data class Caixinha(
    @PrimaryKey val id: String,
    val nome: String,
    val valorTotal: Long,
    val prioridade: Int,
    val saldoAtual: Long = 0,
    val vencimento: String? = null,
    val cor: String = "#58A6FF"
) {
    val falta: Long get() = (valorTotal - saldoAtual).coerceAtLeast(0)
    val progresso: Float get() = if (valorTotal > 0) (saldoAtual.toFloat() / valorTotal).coerceIn(0f, 1f) else 0f
}
