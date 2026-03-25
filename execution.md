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

## Phase 11 — Insights Screen: Tabbed Layout

> **MVP goal:** The Insights screen is restructured into three focused tabs — **Career Profile**, **New Job**, and **Applied Jobs** — so each audience question has a clear home. No new API calls are introduced; all data comes from existing ViewModel flows.

---

### 11.1 InsightsUiState — Add UserProfile

Update `InsightsUiState` in `InsightsViewModel.kt` to carry the logged-in user's profile:

```kotlin
data class InsightsUiState(
    val stats: InsightsStats = InsightsStats(),
    val insights: CareerInsights? = null,
    val isRefreshing: Boolean = false,
    val isRefreshEnabled: Boolean = true,
    val error: String? = null,
    val errorType: ApiErrorType? = null,
    val retryAvailableAt: Long? = null,
    val userProfile: UserProfile = UserProfile()   // ← new
)
```

Import `com.jobassistant.domain.model.UserProfile`.

---

### 11.2 InsightsViewModel — Combine UserProfile Flow

Update `observeData()` to also observe `userProfileDataStore.userProfileFlow`, combining it with the existing jobs + insights combine:

```kotlin
private fun observeData() {
    viewModelScope.launch {
        combine(
            getAllJobsUseCase(),
            getCareerInsightsUseCase(),
            userProfileDataStore.userProfileFlow
        ) { jobs, insights, profile ->
            Triple(jobs, insights, profile)
        }.collect { (jobs, insights, profile) ->
            val stats = computeStats(jobs)
            val isRefreshEnabled = insights == null ||
                (System.currentTimeMillis() - insights.generatedDate) >= REFRESH_COOLDOWN_MS
            _uiState.value = _uiState.value.copy(
                stats = stats,
                insights = insights,
                isRefreshEnabled = isRefreshEnabled,
                userProfile = profile
            )
        }
    }
}
```

No other ViewModel changes required.

---

### 11.3 InsightsScreen — Three-Tab Layout

Rewrite `InsightsScreen.kt` with a `PrimaryTabRow` (3 tabs) above the content area. Use a remembered `selectedTab: Int` and a `when` block to switch content — no `HorizontalPager` needed.

```
Scaffold
  topBar: TopAppBar("Insights")
  content:
    Column {
      PrimaryTabRow(selectedTabIndex) {
        Tab("Career Profile")
        Tab("New Job")
        Tab("Applied Jobs")
      }
      when (selectedTab) {
        0 -> CareerProfileTab(uiState)
        1 -> NewJobTab(uiState, onRefresh)
        2 -> AppliedJobsTab(uiState)
      }
    }
```

---

### 11.4 Tab 0 — Career Profile

`CareerProfileTab` is a scrollable `Column` that gives a descriptive view of the user's professional identity:

**User header:**
- `CompanyAvatar(fullName, size = 56.dp)` centered
- `fullName` as `titleLarge` below avatar
- If `fullName` is blank, show "Complete your profile in Settings"

**Resume summary card:**
- `SectionHeader("Resume")`
- If `resumeText` is blank: "No resume uploaded yet — go to Profile to upload your PDF"
- If `resumeText` is not blank:
  - Word count: `resumeText.split("\\s+".toRegex()).size` → "~N words"
  - Character count: `resumeText.length` characters
  - First 400 chars of resume as a faded preview in a `surfaceVariant` Card

**Career interests card:**
- `SectionHeader("Career Interests")`
- Career goal text (`careerGoal`) — full text in a readable `bodyMedium` block. If blank, prompt to add.
- Keywords as a `FlowRow` of `SuggestionChip` items. If empty, prompt to add.
- Target salary range: if both > 0, show "£X – £Y" (or "$X – $Y"); otherwise "Not set"

**AI career summary card** (only if `careerGoal` is not blank and looks AI-generated — heuristic: length > 80 chars):
- `SectionHeader("AI Career Summary")`
- Full `careerGoal` text in a `secondaryContainer` Card
- Caption: "Generated from your resume by AI"

---

### 11.5 Tab 1 — New Job

`NewJobTab` is an action-oriented view to inform the user's next job application, sourced entirely from `CareerInsights`.

**When no insights exist:**
- Centered empty state: `Icons.Filled.AutoAwesome` (96dp), title "No insights yet", subtitle "Tap Refresh to generate AI recommendations based on your job search history"
- Full-width `Button("Generate Insights")` calling `onRefresh`

**When insights exist:**

```
SectionHeader("Identified Gaps")
FlowRow of SuggestionChip per gap (Warning icon, errorContainer colors)

SectionHeader("What to Improve")
Column of lightbulb action cards (tertiaryContainer) — one per recommendedAction

SectionHeader("Market Feedback")
Card(surfaceVariant) { Text(summaryAnalysis) }

Row(SpaceBetween) {
    Text("Last updated: MMM d, yyyy")
    Button("Refresh", enabled = isRefreshEnabled && !isRefreshing)
}
```

Show `LinearProgressIndicator` spanning full width while `isRefreshing`.

For typed errors (AUTH / RATE_LIMIT) show inline error text below the refresh button using `MaterialTheme.colorScheme.error`.

---

### 11.6 Tab 2 — Applied Jobs

`AppliedJobsTab` shows the historical analytics for submitted applications.

**When `totalApplied == 0`:**
- Centered empty state: `Icons.Filled.BarChart` (80dp), "Apply to some jobs first to see stats"

**When data exists:**
- `StatsCardsRow` (Applied / Interviews / Rejected / Offers — existing component)
- `FunnelRow` (Applied → Interviews → Offers — existing component)
- `RateSection` (Interview Rate + Rejection Rate bars — existing component)
- **Top Companies card** (new):
  ```
  SectionHeader("Top Companies")
  // For each of topCompanies (up to 3):
  Row {
      CompanyAvatar(companyName, size = 32.dp)
      Text(companyName, bodyMedium, bold)
      Spacer(weight=1f)
      Text("N application(s)", labelSmall, onSurfaceVariant)
  }
  ```
  If `topCompanies` is empty, omit this card.

---

### Phase 11 Testing Requirements

**Unit tests** (`test/`):

- `InsightsViewModelTabTest`: mock `UserProfileDataStore` returning a known `UserProfile`; verify `uiState.userProfile.fullName` propagates correctly from the DataStore flow after `observeData()` runs; verify `uiState.stats` and `uiState.userProfile` update together in a single emission.

**Run after completing this phase:**
```
./gradlew testDebugUnitTest jacocoCoverageVerification
./gradlew assembleDebug && adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Phase 11 MVP Checkpoint

- [ ] Insights screen shows 3 tabs: Career Profile / New Job / Applied Jobs
- [ ] Career Profile tab shows resume word count, career goal, keywords as chips, AI summary card
- [ ] New Job tab shows identified gaps, recommended actions, market feedback and Refresh button
- [ ] Applied Jobs tab shows stats cards, funnel, rates, and top companies
- [ ] Empty states show correctly on each tab when no data exists
- [ ] Switching tabs is instant with no recomposition lag
- [ ] All existing Phase 10 checkpoints still pass

---

## Phase 12 — Decouple "Add Job" from "Analyze Fit" (Progressive Disclosure)

> **MVP goal:** "Add Job" is a fast, frictionless tracking form. Fit analysis is a deliberate action that lives on the job detail screen after the job is saved. Users discover the fit score naturally when they open a job they want to investigate.

### Background & Rationale

The current flow conflates two distinct user intents on a single screen:
- **Tracking intent** — "I found a role, I want to log it" → fast, no AI needed
- **Evaluation intent** — "I want to know if this role is worth pursuing" → deliberate, AI-powered

Mixing them means the AI analysis gate blocks a simple save, and the "Add Job" label implies bookmarking but forces the user through an analysis step they may not want yet. The redesign separates these into two stages: **save first, score later**.

---

### 12.1 Simplify `AddJobScreen` — Tracking-Only Form

Remove all AI/analysis content from `AddJobScreen`. It becomes a clean 4-field tracking form:

**Fields to keep:**
- Company Name (required)
- Role Title (required)
- Location (optional)
- Salary Range (optional)

**Fields to remove from this screen:**
- Job Description text field (moves to Job Detail)
- Job URL field (moves to Job Detail)
- Screenshot / OCR tab (moves to Job Detail)
- "Analyze Fit" button
- "Fetch & Analyze" button
- `FitResultSection` (score + pros/cons)
- `DuplicateJobDialog` → keep but trigger from the save action directly

**New primary action:** A single **"Save Job"** button at the bottom. On tap:
1. Call `SaveJobApplicationUseCase` — creates the job with `status = SAVED`, `fitScore = null`
2. On success: navigate directly to the new job's `JobDetailScreen` (pass the saved job ID)
3. On duplicate detected: show `DuplicateJobDialog` inline

**ViewModel changes (`AddJobViewModel`):**
- Remove `analyzeFit()`, `fetchAndAnalyzeUrl()`, `processScreenshot()` — these move to `JobDetailViewModel`
- Remove `AddJobUiState.Analyzing`, `AddJobUiState.FitResult` states
- Simplify to: `Idle`, `Saving`, `Saved(jobId: UUID)`, `Duplicate(…)`, `Error`
- `Saved` state now carries the new job ID so the screen can navigate to it

**Navigation change in `AppNavigation.kt`:**
- When `AddJobScreen` emits `AddJobUiState.Saved(jobId)`, navigate to `Screen.JobDetail.createRoute(jobId.toString())` and pop `AddJobScreen` off the back stack

---

### 12.2 Pending Fit Score State — `FitScoreRing` Shows `?`

The `FitScoreRing` composable already handles `score = null` by rendering "N/A". Change that label to **"?"** to signal "not yet scored" rather than "no score available", making the ring feel like an invitation to act.

Update in `ui/components/FitScoreRing.kt`:
```kotlin
text = if (score != null) "$score" else "?"
```

On the `JobCard` in `DashboardScreen`, the `?` ring acts as a visual affordance — users notice the incomplete ring and know there's something to do.

---

### 12.3 Expand `JobDetailScreen` — Full Evaluation Hub

`JobDetailScreen` becomes the place where users supply the job description and trigger analysis. Move the three input paths here.

**Add a new "Job Description" section** to `JobDetailScreen`, positioned between the fit score card and the editable fields:

```
── Fit Score (FitScoreRing) ──────────────────────────────────────
   [?  ring until analyzed]

── Job Description ───────────────────────────────────────────────
   TabRow: [ Paste Text | Paste URL | Screenshot ]
   [appropriate input for selected tab]

   Button: "Analyze Fit"   ← primary action, full width

── Notes / Location / Salary / Dates ────────────────────────────
```

**Tab content (moved from `AddJobScreen`):**
- **Paste Text** — `OutlinedTextField` (multiline, 4000-char counter, `supportingText`)
- **Paste URL** — URL field + "Fetch & Analyze" button
- **Screenshot** — image picker + OCR preview + "Analyze Fit" button

The job description entered here is also saved to `job.notes` (or a new dedicated `jobDescription` field — see 12.4) so it persists and pre-fills on subsequent opens.

**ViewModel changes (`JobDetailViewModel`):**
- Inject `EvaluateFitUseCase`, `OcrProcessor`, `FetchUrlUseCase`
- Add `jobDescriptionTab: Int` state (0/1/2 for Paste/URL/Screenshot)
- Add `jobDescription: MutableStateFlow<String>` — pre-filled from saved job on load
- Add `ocrText: StateFlow<String>` (from OcrProcessor)
- `fun analyzeFromPaste(text: String)` — calls `EvaluateFitUseCase`, updates `fitAnalysis` + saves score to job
- `fun analyzeFromUrl(url: String)` — fetches, strips HTML, calls `EvaluateFitUseCase`
- `fun analyzeFromScreenshot(uri: Uri, context: Context)` — runs OCR, calls `EvaluateFitUseCase`
- All three paths auto-save the fit score to Room on success via `SaveJobApplicationUseCase`

**BottomAppBar update:**
- Remove the standalone "Re-analyze Fit" button from `BottomAppBar` — analysis is now triggered from within the Job Description section
- Keep the Delete `IconButton` in `BottomAppBar`

---

### 12.4 Persist Job Description on the `JobApplication` Entity

The job description needs a home in the data model so it pre-fills when the user reopens a job.

**Domain model change (`JobApplication.kt`):**
Add field:
```kotlin
val jobDescription: String = ""
```

**Room entity change (`JobApplicationEntity.kt`):**
Add the same field. This requires a database migration.

**Migration** — add a new migration in `AppDatabase`:
```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE job_applications ADD COLUMN jobDescription TEXT NOT NULL DEFAULT ''")
    }
}
```
Bump `@Database(version = 2)` and register the migration in `DatabaseModule`.

**Mapper update** — map `jobDescription` in both directions in `JobApplicationMapper`.

**DAO / Repository** — no changes required; `upsert` handles the new field automatically.

---

### 12.5 Remove Input-Mode Tabs from `AddJobScreen` — Clean Up `AddJobViewModel`

After moving the analysis logic to `JobDetailViewModel`:

- Delete `InputMode` enum from `AddJobScreen.kt`
- Delete `AddJobViewModelOcrTest` test cases that test OCR path through `AddJobViewModel` (they will be re-written for `JobDetailViewModel` in the testing section)
- The `OcrProcessor` and `FetchUrlUseCase` injections move from `AddJobViewModel` to `JobDetailViewModel`

---

### Phase 12 Testing Requirements

**Unit tests** (`test/`):

- `AddJobViewModelSimplifiedTest`: verify `saveJob()` creates a `JobApplication` with `fitScore = null` and `status = SAVED`; verify `Saved(jobId)` state is emitted after a successful save; verify `Duplicate` state is emitted when `findDuplicate` returns a match.
- `JobDetailViewModelAnalysisTest`: mock `EvaluateFitUseCase`; verify `analyzeFromPaste(text)` calls `EvaluateFitUseCase` with the user's `resumeText` and the supplied text; verify the returned `FitAnalysis` updates `uiState.fitAnalysis` and auto-saves the score to Room; verify `analyzeFromUrl(url)` calls `FetchUrlUseCase` then `EvaluateFitUseCase` in sequence; verify `analyzeFromScreenshot` runs OCR then `EvaluateFitUseCase`.
- `JobApplicationMigrationTest`: use `MigrationTestHelper`; verify `MIGRATION_1_2` runs without error on a v1 database; verify existing rows retain their data and the new `jobDescription` column defaults to `""`.

**Instrumented tests** (`androidTest/`):

- `AddJobScreenSimplifiedTest`: render the new `AddJobScreen`; verify only Company Name, Role Title, Location, Salary Range fields are present; verify the "Save Job" button is present; verify "Analyze Fit" button is NOT present; verify tapping "Save Job" with a company name and role navigates to `JobDetailScreen`.
- `JobDetailAnalysisTest`: open a job with `fitScore = null`; verify the `FitScoreRing` shows "?"; enter a job description in the Paste Text tab; tap "Analyze Fit"; verify the ring updates with the mocked score.

**Run after completing this phase:**
```
./gradlew testDebugUnitTest jacocoCoverageVerification
./gradlew connectedDebugAndroidTest
```

### Phase 12 MVP Checkpoint

- [ ] "Add Job" screen has 4 fields only — company, role, location, salary. No description, no URL, no screenshot tab, no Analyze button.
- [ ] Saving a new job navigates directly to its `JobDetailScreen`
- [ ] New jobs show `?` in the `FitScoreRing` on the Dashboard
- [ ] Job Detail has a Job Description section with Paste / URL / Screenshot tabs
- [ ] "Analyze Fit" on Job Detail updates the ring score and saves it to Room
- [ ] Job description persists — reopening the job pre-fills the description field
- [ ] `MIGRATION_1_2` runs cleanly on a device with existing data
- [ ] All Phase 1–11 MVP checkpoints still pass

---

## Phase 13 — CSV Import with AI Column Mapping

> **MVP goal:** A user can upload a CSV of their existing job applications (exported from a spreadsheet, LinkedIn, or any tracker) and the app will use Gemini to intelligently map whatever column names are present to the correct database fields. Parsed entries are shown in a preview screen before the user confirms the import.

### Background & Rationale

Users switching to this app from a spreadsheet or another tracker already have months of history. Asking them to re-enter every row manually is a non-starter. CSV is the universal export format — every spreadsheet app, LinkedIn, and most ATS tools can produce one. The column names will be inconsistent ("Company", "Organisation", "Employer", "Company Name"), so a hard-coded parser would break on most files. Gemini resolves this by reading the headers and a few sample rows and returning a machine-readable column mapping. The app then parses the full file using that mapping.

---

### 13.1 Domain Models — `CsvColumnMapping` and `CsvImportPreview`

Create the following in `domain/model/`:

**`CsvColumnMapping.kt`**
```kotlin
data class CsvColumnMapping(
    /** Maps each CSV column header to a db field name, or "IGNORE" to skip it.
     *  DB field names: companyName, roleTitle, status, appliedDate,
     *                  location, salaryRange, notes, fitScore */
    val columnMappings: Map<String, String>,
    /** Maps raw status string values found in the CSV to ApplicationStatus enum names.
     *  e.g. "Applied" → "APPLIED", "Phone Screen" → "INTERVIEWING" */
    val statusMappings: Map<String, String>,
    /** Java SimpleDateFormat pattern detected in the date column, e.g. "yyyy-MM-dd", "MM/dd/yyyy".
     *  Null if no date column was detected. */
    val datePattern: String?
)
```

**`CsvImportPreview.kt`**
```kotlin
data class CsvImportPreview(
    val jobs: List<JobApplication>,    // fully constructed, ready to insert
    val columnMapping: CsvColumnMapping,
    val totalRows: Int,                // rows in the CSV (excluding header)
    val skippedRows: Int               // rows missing required fields (companyName/roleTitle)
)
```

---

### 13.2 `CsvParser` Utility

Create `util/CsvParser.kt` — a pure Kotlin utility with no Android dependencies (testable on JVM).

**Responsibilities:**
- Parse a raw CSV string into a list of rows, each row being a `List<String>`
- Handle quoted fields (fields containing commas wrapped in double-quotes)
- Handle escaped quotes (`""` inside a quoted field)
- Return `null` if the file is empty or has no header row

**Interface:**
```kotlin
object CsvParser {
    data class ParsedCsv(val headers: List<String>, val rows: List<List<String>>)

    fun parse(csvText: String): ParsedCsv?
}
```

**Sample rows for Gemini:** take the first 5 data rows (or fewer if the file is short) — enough for Gemini to infer column semantics without sending the full file.

---

### 13.3 Gemini API — Add `mapCsvColumns()`

**Add to `ClaudeRepository` interface** (`data/repository/ClaudeRepository.kt`):
```kotlin
suspend fun mapCsvColumns(
    headers: List<String>,
    sampleRows: List<List<String>>
): CsvColumnMapping
```

**Implement in `GeminiRepository`:**

Prompt design — send headers + sample data and ask for a strict JSON response:

```
You are mapping a CSV file of job applications to a database schema.

CSV headers: ["Company", "Role", "Date Applied", "Current Status", "City"]
Sample rows (up to 5):
Row 1: ["Google", "Android Engineer", "2024-03-15", "Rejected", "London"]
Row 2: ["Meta", "SWE", "2024-04-01", "Applied", ""]

Return ONLY a JSON object with these exact fields:
{
  "column_mappings": {
    "<header>": "<db_field or IGNORE>"
    // db_field must be one of: companyName, roleTitle, status, appliedDate,
    //   location, salaryRange, notes, fitScore, IGNORE
  },
  "status_mappings": {
    "<csv_status_value>": "<ApplicationStatus>"
    // ApplicationStatus must be one of: SAVED, APPLIED, INTERVIEWING, OFFERED, REJECTED
  },
  "date_pattern": "<SimpleDateFormat pattern or null>"
}

Rules:
- Every header must appear in column_mappings. Use IGNORE for irrelevant columns.
- companyName and roleTitle are required — if you cannot find them, still map the closest match.
- status_mappings must cover every distinct status value visible in the sample rows.
- For date_pattern: detect the format from sample data (e.g. "yyyy-MM-dd", "dd/MM/yyyy", "MMM d, yyyy"). Set null if no date column.
```

Parse the JSON response into `CsvColumnMapping` using Gson. Throw `ClaudeParseException` if required keys are missing.

---

### 13.4 `ImportCsvUseCase`

Create `domain/usecase/ImportCsvUseCase.kt` — orchestrates the full import pipeline:

```kotlin
class ImportCsvUseCase @Inject constructor(
    private val claudeRepository: ClaudeRepository
) {
    suspend fun preview(csvText: String): ClaudeResult<CsvImportPreview>
    suspend fun commit(preview: CsvImportPreview, repository: JobApplicationRepository)
}
```

**`preview(csvText)` flow:**
1. Call `CsvParser.parse(csvText)` — return error if null
2. Send headers + first 5 rows to `claudeRepository.mapCsvColumns()`
3. Apply the mapping to all data rows:
   - For each row: extract `companyName` and `roleTitle` (skip row if either is blank)
   - Parse `appliedDate` using `datePattern` — try the detected pattern, fall back to a list of common patterns, null if all fail
   - Map `status` value through `statusMappings` → `ApplicationStatus`, default to `APPLIED` if unmapped
   - Parse `fitScore` as `Int?`
   - Map remaining fields to location, salaryRange, notes
4. Return `CsvImportPreview(jobs, mapping, totalRows, skippedRows)`

**Date parsing fallback patterns** (try in order if `datePattern` parsing fails):
```
"yyyy-MM-dd", "dd/MM/yyyy", "MM/dd/yyyy",
"d MMM yyyy", "MMM d, yyyy", "d-MMM-yy", "yyyy/MM/dd"
```

**`commit(preview, repository)` flow:**
- For each job in `preview.jobs`: call `SaveJobApplicationUseCase` (respects duplicate detection)
- This is a fire-and-forget batch; duplicate rows are silently skipped

---

### 13.5 `CsvImportViewModel`

Create `ui/screens/csv/CsvImportViewModel.kt`:

```kotlin
sealed class CsvImportUiState {
    object Idle : CsvImportUiState()
    object ReadingFile : CsvImportUiState()
    object MappingColumns : CsvImportUiState()   // Gemini call in progress
    data class Preview(val preview: CsvImportPreview) : CsvImportUiState()
    object Importing : CsvImportUiState()
    data class Done(val imported: Int, val duplicates: Int) : CsvImportUiState()
    data class Error(val message: String) : CsvImportUiState()
}
```

**Methods:**
- `fun onCsvPicked(uri: Uri, context: Context)` — reads the file content, calls `importCsvUseCase.preview()`
- `fun confirmImport()` — calls `importCsvUseCase.commit()` with the current preview, transitions to `Done`
- `fun reset()` — back to `Idle`

**Injected:** `ImportCsvUseCase`, `SaveJobApplicationUseCase`, `JobApplicationRepository`

---

### 13.6 `CsvImportScreen`

Create `ui/screens/csv/CsvImportScreen.kt` — a dedicated screen with the following states:

**`Idle` state:**
```
Large centered illustration: Icons.Filled.UploadFile (96dp, primary color)
Title: "Import from CSV"
Body: "Upload a CSV file exported from LinkedIn, a spreadsheet, or any job tracker.
       Gemini will automatically map your columns."
Button: "Choose CSV File" (launches GetContent("text/*") or ("*/*"))
```

**`ReadingFile` / `MappingColumns` states:**
```
CircularProgressIndicator
Text: "Reading file…" / "Mapping columns with AI…"
```

**`Preview` state — the main review screen:**

Top summary card:
```
"Found N jobs across N rows  •  N skipped (missing company or role)"
```

Column mapping section (collapsible `Card`):
```
SectionHeader("Detected Column Mapping")
For each mapped column (non-IGNORE):
  Row { Text(csvColumn) → Icon(ArrowForward) → Text(dbField) }
```

Job preview list (`LazyColumn`, max visible 50):
```
For each job in preview.jobs:
  Card {
    Row {
      CompanyAvatar(companyName)
      Column {
        Text(companyName, titleSmall, bold)
        Text(roleTitle, bodySmall)
        Row {
          StatusChip(status)
          if (appliedDate != null) RelativeTimeText(appliedDate)
        }
      }
    }
  }
```

Bottom action bar:
```
Row {
    OutlinedButton("Cancel") { viewModel.reset(); navController.popBackStack() }
    Button("Import N Jobs", enabled = preview.jobs.isNotEmpty()) { viewModel.confirmImport() }
}
```

**`Importing` state:**
```
LinearProgressIndicator (full width)
Text("Importing ${preview.jobs.size} jobs…")
```

**`Done` state:**
```
Icon: Icons.Filled.CheckCircle (96dp, green)
Text("Import complete")
Text("${imported} jobs added  •  ${duplicates} already existed")
Button("View Dashboard") { navigate to Dashboard, pop CsvImport }
```

**`Error` state:**
```
Text(error, color = error)
Button("Try Again") { viewModel.reset() }
```

---

### 13.7 Navigation and Entry Point

**Add to `Screen.kt`:**
```kotlin
object CsvImport : Screen("csv_import")
```

**Add to `AppNavigation.kt`:**
```kotlin
composable(Screen.CsvImport.route) {
    CsvImportScreen(onBack = { navController.popBackStack() })
}
```

**Add to `ProfileScreen.kt`** — in the "Data" section card, below "Export Data":
```kotlin
OutlinedButton(
    onClick = { navController.navigate(Screen.CsvImport.route) },
    modifier = Modifier.fillMaxWidth().testTag("import_csv_button")
) {
    Text("Import from CSV")
}
```

`ProfileScreen` will need `navController` passed in from `AppNavigation` (or use `LocalNavController` if available).

---

### 13.8 Status Value Normalisation Reference

Document the expected `ApplicationStatus` mappings so Gemini's prompt produces consistent results:

| Input value (examples) | Maps to |
|---|---|
| "Applied", "Submitted", "Sent", "In Progress" | `APPLIED` |
| "Rejected", "No", "Declined", "Not Selected", "Closed" | `REJECTED` |
| "Interview", "Phone Screen", "Call Received", "Screening", "Assessment" | `INTERVIEWING` |
| "Offer", "Offered", "Accepted" | `OFFERED` |
| "Saved", "Wishlist", "To Apply", "Interested" | `SAVED` |

These are examples only — Gemini will infer mappings from the actual CSV values in each file.

---

### Phase 13 Testing Requirements

**Unit tests** (`test/`):

- `CsvParserTest`:
  - Standard CSV with comma-separated values parses correctly
  - Quoted fields containing commas parse as a single field
  - Escaped quotes (`""`) inside a quoted field are handled
  - Empty file returns `null`
  - Header-only file (no data rows) returns `ParsedCsv` with empty rows list
  - CSV with trailing newline does not produce an extra empty row

- `ImportCsvUseCaseTest`:
  - Mock `ClaudeRepository.mapCsvColumns()` returning a known `CsvColumnMapping`; verify the correct `JobApplication` fields are populated from mapped columns
  - Row with blank `companyName` is skipped; `skippedRows` count increments
  - Row with blank `roleTitle` is skipped
  - Status value mapped via `statusMappings` → correct `ApplicationStatus`
  - Unknown status value (not in `statusMappings`) defaults to `APPLIED`
  - Date parsed with `datePattern` produces correct epoch millis
  - Date parsing falls back to alternative patterns when `datePattern` fails
  - `fitScore` column with integer value parses to `Int?` correctly
  - `fitScore` column with non-integer value stores `null`

- `CsvImportViewModelTest`:
  - `onCsvPicked` transitions through `ReadingFile` → `MappingColumns` → `Preview`
  - `onCsvPicked` with empty file emits `Error`
  - `confirmImport` transitions `Preview` → `Importing` → `Done`
  - `Done` state carries correct `imported` and `duplicates` counts
  - `reset()` returns to `Idle`

**Run after completing this phase:**
```
./gradlew testDebugUnitTest jacocoCoverageVerification
./gradlew connectedDebugAndroidTest
```

### Phase 13 MVP Checkpoint

- [ ] "Import from CSV" button visible in Profile → Data section
- [ ] Tapping it opens file picker filtered to CSV/text files
- [ ] File is read and sent to Gemini; "Mapping columns with AI…" spinner shown
- [ ] Preview screen shows detected column mapping and list of parsed jobs with StatusChip + date
- [ ] Summary card shows total found, total skipped
- [ ] "Import N Jobs" button inserts all jobs into Room; duplicate rows are silently skipped
- [ ] Done screen shows correct imported/duplicate counts
- [ ] Navigating to Dashboard after import shows all newly added jobs
- [ ] `CsvParserTest` and `ImportCsvUseCaseTest` all pass
- [ ] No regressions — all Phase 1–12 checkpoints still pass

---

## Phase 14 — Redesign `ApplicationStatus` Lifecycle

> **MVP goal:** Replace the 5-value `ApplicationStatus` enum with a 10-value lifecycle that reflects how job applications actually progress. Migrate existing data, update every screen and service that references statuses, and keep all tests passing.

### Background & Rationale

The existing `SAVED → APPLIED → INTERVIEWING → OFFERED → REJECTED` model collapses distinct stages that matter to job seekers:
- "Screening" (an HR call) and "Interviewing" (technical rounds) are completely different stages with different actions
- "Offer received" and "Offer accepted" are two different decisions
- "Rejected" and "No response" feel very different to a candidate — one closes the loop, the other leaves them in limbo
- "Withdrawn" is missing — users do abandon applications

These distinctions matter for AI insights (rejection rate vs. ghost rate), for Gmail parsing (screening email vs. interview invite), and for the candidate's own sanity.

---

### 14.1 New `ApplicationStatus` Enum

Replace `domain/model/ApplicationStatus.kt` with:

```kotlin
enum class ApplicationStatus {
    /** Found the role, saved to apply later. Has not yet submitted anything. */
    INTERESTED,

    /** Application submitted — CV sent, form filled, or "Easy Apply" clicked. */
    APPLIED,

    /** Initial contact from the company — HR screen, recruiter call, or automated pre-screen. */
    SCREENING,

    /** Active interview rounds — phone, video, or on-site technical/behavioural interviews. */
    INTERVIEWING,

    /** Take-home task, coding challenge, or technical/psychometric assessment. */
    ASSESSMENT,

    /** Formal written offer received but not yet accepted or declined. */
    OFFER,

    /** Offer accepted — role secured. Terminal success state. */
    ACCEPTED,

    /** Not progressing — rejected at any stage by the company. Terminal failure state. */
    REJECTED,

    /** Application withdrawn by the user before a decision was made. Terminal state. */
    WITHDRAWN,

    /** No reply received after applying. Different from REJECTED — company went silent. */
    NO_RESPONSE
}
```

---

### 14.2 Database Migration — `MIGRATION_2_3`

The Room `TypeConverter` stores `ApplicationStatus` as a `String` (the enum name). Existing rows contain the old names `"SAVED"` and `"OFFERED"` which no longer exist in the new enum — Room will throw on read if not migrated.

**Add `MIGRATION_2_3` in `AppDatabase.kt`:**

```kotlin
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Rename old values to new names
        database.execSQL("UPDATE job_applications SET status = 'INTERESTED' WHERE status = 'SAVED'")
        database.execSQL("UPDATE job_applications SET status = 'OFFER' WHERE status = 'OFFERED'")
        // APPLIED, INTERVIEWING, REJECTED are unchanged — no UPDATE needed
    }
}
```

Bump `@Database(version = 3)`. Register both `MIGRATION_1_2` and `MIGRATION_2_3` in `create()`:
```kotlin
.addMigrations(MIGRATION_1_2, MIGRATION_2_3)
```

---

### 14.3 Display Names and Pipeline Order

**Canonical display name function** (add as extension in `ApplicationStatus.kt`):

```kotlin
fun ApplicationStatus.displayName(): String = when (this) {
    ApplicationStatus.INTERESTED   -> "Interested"
    ApplicationStatus.APPLIED      -> "Applied"
    ApplicationStatus.SCREENING    -> "Screening"
    ApplicationStatus.INTERVIEWING -> "Interviewing"
    ApplicationStatus.ASSESSMENT   -> "Assessment"
    ApplicationStatus.OFFER        -> "Offer Received"
    ApplicationStatus.ACCEPTED     -> "Accepted"
    ApplicationStatus.REJECTED     -> "Rejected"
    ApplicationStatus.WITHDRAWN    -> "Withdrawn"
    ApplicationStatus.NO_RESPONSE  -> "No Response"
}
```

**Pipeline order** — used for Kanban column order and the status change sheet:
```kotlin
val ACTIVE_PIPELINE = listOf(
    ApplicationStatus.INTERESTED,
    ApplicationStatus.APPLIED,
    ApplicationStatus.SCREENING,
    ApplicationStatus.INTERVIEWING,
    ApplicationStatus.ASSESSMENT,
    ApplicationStatus.OFFER,
    ApplicationStatus.ACCEPTED
)

val TERMINAL_STATUSES = listOf(
    ApplicationStatus.REJECTED,
    ApplicationStatus.WITHDRAWN,
    ApplicationStatus.NO_RESPONSE
)

val ALL_STATUSES = ACTIVE_PIPELINE + TERMINAL_STATUSES
```

Kanban board: show `ACTIVE_PIPELINE` columns first (left to right), then `TERMINAL_STATUSES` grouped at the right end under a faded "Closed" section header.

---

### 14.4 `StatusChip` Color Mapping

Update `ui/components/StatusChip.kt` with distinct tonal pairs for all 10 values:

| Status | Container color | Label color |
|---|---|---|
| `INTERESTED` | `#E8EAF6` (indigo 50) | `#3949AB` (indigo 600) |
| `APPLIED` | `#E3F2FD` (blue 50) | `#1565C0` (blue 800) |
| `SCREENING` | `#E0F7FA` (cyan 50) | `#00838F` (cyan 800) |
| `INTERVIEWING` | `#FFF8E1` (amber 50) | `#F57F17` (amber 900) |
| `ASSESSMENT` | `#F3E5F5` (purple 50) | `#7B1FA2` (purple 800) |
| `OFFER` | `#F1F8E9` (light-green 50) | `#33691E` (light-green 900) |
| `ACCEPTED` | `#E8F5E9` (green 50) | `#1B5E20` (green 900) |
| `REJECTED` | `#FFEBEE` (red 50) | `#C62828` (red 800) |
| `WITHDRAWN` | `#FAFAFA` (grey 50) | `#757575` (grey 600) |
| `NO_RESPONSE` | `#ECEFF1` (blue-grey 50) | `#546E7A` (blue-grey 600) |

---

### 14.5 Screens to Update

Every `when(status)` or `STATUS_ORDER` list in the UI must be replaced with `ALL_STATUSES` (or `ACTIVE_PIPELINE` / `TERMINAL_STATUSES` where appropriate). Remove all local `displayName()` private functions — use the canonical extension from `ApplicationStatus.kt`.

**`DashboardScreen.kt`:**
- Replace `STATUS_ORDER` with `ACTIVE_PIPELINE + TERMINAL_STATUSES`
- Kanban `LazyRow`: render `ACTIVE_PIPELINE` columns normally, then a `KanbanTerminalGroup` composable that shows `REJECTED`, `WITHDRAWN`, `NO_RESPONSE` under a faint `"Closed"` divider label
- List view filter chips: show all 10 statuses horizontally scrollable

**`JobDetailScreen.kt`:**
- `StatusDropdown` must list all `ALL_STATUSES`
- Remove local `displayName()` — use the extension

**`InsightsViewModel.kt` — redefine stats:**
```kotlin
// "Applied" = everything that is not INTERESTED (i.e. something was submitted)
val applied = jobs.count { it.status != ApplicationStatus.INTERESTED }
// "Interviews" = SCREENING + INTERVIEWING + ASSESSMENT (any live interaction)
val interviews = jobs.count {
    it.status in listOf(SCREENING, INTERVIEWING, ASSESSMENT)
}
// "Offers" = OFFER + ACCEPTED
val offers = jobs.count { it.status in listOf(OFFER, ACCEPTED) }
// "Rejections" = REJECTED only (WITHDRAWN and NO_RESPONSE are separate signals)
val rejections = jobs.count { it.status == REJECTED }
```

Add two new `InsightsStats` fields:
```kotlin
val withdrawn: Int = 0
val noResponse: Int = 0
```

Update the `StatsCardsRow` in `InsightsScreen` to show 6 cards: Applied, Interviews, Offers, Rejections, Withdrawn, No Response.

**`HeroStatsStrip` in `DashboardScreen.kt`:** iterate `ALL_STATUSES` to build mini-stat cards.

---

### 14.6 Gmail Email Pipeline Updates

**`EmailPreFilter.kt`** — no changes needed (classifies by regex signals, not enum values).

**`GmailSyncWorker.kt`** — update the `action_type → ApplicationStatus` mapping:

```kotlin
"APPLIED"    -> ApplicationStatus.APPLIED
"REJECTION"  -> ApplicationStatus.REJECTED
"INTERVIEW"  -> ApplicationStatus.SCREENING   // first contact = screening
"ALERT"      -> null  // no status change, just a notification
```

The worker can be enhanced later to detect `INTERVIEWING` vs `SCREENING` from the email body, but for MVP the first interview invite maps to `SCREENING`.

**`GeminiRepository.parseEmail()`** — update the prompt to reflect new status values so the AI returns the right enum names:

```
"action_type": one of APPLIED, REJECTION, INTERVIEW, ALERT, IRRELEVANT
```
This prompt field stays unchanged (still 5 coarse types). The mapping in the worker handles the granularity.

---

### 14.7 Phase 13 CSV Import — Update Status Mappings

Update the Gemini prompt in `GeminiRepository.mapCsvColumns()` to include the full status vocabulary:

```
// ApplicationStatus must be one of:
// INTERESTED, APPLIED, SCREENING, INTERVIEWING, ASSESSMENT,
// OFFER, ACCEPTED, REJECTED, WITHDRAWN, NO_RESPONSE
```

Update `section 13.8 Status Value Normalisation Reference` in `execution.md` accordingly:

| CSV input (examples) | Maps to |
|---|---|
| "Interested", "Wishlist", "Saved", "To Apply" | `INTERESTED` |
| "Applied", "Submitted", "Sent", "In Progress" | `APPLIED` |
| "Phone Screen", "HR Call", "Recruiter Call", "Call Received", "Pre-screen" | `SCREENING` |
| "Interview", "Technical Round", "Final Round", "On-site" | `INTERVIEWING` |
| "Assessment", "Take-home", "Test", "Coding Challenge", "Task" | `ASSESSMENT` |
| "Offer", "Offer Received" | `OFFER` |
| "Accepted", "Signed" | `ACCEPTED` |
| "Rejected", "No", "Declined", "Not Selected", "Closed" | `REJECTED` |
| "Withdrawn", "Cancelled", "Pulled Out" | `WITHDRAWN` |
| "No Response", "Ghosted", "Silence", "No Reply" | `NO_RESPONSE` |

---

### Phase 14 Testing Requirements

**Unit tests** (`test/`):

- `ApplicationStatusTest`: verify all 10 `ApplicationStatus.displayName()` values return non-blank strings; verify `ALL_STATUSES` contains all 10 values; verify `ACTIVE_PIPELINE + TERMINAL_STATUSES == ALL_STATUSES`.
- `StatusChipColorTest` (update existing): verify all 10 statuses have distinct container colors and distinct label colors.
- `InsightsViewModelStatsTest` (update existing): verify `applied` count excludes `INTERESTED` jobs; verify `interviews` count includes `SCREENING`, `INTERVIEWING`, `ASSESSMENT`; verify `offers` includes both `OFFER` and `ACCEPTED`; verify `withdrawn` and `noResponse` counts are computed separately.
- `JobApplicationMigrationTest` (update existing): add test for `MIGRATION_2_3`; verify `"SAVED"` rows become `"INTERESTED"` and `"OFFERED"` rows become `"OFFER"` after migration; verify all other status values are unchanged.

**Run after completing this phase:**
```
./gradlew testDebugUnitTest jacocoCoverageVerification
./gradlew connectedDebugAndroidTest
```

### Phase 14 MVP Checkpoint

- [ ] `ApplicationStatus` has exactly 10 values matching the enum above
- [ ] Existing device data migrates cleanly — no `IllegalArgumentException` on enum decode
- [ ] Kanban shows active pipeline columns then a "Closed" group for Rejected/Withdrawn/No Response
- [ ] Status change sheet lists all 10 statuses with correct display names
- [ ] `StatusChip` renders 10 distinct color pairs
- [ ] Insights stats correctly compute applied/interviews/offers/rejections/withdrawn/no-response
- [ ] Gmail sync maps interview emails to `SCREENING`
- [ ] `MIGRATION_2_3` verified with `MigrationTestHelper`
- [ ] All Phase 1–13 checkpoints still pass

---

## Phase 15 — Quick Evaluate: Frictionless Job Fit Scoring

> **MVP goal:** A user can get a compatibility score for any job description — via paste, URL, or screenshot — without being forced to save the job first. Evaluation is entirely stateless until the user explicitly decides to track the opportunity.

### Background & Rationale

Phase 12 correctly separated "tracking a job" (Add Job) from "evaluating a job" (Job Detail). But it introduced a new problem: **a user must create a permanent DB entry before they can see a score**. This is backwards — the score is often the input to the decision of whether to save at all.

The classic use case: a user browses LinkedIn, spots a role, wants to know in 10 seconds if it's worth pursuing. The current flow forces them to open Add Job → fill in company + role → tap Save → land in Job Detail → find the description section → paste → analyze. That's 6 steps before getting the answer to a 1-question decision.

Phase 15 adds a **"Quick Evaluate"** path: a stateless screen that accepts a job description, returns a score + breakdown, and optionally saves the job if the user decides it's worth tracking.

---

### 15.1 `EvaluateJobScreen` and `EvaluateJobViewModel`

**New screen:** `ui/screens/evaluate/EvaluateJobScreen.kt`
**New ViewModel:** `ui/screens/evaluate/EvaluateJobViewModel.kt`

#### `EvaluateJobViewModel` — states and methods

```kotlin
sealed class EvaluateJobUiState {
    object Idle : EvaluateJobUiState()
    object Analyzing : EvaluateJobUiState()
    data class Result(
        val analysis: FitAnalysis,
        val jobDescription: String    // retained so it pre-fills Job Detail on save
    ) : EvaluateJobUiState()
    data class Saved(val jobId: UUID) : EvaluateJobUiState()
    data class Error(val message: String) : EvaluateJobUiState()
}
```

**Injected:** `EvaluateFitUseCase`, `FetchUrlUseCase`, `OcrProcessor`, `UserProfileDataStore`, `SaveJobApplicationUseCase`, `JobApplicationRepository`

**Methods:**
- `fun analyzeFromPaste(text: String)` — calls `EvaluateFitUseCase`, transitions to `Result`
- `fun analyzeFromUrl(url: String)` — calls `FetchUrlUseCase` then `EvaluateFitUseCase`
- `fun analyzeFromScreenshot(uri: Uri, context: Context)` — OCR then `EvaluateFitUseCase`
- `fun saveJob(companyName: String, roleTitle: String)` — creates a `JobApplication` with `status = APPLIED`, `fitScore = result.analysis.score`, `jobDescription = result.jobDescription`, saves via `SaveJobApplicationUseCase`, emits `Saved(jobId)`. If either name is blank, use "Unknown Company" / "Unknown Role" as a placeholder so the save always succeeds.
- `fun reset()` — back to `Idle`

#### `EvaluateJobScreen` — layout

**`Idle` / input state:**
```
TopAppBar: "Evaluate Fit"  ← back button

TabRow: [ Paste Text | Paste URL | Screenshot ]

(tab content same as JobDetailScreen Job Description section)

Button: "Check Fit"  (disabled while text is blank / URL is blank)
```

**`Analyzing` state:**
```
LinearProgressIndicator (full width)
Text: "Analyzing fit…"
```

**`Result` state — the answer:**
```
Card(surfaceVariant) {
    FitScoreRing(score, size=96dp, strokeWidth=10dp)   centered
    Text("Fit Score", labelMedium)
}

ExpandableSection("Strengths (N)")   — pros, green tinted
ExpandableSection("Weaknesses (N)")  — cons, amber tinted
ExpandableSection("Missing Skills (N)") — missingSkills, blue chips

── Save section ────────────────────────────────────────────
Text("Want to track this opportunity?", labelMedium)
OutlinedTextField("Company Name", optional)
OutlinedTextField("Role Title", optional)
Button("Save & Track")   → calls saveJob()
TextButton("Start Over") → reset()
```

**`Saved` state:**
```
Icon: Icons.Filled.CheckCircle (72dp, green)
Text("Saved to your tracker")
Button("View Job Detail") → navigate to JobDetail(jobId), pop EvaluateJob
TextButton("Evaluate Another") → reset()
```

**`Error` state:**
```
Text(error, color = error)
Button("Try Again") → reset()
```

---

### 15.2 Entry Point — SpeedDial FAB on Dashboard

Replace the single `+` FAB on `DashboardScreen` with a **SpeedDial FAB** that expands into two options.

**`SpeedDialFab` composable** (create in `ui/components/SpeedDialFab.kt`):

```kotlin
@Composable
fun SpeedDialFab(
    expanded: Boolean,
    onToggle: () -> Unit,
    onTrackJob: () -> Unit,
    onEvaluateFit: () -> Unit,
    modifier: Modifier = Modifier
)
```

When collapsed: shows a single `+` FAB (identical to the current one).
When expanded:
- Background scrim (`Box` with `Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.32f)).clickable { onToggle() }`) behind the options
- Two `SmallFloatingActionButton` items stacked above the main FAB, each with a label:
  - `Icons.Filled.Analytics` + "Evaluate Fit" → `onEvaluateFit()`
  - `Icons.Filled.Add` + "Track Job" → `onTrackJob()`
- Main FAB shows `Icons.Filled.Close` when expanded, `Icons.Filled.Add` when collapsed
- Use `AnimatedVisibility(enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut())` on the mini-FABs

**Each mini-FAB row:**
```kotlin
Row(verticalAlignment = Alignment.CenterVertically) {
    Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp) {
        Text(label, modifier = Modifier.padding(horizontal=10.dp, vertical=4.dp))
    }
    Spacer(Modifier.width(12.dp))
    SmallFloatingActionButton(onClick = action) { Icon(icon, null) }
}
```

Replace the existing `floatingActionButton` in `DashboardScreen`'s `Scaffold` with:
```kotlin
SpeedDialFab(
    expanded = speedDialExpanded,
    onToggle = { speedDialExpanded = !speedDialExpanded },
    onTrackJob = { speedDialExpanded = false; onAddJobClick() },
    onEvaluateFit = { speedDialExpanded = false; onEvaluateFitClick() }
)
```

Add `onEvaluateFitClick: () -> Unit = {}` parameter to `DashboardScreen`.

---

### 15.3 Navigation Wiring

**Add to `Screen.kt`:**
```kotlin
object EvaluateJob : Screen("evaluate_job")
```

**Add to `AppNavigation.kt`:**
```kotlin
composable(Screen.EvaluateJob.route) {
    EvaluateJobScreen(
        onBack = { navController.popBackStack() },
        onNavigateToJobDetail = { jobId ->
            navController.navigate(Screen.JobDetail.createRoute(jobId)) {
                popUpTo(Screen.EvaluateJob.route) { inclusive = true }
            }
        }
    )
}
```

**Update Dashboard composable in `AppNavigation.kt`:**
```kotlin
composable(Screen.Dashboard.route) {
    DashboardScreen(
        onJobClick = { jobId -> navController.navigate(Screen.JobDetail.createRoute(jobId)) },
        onAddJobClick = { navController.navigate(Screen.AddJob.route) },
        onEvaluateFitClick = { navController.navigate(Screen.EvaluateJob.route) }
    )
}
```

Add `Screen.EvaluateJob.route` to `screensWithoutBottomNav`.

---

### 15.4 Profile Hint (Optional)

If the user has no resume uploaded (`resumeText` is blank), evaluating fit is meaningless — `EvaluateFitUseCase` will score against an empty resume. Show a non-blocking banner at the top of `EvaluateJobScreen` when `userProfileDataStore.userProfileFlow.first().resumeText.isBlank()`:

```kotlin
if (resumeEmpty) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
        Row(Modifier.padding(12.dp)) {
            Icon(Icons.Filled.Warning, null, tint = MaterialTheme.colorScheme.onErrorContainer)
            Spacer(Modifier.width(8.dp))
            Text("Upload your resume in Profile for accurate scoring",
                 style = MaterialTheme.typography.bodySmall,
                 color = MaterialTheme.colorScheme.onErrorContainer)
        }
    }
}
```

---

### Phase 15 Testing Requirements

**Unit tests** (`test/`):

- `EvaluateJobViewModelTest`:
  - `analyzeFromPaste(text)` calls `EvaluateFitUseCase` with resume text and supplied text; transitions `Idle → Analyzing → Result`
  - `analyzeFromUrl(url)` calls `FetchUrlUseCase` then `EvaluateFitUseCase` in sequence
  - `analyzeFromPaste` with blank text stays `Idle` (guard check)
  - On `ClaudeResult.Error`, state transitions to `Error`
  - `saveJob("Google", "SWE")` calls `SaveJobApplicationUseCase` with `fitScore` from result, `jobDescription` from result, `status = APPLIED`; transitions to `Saved(jobId)`
  - `saveJob("", "")` still saves with placeholder names "Unknown Company" / "Unknown Role"
  - `reset()` returns to `Idle` from any state

**Run after completing this phase:**
```
./gradlew testDebugUnitTest jacocoCoverageVerification
./gradlew assembleDebug && adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Phase 15 MVP Checkpoint

- [ ] Dashboard FAB expands into two options: "Track Job" and "Evaluate Fit"
- [ ] Tapping "Evaluate Fit" opens `EvaluateJobScreen` (no pre-saved job required)
- [ ] All three input methods work: paste, URL fetch, screenshot OCR
- [ ] Score ring animates in; pros/cons/missing skills are collapsible
- [ ] "Save & Track" creates a job with the score pre-filled and navigates to Job Detail
- [ ] Job Detail for the saved job pre-fills the job description
- [ ] "Start Over" resets the screen to input state
- [ ] No-resume banner shows when resume is empty
- [ ] All Phase 1–14 checkpoints still pass

---

## Phase 16 — Job Detail Screen Redesign

> **MVP goal:** The Job Detail screen is decluttered, scannable above the fold, and gives the user clear control over the fit score lifecycle. Every element earns its place. The screen renders identically to all prior phases for data — only the presentation layer changes.

### Background & Motivation

A screenshot audit of the live screen revealed the following structural problems (captured 2026-03-25):

- Company name duplicated in TopAppBar title and content heading
- Fit Score card occupies ~200dp to display a single "?" when no score exists
- No way to re-run fit analysis after a resume update — score goes silently stale
- No timestamp on an existing score (user cannot tell if it is fresh or weeks old)
- Pros / Cons / Missing Skills default to collapsed — the most useful AI output is hidden
- `BottomAppBar` exists solely for a single left-aligned red delete icon — looks unintentional
- Delete is a destructive action but permanently visible at all times
- Status selector is a full-width `OutlinedButton` — visually reads as a primary CTA
- Location, salary, applied date are below the fold — metadata not visible without scrolling
- Job Description tab row includes a "Screenshot" tab that belongs on the Evaluate screen, not here
- If a job description was previously saved it does not pre-populate the input field
- All editable fields (Notes, Location, Salary, Dates) are stacked with no grouping — feels like a raw database form
- "Save Changes" button is at the very bottom of a long scroll

---

### 16.1 Remove the BottomAppBar — Move Delete to Overflow Menu

Remove the entire `bottomBar` parameter from `Scaffold`. The `BottomAppBar` + delete icon is replaced by a three-dot overflow `IconButton` in the `TopAppBar` actions slot.

```kotlin
TopAppBar(
    title = { Text(uiState.job?.companyName ?: "Job Detail") },
    navigationIcon = {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
    },
    actions = {
        Box {
            var menuExpanded by remember { mutableStateOf(false) }
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "More options")
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                    leadingIcon = {
                        Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error)
                    },
                    onClick = { menuExpanded = false; showDeleteDialog = true }
                )
            }
        }
    }
)
```

The delete confirmation `AlertDialog` remains unchanged.

---

### 16.2 Redesign the Header

Replace the plain `Column` header with a `Row` that includes a `CompanyAvatar` (already used on the Dashboard card), followed by company name + role title + metadata chips. Remove the redundant company name from the `TopAppBar` title — it can remain there as context but the content heading no longer needs to repeat it at `headlineMedium`.

**Layout:**
```
[ Avatar 48dp ]  Monzo
                 Senior Android Engineer
                 📍 London   £85k–£110k
                 [ Expired chip — conditional ]
```

- `CompanyAvatar` size `48.dp` left of text column
- Company name: `titleLarge` + `fontWeight = Bold` (down from `headlineMedium` — reduces visual noise)
- Role title: `bodyLarge`, `onSurfaceVariant`
- Location + salary rendered as a single `Row` with `Icon` + `bodySmall` text for each, separated by a `·` divider. Only render if non-null/non-blank.
- "Posting may be expired" `SuggestionChip` stays, below the metadata row

This brings the most important facts visible above the fold without scrolling.

---

### 16.3 Status Selector — Replace OutlinedButton with a Chip Row

The full-width `OutlinedButton` dropdown is replaced by a horizontally scrollable row of `FilterChip`s, one per status. The selected chip uses `statusContainerColor()` and `statusLabelColor()` from the existing `StatusChip.kt` utilities. Non-selected chips use the default surface colors.

```kotlin
LazyRow(
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    contentPadding = PaddingValues(horizontal = 0.dp)
) {
    items(ALL_STATUSES) { s ->
        FilterChip(
            selected = s == status,
            onClick = { viewModel.status.value = s },
            label = { Text(s.displayName(), style = MaterialTheme.typography.labelSmall) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = statusContainerColor(s),
                selectedLabelColor = statusLabelColor(s)
            )
        )
    }
}
```

Benefits: the current stage is visible at a glance as a colored chip; changing status is one tap with no dropdown overlay; all statuses are scannable horizontally.

---

### 16.4 Fit Score Card — Three States

The card now has three distinct states instead of one undifferentiated layout.

**State A — No score, no job description saved**

Compact prompt card (~80dp tall). No ring rendered.

```
┌─────────────────────────────────────────────────────┐
│  ○ No fit score yet                                  │
│    Paste the job description below to analyze        │
└─────────────────────────────────────────────────────┘
```
- Container: `surfaceVariant`
- Icon: `Icons.Outlined.Analytics`, `onSurfaceVariant`
- Body: `bodySmall`, `onSurfaceVariant`

**State B — No score, but a job description IS saved**

Same compact card but the prompt reads "Tap Refresh Score below to analyze" with a teal/primary tint to signal action is ready.

**State C — Score exists**

Full card with ring, score number, and a `Refresh` icon button in the top-right corner.

```
┌──────────────────────────────────────────────────────┐
│  [ FitScoreRing 96dp ]          Fit Score   [↻ icon] │
│                                 Analyzed Mar 12       │
└──────────────────────────────────────────────────────┘
```

- `↻` is `Icons.Filled.Refresh`, sized `20.dp`, `onSurfaceVariant` tint
- Tapping it calls `viewModel.analyzeFromPaste(jobDescription)` — only enabled when `jobDescription.isNotBlank()`; if blank, shows a tooltip/snackbar "Add a job description first"
- Date: format as `"Analyzed MMM d"` using the `lastSeenDate` field already on `JobApplication` (repurposed or a new `analysisDate` field — see 16.8)

The `inlineError` block (AUTH / RATE_LIMIT) stays inside State C, below the ring.

---

### 16.5 Fit Analysis Results — Default Expanded

The three `ExpandableSection` composables (Pros, Cons, Missing Skills) change their initial `expanded` state:

```kotlin
var prosExpanded by remember { mutableStateOf(true) }
var consExpanded by remember { mutableStateOf(true) }
var missingExpanded by remember { mutableStateOf(true) }
```

They are only shown when `analysis != null` (unchanged). The expand/collapse toggle remains so the user can hide them.

---

### 16.6 Job Description Input — Simplified

**Changes:**
1. Remove the "Screenshot" tab entirely from `JOB_DESCRIPTION_TABS`. The tab list becomes `listOf("Paste Text", "Paste URL")` only. Screenshot OCR belongs on the Evaluate screen; it is not a re-analysis workflow on a tracked job.
2. Pre-populate the `Paste Text` field: the `jobDescription` value from the ViewModel is passed into the `OutlinedTextField` on initial composition. If the user previously saved a JD, it appears immediately — no blank field surprise.
3. Rename the button contextually:
   - If `job.fitScore == null` → label `"Analyze Fit"`
   - If `job.fitScore != null` → label `"Refresh Score"`
4. Rename the card title from `"Job Description"` to `"Job Description & Score"` to make the card's dual purpose explicit.
5. Show a `Text("Saved", color = primary, style = labelSmall)` badge next to the card title when `jobDescription.isNotBlank()`, confirming the JD is persisted.

---

### 16.7 Details Card — Group All Editable Fields

Replace the five loose `OutlinedTextField`s and two `DatePickerField`s stacked in the main column with a single `Card` titled `"Details"`.

**Inside the card:**

```
DETAILS
─────────────────────
Notes               [multiline field]
─────────────────────
Location            [single-line field]
Salary Range        [single-line field]
─────────────────────
Applied     [date button]
Interview   [date button]   ← prominent if status == INTERVIEWING or ASSESSMENT
─────────────────────
[  Save Changes  ]
```

- Section uses `SectionHeader` (existing component) for the card title
- Notes gets `minLines = 2, maxLines = 5`
- Location and Salary are in a `Column` with `Arrangement.spacedBy(8.dp)`
- Date row: `Row` with two `OutlinedButton`s side by side (`Modifier.weight(1f)` each)
- **Contextual interview date prominence:** when `status` is `INTERVIEWING` or `ASSESSMENT`, the Interview Date button is rendered full-width above the Applied Date, with a `CalendarMonth` icon and `primaryContainer` background to draw the eye
- `Save Changes` button is inside the Details card, directly below the last field — no more scrolling past everything to save

---

### 16.8 Score Timestamp — `analysisDate` Field

To display "Analyzed Mar 12" on the score card, add `analysisDate: Long?` to `JobApplication` domain model and `JobApplicationEntity`.

- Set to `System.currentTimeMillis()` in `JobDetailViewModel` when `analyzeFromPaste` / `analyzeFromUrl` completes successfully
- Display using `SimpleDateFormat("MMM d", Locale.getDefault())`
- **Room migration required:** increment DB version, add `ALTER TABLE job_applications ADD COLUMN analysisDate INTEGER` migration

Follow the `room-sqlcipher-migrations` skill exactly for the migration.

---

### 16.9 Linked Emails Section

Minor improvement: replace the raw `threadId` string in each `SuggestionChip` with a truncated, human-readable label. Thread IDs are opaque hex strings — display `"Email thread ${index + 1}"` instead, with the raw ID as a `contentDescription` for accessibility.

```kotlin
threadIds.forEachIndexed { index, threadId ->
    SuggestionChip(
        onClick = {},
        label = { Text("Email thread ${index + 1}") },
        modifier = Modifier.semantics { contentDescription = threadId }
    )
}
```

---

### 16.10 TopAppBar Title

Now that the header `Column` shows the company name prominently, the TopAppBar title can be shortened to the role title only (or kept as company name — either is acceptable). The key constraint is: the content area must not repeat exactly what the AppBar title says at the same visual weight.

Recommended: keep `companyName` in the AppBar (nav context), reduce content heading to `titleLarge` (from `headlineMedium`).

---

### Phase 16 File Changes

| File | Change |
|---|---|
| `JobDetailScreen.kt` | Full redesign per 16.1–16.7, 16.9–16.10 |
| `domain/model/JobApplication.kt` | Add `analysisDate: Long? = null` |
| `data/db/entity/JobApplicationEntity.kt` | Add `analysisDate: Long?` column |
| `data/db/mapper/JobApplicationMapper.kt` | Map `analysisDate` in both directions |
| `data/db/AppDatabase.kt` | Increment version, add migration |
| `ui/screens/detail/JobDetailViewModel.kt` | Set `analysisDate` on successful analysis |

---

### Phase 16 Testing Requirements

**Unit tests** (`test/`):

- `JobDetailViewModelTest`:
  - After successful `analyzeFromPaste`, `uiState.job.analysisDate` is non-null and approximately `System.currentTimeMillis()`
  - Refresh button is disabled (or triggers snackbar) when `jobDescription` is blank
  - Status change via chip updates `viewModel.status` StateFlow

**Manual checklist:**
- [ ] BottomAppBar is gone; delete is accessible via three-dot menu only
- [ ] Header shows `CompanyAvatar` + company + role + location/salary metadata
- [ ] All metadata visible without scrolling on a standard 6" screen
- [ ] Status chips scroll horizontally; selected chip shows correct status color
- [ ] Fit score card shows compact prompt when no score exists
- [ ] "Refresh" icon appears on score card when score exists; disabled when no JD saved
- [ ] Timestamp reads "Analyzed [date]" after a successful analysis
- [ ] Pros / Cons / Missing Skills are expanded by default after analysis
- [ ] Screenshot tab is gone from Job Description section
- [ ] Previously saved JD pre-fills the paste text field on open
- [ ] Button label reads "Refresh Score" when score already exists
- [ ] All editable fields are inside the Details card
- [ ] Interview date is full-width and prominent when status is INTERVIEWING or ASSESSMENT
- [ ] Save Changes button is inside the Details card, not at the bottom of the screen
- [ ] Room migration runs cleanly on a device with existing data (no crash, no data loss)
- [ ] All Phase 1–15 checkpoints still pass

**Run after completing this phase:**
```
./gradlew testDebugUnitTest
./gradlew assembleDebug && adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## Cross-Cutting Rules (apply in every phase)

1. **Never log sensitive data:** `resumeText`, email bodies, OAuth tokens, or API keys must never appear in `Log.*` calls.
2. **Always inject via Hilt:** No manual `object` singletons for anything with dependencies. Use `@Singleton` scoped Hilt bindings.
3. **All DB and network operations in coroutines:** No blocking calls on the main thread. Use `Dispatchers.IO` for Room and Retrofit.
4. **StateFlow for UI state:** Every ViewModel exposes a single `uiState: StateFlow<UiState>` collected with `collectAsStateWithLifecycle()`.
5. **Error handling:** All Claude API calls and Gmail API calls must handle `IOException`, `HttpException`, and JSON parse failures — show user-facing error messages via snackbar, never crash.
6. **Duplicate detection active from Phase 2:** The `SaveJobApplicationUseCase` must call `findDuplicate` before every insert for the entire life of the project.
7. **Pre-filter before every Claude email call:** `EmailPreFilter.isJobRelated()` must return `true` before `ParseEmailUseCase` is ever called.
