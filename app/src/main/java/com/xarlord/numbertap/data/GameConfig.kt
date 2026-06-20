package com.xarlord.numbertap.data

/**
 * Centralised game-play constants. All magic numbers live here so that
 * GameEngine, ActionLogger, and tests reference a single source of truth.
 */
object GameConfig {
    /** Seconds a normal (non-tutorial) game starts with. */
    const val INITIAL_TIME_SECONDS = 30.0

    /** Seconds added to the clock when a player uses the revive action. */
    const val REVIVE_BONUS_SECONDS = 5.0

    /** Milliseconds within which two consecutive correct taps count as a combo. */
    const val COMBO_WINDOW_MS: Long = 500

    /** Milliseconds a floating "+Xs" text stays visible before fading. */
    const val FLOATING_TEXT_DURATION_MS: Long = 800

    /** Revive is eligible when current score >= highScore * this factor. */
    const val REVIVE_ELIGIBILITY_THRESHOLD = 0.9

    /** Time given to the player during the tutorial (effectively unlimited). */
    const val TUTORIAL_TIME_SECONDS = 999.0

    /** Coin cost to purchase a revive (5 extra seconds after game over). */
    const val COIN_COST_FOR_REVIVE = 50

    /** Duration of the tile flip animation when a correct number is tapped (#207). */
    const val TILE_FLIP_DURATION_MS = 300
}
