package com.motoapp.pro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.motoapp.pro.model.Caixinha
import com.motoapp.pro.model.Transacao
import com.motoapp.pro.ui.theme.*
import com.motoapp.pro.util.CentimosFormatter
import com.motoapp.pro.util.DateUtils
import com.motoapp.pro.util.WaterfallEngine

@Composable
fun UrgenciaScreen(
    caixinhas: List<Caixinha>,
    transacoes: List<Transacao>,
    carteira: Long
) {
    val hoje = DateUtils.today()
    val gastos30d = transacoes
        .filter { it.tipo == "SAIDA" && it.timestamp >= DateUtils.inicioDoDia() - 30 * 86400000L }
        .sumOf { it.valor }
    val gastoDiarioMedio = if (gastos30d > 0) gastos30d / 30 else 0
    val diasSobrevivencia = if (gastoDiarioMedio > 0) carteira / gastoDiarioMedio else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Text("URGÊNCIA", color = TextMuted, fontSize = 10.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(12.dp))

        // Survival days card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    when {
                        diasSobrevivencia <= 7 -> StatusRed.copy(alpha = 0.2f)
                        diasSobrevivencia <= 30 -> Warning.copy(alpha = 0.2f)
                        else -> StatusGreen.copy(alpha = 0.2f)
                    }
                )
                .padding(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    if (diasSobrevivencia <= 0) "CRÍTICO" else "Sobrevivência",
                    color = when {
                        diasSobrevivencia <= 7 -> StatusRed
                        diasSobrevivencia <= 30 -> Warning
                        else -> StatusGreen
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    if (diasSobrevivencia <= 0) "0" else "$diasSobrevivencia",
                    color = TextPrimary,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
                Text("dias", color = TextSecondary, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Gasto médio: ${CentimosFormatter.format(gastoDiarioMedio)}/dia",
                    color = TextMuted,
                    fontSize = 11.sp
                )
                Text(
                    "Na carteira: ${CentimosFormatter.format(carteira)}",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Alertas por caixinha
        Text("ALERTAS", color = TextMuted, fontSize = 10.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))

        if (caixinhas.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                Text("Nenhuma caixinha", color = TextSecondary)
            }
        } else {
            caixinhas.sortedBy { it.prioridade }.forEach { cx ->
                val status = WaterfallEngine.obterStatus(cx)
                val vencimento = cx.vencimento
                val diasRestantes = if (vencimento != null) DateUtils.daysUntil(vencimento) else null

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Surface)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(cx.nome, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text(
                                "${CentimosFormatter.format(cx.saldoAtual)} / ${CentimosFormatter.format(cx.valorTotal)}",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            if (diasRestantes != null) {
                                Text(
                                    if (diasRestantes <= 0) "⚠️ Vencido!"
                                    else "Vence em $diasRestantes dia${if (diasRestantes != 1) "s" else ""}",
                                    color = if (diasRestantes <= 3) StatusRed else TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}
