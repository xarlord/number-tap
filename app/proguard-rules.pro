# Number Tap ProGuard Rules

# Keep all game data classes (used in GameState copies)
-keep class com.xarlord.numbertap.data.** { *; }

# Keep sealed class hierarchy for TapResult
-keep class com.xarlord.numbertap.game.TapResult { *; }
-keep class com.xarlord.numbertap.game.TapResult$* { *; }

# Keep GameEngine public API
-keep class com.xarlord.numbertap.game.GameEngine { public *; }
-keep class com.xarlord.numbertap.game.ActionLogger { public *; }

# Keep SoundManager (uses reflection-like audio APIs)
-keep class com.xarlord.numbertap.audio.SoundManager { *; }

# Keep Compose themes
-keep class com.xarlord.numbertap.ui.** { *; }
-dontwarn androidx.compose.**

# Kotlin coroutines
-dontwarn kotlinx.coroutines.**
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Standard Android
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
