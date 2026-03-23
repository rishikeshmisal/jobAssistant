plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    id("jacoco")
}

android {
    namespace = "com.jobassistant"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.jobassistant"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.jobassistant.HiltTestRunner"
        buildConfigField("String", "GEMINI_API_KEY", "\"${project.findProperty("GEMINI_API_KEY") ?: ""}\"")
        buildConfigField("String", "GOOGLE_CLIENT_ID", "\"${project.findProperty("GOOGLE_CLIENT_ID") ?: ""}\"")
    }

    signingConfigs {
        create("release") {
            storeFile = file("../myjobassistant-release.jks")
            storePassword = "jobassistant123"
            keyAlias = "myjobassistant"
            keyPassword = "jobassistant123"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            // Enable offline JaCoCo instrumentation so unit tests covered via Robolectric
            // are correctly tracked (Robolectric's sandbox classloader bypasses javaagent).
            enableUnitTestCoverage = true
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/LICENSE.md",
                "META-INF/LICENSE-notice.md",
                "META-INF/NOTICE.md",
                "META-INF/AL2.0",
                "META-INF/LGPL2.1",
                // PDFBox-Android bundles its own copies of these
                "META-INF/DEPENDENCIES",
                "META-INF/versions/**"
            )
        }
        // Android 15+ 16KB page-size support: store .so files uncompressed and
        // page-aligned in the APK so the OS can load them directly without extraction.
        jniLibs {
            useLegacyPackaging = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions { jvmTarget = "17" }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
        arg("room.incremental", "true")
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
            all { test ->
                test.jvmArgs("-Xmx2g")
            }
        }
    }
}

val jacocoFileFilter = listOf(
    // Android boilerplate
    "**/R.class", "**/R\$*.class", "**/BuildConfig.*", "**/Manifest*.*",
    // Test classes
    "**/*Test*.*",
    // Dependency-injection generated
    "**/di/**", "**/*_Hilt*", "**/*Hilt_*",
    "**/hilt_aggregated_deps/**", "**/*GeneratedInjector*",
    "**/dagger/hilt/**",
    // Room-generated implementation classes
    "**/*_Impl*",
    // Jetpack Compose compiler-generated lambdas and UI layer
    "**/ComposableSingletons*",
    "**/ui/**",          // MainActivity, theme — covered by UI/instrumented tests
    // Application class — System.loadLibrary can only be validated on-device
    "**/JobAssistantApp*",
    // PdfTextExtractor relies on PdfRenderer native Android rendering, tested via instrumented tests
    "**/util/PdfTextExtractor*",
    // OcrProcessor wraps ML Kit TextRecognition — native GMS model, tested via instrumented tests
    "**/util/OcrProcessor*",
    // WorkManagerScheduler is a thin WorkManager API wrapper; covered by instrumented tests
    "**/service/WorkManagerScheduler*",
    // GmailTokenManager wraps Google Play Services Identity API (Tasks.await / getAuthorizationClient)
    // which require a real device; covered by instrumented tests
    "**/repository/GmailTokenManager*",
    // GeminiRepository wraps the Gemini SDK — requires a real API key; JSON parsing tested separately
    "**/repository/GeminiRepository*"
)

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    reports {
        xml.required = true
        html.required = true
    }
    val buildDir = layout.buildDirectory.get().asFile
    // Original Kotlin-compiled class files (pre-instrumentation) for source mapping.
    val kotlinTree = fileTree("${buildDir}/tmp/kotlin-classes/debug") { exclude(jacocoFileFilter) }
    val javaTree = fileTree("${buildDir}/intermediates/javac/debug") { exclude(jacocoFileFilter) }
    sourceDirectories.setFrom(files("src/main/java"))
    classDirectories.setFrom(files(kotlinTree, javaTree))
    // AGP offline instrumentation writes the exec file here when enableUnitTestCoverage=true
    executionData.setFrom(
        fileTree(buildDir) {
            include(
                "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
                "jacoco/testDebugUnitTest.exec"
            )
        }
    )
}

tasks.register<JacocoCoverageVerification>("jacocoCoverageVerification") {
    dependsOn("jacocoTestReport")
    val buildDir = layout.buildDirectory.get().asFile
    val kotlinTree = fileTree("${buildDir}/tmp/kotlin-classes/debug") { exclude(jacocoFileFilter) }
    val javaTree = fileTree("${buildDir}/intermediates/javac/debug") { exclude(jacocoFileFilter) }
    classDirectories.setFrom(files(kotlinTree, javaTree))
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(
        fileTree(buildDir) {
            include(
                "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
                "jacoco/testDebugUnitTest.exec"
            )
        }
    )
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.activity)
    implementation(libs.compose.navigation)
    implementation(libs.compose.icons.extended)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(platform(libs.compose.bom))

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.sqlcipher)
    implementation(libs.sqlite)

    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    implementation(libs.datastore)
    implementation(libs.datastore.preferences)
    implementation(libs.security.crypto)

    implementation(libs.workmanager)
    implementation(libs.pdfium)
    implementation(libs.pdfbox.android)
    implementation(libs.mlkit.text)

    implementation(libs.credentials)
    implementation(libs.credentials.play)
    implementation(libs.google.id)
    implementation(libs.play.services.auth)

    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.compose)
    implementation(libs.coroutines.android)
    implementation(libs.gson)
    implementation(libs.generativeai)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.room.test)
    testImplementation(libs.datastore.preferences)
    testImplementation("org.robolectric:robolectric:4.12.2")
    testImplementation("androidx.test:core:1.6.1")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    androidTestImplementation(libs.junit.ext)
    androidTestImplementation(libs.espresso)
    androidTestImplementation(libs.android.test.runner)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.hilt.test)
    androidTestImplementation(libs.work.test)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test)
    androidTestImplementation(libs.turbine)
    androidTestImplementation(libs.coroutines.test)
    kspAndroidTest(libs.hilt.compiler)
    debugImplementation(libs.compose.ui.test.manifest)
}
