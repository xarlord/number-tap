package com.xarlord.numbertap.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.xarlord.numbertap.data.GameTheme
import com.xarlord.numbertap.data.ThemeColors
import com.xarlord.numbertap.data.ThemeStyle
import com.xarlord.numbertap.data.Tile

@Composable
internal fun GridContainer(
    tiles: List<List<Tile>>,
    targetNumber: Int,
    shakeOffsetPx: Pair<Float, Float>,
    onTileTap: (row: Int, col: Int) -> Unit,
    isTutorial: Boolean,
    isHardMode: Boolean,
    hiddenTileIds: Set<Int>,
    animatingTileId: Int?,
    theme: GameTheme,
    colors: ThemeColors,
    style: ThemeStyle
) {
    val tileSize = if (isTutorial) 88.dp else if (tiles.size <= 4) 78.dp else 64.dp

    Column(
        modifier = Modifier.offset { IntOffset(shakeOffsetPx.first.toInt(), shakeOffsetPx.second.toInt()) },
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        tiles.forEachIndexed { row, rowTiles ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                rowTiles.forEachIndexed { col, tile ->
                    ThemedTile(
                        tile = tile,
                        isTarget = tile.currentValue == targetNumber,
                        isTutorial = isTutorial,
                        isHardMode = isHardMode,
                        isHidden = tile.id in hiddenTileIds && tile.currentValue != targetNumber,
                        tileSize = tileSize,
                        theme = theme,
                        colors = colors,
                        style = style,
                        onClick = { onTileTap(row, col) },
                        isAnimating = tile.id == animatingTileId
                    )
                }
            }
        }
    }
}
