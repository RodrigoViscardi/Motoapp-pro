package com.motoapp.pro.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import com.motoapp.pro.ui.theme.*
import com.motoapp.pro.util.CentimosFormatter

@Composable
fun WalletCard(
    carteira: Long,
    extra: Long,
    caixinhaCount: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                    listOf(GradientStart, GradientEnd)
                )
            )
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = "CARTEIRA",
                color = TextPrimary.copy(alpha = 0.7f),
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = CentimosFormatter.format(carteira),
                style = MoneyTextStyle,
                color = TextPrimary
            )
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column {
                    Text("Extra", color = TextPrimary.copy(alpha = 0.6f), fontSize = 10.sp)
                    Text(
                        CentimosFormatter.format(extra),
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
                Column {
                    Text("Caixas", color = TextPrimary.copy(alpha = 0.6f), fontSize = 10.sp)
                    Text(
                        "$caixinhaCount",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
