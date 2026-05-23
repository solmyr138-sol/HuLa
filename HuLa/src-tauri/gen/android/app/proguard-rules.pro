# HuLa Android release — keep Tauri bridge, WebView, plugins (R8 minify breaks these if stripped)

-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod, Exceptions

# App + JNI entrypoints (Rust calls Activity.show/hide)
-keep class com.hula.app.** { *; }
-keepclassmembers class com.hula.app.MainActivity {
    public void show();
    public void hide();
}

# Tauri Android runtime
-keep class app.tauri.** { *; }
-keep class com.plugin.** { *; }

# WebView JS bridge
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keep class androidx.webkit.** { *; }

# ML Kit barcode (bundled scanner)
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.mlkit.**
-dontwarn com.google.android.gms.**

# Kotlin metadata for reflection used by plugins
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.** { *; }
