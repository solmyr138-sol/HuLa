import java.util.Properties
import org.gradle.api.tasks.compile.JavaCompile

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("rust")
}

val tauriProperties = Properties().apply {
    val propFile = file("tauri.properties")
    if (propFile.exists()) {
        propFile.inputStream().use { load(it) }
    }
}

android {
    compileSdk = 36
    namespace = "com.hula.app"
    defaultConfig {
        manifestPlaceholders["usesCleartextTraffic"] = "false"
        applicationId = "com.hula.app"
        minSdk = 26
        targetSdk = 36
        versionCode = tauriProperties.getProperty("tauri.android.versionCode", "1").toInt()
        versionName = tauriProperties.getProperty("tauri.android.versionName", "1.0")
    }
    buildTypes {
        getByName("debug") {
            manifestPlaceholders["usesCleartextTraffic"] = "true"
            isDebuggable = true
            isJniDebuggable = true
            isMinifyEnabled = false
            packaging {
                jniLibs.keepDebugSymbols.add("*/arm64-v8a/*.so")
                jniLibs.keepDebugSymbols.add("*/armeabi-v7a/*.so")
                jniLibs.keepDebugSymbols.add("*/x86/*.so")
                jniLibs.keepDebugSymbols.add("*/x86_64/*.so")
            }
        }
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                *fileTree(".") { include("**/*.pro") }
                    .plus(getDefaultProguardFile("proguard-android-optimize.txt"))
                    .toList().toTypedArray()
            )
        }
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        buildConfig = true
    }
}

rust {
    rootDirRel = "../../../"
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

dependencies {
    implementation("androidx.webkit:webkit:1.14.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation("com.google.android.material:material:1.12.0")

    // 使用 bundled 版本的 ML Kit，不依赖 Google Play Services
    // 解决国产手机（如 Vivo）上扫码功能无法使用的问题
    implementation("com.google.mlkit:barcode-scanning:17.3.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.4")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.0")
}

apply(from = "tauri.build.gradle.kts")

// Host CLI checks devUrl on 127.0.0.1; emulator WebView must load 10.0.2.2
fun patchAndroidDevUrl() {
    val assets = file("src/main/assets/tauri.conf.json")
    if (!assets.exists()) return
    // Emulator on Windows: adb reverse + tauri --host 127.0.0.1 (see scripts/android-dev.ps1)
    val emulatorUrl = "http://127.0.0.1:5210/"
    val text = assets.readText()
    var patched = text.replace(Regex(""""devUrl"\s*:\s*"[^"]*""""), """"devUrl":"$emulatorUrl"""")
    patched = patched.replace(Regex("""http://(?:192\.168\.\d+\.\d+|10\.0\.2\.2):5210"""), "http://127.0.0.1:5210")
    if (text != patched) assets.writeText(patched)
}

// Tauri-generated Rust.kt loads hula_app_lib without libc++ first
fun patchRustKt() {
    val rustKt = file("src/main/java/com/hula/app/generated/Rust.kt")
    if (!rustKt.exists()) return
    val text = rustKt.readText()
    if (text.contains("loadLibrary(\"c++_shared\")")) return
    val patched = text.replace(
        "init {\n        System.loadLibrary(\"hula_app_lib\")",
        "init {\n        System.loadLibrary(\"c++_shared\")\n        System.loadLibrary(\"hula_app_lib\")"
    )
    if (patched != text) rustKt.writeText(patched)
}

tasks.configureEach {
    if (name.startsWith("rustBuild") || (name.contains("compile") && name.contains("Kotlin"))) {
        doFirst {
            patchAndroidDevUrl()
            patchRustKt()
        }
    }
}
