package com.motoapp.pro.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.motoapp.pro.model.Caixinha
import com.motoapp.pro.ui.theme.*
import com.motoapp.pro.util.CentimosFormatter
import com.motoapp.pro.util.WaterfallEngine

@Composable
fun CaixinhaCard(
    caixinha: Caixinha,
    onClick: () -> Unit = {},
    onTransferClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val status = remember(caixinha) { WaterfallEngine.obterStatus(caixinha) }
    val metaDiaria = remember(caixinha) { WaterfallEngine.calcularMetaDiaria(caixinha) }
    val progress by animateFloatAsState(
        targetValue = caixinha.progresso,
        label = "progress"
    )

    val corBarra = Color(android.graphics.Color.parseColor(caixinha.cor))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .clickable(onClick = onClick)
            .padding(start = 14.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)
    ) {
        // Left color bar
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .align(Alignment.CenterStart)
                .offset(x = (-14).dp)
                .clip(RoundedCornerShape(2.dp))
                .background(corBarra)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        caixinha.nome,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "P${caixinha.prioridade}",
                        fontSize = 10.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    "${CentimosFormatter.format(caixinha.saldoAtual)} / ${CentimosFormatter.format(caixinha.valorTotal)}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                StatusBadge(status.cor, status.label)
                Spacer(Modifier.height(2.dp))
                Text(
                    "${CentimosFormatter.format(metaDiaria)}/dia",
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }

        // Progress bar
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Border)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(RoundedCornerShape(2.dp))
                    .background(corBarra)
            )
        }

        // Bottom row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                status.texto,
                fontSize = 10.sp,
                color = TextSecondary
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(SurfaceVariant)
                    .clickable(onClick = onTransferClick)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text("↔", fontSize = 11.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
fun StatusBadge(cor: String, label: String) {
    val bgColor = when (cor) {
        "green" -> StatusGreenBg
        "yellow" -> StatusYellowBg
        else -> StatusRedBg
    }
    val textColor = when (cor) {
        "green" -> StatusGreen
        "yellow" -> StatusYellow
        else -> StatusRed
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            label,
            color = textColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp
        )
    }
}
