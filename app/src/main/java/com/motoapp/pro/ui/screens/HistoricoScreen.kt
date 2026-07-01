package com.motoapp.pro.ui.screens

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
import com.motoapp.pro.model.Transacao
import com.motoapp.pro.ui.components.RecentList
import com.motoapp.pro.ui.theme.*

@Composable
fun HistoricoScreen(
    transacoes: List<Transacao>,
    getCaixinhaNome: (String?) -> String?,
    onSetPeriodo: (String) -> Unit,
    periodo: String
) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(16.dp))
        Text("HISTÓRICO", color = TextMuted, fontSize = 10.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            PeriodButton("hoje", "Hoje", periodo, onSetPeriodo, Modifier.weight(1f))
            PeriodButton("semana", "Semana", periodo, onSetPeriodo, Modifier.weight(1f))
            PeriodButton("mes", "Mês", periodo, onSetPeriodo, Modifier.weight(1f))
        }

        Spacer(Modifier.height(10.dp))

        RecentList(
            transacoes = transacoes,
            getCaixinhaNome = getCaixinhaNome,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PeriodButton(
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
