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
import com.motoapp.pro.model.Caixinha
import com.motoapp.pro.ui.components.DraggableCaixinhaList
import com.motoapp.pro.ui.theme.*

@Composable
fun CaixasScreen(
    caixinhas: List<Caixinha>,
    onReorder: (Int, Int) -> Unit,
    onClick: (String) -> Unit,
    onAdd: () -> Unit,
    onTransfer: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "CAIXINHAS",
                    color = TextMuted,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Primary)
                        .clickable(onClick = onAdd)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("+ Nova", color = Background, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(10.dp))

            DraggableCaixinhaList(
                caixinhas = caixinhas,
                onReorder = onReorder,
                onClick = onClick,
                onTransfer = onTransfer,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
