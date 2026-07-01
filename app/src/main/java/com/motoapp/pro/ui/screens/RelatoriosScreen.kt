package com.motoapp.pro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
fun RelatoriosScreen(
    transacoes: List<Transacao>,
    caixinhas: List<Caixinha>,
    onSetPeriodo: (String) -> Unit,
    periodo: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Text("RELATÓRIOS", color = TextMuted, fontSize = 10.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            PeriodBtn("hoje", "Hoje", periodo, onSetPeriodo, Modifier.weight(1f))
            PeriodBtn("semana", "Semana", periodo, onSetPeriodo, Modifier.weight(1f))
            PeriodBtn("mes", "Mês", periodo, onSetPeriodo, Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))

        val filtradas = transacoes.filter { tx ->
            when (periodo) {
                "hoje" -> tx.data == DateUtils.today()
                "semana" -> tx.timestamp >= DateUtils.inicioDaSemana()
                "mes" -> tx.timestamp >= DateUtils.inicioDoMes()
                else -> true
            }
        }

        val totalGanhos = filtradas.filter { it.tipo == "ENTRADA" }.sumOf { it.valor }
        val totalGastos = filtradas.filter { it.tipo == "SAIDA" }.sumOf { it.valor }
        val totalDistrib = filtradas.filter { it.tipo == "DISTRIBUICAO" }.sumOf { it.valor }
        val saldo = totalGanhos - totalGastos - totalDistrib

        // Summary cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SummaryCard("Ganhos", CentimosFormatter.format(totalGanhos), StatusGreen, Modifier.weight(1f))
            SummaryCard("Gastos", CentimosFormatter.format(totalGastos), StatusRed, Modifier.weight(1f))
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SummaryCard("Distrib.", CentimosFormatter.format(totalDistrib), Primary, Modifier.weight(1f))
            SummaryCard("Saldo", CentimosFormatter.format(saldo),
                if (saldo >= 0) StatusGreen else StatusRed, Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))
        Text("CAIXINHAS", color = TextMuted, fontSize = 10.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))

        if (caixinhas.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                Text("Nenhuma caixinha", color = TextSecondary)
            }
        } else {
            caixinhas.sortedBy { it.prioridade }.forEach { cx ->
                val cor = androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(cx.cor))
                val progress = cx.progresso
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(cor)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(cx.nome, color = TextPrimary, fontSize = 12.sp)
                    }
                    Text(
                        "${CentimosFormatter.format(cx.saldoAtual)} / ${CentimosFormatter.format(cx.valorTotal)}",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun PeriodBtn(
    value: String,
    label: String,
    current: String,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isActive = value == current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) SurfaceVariant else Surface)
            .clickable { onClick(value) }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (isActive) TextPrimary else TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun SummaryCard(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Surface)
            .padding(12.dp)
    ) {
        Column {
            Text(label, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                color = color,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }
    }
}
