---
name: android-hilt-patterns
description: >
  Canonical Hilt dependency injection patterns for this Android project. Use this skill
  whenever creating or modifying any Hilt module, adding @Inject constructors, writing
  ViewModels with @HiltViewModel, setting up WorkManager workers with @HiltWorker, or
  wiring any new repository, use case, or service into the DI graph. Also use when
  debugging "MissingBinding", "EntryPoint", or "Unscoped" Hilt compile errors. Never
  create manual singletons or object-level instances for anything with dependencies —
  this skill defines the correct pattern for every layer in the project.
---

# Hilt DI Patterns — Project Reference

## Module inventory

| Module | Location | Provides |
|---|---|---|
| `DatabaseModule` | `di/DatabaseModule.kt` | `AppDatabase`, DAOs, `ByteArray` passphrase |
| `NetworkModule` | `di/NetworkModule.kt` | `OkHttpClient`, `Retrofit`, `AnthropicApiService` |
| `RepositoryModule` | `di/RepositoryModule.kt` | Interface→Impl bindings for all repositories |
| `WorkerModule` | `di/WorkerModule.kt` | `HiltWorkerFactory` binding |

All modules are `@InstallIn(SingletonComponent::class)` unless noted otherwise.

---

## Application class

```kotlin
@HiltAndroidApp
class JobAssistantApp : Application() {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        WorkManager.initialize(
            this,
            Configuration.Builder().setWorkerFactory(workerFactory).build()
        )
    }
}
```

> `WorkManager.initialize()` must use `HiltWorkerFactory` — without it, `@HiltWorker`
> classes fail silently at runtime with a generic worker error.

---

## MainActivity

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { JobAssistantApp() }
    }
}
```

---

## ViewModels

All ViewModels use `@HiltViewModel` + `@Inject constructor`. Never use `ViewModelProvider`
factory manually.

```kotlin
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getJobApplicationsUseCase: GetJobApplicationsUseCase,
    private val saveJobApplicationUseCase: SaveJobApplicationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init { loadJobs() }

    private fun loadJobs() {
        viewModelScope.launch {
            getJobApplicationsUseCase.execute()
                .catch { e -> _uiState.value = DashboardUiState.Error(e.message ?: "Unknown error") }
                .collect { jobs -> _uiState.value = DashboardUiState.Success(jobs) }
        }
    }
}

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(val jobs: List<JobApplication>) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}
```

Collect in Compose with:

```kotlin
val uiState by viewModel.uiState.collectAsStateWithLifecycle()
```

---

## Repositories — interface in domain, impl in data

```kotlin
// domain/repository/JobApplicationRepository.kt
interface JobApplicationRepository {
    fun observeAll(): Flow<List<JobApplication>>
    suspend fun save(job: JobApplication): SaveResult
    suspend fun update(job: JobApplication)
    suspend fun delete(job: JobApplication)
    suspend fun findByThreadId(threadId: String): JobApplication?
}

// data/repository/JobApplicationRepositoryImpl.kt
class JobApplicationRepositoryImpl @Inject constructor(
    private val dao: JobApplicationDao
) : JobApplicationRepository {
    override fun observeAll(): Flow<List<JobApplication>> = dao.observeAll()

    override suspend fun save(job: JobApplication): SaveResult =
        withContext(Dispatchers.IO) {
            saveJobApplicationUseCase.execute(job)   // duplicate check lives in use case
        }

    // ... other methods
}
```

Bind in `RepositoryModule`:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindJobApplicationRepository(
        impl: JobApplicationRepositoryImpl
    ): JobApplicationRepository

    @Binds @Singleton
    abstract fun bindCareerInsightsRepository(
        impl: CareerInsightsRepositoryImpl
    ): CareerInsightsRepository

    @Binds @Singleton
    abstract fun bindClaudeRepository(
        impl: ClaudeRepositoryImpl
    ): ClaudeRepository
}
```

> Use `abstract class` + `@Binds` for interface→impl bindings (more efficient than
> `@Provides`). Use concrete `object` + `@Provides` only when construction logic is needed.

---

## Use cases

Thin wrappers that enforce business rules. One public `execute()` / `invoke()` method.
Always `@Inject constructor`, never scoped (they're stateless).

```kotlin
class EvaluateFitUseCase @Inject constructor(
    private val claudeRepository: ClaudeRepository,
    private val jobApplicationRepository: JobApplicationRepository
) {
    suspend fun execute(resumeText: String, jobDescription: String): ClaudeResult<FitAnalysis> {
        return claudeRepository.evaluateFit(resumeText, jobDescription)
    }
}
```

---

## WorkManager workers

Requires `@HiltWorker` + `@AssistedInject`. Never use `@Inject` on a Worker — it will
crash at runtime.

```kotlin
@HiltWorker
class GmailSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val gmailRepository: GmailRepository,
    private val jobApplicationRepository: JobApplicationRepository,
    private val parseEmailUseCase: ParseEmailUseCase
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            gmailRepository.fetchRecentEmails()
                .filter { EmailPreFilter.isJobRelated(it) }
                .forEach { email -> parseEmailUseCase.execute(email) }
            Result.success()
        } catch (e: IOException) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        fun buildPeriodicRequest(): PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<GmailSyncWorker>(15, TimeUnit.MINUTES)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
    }
}
```

Bind the factory in `WorkerModule`:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
interface WorkerModule {
    @Binds
    fun bindHiltWorkerFactory(factory: HiltWorkerFactory): WorkerFactory
}
```

---

## NetworkModule

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides @Singleton
    fun provideOkHttpClient(
        userProfileDataStore: UserProfileDataStore
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(ApiKeyInterceptor(userProfileDataStore))
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
        })
        .build()

    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient, gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.anthropic.com/v1/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides @Singleton
    fun provideAnthropicApiService(retrofit: Retrofit): AnthropicApiService =
        retrofit.create(AnthropicApiService::class.java)
}
```

---

## Scoping rules

| What | Scope | Why |
|---|---|---|
| `AppDatabase` | `@Singleton` | One encrypted DB per process |
| All DAOs | `@Singleton` | Provided by `@Singleton` DB |
| `OkHttpClient` / `Retrofit` | `@Singleton` | Connection pool reuse |
| All Repositories | `@Singleton` | Single source of truth for data |
| ViewModels | ViewModel scope (auto) | Survives config changes |
| Use Cases | Unscoped | Stateless — new instance per injection is fine |
| Workers | `@HiltWorker` (assisted) | WorkManager manages lifecycle |

---

## Common compile errors and fixes

| Error | Cause | Fix |
|---|---|---|
| `[Hilt] MissingBinding` | Interface injected but not bound | Add `@Binds` in `RepositoryModule` |
| `[Hilt] Unscoped in Singleton` | Unscoped dep injected into `@Singleton` | Add `@Singleton` to the dep, or make the consumer unscoped |
| Worker crashes with `IllegalStateException` | `@Inject` used instead of `@AssistedInject` | Switch to `@HiltWorker` + `@AssistedInject` |
| `Cannot create instance of ViewModel` | Missing `@HiltViewModel` or `@AndroidEntryPoint` on Activity | Add both annotations |
| `DuplicateBindings` | Same interface bound twice | Check for duplicate `@Binds` across modules |

---

## What never to do

```kotlin
// WRONG — manual singleton
object DatabaseHolder {
    val db: AppDatabase by lazy { Room.databaseBuilder(...).build() }
}

// WRONG — manual ViewModel factory
val vm = ViewModelProvider(this, MyViewModelFactory(repo)).get(MyViewModel::class.java)

// WRONG — Worker with @Inject
class MyWorker @Inject constructor(...) : CoroutineWorker(...)   // will crash at runtime

// RIGHT — all of the above patterns are in this document
```
