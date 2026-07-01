package com.motoapp.pro.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.motoapp.pro.model.Jornada
import com.motoapp.pro.model.Lancamento
import com.motoapp.pro.ui.theme.*
import com.motoapp.pro.util.CentimosFormatter

@Composable
fun ShiftSection(
    jornada: Jornada?,
    lancamentos: List<Lancamento>,
    onStart: () -> Unit,
    onEnd: () -> Unit,
    onUpdateKM: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (jornada == null || jornada.fim != null) {
        // Inactive shift
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Surface)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Nenhum turno ativo", color = TextSecondary, fontSize = 13.sp)
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Primary)
                        .clickable(onClick = onStart)
                        .padding(horizontal = 24.dp, vertical = 10.dp)
                ) {
                    Text("▶ Iniciar", color = Background, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }
        return
    }

    val ganhos = jornada.getGanhos(lancamentos)
    val gastos = jornada.getGastos(lancamentos)
    val km = jornada.kmTotal

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Surface)
                .padding(14.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("TURNO", color = TextMuted, fontSize = 9.sp, letterSpacing = 1.sp)
                        ElapsedTimer(startTime = jornada.inicio)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(StatusRedBg)
                            .clickable(onClick = onEnd)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("⏹", fontSize = 14.sp)
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem("Ganhos", CentimosFormatter.format(ganhos), StatusGreen)
                    StatItem("Gastos", CentimosFormatter.format(gastos), StatusRed)
                    StatItem("KM", "$km km", TextPrimary)
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(2f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceVariant)
                            .clickable { }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text("KM: ${jornada.kmFinal}", color = TextSecondary, fontSize = 12.sp)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Primary)
                            .clickable { }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("OK", color = Background, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ElapsedTimer(startTime: Long) {
    var elapsed by remember { mutableStateOf(0L) }

    LaunchedEffect(startTime) {
        while (true) {
            elapsed = (System.currentTimeMillis() - startTime) / 1000
            kotlinx.coroutines.delay(1000)
        }
    }

    val horas = (elapsed / 3600).toInt()
    val minutos = ((elapsed % 3600) / 60).toInt()
    val segundos = (elapsed % 60).toInt()

    Text(
        text = "${horas.toString().padStart(2, '0')}:${minutos.toString().padStart(2, '0')}:${segundos.toString().padStart(2, '0')}",
        color = Success,
        fontWeight = FontWeight.Bold,
        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
        fontSize = 18.sp
    )
}

@Composable
private fun StatItem(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TextMuted, fontSize = 9.sp)
        Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 13.sp,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
    }
}
