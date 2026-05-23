import java.io.FileInputStream
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

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
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
    if (keystorePropertiesFile.exists()) {
        signingConfigs {
            create("release") {
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["password"] as String
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["password"] as String
            }
        }
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
            // 本地 active_config=local 使用 http 局域网网关；生产环境仍走 https URL
            manifestPlaceholders["usesCleartextTraffic"] = "true"
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
            // Debug 正常、Release 闪退多为 R8 误删 Tauri/WebView/ML Kit；先关混淆，规则见 proguard-rules.pro
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                *fileTree(".") { include("**/*.pro") }
                    .plus(getDefaultProguardFile("proguard-android.txt"))
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
    packaging {
        jniLibs {
            excludes += setOf("**/libc++_shared.so")
        }
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

// Honor/Huawei: loadLibrary("c++_shared") resolves to /hw_product/... and crashes; Rust uses static libc++.
fun patchRustKt() {
    val rustKt = file("src/main/java/com/hula/app/generated/Rust.kt")
    if (!rustKt.exists()) return
    val text = rustKt.readText()
    val stripped = text.replace(
        Regex("""\s*System\.loadLibrary\("c\+\+_shared"\)\s*;?\s*\n"""),
        "\n"
    )
    if (stripped != text) {
        rustKt.writeText(stripped)
        logger.lifecycle("patchRustKt: removed c++_shared preload from Rust.kt")
    }
}

tasks.configureEach {
    if (name.startsWith("rustBuild")) {
        doFirst { patchAndroidDevUrl() }
        doLast { patchRustKt() }
    }
    if (name.contains("compile") && name.contains("Kotlin")) {
        doFirst { patchRustKt() }
    }
}
