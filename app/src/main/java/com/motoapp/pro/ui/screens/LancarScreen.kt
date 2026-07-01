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
import com.motoapp.pro.model.Transacao
import com.motoapp.pro.ui.components.*
import com.motoapp.pro.ui.theme.*

@Composable
fun LancarScreen(
    jornada: Jornada?,
    lancamentos: List<Lancamento>,
    transacoes: List<Transacao>,
    getCaixinhaNome: (String?) -> String?,
    onStartShift: () -> Unit,
    onEndShift: () -> Unit,
    onUpdateKM: (Int) -> Unit,
    onLaunch: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        Text("TURNO", color = TextMuted, fontSize = 10.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))

        ShiftSection(
            jornada = jornada,
            lancamentos = lancamentos,
            onStart = onStartShift,
            onEnd = onEndShift,
            onUpdateKM = onUpdateKM
        )

        Spacer(Modifier.height(20.dp))
        Text("LANÇAR", color = TextMuted, fontSize = 10.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            LaunchButton(
                emoji = "💵",
                label = "Ganho",
                color = StatusGreen,
                bgColor = StatusGreenBg,
                onClick = { onLaunch("ganho") },
                enabled = jornada?.ativa == true,
                modifier = Modifier.weight(1f)
            )
            LaunchButton(
                emoji = "💸",
                label = "Gasto",
                color = StatusRed,
                bgColor = StatusRedBg,
                onClick = { onLaunch("gasto") },
                enabled = jornada?.ativa == true,
                modifier = Modifier.weight(1f)
            )
            LaunchButton(
                emoji = "⛽",
                label = "Combustível",
                color = Warning,
                bgColor = Warning.copy(alpha = 0.1f),
                onClick = { onLaunch("combustivel") },
                enabled = jornada?.ativa == true,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))
        Text("ÚLTIMOS", color = TextMuted, fontSize = 10.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))

        val recentes = transacoes.filter { it.tipo in listOf("ENTRADA", "SAIDA") }.take(5)
        if (recentes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Nenhum lançamento", color = TextSecondary, fontSize = 12.sp)
            }
        } else {
            recentes.forEach { tx ->
                val destNome = if (tx.destino != null) getCaixinhaNome(tx.destino) else null
                TransactionItem(
                    transacao = tx,
                    destinoNome = destNome,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun LaunchButton(
    emoji: String,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    bgColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (enabled) Surface else SurfaceVariant)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 20.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                label,
                color = if (enabled) color else TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}
