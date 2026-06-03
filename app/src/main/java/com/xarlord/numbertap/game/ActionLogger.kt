package com.xarlord.numbertap.game

import android.util.Log
import com.xarlord.numbertap.data.ActionType
import com.xarlord.numbertap.data.GameAction
import org.json.JSONObject

object ActionLogger {
    private const val TAG = "NumberTap:Action"

    fun log(action: GameAction) {
        val json = JSONObject().apply {
            put("ts", action.timestamp)
            put("type", action.type.name)
            put("tile", "[${action.tileRow},${action.tileCol}]")
            put("value", action.tileValue)
            put("target", action.targetValue)
            put("score", action.score)
            put("time", action.timeRemaining)
        }
        Log.d(TAG, json.toString())
    }

    fun logGameStart(score: Int, highScore: Int) {
        log(GameAction(
            timestamp = System.currentTimeMillis(),
            type = ActionType.GAME_START,
            score = score,
            timeRemaining = 30.0
        ))
    }

    fun logTap(row: Int, col: Int, value: Int, target: Int, correct: Boolean, score: Int, time: Double) {
        log(GameAction(
            timestamp = System.currentTimeMillis(),
            type = if (correct) ActionType.TAP_CORRECT else ActionType.TAP_WRONG,
            tileRow = row,
            tileCol = col,
            tileValue = value,
            targetValue = target,
            score = score,
            timeRemaining = time
        ))
    }

    fun logGameOver(score: Int, highScore: Int, time: Double) {
        log(GameAction(
            timestamp = System.currentTimeMillis(),
            type = ActionType.GAME_OVER,
            score = score,
            timeRemaining = time
        ))
    }

    fun logGridTransition(score: Int, newSize: Int) {
        log(GameAction(
            timestamp = System.currentTimeMillis(),
            type = ActionType.GRID_TRANSITION,
            score = score
        ))
    }
}
