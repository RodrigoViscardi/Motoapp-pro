package com.motoapp.pro.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "jornadas")
data class Jornada(
    @PrimaryKey val id: String,
    val data: String,
    val kmInicial: Int = 0,
    val kmFinal: Int = 0,
    val inicio: Long,
    val fim: Long? = null,
    val lancamentos: String = "[]"
) {
    val kmTotal: Int get() = (kmFinal - kmInicial).coerceAtLeast(0)
    val ativa: Boolean get() = fim == null

    fun getGanhos(lancamentosDecoded: List<Lancamento>): Long =
        lancamentosDecoded.filter { it.tipo == "ganho" }.sumOf { it.valor }

    fun getGastos(lancamentosDecoded: List<Lancamento>): Long =
        lancamentosDecoded.filter { it.tipo == "gasto" || it.tipo == "combustivel" }.sumOf { it.valor }

    fun getLucro(lancamentosDecoded: List<Lancamento>): Long =
        getGanhos(lancamentosDecoded) - getGastos(lancamentosDecoded)
}
