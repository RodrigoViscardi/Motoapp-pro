package com.motoapp.pro.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.motoapp.pro.model.Caixinha
import com.motoapp.pro.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun DraggableCaixinhaList(
    caixinhas: List<Caixinha>,
    onReorder: (Int, Int) -> Unit,
    onClick: (String) -> Unit,
    onTransfer: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (caixinhas.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            Text("Nenhuma caixinha", color = TextSecondary)
        }
        return
    }

    var draggedItemIndex by remember { mutableStateOf(-1) }
    var dragOffset by remember { mutableStateOf(0f) }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        state = rememberLazyListState()
    ) {
        itemsIndexed(caixinhas, key = { _, cx -> cx.id }) { index, caixinha ->
            val elevation by animateDpAsState(
                targetValue = if (index == draggedItemIndex) 8.dp else 0.dp,
                label = "elevation"
            )

            Box(
                modifier = Modifier
                    .zIndex(if (index == draggedItemIndex) 1f else 0f)
                    .offset { IntOffset(0, if (index == draggedItemIndex) dragOffset.roundToInt() else 0) }
                    .then(
                        if (index == draggedItemIndex) Modifier.shadow(elevation, RoundedCornerShape(12.dp))
                        else Modifier
                    )
                    .pointerInput(index) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                draggedItemIndex = index
                                dragOffset = 0f
                            },
                            onDragEnd = {
                                if (draggedItemIndex >= 0) {
                                    val targetIndex = (index + (dragOffset / 80.dp.toPx()).roundToInt())
                                        .coerceIn(0, caixinhas.size - 1)
                                    if (targetIndex != draggedItemIndex) {
                                        onReorder(draggedItemIndex, targetIndex)
                                    }
                                }
                                draggedItemIndex = -1
                                dragOffset = 0f
                            },
                            onDragCancel = {
                                draggedItemIndex = -1
                                dragOffset = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragOffset += dragAmount.y
                            }
                        )
                    }
            ) {
                CaixinhaCard(
                    caixinha = caixinha,
                    onClick = { onClick(caixinha.id) },
                    onTransferClick = { onTransfer(caixinha.id) }
                )
            }
        }
    }
}
