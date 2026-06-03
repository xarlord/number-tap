# Number Tap ProGuard Rules

# Keep game data classes
-keepclassmembers class com.xarlord.numbertap.data.** { *; }

# Keep sealed class hierarchy for TapResult
-keep class com.xarlord.numbertap.game.TapResult { *; }
-keep class com.xarlord.numbertap.game.TapResult$* { *; }

# SoundPool
-keep class android.media.SoundPool { *; }

# Standard Android
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
