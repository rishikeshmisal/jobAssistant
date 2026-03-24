# AI Job Assistant — Claude Execution Guide

## How to Use This Document

Each phase is a **runnable MVP**. The app must compile and run at the end of every phase before moving to the next. Read the entire phase before starting it. All file paths are relative to the project root unless otherwise noted.

**Package name:** `com.jobassistant`
**Application ID:** `com.jobassistant`
**Min SDK:** 26 | **Target SDK:** 35 | **Compile SDK:** 35

---

## Phase 1 — Prerequisites & Project Scaffold

> **MVP goal:** App launches on device/emulator. Room DB initializes with SQLCipher. Navigation shell exists. No plain-text database file on device.

### 1.1 Create the Android Project

1. Create a new Android Studio project using the "Empty Activity" template.
2. Set application ID to `com.jobassistant`, language Kotlin, min SDK 26, target SDK 35.
3. Enable Jetpack Compose in the wizard.
4. Set the root package to `com.jobassistant`.

### 1.2 Project-level `build.gradle.kts`

Add the following to `build.gradle.kts` (project level):
```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
}
```

### 1.3 `libs.versions.toml` — All Dependencies

Define all versions and libraries in `gradle/libs.versions.toml`:

```toml
[versions]
agp = "8.5.0"
kotlin = "2.0.0"
hilt = "2.51.1"
ksp = "2.0.0-1.0.21"
compose-bom = "2024.06.00"
room = "2.6.1"
retrofit = "2.11.0"
okhttp = "4.12.0"
datastore = "1.1.1"
security-crypto = "1.1.0-alpha06"
workmanager = "2.9.0"
pdfium = "1.9.0"
mlkit-text = "16.0.0"
credentials = "1.3.0"
google-id = "1.1.1"
coroutines = "1.8.1"
lifecycle = "2.8.3"
navigation = "2.7.7"
sqlcipher = "4.5.4"
sqlite = "2.4.0"
junit = "4.13.2"
junit-ext = "1.2.1"
espresso = "3.6.1"
mockk = "1.13.11"
coroutines-test = "1.8.1"
turbine = "1.1.0"
compose-ui-test = "1.6.8"
hilt-test = "2.51.1"
work-test = "2.9.0"
jacoco = "0.8.11"

[libraries]
# Compose
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-activity = { group = "androidx.activity", name = "activity-compose", version = "1.9.0" }
compose-navigation = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }
compose-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }

# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "1.2.0" }
hilt-work = { group = "androidx.hilt", name = "hilt-work", version = "1.2.0" }
hilt-work-compiler = { group = "androidx.hilt", name = "hilt-compiler", version = "1.2.0" }

# Room
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }

# SQLCipher
sqlcipher = { group = "net.zetetic", name = "android-database-sqlcipher", version.ref = "sqlcipher" }
sqlite = { group = "androidx.sqlite", name = "sqlite-ktx", version.ref = "sqlite" }

# Networking
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-gson = { group = "com.squareup.retrofit2", name = "converter-gson", version.ref = "retrofit" }
okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version.ref = "okhttp" }
okhttp-logging = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }

# DataStore
datastore = { group = "androidx.datastore", name = "datastore", version.ref = "datastore" }
security-crypto = { group = "androidx.security", name = "security-crypto", version.ref = "security-crypto" }

# WorkManager
workmanager = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "workmanager" }

# PDF
pdfium = { group = "com.github.barteksc", name = "pdfium-android", version.ref = "pdfium" }

# ML Kit OCR
# Use unbundled ML Kit (downloads via Play Services) — saves ~15MB vs bundled com.google.mlkit:text-recognition
mlkit-text = { group = "com.google.android.gms", name = "play-services-mlkit-text-recognition", version.ref = "mlkit-text" }

# Google Auth
credentials = { group = "androidx.credentials", name = "credentials", version.ref = "credentials" }
credentials-play = { group = "androidx.credentials", name = "credentials-play-services-auth", version.ref = "credentials" }
google-id = { group = "com.google.android.libraries.identity.googleid", name = "googleid", version.ref = "google-id" }

# Lifecycle
lifecycle-viewmodel = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-ktx", version.ref = "lifecycle" }
lifecycle-runtime = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }
lifecycle-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }

# Coroutines
coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }

# Gson
gson = { group = "com.google.code.gson", name = "gson", version = "2.11.0" }

# Testing
junit = { group = "junit", name = "junit", version.ref = "junit" }
junit-ext = { group = "androidx.test.ext", name = "junit", version.ref = "junit-ext" }
espresso = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espresso" }
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
mockk-android = { group = "io.mockk", name = "mockk-android", version.ref = "mockk" }
coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines-test" }
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }
compose-ui-test = { group = "androidx.compose.ui", name = "ui-test-junit4", version.ref = "compose-ui-test" }
compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest", version.ref = "compose-ui-test" }
hilt-test = { group = "com.google.dagger", name = "hilt-android-testing", version.ref = "hilt-test" }
work-test = { group = "androidx.work", name = "work-testing", version.ref = "work-test" }
room-test = { group = "androidx.room", name = "room-testing", version.ref = "room" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

### 1.4 App-level `app/build.gradle.kts`

```kotlin
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
    compileSdk = 35

    defaultConfig {
        applicationId = "com.jobassistant"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // Inject Claude API key from local.properties
        buildConfigField("String", "CLAUDE_API_KEY", "\"${project.findProperty("CLAUDE_API_KEY") ?: ""}\"")
        buildConfigField("String", "GOOGLE_CLIENT_ID", "\"${project.findProperty("GOOGLE_CLIENT_ID") ?: ""}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions { jvmTarget = "17" }

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

// JaCoCo coverage enforcement — fails build if coverage < 80%
tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    reports {
        xml.required = true
        html.required = true
    }
    val fileFilter = listOf("**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*", "**/*Test*.*", "**/di/**", "**/*_Hilt*", "**/*Hilt_*")
    val debugTree = fileTree("${buildDir}/intermediates/javac/debug") { exclude(fileFilter) }
    val kotlinDebugTree = fileTree("${buildDir}/tmp/kotlin-classes/debug") { exclude(fileFilter) }
    sourceDirectories.setFrom(files("src/main/java"))
    classDirectories.setFrom(files(debugTree, kotlinDebugTree))
    executionData.setFrom(fileTree(buildDir) { include("jacoco/testDebugUnitTest.exec") })
}

tasks.register<JacocoCoverageVerification>("jacocoCoverageVerification") {
    dependsOn("jacocoTestReport")
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal()  // 80% minimum coverage
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
    implementation(libs.security.crypto)

    implementation(libs.workmanager)
    implementation(libs.pdfium)
    implementation(libs.mlkit.text)

    implementation(libs.credentials)
    implementation(libs.credentials.play)
    implementation(libs.google.id)

    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.compose)
    implementation(libs.coroutines.android)
    implementation(libs.gson)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.room.test)
    androidTestImplementation(libs.junit.ext)
    androidTestImplementation(libs.espresso)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.hilt.test)
    androidTestImplementation(libs.work.test)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test)
    kspAndroidTest(libs.hilt.compiler)
    debugImplementation(libs.compose.ui.test.manifest)
}
```

### 1.5 `local.properties`

Add to `local.properties` (never commit this file — verify it's in `.gitignore`):
```
CLAUDE_API_KEY=your_anthropic_api_key_here
GOOGLE_CLIENT_ID=your_google_oauth_client_id_here
```

### 1.6 `AndroidManifest.xml`

Replace the contents of `app/src/main/AndroidManifest.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.USE_CREDENTIALS" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
        android:maxSdkVersion="32" />
    <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />

    <application
        android:name=".JobAssistantApp"
        android:allowBackup="false"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:networkSecurityConfig="@xml/network_security_config"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.JobAssistant">

        <activity
            android:name=".ui.MainActivity"
            android:exported="true"
            android:theme="@style/Theme.JobAssistant">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

### 1.7 Network Security Config

Create `app/src/main/res/xml/network_security_config.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">api.anthropic.com</domain>
        <domain includeSubdomains="true">googleapis.com</domain>
        <domain includeSubdomains="true">accounts.google.com</domain>
        <pin-set>
            <!-- Add certificate pins in production -->
        </pin-set>
    </domain-config>
</network-security-config>
```

### 1.8 Package Structure

Create the following empty package directories under `app/src/main/java/com/jobassistant/`:
```
data/
  db/
  repository/
  remote/
domain/
  model/
  repository/
  usecase/
ui/
  screens/
    dashboard/
    detail/
    profile/
    insights/
    onboarding/
  theme/
  components/
service/
di/
util/
```

### 1.9 Application Class

Create `app/src/main/java/com/jobassistant/JobAssistantApp.kt`:
```kotlin
@HiltAndroidApp
class JobAssistantApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Load SQLCipher native libraries at startup
        System.loadLibrary("sqlcipher")
    }
}
```

### 1.10 Database Passphrase Manager

Create `app/src/main/java/com/jobassistant/util/PassphraseManager.kt`:
- Store the DB passphrase directly in the **Android Keystore** using `KeyStore.getInstance("AndroidKeyStore")` with a `KeyGenerator` for AES-256. Do NOT use `EncryptedSharedPreferences` here — the passphrase is a single security-critical value and the Keystore is the appropriate home for cryptographic material.
- On first call to `getOrCreatePassphrase(context)`: generate a 32-byte random passphrase via `SecureRandom`, base64-encode it, encrypt with the Keystore key, and write the encrypted blob to a raw private file (`context.filesDir/db_passphrase.enc`).
- On subsequent calls: read the encrypted blob, decrypt using the Keystore key, return the passphrase.
- Never log or expose the passphrase value.

> **Why not EncryptedSharedPreferences?** `EncryptedSharedPreferences` has known reliability issues on some Android 8/9 devices (random key corruption after backup/restore). For the DB passphrase specifically, direct Keystore + encrypted file is more robust and is the approach recommended in the Android Security Best Practices guide.

### 1.11 Room Database Shell

Create `app/src/main/java/com/jobassistant/data/db/AppDatabase.kt`:
- Annotate with `@Database(entities = [], version = 1)` (entities will be added in Phase 2).
- Use SQLCipher `SupportFactory` in the `Room.databaseBuilder` call:
  ```kotlin
  val passphrase = PassphraseManager.getOrCreatePassphrase(context).toByteArray()
  val factory = SupportFactory(passphrase)
  Room.databaseBuilder(context, AppDatabase::class.java, "jobassistant.db")
      .openHelperFactory(factory)
      .build()
  ```

### 1.12 Hilt Database Module

Create `app/src/main/java/com/jobassistant/di/DatabaseModule.kt`:
- `@Module`, `@InstallIn(SingletonComponent::class)`
- Provide `AppDatabase` as a singleton using the SQLCipher factory from step 1.11.

### 1.13 MainActivity Shell

Create `app/src/main/java/com/jobassistant/ui/MainActivity.kt`:
- `@AndroidEntryPoint`
- In `onCreate`, call `setContent { JobAssistantTheme { Text("Hello Job Assistant") } }` as a placeholder.

### 1.14 ProGuard Rules

Add to `app/proguard-rules.pro`:
```
-keep class com.jobassistant.data.db.** { *; }
-keep class com.jobassistant.data.remote.model.** { *; }
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }
-dontwarn net.sqlcipher.**
```

### 1.15 `.gitignore`

Verify `local.properties` is listed in `.gitignore`. If not, add it.

### Phase 1 Testing Requirements

**Coverage target: >80% of all new code in this phase.**

**Unit tests** (`test/` — runs on JVM):

- `PassphraseManagerTest`: verify `getOrCreatePassphrase` returns the same value on repeated calls (idempotent), verify it returns a non-empty string, verify different app instances return the same stored value.
- `AppDatabaseTest`: verify `AppDatabase` builds successfully with a SQLCipher `SupportFactory` using a test passphrase (use `Room.inMemoryDatabaseBuilder` with the factory).

**Run after completing this phase:**
```
./gradlew testDebugUnitTest jacocoCoverageVerification
```

### Phase 1 MVP Checkpoint

- [ ] Project syncs without Gradle errors
- [ ] App installs and launches on emulator/device showing "Hello Job Assistant"
- [ ] No `jobassistant.db` plain-text file visible in `/data/data/com.jobassistant/databases/` (only encrypted blob)
- [ ] `BuildConfig.CLAUDE_API_KEY` is populated (log it once in debug, then remove the log)
- [ ] `./gradlew testDebugUnitTest jacocoCoverageVerification` passes with ≥80% coverage

---

## Phase 2 — Data & Domain Layer

> **MVP goal:** Full offline job tracker. Can insert, read, update, and delete `JobApplication` entries. `UserProfile` persists across app restarts. All domain models and repositories wired.

### 2.1 Domain Models

Create the following Kotlin data classes in `domain/model/`:

**`ApplicationStatus.kt`**
```kotlin
enum class ApplicationStatus { SAVED, APPLIED, INTERVIEWING, OFFERED, REJECTED }
```

**`AppTheme.kt`**
```kotlin
enum class AppTheme { GREEN, RED, BLUE, YELLOW }
```

**`JobApplication.kt`** (domain model — not the Room entity)
- Fields: `id: UUID`, `companyName: String`, `roleTitle: String`, `jobUrl: String?`, `status: ApplicationStatus`, `fitScore: Int?`, `location: String?`, `salaryRange: String?`, `appliedDate: Long?`, `interviewDate: Long?`, `notes: String`, `linkedEmailThreadIds: List<String>`, `lastSeenDate: Long`

**`UserProfile.kt`** (domain model)
- Fields: `userId: String`, `fullName: String`, `resumeText: String`, `keywords: List<String>`, `careerGoal: String`, `targetSalaryMin: Int`, `targetSalaryMax: Int`, `selectedTheme: AppTheme`, `isOnboardingComplete: Boolean`

**`CareerInsights.kt`** (domain model)
- Fields: `id: UUID`, `generatedDate: Long`, `identifiedGaps: List<String>`, `recommendedActions: List<String>`, `summaryAnalysis: String`

### 2.2 Room Entities

Create `data/db/entity/JobApplicationEntity.kt`:
- Mirror the `JobApplication` domain model exactly.
- Annotate with `@Entity(tableName = "job_applications")`.
- Annotate `id` with `@PrimaryKey`.

Create `data/db/entity/CareerInsightsEntity.kt`:
- Mirror the `CareerInsights` domain model.
- Annotate with `@Entity(tableName = "career_insights")`.

### 2.3 TypeConverters

Create `data/db/Converters.kt` annotated with `@ProvidedTypeConverter`:
- `List<String>` ↔ JSON string (use Gson).
- `UUID` ↔ String.
- `ApplicationStatus` ↔ String.

### 2.4 DAOs

Create `data/db/dao/JobApplicationDao.kt`:
```kotlin
@Dao
interface JobApplicationDao {
    @Query("SELECT * FROM job_applications ORDER BY appliedDate DESC")
    fun getAllAsFlow(): Flow<List<JobApplicationEntity>>

    @Query("SELECT * FROM job_applications WHERE status = :status")
    fun getByStatusAsFlow(status: String): Flow<List<JobApplicationEntity>>

    @Query("SELECT * FROM job_applications WHERE id = :id")
    suspend fun getById(id: String): JobApplicationEntity?

    @Query("SELECT * FROM job_applications WHERE companyName LIKE :company AND roleTitle LIKE :role LIMIT 1")
    suspend fun findDuplicate(company: String, role: String): JobApplicationEntity?

    @Upsert
    suspend fun upsert(entity: JobApplicationEntity)

    @Delete
    suspend fun delete(entity: JobApplicationEntity)
}
```

Create `data/db/dao/CareerInsightsDao.kt`:
```kotlin
@Dao
interface CareerInsightsDao {
    @Query("SELECT * FROM career_insights ORDER BY generatedDate DESC LIMIT 1")
    fun getLatestAsFlow(): Flow<CareerInsightsEntity?>

    @Upsert
    suspend fun upsert(entity: CareerInsightsEntity)
}
```

### 2.5 Update AppDatabase

Update `data/db/AppDatabase.kt`:
- Add `JobApplicationEntity` and `CareerInsightsEntity` to the `@Database` entities list.
- Add the `Converters` class via `@TypeConverters`.
- Expose `jobApplicationDao()` and `careerInsightsDao()` abstract functions.
- Bump `version` to 1 (already set).

### 2.6 Mapper Functions

Create `data/db/mapper/JobApplicationMapper.kt`:
- `JobApplicationEntity.toDomain(): JobApplication`
- `JobApplication.toEntity(): JobApplicationEntity`

Create `data/db/mapper/CareerInsightsMapper.kt`:
- `CareerInsightsEntity.toDomain(): CareerInsights`
- `CareerInsights.toEntity(): CareerInsightsEntity`

### 2.7 Repository Interfaces

Create `domain/repository/JobApplicationRepository.kt`:
```kotlin
interface JobApplicationRepository {
    fun getAllAsFlow(): Flow<List<JobApplication>>
    fun getByStatusAsFlow(status: ApplicationStatus): Flow<List<JobApplication>>
    suspend fun getById(id: UUID): JobApplication?
    suspend fun save(job: JobApplication)
    suspend fun delete(job: JobApplication)
    suspend fun findDuplicate(companyName: String, roleTitle: String): JobApplication?
}
```

Create `domain/repository/CareerInsightsRepository.kt`:
```kotlin
interface CareerInsightsRepository {
    fun getLatestAsFlow(): Flow<CareerInsights?>
    suspend fun save(insights: CareerInsights)
}
```

### 2.8 Repository Implementations

Create `data/repository/JobApplicationRepositoryImpl.kt`:
- Implements `JobApplicationRepository`.
- Delegates all calls to `JobApplicationDao` and applies mappers.
- For `save`: call `findDuplicate` first; if a match exists, update the existing entry rather than inserting a new one.

Create `data/repository/CareerInsightsRepositoryImpl.kt`:
- Implements `CareerInsightsRepository`.
- Delegates to `CareerInsightsDao`.

### 2.9 UserProfile DataStore

Create `data/repository/UserProfileDataStore.kt`:
- Use **EncryptedDataStore**: combine `DataStore<Preferences>` with an `EncryptedFile` from `androidx.security:security-crypto`. This is the standardised approach for all non-passphrase sensitive data (profile fields, OAuth tokens, API keys) — do NOT use `EncryptedSharedPreferences` anywhere else in the project.
- Store each `UserProfile` field as a separate `Preferences.Key`.
- Expose: `userProfileFlow: Flow<UserProfile>`, `suspend fun save(profile: UserProfile)`, `suspend fun update(block: UserProfile.() -> UserProfile)`.
- All OAuth tokens (added in Phase 7) and the BYOK API key (added in Phase 9) must also be stored in this same `UserProfileDataStore` — do not create separate `EncryptedSharedPreferences` instances.

> **Storage consistency rule:** The project uses exactly two secure storage mechanisms — (1) Android Keystore for the SQLCipher passphrase only, (2) `EncryptedDataStore` for all other sensitive data. Never introduce a third mechanism.

### 2.10 PDF Text Extraction Utility

Create `util/PdfTextExtractor.kt`:
- Accept a `Uri` (from file picker) and a `Context`.
- Open the PDF using `PdfiumAndroid`'s `PdfiumCore`.
- Iterate all pages, extract text from each page using `pdfiumCore.getPageAsText(pageIndex)`.
- Concatenate all page text with newlines and return as `String`.
- Handle exceptions gracefully — return `null` if extraction fails.

### 2.11 Hilt Modules

Update `di/DatabaseModule.kt`:
- Add `@Provides` for `JobApplicationDao` and `CareerInsightsDao` sourced from the `AppDatabase`.

Create `di/RepositoryModule.kt`:
- Bind `JobApplicationRepositoryImpl` → `JobApplicationRepository`
- Bind `CareerInsightsRepositoryImpl` → `CareerInsightsRepository`
- Provide `UserProfileDataStore` as a singleton.

### 2.12 Domain Use Cases

Create the following use case classes in `domain/usecase/`:
- `GetAllJobsUseCase` — returns `Flow<List<JobApplication>>`
- `GetJobsByStatusUseCase` — returns `Flow<List<JobApplication>>`
- `SaveJobApplicationUseCase` — calls `findDuplicate` then `save`
- `UpdateJobStatusUseCase` — loads existing job, changes status, saves
- `DeleteJobApplicationUseCase`
- `GetCareerInsightsUseCase`
- `SaveCareerInsightsUseCase`

Each use case takes its repository via constructor injection and exposes a single `invoke` operator.

### Phase 2 Testing Requirements

**Coverage target: >80% of all new code in this phase.**

**Unit tests** (`test/` — runs on JVM):

- `JobApplicationRepositoryTest`: mock `JobApplicationDao`; verify `save` calls `findDuplicate` before upsert; verify duplicate detection returns existing entity instead of inserting; verify `getById` maps entity to domain model correctly.
- `CareerInsightsRepositoryTest`: mock `CareerInsightsDao`; verify `save` and `getLatestAsFlow` map correctly.
- `UserProfileDataStoreTest`: use an in-memory `DataStore<Preferences>` test double; verify each profile field saves and reloads correctly; verify `update` applies the transform correctly.
- `PdfTextExtractorTest`: provide a real test PDF asset in `test/resources/`; verify extracted text is non-empty and contains expected content.
- `SaveJobApplicationUseCaseTest`: verify duplicate detection flow — mock repo returns a match → use case returns `SaveResult.Duplicate`; mock repo returns null → use case calls `save`.
- `UpdateJobStatusUseCaseTest`: verify status is changed and `save` is called with the updated value.
- `GetAllJobsUseCaseTest`: verify it delegates to `getAllAsFlow()` and emits mapped domain models.

**Instrumented tests** (`androidTest/` — runs on device):

- `JobApplicationDaoTest`: use `Room.inMemoryDatabaseBuilder` with SQLCipher factory; test `upsert`, `getById`, `getAllAsFlow` emissions, `findDuplicate` exact and fuzzy matches, and `delete`.
- `CareerInsightsDaoTest`: test `upsert` and `getLatestAsFlow` returns the most recent entry.

**Run after completing this phase:**
```
./gradlew testDebugUnitTest jacocoCoverageVerification
./gradlew connectedDebugAndroidTest
```

### Phase 2 MVP Checkpoint

- [ ] Add a `JobApplication` entry via a temporary debug button in `MainActivity`
- [ ] Query all entries and log them — confirm they persist after app restart
- [ ] Confirm `findDuplicate` returns the existing entry when same company+role is saved twice
- [ ] Confirm `UserProfile` saves and reloads correctly across restarts
- [ ] All unit tests pass; DAO instrumented tests pass; coverage ≥80%

---

## Phase 3 — Navigation Shell + Dynamic Theming

> **MVP goal:** Full app navigation works. Theme switches live across all screens. Onboarding flow shown on first run. All screens exist as placeholders with correct titles.

### 3.1 Theme Engine

Create `ui/theme/AppColors.kt`:
- Define seed colors for each theme:
  ```kotlin
  val ThemeSeedColors = mapOf(
      AppTheme.GREEN  to Color(0xFF2E7D32),
      AppTheme.RED    to Color(0xFFC62828),
      AppTheme.BLUE   to Color(0xFF1565C0),
      AppTheme.YELLOW to Color(0xFFF9A825)
  )
  ```

Create `ui/theme/JobAssistantTheme.kt`:
- Accept `appTheme: AppTheme` and `darkTheme: Boolean` parameters.
- Generate a `ColorScheme` from the seed color using `dynamicColorScheme` (API 31+) with a manual fallback using `lightColorScheme`/`darkColorScheme` with the seed on API 26–30.
- Wrap content in `MaterialTheme(colorScheme = colorScheme)`.

### 3.2 ThemeSelector Component

Create `ui/components/ThemeSelector.kt`:
- A horizontal `Row` of 4 colored `FilterChip` or filled `IconButton` elements (one per theme).
- Active theme is highlighted with a checkmark or border.
- Clicking a chip triggers a callback `onThemeSelected: (AppTheme) -> Unit`.

### 3.3 Navigation Routes

Create `ui/navigation/Screen.kt`:
```kotlin
sealed class Screen(val route: String) {
    object Onboarding  : Screen("onboarding")
    object Dashboard   : Screen("dashboard")
    object JobDetail   : Screen("job_detail/{jobId}") {
        fun createRoute(jobId: String) = "job_detail/$jobId"
    }
    object AddJob      : Screen("add_job")
    object Profile     : Screen("profile")
    object Insights    : Screen("insights")
}
```

### 3.4 MainViewModel

Create `ui/MainViewModel.kt`:
- Inject `UserProfileDataStore`.
- Expose `uiState: StateFlow<MainUiState>` where `MainUiState` contains `selectedTheme: AppTheme` and `isOnboardingComplete: Boolean`.
- Expose `fun setTheme(theme: AppTheme)` which calls `userProfileDataStore.update`.

### 3.5 NavHost + Bottom Navigation

Create `ui/navigation/AppNavigation.kt`:
- `NavHost` with start destination: `Screen.Onboarding` if `!isOnboardingComplete`, else `Screen.Dashboard`.
- Routes: `Onboarding`, `Dashboard`, `JobDetail`, `AddJob`, `Profile`, `Insights`.
- Bottom navigation bar with 3 tabs: Dashboard, Profile, Insights (hide on Onboarding and JobDetail screens).

### 3.6 Placeholder Screens

Create the following Composable screens — each shows its name as a centered `Text` for now:
- `ui/screens/dashboard/DashboardScreen.kt`
- `ui/screens/detail/JobDetailScreen.kt` (accepts `jobId: String` parameter)
- `ui/screens/detail/AddJobScreen.kt`
- `ui/screens/profile/ProfileScreen.kt`
- `ui/screens/insights/InsightsScreen.kt`

### 3.7 Onboarding Screen (Shell)

Create `ui/screens/onboarding/OnboardingScreen.kt`:
- Multi-step pager with 3 steps:
  - Step 1: Welcome + name input
  - Step 2: Career goal input
  - Step 3: "Connect Gmail later" prompt + "Get Started" button
- On "Get Started": set `isOnboardingComplete = true` in `UserProfileDataStore`, navigate to `Dashboard`.
- Full onboarding implementation (resume upload, etc.) is wired in Phase 5.

### 3.8 Update MainActivity

Update `ui/MainActivity.kt`:
- `@AndroidEntryPoint`
- Inject `MainViewModel` using `hiltViewModel()`.
- Collect `uiState` as state.
- Pass `selectedTheme` to `JobAssistantTheme`.
- Render `AppNavigation` inside the theme.
- Place `ThemeSelector` in a persistent `TopAppBar` or as a row above the nav content.

### Phase 3 Testing Requirements

**Coverage target: >80% of all new code in this phase.**

**Unit tests** (`test/`):

- `MainViewModelTest`: use `TestCoroutineDispatcher`; mock `UserProfileDataStore`; verify `setTheme(GREEN)` emits the correct theme in `uiState`; verify `isOnboardingComplete = false` results in correct nav start destination state; verify `isOnboardingComplete = true` skips onboarding.
- `AppColorsTest`: verify all 4 `AppTheme` values have a corresponding non-null seed color in `ThemeSeedColors`.

**Compose UI tests** (`androidTest/`):

- `ThemeSelectorTest`: render `ThemeSelector` in isolation; verify all 4 color chips are displayed; tap each chip and verify the `onThemeSelected` callback fires with the correct `AppTheme`.
- `NavigationTest`: use `createAndroidComposeRule<MainActivity>` with Hilt test; verify bottom nav items are visible after onboarding; verify tapping each nav item navigates to the correct screen (match by screen title text).
- `OnboardingScreenTest`: verify completing all onboarding steps results in navigation to Dashboard; verify "Get Started" button is disabled until required fields are filled.

**Run after completing this phase:**
```
./gradlew testDebugUnitTest jacocoCoverageVerification
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.jobassistant.ui.ThemeSelectorTest,com.jobassistant.ui.NavigationTest,com.jobassistant.ui.OnboardingScreenTest
```

### Phase 3 MVP Checkpoint

- [ ] App launches and shows Onboarding on first run
- [ ] After completing onboarding, Dashboard placeholder screen is shown
- [ ] Bottom nav tabs navigate between Dashboard, Profile, Insights
- [ ] Selecting GREEN/RED/BLUE/YELLOW immediately re-colors all buttons, FABs, and cards across all screens
- [ ] Theme selection persists after app restart
- [ ] All unit and Compose UI tests pass; coverage ≥80%

---

## Phase 4 — AI Service Layer

> **MVP goal:** Manual job paste → Claude evaluate_fit → fit score + pros/cons displayed. All 4 Claude endpoints callable and returning typed Kotlin objects.

### 4.1 API Request/Response Models

Use the **Anthropic Tool Use (Function Calling) API** — do NOT use prompt engineering ("respond only with JSON"). The Tool Use API forces Claude to output a structured tool call object, eliminating the risk of conversational wrapper text that breaks Gson parsing.

Create the following data classes in `data/remote/model/`:

**`ClaudeRequest.kt`**
```kotlin
data class ClaudeRequest(
    val model: String = "claude-sonnet-4-6",   // Claude Sonnet 4.6 — valid API ID as of 2026
    val max_tokens: Int = 1024,
    val tools: List<ClaudeTool>,
    val tool_choice: ToolChoice,
    val messages: List<ClaudeMessage>
)
data class ClaudeMessage(val role: String = "user", val content: String)
data class ClaudeTool(
    val name: String,
    val description: String,
    val input_schema: Map<String, Any>   // JSON Schema object as a Map
)
data class ToolChoice(val type: String = "tool", val name: String)
```

**`ClaudeResponse.kt`**
```kotlin
data class ClaudeResponse(val content: List<ContentBlock>)
data class ContentBlock(
    val type: String,              // "tool_use" for structured output
    val id: String? = null,
    val name: String? = null,      // tool name, e.g. "evaluate_fit"
    val input: com.google.gson.JsonObject? = null  // the structured output — parse this
)
```

**`CareerProfileResponse.kt`**
```kotlin
data class CareerProfileResponse(
    val current_level: String,
    val target_roles: List<String>,
    val skill_gaps: List<String>,
    val recommended_focus_areas: List<String>,
    val goal_map: String
)
```

**`FitAnalysisResponse.kt`**
```kotlin
data class FitAnalysisResponse(
    val score: Int,
    val pros: List<String>,
    val cons: List<String>,
    val missing_skills: List<String>
)
```

**`EmailActionResponse.kt`**
```kotlin
data class EmailActionResponse(
    val action_type: String,   // APPLIED, REJECTION, INTERVIEW, ALERT, IRRELEVANT
    val target_company: String?,
    val role_title: String?,
    val date: Long?,
    val interview_link: String?
)
```

**`CareerInsightsResponse.kt`**
```kotlin
data class CareerInsightsResponse(
    val identified_gaps: List<String>,
    val recommended_actions: List<String>,
    val market_feedback_summary: String
)
```

### 4.2 Tool Schemas

Create `data/remote/ClaudeTools.kt` — defines each Claude tool as a `ClaudeTool` instance with a full JSON Schema `input_schema`. These replace text-based prompt engineering entirely.

```kotlin
object ClaudeTools {

    val ANALYZE_INTENT = ClaudeTool(
        name = "analyze_intent",
        description = "Analyze a resume and user interests to generate a structured career profile.",
        input_schema = mapOf(
            "type" to "object",
            "properties" to mapOf(
                "current_level" to mapOf("type" to "string", "description" to "e.g. Junior, Mid-level, Senior"),
                "target_roles" to mapOf("type" to "array", "items" to mapOf("type" to "string")),
                "skill_gaps" to mapOf("type" to "array", "items" to mapOf("type" to "string")),
                "recommended_focus_areas" to mapOf("type" to "array", "items" to mapOf("type" to "string")),
                "goal_map" to mapOf("type" to "string", "description" to "1-2 sentence career trajectory summary")
            ),
            "required" to listOf("current_level", "target_roles", "skill_gaps", "recommended_focus_areas", "goal_map")
        )
    )

    val EVALUATE_FIT = ClaudeTool(
        name = "evaluate_fit",
        description = "Score how well a candidate's resume matches a job description.",
        input_schema = mapOf(
            "type" to "object",
            "properties" to mapOf(
                "score" to mapOf("type" to "integer", "minimum" to 1, "maximum" to 100),
                "pros" to mapOf("type" to "array", "items" to mapOf("type" to "string")),
                "cons" to mapOf("type" to "array", "items" to mapOf("type" to "string")),
                "missing_skills" to mapOf("type" to "array", "items" to mapOf("type" to "string"))
            ),
            "required" to listOf("score", "pros", "cons", "missing_skills")
        )
    )

    val PARSE_EMAIL = ClaudeTool(
        name = "parse_email_context",
        description = "Classify a job-related email and extract key details.",
        input_schema = mapOf(
            "type" to "object",
            "properties" to mapOf(
                "action_type" to mapOf("type" to "string", "enum" to listOf("APPLIED", "REJECTION", "INTERVIEW", "ALERT", "IRRELEVANT")),
                "target_company" to mapOf("type" to "string"),
                "role_title" to mapOf("type" to "string"),
                "date" to mapOf("type" to "integer", "description" to "Unix timestamp in milliseconds, or null"),
                "interview_link" to mapOf("type" to "string")
            ),
            "required" to listOf("action_type")
        )
    )

    val GENERATE_INSIGHTS = ClaudeTool(
        name = "generate_career_insights",
        description = "Analyze job application history and generate actionable career insights.",
        input_schema = mapOf(
            "type" to "object",
            "properties" to mapOf(
                "identified_gaps" to mapOf("type" to "array", "items" to mapOf("type" to "string")),
                "recommended_actions" to mapOf("type" to "array", "items" to mapOf("type" to "string")),
                "market_feedback_summary" to mapOf("type" to "string", "description" to "2-3 sentence summary")
            ),
            "required" to listOf("identified_gaps", "recommended_actions", "market_feedback_summary")
        )
    )
}
```

### 4.3 Retrofit Service

Create `data/remote/AnthropicApiService.kt`:
```kotlin
interface AnthropicApiService {
    @POST("v1/messages")
    suspend fun sendMessage(@Body request: ClaudeRequest): ClaudeResponse
}
```

### 4.4 OkHttp Client + Retrofit Instance

Create `di/NetworkModule.kt`:
- `@Module`, `@InstallIn(SingletonComponent::class)`
- Provide `OkHttpClient` with:
  - `ApiKeyInterceptor`: adds headers `x-api-key: BuildConfig.CLAUDE_API_KEY` and `anthropic-version: 2023-06-01`
  - `HttpLoggingInterceptor` (BODY level in DEBUG, NONE in RELEASE)
  - `connectTimeout(30, TimeUnit.SECONDS)`, `readTimeout(60, TimeUnit.SECONDS)`
- Provide `Retrofit` with base URL `https://api.anthropic.com/`, Gson converter, and the above `OkHttpClient`.
- Provide `AnthropicApiService` from the `Retrofit` instance.

### 4.5 Claude Repository

Create `data/repository/ClaudeRepository.kt`:

```kotlin
class ClaudeRepository @Inject constructor(
    private val api: AnthropicApiService,
    private val gson: Gson
) {
    suspend fun analyzeIntent(resumeText: String, userInterests: String): CareerProfileResponse
    suspend fun evaluateFit(resumeText: String, jobDescription: String): FitAnalysisResponse
    suspend fun parseEmail(subject: String, body: String): EmailActionResponse
    suspend fun generateInsights(profileText: String, historySummary: String): CareerInsightsResponse
}
```

Each function:
1. Constructs a `ClaudeRequest` with the matching tool from `ClaudeTools` and sets `tool_choice = ToolChoice(type = "tool", name = tool.name)` to force Claude to invoke that specific tool.
2. Formats the user message as a plain string (e.g., `"Resume:\n$resumeText\n\nJob Description:\n$jobDescription"`).
3. Calls `api.sendMessage(request)`.
4. Finds the `tool_use` block: `response.content.first { it.type == "tool_use" }`.
5. Parses `block.input.toString()` using `gson.fromJson(inputJson, ResponseType::class.java)`.
6. Returns the typed response or throws a `ClaudeParseException` if no `tool_use` block is present or Gson fails.

> **Why Tool Use?** With `tool_choice` set to a specific tool, Claude is contractually required by the API to output a `tool_use` block conforming to the schema. There is no risk of conversational text wrapping the JSON, which is the primary failure mode of prompt-engineered JSON responses.

### 4.6 Domain Use Cases for AI

Create in `domain/usecase/`:
- `EvaluateFitUseCase`: takes `resumeText`, `jobDescription` → returns `FitAnalysisResponse`
- `AnalyzeIntentUseCase`: takes `resumeText`, `userInterests` → returns `CareerProfileResponse`
- `ParseEmailUseCase`: takes `subject`, `body` → returns `EmailActionResponse`
- `GenerateInsightsUseCase`: checks if latest `CareerInsights` is older than 7 days before calling Claude; returns cached result if fresh.

### 4.7 AddJob Screen — Manual Paste with Fit Score

Update `ui/screens/detail/AddJobScreen.kt`:
- Text fields: Company Name, Role Title, Job Description (multi-line), Location, Salary Range.
- "Analyze Fit" button: calls `EvaluateFitUseCase` with the user's stored `resumeText` and the entered job description.
- While loading: show `CircularProgressIndicator`.
- On result: display the score (large number), pros (green list), cons (red list), missing skills (orange list).
- "Save Job" button: saves `JobApplication` with the fit score to Room via `SaveJobApplicationUseCase`.

Create `ui/screens/detail/AddJobViewModel.kt`:
- Inject `EvaluateFitUseCase`, `SaveJobApplicationUseCase`, `UserProfileDataStore`.
- Expose `uiState: StateFlow<AddJobUiState>` with loading, result, and error states.

### Phase 4 Testing Requirements

**Coverage target: >80% of all new code in this phase.**

**Unit tests** (`test/`):

- `ClaudeRepositoryTest`: use `MockWebServer` (OkHttp); for each of the 4 endpoints: enqueue a valid Anthropic Tool Use response (JSON body with `content[0].type = "tool_use"` and the correct `input` object) → verify the correct Kotlin data class is returned; enqueue a response with no `tool_use` block → verify `ClaudeParseException` is thrown; enqueue a 429 rate-limit response → verify the exception is propagated. Verify that each outgoing request body contains the correct `tool_choice.name` for that endpoint.
- `EvaluateFitUseCaseTest`: mock `ClaudeRepository`; verify the use case passes `resumeText` and `jobDescription` correctly; verify it returns the `FitAnalysisResponse` unchanged.
- `GenerateInsightsUseCaseTest`: mock `CareerInsightsRepository` returning insights from 2 hours ago → verify Claude is NOT called (cache hit); mock returning insights from 8 days ago → verify Claude IS called.
- `ClaudeToolSchemasTest`: verify each `ClaudeTool` in `ClaudeTools` has a non-empty `name`, non-empty `description`, and an `input_schema` containing all required field names (e.g., `EVALUATE_FIT` schema contains `"score"`, `"pros"`, `"cons"`, `"missing_skills"` as required properties); verify `PARSE_EMAIL` schema has `"enum"` values for `action_type`.
- `AddJobViewModelTest`: mock `EvaluateFitUseCase` and `SaveJobApplicationUseCase`; verify `uiState` transitions: idle → loading → result; verify error state is emitted on exception.

**Compose UI tests** (`androidTest/`):

- `AddJobScreenTest`: render `AddJobScreen`; enter a job description; mock the ViewModel to emit a `FitAnalysisResponse`; verify score, pros, cons, and missing skills are all visible on screen; verify "Save Job" button triggers the save callback.

**Run after completing this phase:**
```
./gradlew testDebugUnitTest jacocoCoverageVerification
```

### Phase 4 MVP Checkpoint

- [ ] Open AddJob screen, paste a job description, tap "Analyze Fit"
- [ ] Fit score (1–100), pros, cons, and missing skills display correctly
- [ ] Tapping "Save Job" inserts the `JobApplication` with the fit score into Room
- [ ] All 4 Claude endpoints callable — test `analyzeIntent` and `generateInsights` via a temporary debug screen or unit test
- [ ] No API key visible in any log output in release builds
- [ ] `MockWebServer`-backed Claude tests pass; coverage ≥80%

---

## Phase 5 — Profile + Full Job Input

> **MVP goal:** Complete onboarding flow with resume upload. `analyze_intent` runs on first setup. All 3 job input paths work (URL scrape, paste, screenshot placeholder). Duplicate detection active.

### 5.1 File Picker + PDF Extraction

In `ui/screens/profile/ProfileScreen.kt`:
- Add a `ActivityResultLauncher` for `ActivityResultContracts.GetContent("application/pdf")`.
- On PDF selected: call `PdfTextExtractor.extract(uri, context)` in a coroutine.
- Display extracted text preview (first 300 chars) for user confirmation.
- Save `resumeText` to `UserProfileDataStore`.

### 5.2 ProfileViewModel

Create `ui/screens/profile/ProfileViewModel.kt`:
- Inject `UserProfileDataStore`, `AnalyzeIntentUseCase`.
- Expose `profileState: StateFlow<UserProfile>`.
- `fun onResumePicked(uri: Uri, context: Context)`: extract PDF text, update profile, trigger `analyzeIntentIfNeeded()`.
- `fun analyzeIntentIfNeeded()`: if `resumeText` is non-empty and no `CareerProfile` stored yet, call `AnalyzeIntentUseCase` and save result to `UserProfileDataStore.careerGoal` (use the `goal_map` field).
- Expose fields: `fullName`, `keywords`, `careerGoal`, `targetSalaryMin/Max` as editable state.

### 5.3 Complete Profile Screen UI

Update `ui/screens/profile/ProfileScreen.kt`:
- Section: "Your Resume" — PDF upload button, extraction status, preview text.
- Section: "Career Details" — `TextField` for full name, career goal, keywords (comma-separated), salary range.
- Section: "AI Career Summary" — displays `goal_map` from `AnalyzeIntentUseCase` result once available.
- "Save Profile" button persists all changes via `ProfileViewModel`.

### 5.4 Complete Onboarding Flow

Update `ui/screens/onboarding/OnboardingScreen.kt`:
- Step 1: Name input → saves to `UserProfileDataStore`.
- Step 2: Career goal + keywords input.
- Step 3: Resume upload (reuse `ProfileScreen`'s PDF picker logic). Optional — user can skip and add later.
- Step 4: "Connect Gmail" prompt (shows Google Sign-In button). Skip for now — Gmail is wired in Phase 7. Show "Set up later" option.
- On final step completion: set `isOnboardingComplete = true`, navigate to Dashboard.

### 5.5 URL Job Input

In `AddJobScreen`, add a second input mode: "Paste URL":
- `TextField` for URL entry.
- "Fetch" button: use `OkHttpClient` to `GET` the URL, strip HTML tags via regex (`<[^>]*>` → `""`), truncate to 4000 chars, pass to `EvaluateFitUseCase`.
- Handle network errors with a snackbar message.

Create `util/HtmlStripper.kt`:
- `fun stripHtml(html: String): String` — removes HTML tags, decodes common HTML entities (`&amp;`, `&lt;`, `&gt;`, `&nbsp;`), collapses whitespace.

### 5.6 Duplicate Detection on Save

In `SaveJobApplicationUseCase`:
- Before inserting, call `JobApplicationRepository.findDuplicate(companyName, roleTitle)`.
- If a duplicate exists: return `SaveResult.Duplicate(existing)` instead of inserting.
- The ViewModel shows a dialog: "A job at [Company] for [Role] already exists. Update the existing entry or create a new one?"

### Phase 5 Testing Requirements

**Coverage target: >80% of all new code in this phase.**

**Unit tests** (`test/`):

- `HtmlStripperTest`: provide 5 representative HTML strings (with nested tags, entities, scripts, inline styles); verify the stripped output contains only readable plain text and no HTML artifacts.
- `PdfTextExtractorTest` (extended): test with a multi-page PDF asset; verify all pages are concatenated; verify a corrupted PDF returns `null` without throwing.
- `ProfileViewModelTest`: mock `UserProfileDataStore` and `AnalyzeIntentUseCase`; verify `onResumePicked` triggers PDF extraction then `analyzeIntentIfNeeded`; verify `analyzeIntentIfNeeded` does NOT call Claude if `resumeText` is empty; verify profile fields are saved correctly after "Save Profile".
- `AnalyzeIntentUseCaseTest`: mock `ClaudeRepository`; verify it is only called when `resumeText` is non-empty; verify the `goal_map` field is correctly passed to `UserProfileDataStore`.
- `SaveJobApplicationUseCaseTest` (URL path): mock `OkHttpClient` response with HTML content; mock `HtmlStripper`; verify the stripped text is passed to `EvaluateFitUseCase`.

**Compose UI tests** (`androidTest/`):

- `ProfileScreenTest`: verify PDF upload button is visible; mock ViewModel to emit a profile state; verify all fields (name, career goal, keywords, salary) render correctly; verify "Save Profile" button calls the ViewModel save function.
- `DuplicateDetectionTest`: render `AddJobScreen`; mock ViewModel to return `SaveResult.Duplicate`; verify the duplicate confirmation dialog appears with the correct company and role text.

**Run after completing this phase:**
```
./gradlew testDebugUnitTest jacocoCoverageVerification
./gradlew connectedDebugAndroidTest
```

### Phase 5 MVP Checkpoint

- [ ] Complete onboarding: enter name, goal, upload a PDF resume — all fields persist
- [ ] `analyze_intent` fires after PDF upload and populates the "AI Career Summary" section
- [ ] Add a job via URL fetch — job description is extracted and fit score displayed
- [ ] Add a job via manual paste — works as before
- [ ] Attempt to add a duplicate job — duplicate detection dialog appears
- [ ] All unit and UI tests pass; coverage ≥80%

---

## Phase 6 — Full Dashboard UI

> **MVP goal:** Full Kanban board and List view. Job detail screen with editing. Status transitions. Empty states. Insights tab with aggregate stats.

### 6.1 DashboardViewModel

Create `ui/screens/dashboard/DashboardViewModel.kt`:
- Inject `GetAllJobsUseCase`, `GetJobsByStatusUseCase`, `UpdateJobStatusUseCase`, `DeleteJobApplicationUseCase`.
- Expose `uiState: StateFlow<DashboardUiState>`:
  ```kotlin
  data class DashboardUiState(
      val jobsByStatus: Map<ApplicationStatus, List<JobApplication>> = emptyMap(),
      val viewMode: ViewMode = ViewMode.KANBAN,  // KANBAN or LIST
      val isLoading: Boolean = false
  )
  ```
- `fun setStatus(job: JobApplication, newStatus: ApplicationStatus)`.
- `fun deleteJob(job: JobApplication)`.
- `fun setViewMode(mode: ViewMode)`.

### 6.2 Kanban Board

Update `ui/screens/dashboard/DashboardScreen.kt`:
- Top row: view mode toggle (Kanban / List icons) + FAB to navigate to `AddJobScreen`.
- **Kanban view:** Horizontal `LazyRow` of status columns. Each column is a `LazyColumn` of `JobCard` composables.
- Each `JobCard` shows: company name, role title, fit score badge (colored by score range: red <40, yellow 40–70, green >70), status chip, applied date.
- Long-press on a card → bottom sheet with status change options.
- Tap a card → navigate to `JobDetailScreen(jobId)`.

### 6.3 List View

- **List view:** Single `LazyColumn` sorted by `appliedDate` descending.
- Each row: company + role + status + fit score.
- Swipe-to-delete with undo snackbar.
- Filter row at top: `FilterChip` row for status filtering.

### 6.4 Empty State

When `jobsByStatus` is empty:
- Show a centered illustration (use a simple `Icon` from Material Icons Extended) with text "No jobs yet" and a "Add your first job" button that navigates to `AddJobScreen`.

### 6.5 JobDetailViewModel

Create `ui/screens/detail/JobDetailViewModel.kt`:
- Inject `GetAllJobsUseCase`, `SaveJobApplicationUseCase`, `UpdateJobStatusUseCase`, `EvaluateFitUseCase`, `UserProfileDataStore`.
- Load job by ID on init.
- Expose editable fields as `MutableStateFlow`.
- `fun reAnalyzeFit()`: re-calls `EvaluateFitUseCase` with stored resume + job description.
- `fun saveChanges()`.

### 6.6 JobDetailScreen

Update `ui/screens/detail/JobDetailScreen.kt`:
- Top: company name (large), role title, status dropdown.
- Fit score card: large score number + colored ring/progress bar + pros/cons/missing skills expandable sections.
- Editable fields: notes, location, salary range, applied date (date picker), interview date (date picker).
- "Re-analyze Fit" button (calls `reAnalyzeFit()`).
- "Linked Emails" section: list of thread IDs (tappable — opens Gmail intent in Phase 7).
- Delete job button with confirmation dialog.

### 6.7 InsightsViewModel

Create `ui/screens/insights/InsightsViewModel.kt`:
- Inject `GetAllJobsUseCase`, `GenerateInsightsUseCase`, `GetCareerInsightsUseCase`.
- Compute aggregate stats from the jobs flow:
  - Total applied, interviews, rejections, offers.
  - Interview rate: `interviews / applied * 100`.
  - Rejection rate: `rejections / applied * 100`.
  - Top 3 companies by application count.
- Expose `insightsState: StateFlow<InsightsUiState>`.
- `fun refreshInsights()`: calls `GenerateInsightsUseCase` (which checks cache age).

### 6.8 InsightsScreen

Update `ui/screens/insights/InsightsScreen.kt`:
- Stats cards row: Applied / Interviews / Rejections / Offers (colored `Card` composables).
- Interview rate and rejection rate as labeled horizontal progress bars.
- "AI Career Insights" section:
  - Shows last generated date.
  - Lists `identifiedGaps` as warning chips.
  - Lists `recommendedActions` as bulleted text.
  - Shows `summaryAnalysis` in a card.
  - "Refresh Insights" button (disabled if insights are less than 24 hours old).
- Empty state: "Apply to some jobs first to see insights".

### Phase 6 Testing Requirements

**Coverage target: >80% of all new code in this phase.**

**Unit tests** (`test/`):

- `DashboardViewModelTest`: mock `GetAllJobsUseCase` emitting a list of jobs; verify `uiState.jobsByStatus` groups them correctly by `ApplicationStatus`; verify `setStatus` calls `UpdateJobStatusUseCase` with the correct args; verify `deleteJob` calls `DeleteJobApplicationUseCase`; verify `setViewMode` toggles correctly.
- `InsightsViewModelTest`: mock `GetAllJobsUseCase` returning a known list (e.g., 10 APPLIED, 3 INTERVIEWING, 7 REJECTED); verify computed `interviewRate` is 30%, `rejectionRate` is 70%; verify `refreshInsights` calls `GenerateInsightsUseCase` only when `isRefreshEnabled`.
- `JobDetailViewModelTest`: mock all injected use cases; verify `reAnalyzeFit` calls `EvaluateFitUseCase` and updates `uiState.fitAnalysis`; verify `saveChanges` calls `SaveJobApplicationUseCase` with updated fields.

**Compose UI tests** (`androidTest/`):

- `DashboardScreenTest`: seed Room with jobs across all statuses; verify each Kanban column header is visible; verify the correct number of job cards renders in each column; verify tapping a job card navigates to `JobDetailScreen`.
- `JobDetailScreenTest`: provide a job with a known fit score; verify score renders as the correct number; verify changing status via the dropdown persists after navigating back.
- `InsightsScreenTest`: seed with known job data; verify the correct ratio values display; mock ViewModel `refreshInsights`; verify the AI Insights card updates with new content.
- `EmptyStateTest`: render `DashboardScreen` with no jobs; verify the empty state message and "Add your first job" button are visible.

**Run after completing this phase:**
```
./gradlew testDebugUnitTest jacocoCoverageVerification
./gradlew connectedDebugAndroidTest
```

### Phase 6 MVP Checkpoint

- [ ] Kanban board shows all jobs grouped by status with correct colors
- [ ] Tap a job card → detail screen loads with fit score, pros/cons
- [ ] Change job status from detail screen → Kanban updates immediately
- [ ] Swipe-to-delete in list view works with undo
- [ ] Empty state shows on Dashboard and Insights when no data exists
- [ ] Insights tab shows correct aggregate stats; "Refresh Insights" calls Claude and updates the display
- [ ] All unit and Compose UI tests pass; coverage ≥80%

---

## Phase 7 — Gmail Integration

> **MVP goal:** Gmail syncs in background. Emails auto-update job statuses. Interview calendar intent fires. Thread deduplication prevents duplicate entries.

### 7.1 Google Sign-In

In `ui/screens/onboarding/OnboardingScreen.kt` (Step 4) and `ui/screens/profile/ProfileScreen.kt`:
- Use `CredentialManager.create(context)` with a `GetCredentialRequest` containing `GetGoogleIdOption`.
- Configure `GetGoogleIdOption` with `BuildConfig.GOOGLE_CLIENT_ID` and `requestIdToken = true`.
- On success: extract the `GoogleIdTokenCredential`, store the ID token and account email in `UserProfileDataStore` under encrypted keys.
- On failure: show error snackbar; allow skip.

### 7.2 Gmail Retrofit Service

Create `data/remote/GmailApiService.kt`:
```kotlin
interface GmailApiService {
    @GET("gmail/v1/users/me/messages")
    suspend fun listMessages(
        @Header("Authorization") token: String,
        @Query("q") query: String = "newer_than:1d",
        @Query("maxResults") maxResults: Int = 50
    ): GmailListResponse

    @GET("gmail/v1/users/me/messages/{id}")
    suspend fun getMessage(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Query("format") format: String = "full"
    ): GmailMessage
}
```

Create response models in `data/remote/model/GmailModels.kt`:
- `GmailListResponse(messages: List<GmailMessageRef>)`
- `GmailMessageRef(id: String, threadId: String)`
- `GmailMessage(id: String, threadId: String, payload: GmailPayload)`
- `GmailPayload(headers: List<GmailHeader>, body: GmailBody?, parts: List<GmailPart>?)`
- `GmailHeader(name: String, value: String)`
- `GmailBody(data: String?)` — base64url encoded
- `GmailPart(mimeType: String, body: GmailBody?)`

Add a second `Retrofit` instance in `di/NetworkModule.kt` with base URL `https://www.googleapis.com/` — inject it with a qualifier `@Named("gmail")`.

### 7.3 Email Pre-filter Engine

Create `service/EmailPreFilter.kt`:
```kotlin
object EmailPreFilter {
    fun isJobRelated(subject: String, body: String, senderEmail: String): Boolean
    fun classify(subject: String, body: String, senderEmail: String): EmailCategory
}

enum class EmailCategory { APPLIED, REJECTION, INTERVIEW, ALERT, IRRELEVANT }
```

Pre-filter rules (implement as regex patterns):
- **APPLIED:** subject/body contains (case-insensitive): `"thank you for apply"`, `"application received"`, `"we received your application"`, `"successfully submitted"`
- **REJECTION:** `"not moving forward"`, `"other candidates"`, `"position.*filled"`, `"regret to inform"`, `"unfortunately.*not"`, `"not selected"`
- **INTERVIEW:** `"interview"`, `"schedule.*call"`, `"meet with.*team"`, `"calendly\.com"`, `"zoom\.us"`, `"microsoft teams"`, `"google meet"`
- **ALERT:** sender domain matches `linkedin\.com`, `indeed\.com`, `glassdoor\.com`, `lever\.co`, `greenhouse\.io`
- **IRRELEVANT:** anything else (unsubscribe links, marketing)

### 7.4 GmailSyncWorker

Create `service/GmailSyncWorker.kt`:
```kotlin
@HiltWorker
class GmailSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val gmailApiService: GmailApiService,
    private val parseEmailUseCase: ParseEmailUseCase,
    private val saveJobUseCase: SaveJobApplicationUseCase,
    private val jobRepository: JobApplicationRepository,
    private val userProfileDataStore: UserProfileDataStore
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result { ... }
}
```

`doWork()` implementation:
1. Read OAuth token from `UserProfileDataStore`. If null, return `Result.success()` (Gmail not connected).
2. Fetch messages from Gmail API with query `"newer_than:1d"`, max 50 messages.
3. For each message:
   a. Extract subject (from headers) and decode body (base64url decode `payload.body.data` or find text/plain part).
   b. Check `linkedEmailThreadIds` across all jobs — if `threadId` already processed, skip (thread dedup).
   c. Run `EmailPreFilter.isJobRelated()` — if false, skip.
   d. Call `ParseEmailUseCase` → get `EmailActionResponse`.
   e. Based on `action_type`:
      - `APPLIED`: Create new `JobApplication` (status=APPLIED) via `SaveJobApplicationUseCase` (triggers duplicate check).
      - `REJECTION`: Find job by company+role match → call `UpdateJobStatusUseCase(REJECTED)`.
      - `INTERVIEW`: Find job → update status to INTERVIEWING, store `interviewDate` and `interview_link`. Fire `CalendarIntentHelper.prompt()`.
      - `ALERT`: Fire a high-priority notification.
      - `IRRELEVANT`: Skip.
   f. Append `threadId` to the matched/created job's `linkedEmailThreadIds`.
4. Return `Result.success()`. On API error, return `Result.retry()`.

### 7.5 WorkManager Scheduler

Create `service/WorkManagerScheduler.kt`:
```kotlin
object WorkManagerScheduler {
    fun scheduleGmailSync(context: Context) {
        val request = PeriodicWorkRequestBuilder<GmailSyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "gmail_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
```

Call `WorkManagerScheduler.scheduleGmailSync(context)` from `JobAssistantApp.onCreate()` — it only executes if a Gmail token exists.

> **Doze Mode & App Standby:** Android will defer the worker when the device is in Doze Mode or when the app is in an App Standby bucket. The 15-minute interval is a minimum — the actual interval may be significantly longer on idle devices. To provide reliable sync, add a battery optimisation exemption prompt to the Gmail connect flow in ProfileScreen:
> ```kotlin
> val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
>     data = Uri.parse("package:${context.packageName}")
> }
> context.startActivity(intent)
> ```
> Show this as an optional, user-initiated step ("For reliable email sync, allow background activity") — do not show it on first launch as it is considered aggressive by Google Play policy.

### 7.6 Calendar Intent Helper

Create `util/CalendarIntentHelper.kt`:
```kotlin
object CalendarIntentHelper {
    fun buildInsertIntent(title: String, startMillis: Long, description: String, location: String?): Intent {
        return Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, title)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, startMillis + 60 * 60 * 1000L)
            putExtra(CalendarContract.Events.DESCRIPTION, description)
            location?.let { putExtra(CalendarContract.Events.EVENT_LOCATION, it) }
        }
    }
}
```

Show a `AlertDialog` (not auto-firing) asking the user "Add this interview to your calendar?" with the extracted datetime — only launch the intent on user confirmation.

### 7.7 Notification Helper

Create `util/NotificationHelper.kt`:
- Creates a notification channel `"job_alerts"` on first call (required for API 26+).
- `fun showJobAlert(context: Context, title: String, body: String)`: fires a high-priority notification with the alert content.

### 7.8 Update Profile Screen — Gmail Connect Button

In `ui/screens/profile/ProfileScreen.kt`, add a "Gmail" section:
- If not connected: "Connect Gmail" button → triggers the Sign-In flow from step 7.1.
- If connected: shows connected email address + "Sync Now" button (triggers a one-time `GmailSyncWorker` run) + "Disconnect" option.

### Phase 7 Testing Requirements

**Coverage target: >80% of all new code in this phase.**

**Unit tests** (`test/`):

- `EmailPreFilterTest`: provide 20 sample email subjects/bodies (5 per category: APPLIED, REJECTION, INTERVIEW, ALERT, plus 5 IRRELEVANT); verify `classify()` returns the correct `EmailCategory` for each; verify unsubscribe and marketing emails are always classified as IRRELEVANT.
- `GmailSyncWorkerTest`: use `WorkManagerTestInitHelper`; mock `GmailApiService`, `ParseEmailUseCase`, `SaveJobApplicationUseCase`, `JobApplicationRepository`; verify the full pipeline for each action type: APPLIED creates a new job; REJECTION calls `UpdateJobStatusUseCase(REJECTED)`; INTERVIEW triggers a Calendar intent build; ALERT calls `NotificationHelper`; test thread dedup: pre-seed a job with a `threadId` → verify the worker skips that thread on re-sync.
- `CalendarIntentHelperTest`: verify `buildInsertIntent` produces an `Intent` with `ACTION_INSERT`, correct `EXTRA_EVENT_BEGIN_TIME`, and `EXTRA_EVENT_END_TIME` = start + 1 hour.
- `NotificationHelperTest`: verify the notification channel is created with the correct ID and importance level.

**Instrumented tests** (`androidTest/`):

- `GmailSyncWorkerIntegrationTest`: use `WorkManagerTestInitHelper.initializeTestWorkManager`; enqueue a one-time `GmailSyncWorker`; mock `GmailApiService` to return a test APPLIED email; verify the `JobApplication` is inserted into the in-memory Room DB after the worker completes.

**Run after completing this phase:**
```
./gradlew testDebugUnitTest jacocoCoverageVerification
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.jobassistant.service.GmailSyncWorkerIntegrationTest
```

### Phase 7 MVP Checkpoint

- [ ] Sign in with Google from the Profile screen — email address shown as connected
- [ ] Manually trigger "Sync Now" — worker runs and processes recent emails
- [ ] Send yourself a test "Thank you for applying" email — verify a `JobApplication` is auto-created in Room
- [ ] Send yourself a rejection email — verify an existing job's status updates to REJECTED
- [ ] Thread ID deduplication: re-run sync — verify no duplicate entries are created
- [ ] `EmailPreFilterTest` passes all 20 sample emails with correct classification; coverage ≥80%

---

## Phase 8 — OCR + Screenshot Job Input

> **MVP goal:** User can share a screenshot of a job posting → OCR extracts text → fit score displayed → job saved.

### 8.1 ML Kit OCR Processor

Create `util/OcrProcessor.kt`:
```kotlin
class OcrProcessor @Inject constructor() {
    suspend fun extractText(bitmap: Bitmap): String {
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        return suspendCoroutine { cont ->
            recognizer.process(image)
                .addOnSuccessListener { result -> cont.resume(result.text) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }
    }
}
```

### 8.2 Screenshot Input in AddJobScreen

Add a third input tab in `AddJobScreen`: "Screenshot":
- "Pick Screenshot" button: launches `ActivityResultContracts.GetContent("image/*")`.
- Selected image → decoded to `Bitmap` → passed to `OcrProcessor.extractText()`.
- Extracted text shown in a read-only preview box.
- "Analyze Fit" button uses the extracted text as the job description.
- Flow continues identically to manual paste path.

### 8.3 System Share Sheet Target (Optional Enhancement)

In `AndroidManifest.xml`, add a second `<intent-filter>` to `MainActivity` to receive shared images:
```xml
<intent-filter>
    <action android:name="android.intent.action.SEND" />
    <category android:name="android.intent.category.DEFAULT" />
    <data android:mimeType="image/*" />
</intent-filter>
```

In `MainActivity`, handle `intent.action == Intent.ACTION_SEND` on `onCreate` and `onNewIntent`:
- Extract the image URI from `intent.getParcelableExtra(Intent.EXTRA_STREAM)`.
- Navigate directly to `AddJobScreen` with the image URI pre-loaded.

### Phase 8 Testing Requirements

**Coverage target: >80% of all new code in this phase.**

**Unit tests** (`test/`):

- `OcrProcessorTest`: provide a real test `Bitmap` asset with known text content; verify `extractText()` returns a string containing the expected words (use `runTest` + `suspendCoroutine`). Provide a blank/white bitmap; verify the result is an empty string rather than an exception.
- `AddJobViewModelOcrTest`: mock `OcrProcessor` to return a known string; mock `EvaluateFitUseCase`; verify the OCR text is passed directly to `evaluateFit` without modification.

**Compose UI tests** (`androidTest/`):

- `AddJobScreenOcrTabTest`: render `AddJobScreen`; tap the "Screenshot" tab; verify "Pick Screenshot" button is visible; mock the ActivityResult to return a test image URI; mock the ViewModel's OCR state to emit extracted text; verify the text preview box shows the extracted content; verify "Analyze Fit" becomes enabled.

**Run after completing this phase:**
```
./gradlew testDebugUnitTest jacocoCoverageVerification
./gradlew connectedDebugAndroidTest
```

### Phase 8 MVP Checkpoint

- [ ] Pick a screenshot of a job posting from gallery → OCR text extracted and displayed
- [ ] "Analyze Fit" works using OCR-extracted text
- [ ] Job saved correctly with fit score from screenshot input
- [ ] Share a job posting screenshot from the gallery app → app opens directly to AddJob with pre-loaded image
- [ ] OCR unit and UI tests pass; coverage ≥80%

---

## Phase 9 — Polish & Security Hardening

> **MVP goal:** App is release-ready. R8 obfuscation enabled. Export/backup working. All empty states polished.

### 9.1 R8 Full Mode

In `gradle.properties`, add:
```
android.enableR8.fullMode=true
```

Verify `proguard-rules.pro` keeps all Room entities, Retrofit models, and Gson-serialized classes. Run a release build and test all features still function.

### 9.2 Encrypted Export / Backup

Create `util/ExportManager.kt`:
- `suspend fun exportToJson(context: Context, jobs: List<JobApplication>): Uri`:
  - Serialize jobs list to JSON using Gson.
  - Write to a file in `context.getExternalFilesDir(null)` named `"jobassistant_backup_YYYYMMDD.json"`.
  - Return the file URI.
- In `ProfileScreen`, add an "Export Data" button that calls `ExportManager.exportToJson` and fires a share intent with the resulting file URI.

### 9.3 Job Expiry Warning

In `JobDetailScreen` and `JobCard`:
- If `status == SAVED` and `lastSeenDate` is older than 30 days, show a warning chip: "Posting may be expired".

### 9.4 API Key BYOK (Bring Your Own Key)

In `ui/screens/profile/ProfileScreen.kt`, add an "API Settings" section:
- `TextField` for the user to enter their own Anthropic API key (password-style input).
- Save the entered key to `UserProfileDataStore` under `userApiKey`.
- In `NetworkModule`, modify `ApiKeyInterceptor` to prefer `UserProfileDataStore.userApiKey` over `BuildConfig.CLAUDE_API_KEY` if set.

### Phase 9 Testing Requirements

**Coverage target: >80% of all new code in this phase. Total project coverage must also be ≥80%.**

**Unit tests** (`test/`):

- `ExportManagerTest`: mock `JobApplicationRepository` returning a known list; call `exportToJson`; parse the output JSON and verify all fields round-trip correctly (no data loss); verify the output filename contains today's date in `YYYYMMDD` format.
- `ApiKeyInterceptorTest`: set a `userApiKey` in a mock `UserProfileDataStore`; verify the interceptor injects the user's key, not `BuildConfig.CLAUDE_API_KEY`; clear the user key; verify `BuildConfig.CLAUDE_API_KEY` is used as fallback.

**Full regression suite** — run the entire test suite on a release build to verify R8 has not broken anything:

```
./gradlew assembleRelease
./gradlew testReleaseUnitTest jacocoCoverageVerification
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.jobassistant.AllTestsSuite
```

**Coverage report** — generate and inspect the HTML report to confirm overall project coverage:
```
./gradlew jacocoTestReport
open app/build/reports/jacoco/jacocoTestReport/html/index.html
```

### Phase 9 Final Verification

- [ ] Release build compiles without errors
- [ ] All 8 phase MVP checkpoints still pass on the release build
- [ ] R8 obfuscation does not break Room, Retrofit, or Gson deserialization
- [ ] Export produces a valid JSON file that can be re-imported (or at least read)
- [ ] Job expiry warning shows correctly on old SAVED jobs
- [ ] BYOK key overrides the default API key correctly
- [ ] Full test suite passes on release build; overall project coverage ≥80%
- [ ] JaCoCo HTML report generated and reviewed

---

## Phase 10 — UI/UX Overhaul

> **MVP goal:** The app looks and feels polished enough to show to a recruiter. Every screen uses edge-to-edge layout, a consistent 4dp spacing grid, proper system-inset handling, smooth transitions, and the upgraded shared component library. No functional regressions from Phases 1–9.

---

### 10.1 Edge-to-Edge Layout

In `ui/MainActivity.kt`:
- Call `enableEdgeToEdge()` (from `androidx.activity:activity`) immediately before `setContent { … }`.
- Remove the outer `Column { ThemeSelector(…); AppNavigation(…) }` wrapper — the theme selector is being relocated to the Profile screen in step 10.3.
- Replace it with a direct call to `AppNavigation(…)` inside `setContent { JobAssistantTheme(…) { … } }`.

In `ui/navigation/AppNavigation.kt`:
- Pass `WindowInsets.systemBars` or use `Scaffold`-level `contentWindowInsets` so every screen handles status-bar and navigation-bar padding automatically.
- The bottom navigation bar must add `navigationBarsPadding()` so it sits above the gesture nav bar.

In every screen's `Scaffold`:
- Set `contentWindowInsets = ScaffoldDefaults.contentWindowInsets` (the default) — do **not** manually add `statusBarsPadding()` in screen bodies; let the `Scaffold` handle it.

---

### 10.2 Shared UI Component Library

Create the following new components in `ui/components/`. Each is a pure, stateless `@Composable` function with no ViewModel dependency.

#### `CompanyAvatar.kt`
A 40×40dp circle showing the first letter of the company name in a tonal container:
```kotlin
@Composable
fun CompanyAvatar(companyName: String, modifier: Modifier = Modifier) {
    val letter = companyName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    // Deterministically pick a container color from the theme palette based on letter index
    // so the same company always gets the same color without storing it.
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold
        )
    }
}
```

#### `FitScoreRing.kt`
A Canvas-drawn circular arc indicator that animates from 0 to the target score:
```kotlin
@Composable
fun FitScoreRing(
    score: Int?,           // null = not yet analyzed
    size: Dp = 72.dp,
    strokeWidth: Dp = 7.dp,
    modifier: Modifier = Modifier
) {
    // Animate the sweep angle from 0 to (score/100 * 270) degrees using animateFloatAsState
    // Draw a background track arc (270° sweep, starting at 135°, grey tint)
    // Draw the filled arc on top using fitScoreColor(score) — same color logic as Phase 6
    // Draw the score as a centered Text inside the ring; "N/A" if score == null
}
```
The arc spans 270° (135° start, sweeping clockwise) so it looks like a gauge.

#### `RelativeTimeText.kt`
Formats a nullable `Long` epoch-millis timestamp as a human-readable relative string:
- < 1 hour → "just now"
- < 24 hours → "X hours ago"
- < 7 days → "X days ago"
- ≥ 7 days → formatted as "MMM d" (e.g. "Mar 15")

```kotlin
@Composable
fun RelativeTimeText(epochMillis: Long?, style: TextStyle, color: Color, modifier: Modifier = Modifier)
```

#### `SectionHeader.kt`
A styled section divider used consistently across Profile and Insights:
```kotlin
@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 1.2.sp,
        modifier = modifier.padding(bottom = 8.dp)
    )
}
```

#### `StatusChip.kt`
A colored `SuggestionChip`-style component that maps each `ApplicationStatus` to a distinct tonal color pair (container + label):
- SAVED → surfaceVariant
- APPLIED → primaryContainer
- INTERVIEWING → tertiaryContainer
- OFFERED → `Color(0xFF43A047)` tinted container
- REJECTED → errorContainer

```kotlin
@Composable
fun StatusChip(status: ApplicationStatus, modifier: Modifier = Modifier)
```

---

### 10.3 Theme Selector — Move to Profile Screen

**Remove** from `MainActivity`:
- Delete the `ThemeSelector` row that wraps `AppNavigation`. The top-level layout is now just `AppNavigation` inside `JobAssistantTheme`.

**Add** to `ui/screens/profile/ProfileScreen.kt`:
- Add a new section at the top of the Profile screen: **"Appearance"**, above the Resume section.
- Display the 4 theme options as a horizontal `Row` of 40×40dp filled `Box` circles, each using its seed color (`ThemeSeedColors[theme]`). The active theme gets a white checkmark icon `Icons.Filled.Check` centered inside the circle.
- Wire it to `viewModel.setTheme(theme)` (add `fun setTheme(theme: AppTheme)` to `ProfileViewModel` — delegates to `userProfileDataStore.update { copy(selectedTheme = theme) }`).
- The `MainViewModel.uiState.selectedTheme` still drives `JobAssistantTheme` — the Profile screen only needs to call a `setTheme` callback passed down from `AppNavigation` or collected from `MainViewModel` via a shared `hiltViewModel()` call in `ProfileScreen`.

Update `ui/navigation/AppNavigation.kt`:
- Pass `onThemeSelected = mainViewModel::setTheme` into `ProfileScreen` (or let `ProfileScreen` inject `MainViewModel` directly using `hiltViewModel<MainViewModel>()`).

---

### 10.4 Dashboard — Hero Stats Strip

In `ui/screens/dashboard/DashboardScreen.kt`, add a **`HeroStatsStrip`** composable rendered directly below the `TopAppBar` and above the Kanban/List content, inside the `Scaffold` body (not in `topBar`):

```kotlin
@Composable
private fun HeroStatsStrip(jobsByStatus: Map<ApplicationStatus, List<JobApplication>>) {
    // A horizontally-scrolling Row of mini stat cards (no scroll indicator needed)
    // Show: Total, Saved, Applied, Interviewing, Offered, Rejected — each as a compact
    // Card (60dp tall) with a large bold number and a small label below it.
    // "Total" card uses primaryContainer, others use their StatusChip colors.
}
```

This strip is always visible in both Kanban and List modes.

---

### 10.5 Dashboard — Upgraded Job Cards

Update `JobCard` in `DashboardScreen.kt`:

**Before** (current):
- Company name text
- Role title text
- FitScoreBadge (text badge) + date text

**After**:
```
Row {
    CompanyAvatar(companyName)        ← new: 40×40dp circle avatar
    Column {
        Text(companyName, titleSmall, bold)
        Text(roleTitle, bodySmall, onSurfaceVariant)
        Row {
            StatusChip(job.status)    ← new: replaces the implicit status from column header
            Spacer(weight=1f)
            RelativeTimeText(appliedDate)  ← new: "2 days ago" instead of "Mar 15"
        }
    }
    FitScoreRing(score, size=48.dp)   ← new: ring replaces text badge
}
```

Update `ListJobRow` similarly — lead with `CompanyAvatar` on the left, trail with a small `FitScoreRing(size=40.dp)`.

---

### 10.6 Dashboard — Status Change Bottom Sheet Upgrade

Update `StatusChangeSheet` to use icon-labelled rows instead of plain `TextButton`s:

```kotlin
STATUS_ORDER.forEach { status ->
    val isCurrentStatus = status == job.status
    ListItem(
        headlineContent = { Text(status.displayName()) },
        leadingContent = { StatusChip(status) },
        trailingContent = {
            if (isCurrentStatus) Icon(Icons.Filled.Check, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary)
        },
        modifier = Modifier.clickable { onStatusSelected(status) }
    )
}
```
Add a `HorizontalDivider()` at the top of the sheet below the drag handle, before listing statuses.

---

### 10.7 Job Detail — Fit Score Ring + Collapsing Sections

In `ui/screens/detail/JobDetailScreen.kt`:

**Score card** — replace the "large score number + colored ring/progress bar" placeholder spec with the real `FitScoreRing`:
```kotlin
// Centered in a Card at the top of the detail screen:
Card(modifier = Modifier.fillMaxWidth()) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
        FitScoreRing(score = job.fitScore, size = 96.dp, strokeWidth = 10.dp)
        Spacer(Modifier.height(8.dp))
        Text("Fit Score", style = MaterialTheme.typography.labelMedium)
    }
}
```

**Pros / Cons / Missing Skills** — replace flat text lists with collapsible `AnimatedVisibility` sections:
```kotlin
// Each section: a clickable Row header (label + count badge + expand/collapse icon)
// that toggles AnimatedVisibility { content }
// Pros: green left-border indicator, each item as a Row with Icons.Filled.CheckCircle
// Cons: amber left-border indicator, each item as a Row with Icons.Filled.RemoveCircle
// Missing Skills: blue chip (SuggestionChip) in a FlowRow
```

**Bottom action bar** — add a `BottomAppBar` (not a floating button) with:
- Primary action: `Button("Re-analyze Fit")` (full width minus padding)
- Secondary: `IconButton(Icons.Filled.Delete)` with a confirmation `AlertDialog`

---

### 10.8 Add Job — Animated Score Reveal + Tab Upgrade

In `ui/screens/detail/AddJobScreen.kt`:

**Tab indicator**: Replace the existing tab implementation with `TabRow` using a custom `indicator` that draws a `Box` with `Modifier.tabIndicatorOffset()` using `MaterialTheme.colorScheme.primary` as the fill color with `clip(RoundedCornerShape(topStart=3.dp, topEnd=3.dp))`.

**Character counter** on the job description `OutlinedTextField`:
```kotlin
OutlinedTextField(
    value = jobDescription,
    onValueChange = { if (it.length <= 4000) jobDescription = it },
    label = { Text("Job Description") },
    supportingText = { Text("${jobDescription.length} / 4000") },
    minLines = 6,
    maxLines = 12,
    ...
)
```

**Score reveal animation**: Wrap the `FitScoreRing` + pros/cons section in `AnimatedVisibility(visible = fitResult != null, enter = fadeIn() + scaleIn(initialScale = 0.85f))` so the results card grows into view after the API call completes.

**Loading state**: Replace `CircularProgressIndicator` with a `LinearProgressIndicator` spanning the full card width while the API call is in progress, with the text "Analyzing fit…" below it.

---

### 10.9 Profile Screen — Section Cards + Appearance Section

In `ui/screens/profile/ProfileScreen.kt`:

**Wrap each section** in a `Card(modifier = Modifier.fillMaxWidth())` with 16dp internal padding and a `SectionHeader` title:
- "Appearance" → theme color picker (from 10.3)
- "Resume" → PDF upload + status
- "Career Details" → name / goal / keywords / salary fields
- "AI Career Summary" → goal map card
- "Data" → export button
- "API Settings" → API key field
- "Gmail Integration" → connect/disconnect

**Resume uploaded state**: replace the plain text preview with a styled file card:
```kotlin
// When resumeText is not blank:
Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.PictureAsPdf, contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer)
        Spacer(Modifier.width(12.dp))
        Column {
            Text("Resume loaded", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer)
            Text("${profileState.resumeText.length} characters",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
        }
    }
}
```

**Top of screen**: Add a centered `CompanyAvatar`-style avatar for the user's initials (use `fullName`), 56×56dp, above the Appearance section. Show `fullName` as a `titleLarge` below the avatar.

---

### 10.10 Insights Screen — Icon Stats + Funnel Row

In `ui/screens/insights/InsightsScreen.kt`:

**Stat cards**: Add a leading `Icon` to each `StatCard`:
```kotlin
// Applied → Icons.Filled.Send
// Interviews → Icons.Filled.Event
// Rejected → Icons.Filled.Cancel
// Offers → Icons.Filled.EmojiEvents
```
Each card: icon (24dp, tinted with the card's color) + number + label stacked vertically.

**Funnel visualization**: Add a `FunnelRow` composable between the stats cards and the rates card:
```kotlin
// A horizontal Row that shows:  Applied → [arrow icon] → Interviewing → [arrow icon] → Offered
// Each stage is a Box with a percentage label below it (e.g. "30% interview rate")
// Use thin connecting arrows (Icons.AutoMirrored.Filled.ArrowForward, 16dp)
// Container uses MaterialTheme.colorScheme.surfaceVariant, corner radius 8dp
```

**Recommended actions**: Change from bullet `Text` items to `Card`-wrapped rows with a leading `Icons.Filled.Lightbulb` icon, each action in its own mini card:
```kotlin
it.recommendedActions.forEach { action ->
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Lightbulb, contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(action, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer)
        }
    }
}
```

---

### 10.11 Onboarding — Step Indicator + Welcome Illustration

In `ui/screens/onboarding/OnboardingScreen.kt`:

**Step indicator dots**: Below the pager content and above the navigation buttons, add a `Row` of dot indicators:
```kotlin
Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    repeat(totalSteps) { index ->
        val isActive = index == currentStep
        Box(
            modifier = Modifier
                .height(8.dp)
                .width(if (isActive) 24.dp else 8.dp)   // active dot is pill-shaped
                .clip(CircleShape)
                .background(
                    if (isActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
                .animateContentSize()   // smooth width transition
        )
    }
}
```

**Welcome step illustration**: On Step 1 (Welcome + name input), add a large centered `Icon` using `Icons.Filled.WorkspacePremium` (96dp, tinted `primary`) above the title text. Each step should have a distinct icon:
- Step 1 (Welcome): `Icons.Filled.WorkspacePremium`
- Step 2 (Career goal): `Icons.Filled.TrendingUp`
- Step 3 (Resume upload): `Icons.Filled.UploadFile`
- Step 4 (Gmail): `Icons.Filled.Email`

**Navigation buttons**: Replace plain `Button("Next")` / `Button("Get Started")` with a `Row` that also shows a back `TextButton` on steps 2+ so users can go back:
```kotlin
Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
    if (currentStep > 0) TextButton(onClick = { goBack() }) { Text("Back") }
    else Spacer(Modifier.weight(1f))
    Button(onClick = { goForward() }) {
        Text(if (currentStep == lastStep) "Get Started" else "Next")
    }
}
```

---

### 10.12 Global Animations & Transitions

Apply the following consistently where state transitions exist:

1. **Screen transitions in `NavHost`**: Set `enterTransition = fadeIn(tween(200))` and `exitTransition = fadeOut(tween(200))` as defaults in the `NavHost` call in `AppNavigation.kt`.

2. **FitScoreRing animation**: Uses `animateFloatAsState(targetValue = sweepAngle, animationSpec = tween(durationMillis = 800, easing = EaseOutCubic))` for the arc sweep.

3. **Score card reveal** (AddJob): `AnimatedVisibility(enter = fadeIn() + scaleIn(initialScale = 0.85f))` — already specified in 10.8.

4. **Collapsing sections** (Job Detail): `AnimatedVisibility(enter = expandVertically(), exit = shrinkVertically())`.

5. **Kanban card additions**: Use `animateItemPlacement()` modifier on `LazyColumn` items so cards animate when their status changes and they move columns.

6. **Profile section cards**: Wrap the main `Column` scroll content in `AnimatedContent(targetState = profileUiState)` for the loading/result/error state so transitions are smooth.

---

### Phase 10 Testing Requirements

**Coverage target: >80% of all new UI component code.**

**Unit tests** (`test/`):

- `RelativeTimeTextTest`: verify all time brackets: < 1 hour, < 24 hours, < 7 days, ≥ 7 days, and null → "—".
- `FitScoreRingTest` (logic only, not Canvas): verify the sweep angle calculation from score: `score=0` → `0f`, `score=50` → `135f` (50% of 270°), `score=100` → `270f`, `score=null` → `0f`.
- `StatusChipColorTest`: verify each `ApplicationStatus` maps to a distinct, non-null container color.
- `CompanyAvatarLetterTest`: verify first letter extraction handles empty string (`"?"`) and single-char company names.

**Compose UI tests** (`androidTest/`):

- `CompanyAvatarTest`: render `CompanyAvatar("Google")` → verify text "G" is displayed.
- `FitScoreRingUiTest`: render `FitScoreRing(score = 75)` → verify the text "75" is displayed inside; render with `score = null` → verify "N/A" is displayed.
- `HeroStatsStripTest`: render `DashboardScreen` with a seeded list of known jobs → verify the strip shows the correct count for each status.
- `ThemePickerTest`: render `ProfileScreen`; verify 4 colored circles are present in the Appearance section; tap one → verify `MainViewModel.setTheme` is called with the correct `AppTheme`.
- `OnboardingStepIndicatorTest`: verify step dots render correctly — first dot is pill-shaped (wider) on step 0, second dot becomes pill-shaped on step 1; verify back button is hidden on step 0 and visible on step 1+.
- `ScoreRevealAnimationTest`: mock `AddJobViewModel` to emit an `Idle` state then a `Result` state; verify the score card composable is not visible before the result and becomes visible after.

**Run after completing this phase:**
```
./gradlew testDebugUnitTest jacocoCoverageVerification
./gradlew connectedDebugAndroidTest
```

### Phase 10 MVP Checkpoint

- [ ] App runs edge-to-edge — status bar icons and gesture bar are visible through the app background
- [ ] Theme selector is gone from the top bar; color picker appears in Profile → Appearance section; selecting a color immediately re-themes the whole app
- [ ] Dashboard hero strip shows correct job counts
- [ ] Job cards show `CompanyAvatar`, `StatusChip`, `RelativeTimeText`, and `FitScoreRing`
- [ ] Status change bottom sheet shows current status with a checkmark
- [ ] Job detail fit score ring animates from 0 to the score value on open
- [ ] Pros/cons/missing-skills sections are expandable and collapsed by default
- [ ] Add Job score card fades/scales in after analysis completes
- [ ] Profile screen has grouped section cards and a resume file card
- [ ] Insights screen shows icon-labelled stat cards and the funnel row
- [ ] Onboarding shows step dots, back button on step 2+, and a step illustration icon
- [ ] All screen transitions use fade animation (no jarring cuts)
- [ ] All unit and Compose UI tests pass; coverage ≥80%

---

## Cross-Cutting Rules (apply in every phase)

1. **Never log sensitive data:** `resumeText`, email bodies, OAuth tokens, or API keys must never appear in `Log.*` calls.
2. **Always inject via Hilt:** No manual `object` singletons for anything with dependencies. Use `@Singleton` scoped Hilt bindings.
3. **All DB and network operations in coroutines:** No blocking calls on the main thread. Use `Dispatchers.IO` for Room and Retrofit.
4. **StateFlow for UI state:** Every ViewModel exposes a single `uiState: StateFlow<UiState>` collected with `collectAsStateWithLifecycle()`.
5. **Error handling:** All Claude API calls and Gmail API calls must handle `IOException`, `HttpException`, and JSON parse failures — show user-facing error messages via snackbar, never crash.
6. **Duplicate detection active from Phase 2:** The `SaveJobApplicationUseCase` must call `findDuplicate` before every insert for the entire life of the project.
7. **Pre-filter before every Claude email call:** `EmailPreFilter.isJobRelated()` must return `true` before `ParseEmailUseCase` is ever called.
