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
    theme: GameTheme,
    colors: ThemeColors,
    style: ThemeStyle
) {
    // #199: Dynamic tile sizes for 4x4, 5x5, and 6x6 grids
    val tileSize = when {
        isTutorial -> 88.dp
        tiles.size <= 4 -> 78.dp
        tiles.size == 5 -> 64.dp
        else -> 52.dp  // 6x6 grid
    }

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
                        onClick = { onTileTap(row, col) }
                    )
                }
            }
        }
    }
}
