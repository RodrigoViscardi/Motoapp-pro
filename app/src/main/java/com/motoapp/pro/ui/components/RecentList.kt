package com.motoapp.pro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.motoapp.pro.model.Transacao
import com.motoapp.pro.ui.theme.*
import com.motoapp.pro.util.CentimosFormatter

@Composable
fun TransactionItem(
    transacao: Transacao,
    destinoNome: String? = null,
    modifier: Modifier = Modifier
) {
    val icon = when (transacao.tipo) {
        "ENTRADA" -> "💰"
        "SAIDA" -> "💸"
        "DISTRIB" -> "⚡"
        "TRANSF" -> "↔"
        else -> "📄"
    }

    val isIncome = transacao.tipo == "ENTRADA"
    val bgColor = when (transacao.tipo) {
        "ENTRADA" -> IncomeBg
        "SAIDA" -> ExpenseBg
        "DISTRIB" -> DistribBg
        "TRANSF" -> TransferBg
        else -> SurfaceVariant
    }
    val valorColor = if (isIncome) IncomeColor else ExpenseColor

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Surface)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 14.sp)
        }

        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                transacao.nota ?: transacao.tipo,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                destinoNome ?: transacao.origem ?: java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale("pt", "BR")).format(java.util.Date(transacao.timestamp)),
                color = TextSecondary,
                fontSize = 10.sp
            )
        }

        Text(
            text = "${if (isIncome) "+" else "-"}${CentimosFormatter.format(transacao.valor)}",
            color = valorColor,
            fontWeight = FontWeight.Bold,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            fontSize = 12.sp
        )
    }
}

@Composable
fun RecentList(
    transacoes: List<Transacao>,
    getCaixinhaNome: (String?) -> String?,
    modifier: Modifier = Modifier,
    emptyText: String = "Nenhuma transação"
) {
    if (transacoes.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(emptyText, color = TextSecondary, fontSize = 12.sp)
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(transacoes.take(100), key = { it.id }) { tx ->
            val destNome = if (tx.destino != null) getCaixinhaNome(tx.destino) else null
            TransactionItem(
                transacao = tx,
                destinoNome = destNome
            )
        }
    }
}
