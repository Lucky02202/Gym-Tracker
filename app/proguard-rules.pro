# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Keep entity/data classes used by Room + reflection-based serialization for backup
-keep class com.gymtracker.app.data.entity.** { *; }
