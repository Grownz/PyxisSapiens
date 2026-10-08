# PyxisSapiens R8 / ProGuard rules.
# Room, Hilt, Compose and MapLibre ship consumer rules; the following keeps cover
# reflective/JSON usage and native-backed libraries.

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class de.pyxissapiens.**$$serializer { *; }
-keepclassmembers class de.pyxissapiens.** { *** Companion; }
-keepclasseswithmembers class de.pyxissapiens.** { kotlinx.serialization.KSerializer serializer(...); }

# SQLCipher
-keep class net.zetetic.database.** { *; }
-keep class net.sqlcipher.** { *; }

# MapLibre
-keep class org.maplibre.android.** { *; }
-dontwarn org.maplibre.android.**

# General
-keepattributes SourceFile,LineNumberTable
