package com.xarlord.numbertap.data

enum class TileState { ACTIVE, TAPPED_CORRECT, TAPPED_WRONG }

data class Tile(
    val id: Int,
    val currentValue: Int,
    val state: TileState = TileState.ACTIVE
)

/** Returns a color for tile based on value range for visual distinction */
val Tile.valueColorHex: Long
    get() = when {
        currentValue <= 10 -> 0xFFE2E8F0 // Cool white
        currentValue <= 20 -> 0xFF93C5FD // Blue
        currentValue <= 30 -> 0xFFA78BFA // Purple
        currentValue <= 40 -> 0xFFF472B6 // Pink
        else -> 0xFFFCA5A5 // Warm red
    }
