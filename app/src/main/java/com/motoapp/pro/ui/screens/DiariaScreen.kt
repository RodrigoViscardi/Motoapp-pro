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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.motoapp.pro.model.Jornada
import com.motoapp.pro.model.Lancamento
import com.motoapp.pro.ui.theme.*
import com.motoapp.pro.util.CentimosFormatter
import com.motoapp.pro.util.DateUtils

@Composable
fun DiariaScreen(
    jornada: Jornada?,
    lancamentos: List<Lancamento>,
    onEndShift: () -> Unit,
    onUpdateKM: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Text("DIÁRIA", color = TextMuted, fontSize = 10.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(12.dp))

        if (jornada == null || !jornada.ativa) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Nenhum turno ativo", color = TextSecondary, fontSize = 14.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Inicie um turno na aba Lançar",
                        color = TextMuted,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            return
        }

        // Active shift summary
        val totalGanhos = lancamentos.filter { it.tipo == "ganho" }.sumOf { it.valor }
        val totalGastos = lancamentos.filter { it.tipo in listOf("gasto", "combustivel") }.sumOf { it.valor }
        val saldo = totalGanhos - totalGastos
        val kmRodados = jornada.kmFinal - jornada.kmInicial
        val eficiencia = if (kmRodados > 0) CentimosFormatter.format(saldo / kmRodados) else "0"

        // KM display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Surface)
                .padding(12.dp)
        ) {
            Column {
                Text("KM PERCORRIDOS", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                Text("$kmRodados km", color = TextPrimary, fontSize = 18.sp,
                    fontWeight = FontWeight.Bold, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                Spacer(Modifier.height(4.dp))
                Text("Eficiência: $eficiencia/km", color = TextSecondary, fontSize = 11.sp)
            }
        }

        Spacer(Modifier.height(10.dp))

        // Summary cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MiniCard("Ganhos", CentimosFormatter.format(totalGanhos), StatusGreen, Modifier.weight(1f))
            MiniCard("Gastos", CentimosFormatter.format(totalGastos), StatusRed, Modifier.weight(1f))
            MiniCard("Saldo", CentimosFormatter.format(saldo),
                if (saldo >= 0) StatusGreen else StatusRed, Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))

        // Lancamentos list
        Text("LANÇAMENTOS", color = TextMuted, fontSize = 10.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))

        if (lancamentos.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                Text("Nenhum lançamento", color = TextSecondary, fontSize = 12.sp)
            }
        } else {
            lancamentos.forEach { lanc ->
                val cor = when (lanc.tipo) {
                    "ganho" -> StatusGreen
                    "gasto" -> StatusRed
                    "combustivel" -> Warning
                    else -> TextSecondary
                }
                val label = when (lanc.tipo) {
                    "ganho" -> "Ganho"
                    "gasto" -> "Gasto"
                    "combustivel" -> "Comb."
                    else -> lanc.tipo
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(cor)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(label, color = TextPrimary, fontSize = 12.sp)
                    }
                    Text(
                        CentimosFormatter.format(lanc.valor),
                        color = cor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Fechar turno button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(StatusRed.copy(alpha = 0.15f))
                .clickable(onClick = onEndShift)
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "🔒 Fechar Turno",
                color = StatusRed,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun MiniCard(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Surface)
            .padding(10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(label, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                color = color,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }
    }
}
