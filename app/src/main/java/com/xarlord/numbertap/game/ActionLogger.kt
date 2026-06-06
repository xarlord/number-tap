package com.xarlord.numbertap.game

/**
 * Legacy singleton adapter. Prefer injecting ActionLoggerProvider directly.
 * This will be removed once all callers are migrated.
 */
object ActionLogger : ActionLoggerProvider by LogcatActionLogger()
