package com.xarlord.numbertap.data

enum class TileState { ACTIVE, TAPPED_CORRECT, TAPPED_WRONG }

data class Tile(
    val id: Int,
    val currentValue: Int,
    val state: TileState = TileState.ACTIVE,
    val isTarget: Boolean = false
)
