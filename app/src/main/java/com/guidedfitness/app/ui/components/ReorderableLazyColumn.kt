package com.guidedfitness.app.ui.components

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * Minimal drag-and-drop reordering for LazyColumn without extra dependencies.
 * Long-press an item to drag; [onMove] is called as the item crosses others.
 */
@Composable
fun <T> ReorderableLazyColumn(
    items: List<T>,
    key: (T) -> Any,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(bottom = 16.dp),
    onMove: (fromIndex: Int, toIndex: Int) -> Unit,
    itemContent: @Composable (item: T, isDragging: Boolean) -> Unit
) {
    var draggingIndex by remember { mutableIntStateOf(-1) }
    var draggingOffset by remember { mutableFloatStateOf(0f) }

    fun LazyListItemInfo.centerY(): Float = offset + size / 2f

    fun currentVisibleInfo(): List<LazyListItemInfo> = state.layoutInfo.visibleItemsInfo

    fun findTargetIndex(dragCenterY: Float): Int? {
        val visible = currentVisibleInfo()
        if (visible.isEmpty()) return null
        // Find the closest visible item center to the dragged center.
        val closest = visible.minByOrNull { info -> abs(info.centerY() - dragCenterY) } ?: return null
        return closest.index
    }

    LazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(
            items = items,
            key = { _, item -> key(item) }
        ) { index, item ->
            val isDragging = index == draggingIndex
            val itemInfo = state.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }

            val startOffset = itemInfo?.offset?.toFloat() ?: 0f
            val dragCenterY = startOffset + draggingOffset + (itemInfo?.size?.toFloat() ?: 0f) / 2f

            val dragModifier = Modifier
                .graphicsLayer {
                    if (isDragging) {
                        translationY = draggingOffset
                        // Slight elevation effect by scaling.
                        scaleX = 1.02f
                        scaleY = 1.02f
                    }
                }
                .pointerInput(items, draggingIndex) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = {
                            draggingIndex = index
                            draggingOffset = 0f
                        },
                        onDragEnd = {
                            draggingIndex = -1
                            draggingOffset = 0f
                        },
                        onDragCancel = {
                            draggingIndex = -1
                            draggingOffset = 0f
                        },
                        onDrag = { change, dragAmount ->
                            if (draggingIndex != index) return@detectDragGesturesAfterLongPress
                            change.consume()
                            draggingOffset += dragAmount.y

                            val target = findTargetIndex(dragCenterY)
                            if (target != null && target != draggingIndex) {
                                onMove(draggingIndex, target)
                                draggingIndex = target
                                // Reset offset so the item doesn't "jump" far after list mutation.
                                draggingOffset = 0f
                            }
                        }
                    )
                }

            androidx.compose.foundation.layout.Box(modifier = dragModifier) {
                itemContent(item, isDragging)
            }
        }
    }
}

