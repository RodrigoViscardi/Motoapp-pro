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
import com.motoapp.pro.ui.components.*
import com.motoapp.pro.ui.theme.*
import com.motoapp.pro.util.CentimosFormatter
import com.motoapp.pro.util.DateUtils
import com.motoapp.pro.util.WaterfallEngine

@Composable
fun PainelScreen(
    carteira: Long,
    caixinhas: List<Caixinha>,
    transacoes: List<Transacao>,
    jornadaAtiva: Boolean,
    onDistribuir: () -> Unit,
    onCaixinhaClick: (String) -> Unit,
    onAddIncome: () -> Unit
) {
    val hoje = DateUtils.today()
    val txHoje = transacoes.filter { it.data == hoje }
    val ganhos = txHoje.filter { it.tipo == "ENTRADA" }.sumOf { it.valor }
    val gastos = txHoje.filter { it.tipo == "SAIDA" }.sumOf { it.valor }
    val lucro = ganhos - gastos
    val kmHoje = 0 // simplified; actual KM from jornada

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        // Stats
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            StatCard("Ganhos", CentimosFormatter.format(ganhos), StatusGreen, Modifier.weight(1f))
            StatCard("Gastos", CentimosFormatter.format(gastos), StatusRed, Modifier.weight(1f))
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            StatCard("Lucro", CentimosFormatter.format(lucro),
                if (lucro >= 0) StatusGreen else StatusRed, Modifier.weight(1f))
            StatCard("R$/KM", CentimosFormatter.format(if (kmHoje > 0) lucro / kmHoje else 0),
                Warning, Modifier.weight(1f))
        }

        // Status das caixinhas
        Spacer(Modifier.height(16.dp))
        Text("STATUS", color = TextMuted, fontSize = 10.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))

        if (caixinhas.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Nenhuma caixinha", color = TextSecondary)
            }
        } else {
            caixinhas.sortedBy { it.prioridade }.forEach { cx ->
                val status = WaterfallEngine.obterStatus(cx)
                val meta = WaterfallEngine.calcularMetaDiaria(cx)
                val progress = cx.progresso
                val cor = androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(cx.cor))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Surface)
                        .clickable { onCaixinhaClick(cx.id) }
                        .padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 10.dp)
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
                                color = TextSecondary, fontSize = 11.sp
                            )
                        }
                        StatusBadge(status.cor, status.label)
                    }
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Border)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .clip(RoundedCornerShape(2.dp))
                                .background(cor)
                        )
                    }
                }
            }
        }

        // Botão distribuir
        Spacer(Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .then(
                    if (carteira > 0) Modifier.background(
                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                            listOf(Success, Success.copy(alpha = 0.8f))
                        )
                    ) else Modifier.background(SurfaceVariant)
                )
                .clickable(enabled = carteira > 0, onClick = onDistribuir)
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "⚡ Distribuir Cascata",
                color = if (carteira > 0) Background else TextMuted,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun StatCard(
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
            Text(value, color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
        }
    }
}
