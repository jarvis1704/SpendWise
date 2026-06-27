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
# EntryPointAccessors uses the interface type to cast the generated SingletonC
# component at runtime. Both the interface AND every class implementing it must
# be kept so R8 doesn't strip the concrete transactionRepository() /
# userPreferencesManager() method bodies from the generated component.
-keep interface com.biprangshu.spendwise.di.WidgetEntryPoint { *; }
-keep class * implements com.biprangshu.spendwise.di.WidgetEntryPoint { *; }

# UserPreferencesManager has no @Inject constructor (provided via @Provides),
# so R8 has more freedom to rename its members. Keep the class and all public
# Flow properties accessed from the widget.
-keep class com.biprangshu.spendwise.data.preferences.UserPreferencesManager { *; }

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