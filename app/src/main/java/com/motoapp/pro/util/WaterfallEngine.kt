package com.motoapp.pro.util

import com.motoapp.pro.model.Caixinha

data class Alocacao(
    val caixinhaId: String,
    val valor: Long,
    val completa: Boolean
)

data class WaterfallResult(
    val alocacoes: List<Alocacao>,
    val saldoRestante: Long,
    val lucroExtra: Long
)

object WaterfallEngine {
    fun distribuir(
        carteira: Long,
        caixinhas: List<Caixinha>,
        aplicarAlocacao: (Caixinha, Long) -> Unit
    ): WaterfallResult {
        if (carteira <= 0 || caixinhas.isEmpty()) {
            return WaterfallResult(emptyList(), carteira, 0)
        }

        val alocacoes = mutableListOf<Alocacao>()
        var saldo = carteira
        val ordenadas = caixinhas.sortedBy { it.prioridade }

        for (cx in ordenadas) {
            if (saldo <= 0) break
            if (cx.valorTotal <= 0) continue
            val falta = cx.valorTotal - cx.saldoAtual
            if (falta <= 0) continue

            val valor = saldo.coerceAtMost(falta)
            aplicarAlocacao(cx, valor)
            saldo -= valor
            alocacoes.add(Alocacao(cx.id, valor, valor >= falta))
        }

        return WaterfallResult(
            alocacoes = alocacoes,
            saldoRestante = saldo,
            lucroExtra = if (saldo > 0) saldo else 0
        )
    }

    fun calcularMetaDiaria(caixinha: Caixinha): Long {
        if (caixinha.valorTotal <= 0 || caixinha.vencimento == null) return 0
        val falta = caixinha.falta
        if (falta <= 0) return 0
        val dias = DateUtils.daysUntil(caixinha.vencimento)
        if (dias <= 0) return falta
        return (falta + dias - 1) / dias
    }

    fun obterStatus(caixinha: Caixinha): StatusInfo {
        if (caixinha.valorTotal <= 0) return StatusInfo("green", "OK", "Sem meta")
        if (caixinha.falta <= 0) return StatusInfo("green", "OK", "Pago!")

        val dias = DateUtils.daysUntil(caixinha.vencimento)
        if (dias < 0) return StatusInfo("red", "Atrasada", "${-dias}d")
        if (dias == 0) return StatusInfo("red", "Hoje!", "Vence")

        val pago = caixinha.progresso
        val esperado = 1.0f - (dias / 30.0f)
        return when {
            pago >= esperado -> StatusInfo("green", "Em dia", "${dias}d")
            pago >= esperado * 0.7f -> StatusInfo("yellow", "Atenção", "${dias}d")
            else -> StatusInfo("red", "Crítico", "${dias}d")
        }
    }
}

data class StatusInfo(
    val cor: String,
    val label: String,
    val texto: String
)
