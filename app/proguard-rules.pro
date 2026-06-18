# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ── Glance AppWidget ──────────────────────────────────────────────────────────
# Keep widget and receiver classes so Android framework can instantiate them.
-keep class com.biprangshu.spendwise.widget.** { *; }

# ── Hilt EntryPoint for Widget ────────────────────────────────────────────────
# EntryPointAccessors.fromApplication() uses reflection to find the interface.
-keep interface com.biprangshu.spendwise.di.WidgetEntryPoint { *; }

# ── Room ──────────────────────────────────────────────────────────────────────
# Room @Query methods that return data classes need field names preserved
# so Room can map SQL column aliases to constructor parameters.
-keep class com.biprangshu.spendwise.data.local.dao.FinancialSummaryTuple { *; }
-keep class com.biprangshu.spendwise.data.local.entity.** { *; }

# Keep Room DAO interfaces — Room generates implementations at compile time,
# but R8 can strip the interface methods that the generated code references.
-keep interface com.biprangshu.spendwise.data.local.dao.** { *; }

# Keep Room Database class
-keep class com.biprangshu.spendwise.data.local.database.** { *; }

# ── Domain models ─────────────────────────────────────────────────────────────
# TransactionType enum is used via valueOf() in mapper — R8 can rename enum constants.
-keepclassmembers enum com.biprangshu.spendwise.domain.model.TransactionType {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ── Kotlin Serialization ─────────────────────────────────────────────────────
# Keep @Serializable classes and their companions (used by navigation routes etc.)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers @kotlinx.serialization.Serializable class com.biprangshu.spendwise.** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# ── Firebase ──────────────────────────────────────────────────────────────────
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**