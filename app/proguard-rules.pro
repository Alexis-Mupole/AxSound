# Media3 ExoPlayer & Session rules
-keep class androidx.media3.exoplayer.** { *; }
-keep class androidx.media3.session.** { *; }
-keep class androidx.media3.common.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Audio Effects
-keep class android.media.audiofx.** { *; }

# Coil
-keep class coil.** { *; }
-dontwarn coil.**
