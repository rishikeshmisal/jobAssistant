---
name: room-sqlcipher-migrations
description: >
  Exact patterns for Room DB setup with SQLCipher encryption in this Android project.
  Use this skill whenever creating or modifying Room entities, DAOs, the AppDatabase
  class, TypeConverters, database migrations, or the Hilt DatabaseModule. Also use when
  adding new @Entity classes, changing existing entity fields, writing DAO queries, or
  debugging Room compile-time errors. The SQLCipher passphrase and SupportFactory wiring
  is easy to get wrong — always consult this skill before touching the database layer.
---

# Room + SQLCipher — Project Patterns

## Database class

The `SupportFactory` is the critical wiring between SQLCipher and Room. Without it, Room
creates an unencrypted database silently.

```kotlin
@Database(
    entities = [
        JobApplication::class,
        CareerInsights::class
    ],
    version = 1,
    exportSchema = true          // keep schema history in app/schemas/ — commit to git
)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun jobApplicationDao(): JobApplicationDao
    abstract fun careerInsightsDao(): CareerInsightsDao
}
```

## DatabaseModule (Hilt)

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides @Singleton
    fun providePassphrase(context: Application): ByteArray {
        val keyAlias = "db_passphrase_key"
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

        if (!keyStore.containsAlias(keyAlias)) {
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
                .apply {
                    init(
                        KeyGenParameterSpec.Builder(keyAlias,
                            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                            .setKeySize(256)
                            .build()
                    )
                }.generateKey()
        }

        // Store the random passphrase in EncryptedSharedPreferences, keyed by alias
        val prefs = EncryptedSharedPreferences.create(
            context, "db_prefs",
            MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val existing = prefs.getString(keyAlias, null)
        if (existing != null) return Base64.decode(existing, Base64.DEFAULT)

        val generated = ByteArray(32).also { SecureRandom().nextBytes(it) }
        prefs.edit().putString(keyAlias, Base64.encodeToString(generated, Base64.DEFAULT)).apply()
        return generated
    }

    @Provides @Singleton
    fun provideAppDatabase(
        context: Application,
        passphrase: ByteArray
    ): AppDatabase {
        val factory = SupportFactory(passphrase)
        return Room.databaseBuilder(context, AppDatabase::class.java, "jobassistant.db")
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()  // replace with addMigrations() before v1→v2
            .build()
    }
}
```

> **Security note:** The passphrase ByteArray should be zeroed after use in production.
> The current pattern keeps it in memory as a Hilt singleton — acceptable for MVP, but
> note it for the security hardening phase.

---

## TypeConverters — required for List<String> and UUID

All three entities use types Room cannot store natively. Register converters at the
`@Database` level, not at the entity level.

```kotlin
class AppTypeConverters {

    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>): String = gson.toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String> =
        gson.fromJson(value, object : TypeToken<List<String>>() {}.type) ?: emptyList()

    @TypeConverter
    fun fromUUID(uuid: UUID): String = uuid.toString()

    @TypeConverter
    fun toUUID(value: String): UUID = UUID.fromString(value)

    @TypeConverter
    fun fromStatus(status: ApplicationStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): ApplicationStatus = ApplicationStatus.valueOf(value)
}
```

---

## Entities

### JobApplication

```kotlin
@Entity(tableName = "job_applications")
data class JobApplication(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val companyName: String,
    val roleTitle: String,
    val jobUrl: String? = null,
    val status: ApplicationStatus = ApplicationStatus.SAVED,
    val fitScore: Int? = null,
    val location: String? = null,
    val salaryRange: String? = null,
    val appliedDate: Long? = null,
    val interviewDate: Long? = null,
    val notes: String = "",
    val linkedEmailThreadIds: List<String> = emptyList(),
    val lastSeenDate: Long = System.currentTimeMillis()  // for expiry warning (Phase 9)
)

enum class ApplicationStatus { SAVED, APPLIED, INTERVIEWING, OFFERED, REJECTED }
```

### CareerInsights

```kotlin
@Entity(tableName = "career_insights")
data class CareerInsights(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val generatedDate: Long = System.currentTimeMillis(),
    val identifiedGaps: List<String> = emptyList(),
    val recommendedActions: List<String> = emptyList(),
    val summaryAnalysis: String = ""
)
```

---

## DAOs

### JobApplicationDao

```kotlin
@Dao
interface JobApplicationDao {

    @Query("SELECT * FROM job_applications ORDER BY lastSeenDate DESC")
    fun observeAll(): Flow<List<JobApplication>>

    @Query("SELECT * FROM job_applications WHERE status = :status ORDER BY lastSeenDate DESC")
    fun observeByStatus(status: ApplicationStatus): Flow<List<JobApplication>>

    @Query("SELECT * FROM job_applications WHERE id = :id")
    suspend fun findById(id: UUID): JobApplication?

    // Duplicate detection — call before every insert
    @Query("""
        SELECT * FROM job_applications
        WHERE lower(companyName) = lower(:company)
        AND lower(roleTitle) = lower(:role)
        LIMIT 1
    """)
    suspend fun findDuplicate(company: String, role: String): JobApplication?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(job: JobApplication)

    @Update
    suspend fun update(job: JobApplication)

    @Delete
    suspend fun delete(job: JobApplication)

    @Query("SELECT * FROM job_applications WHERE :threadId IN (linkedEmailThreadIds)")
    suspend fun findByThreadId(threadId: String): JobApplication?

    // For insights aggregation — anonymized counts only, never raw company names
    @Query("SELECT COUNT(*) FROM job_applications WHERE status = :status")
    suspend fun countByStatus(status: ApplicationStatus): Int
}
```

### CareerInsightsDao

```kotlin
@Dao
interface CareerInsightsDao {

    @Query("SELECT * FROM career_insights ORDER BY generatedDate DESC LIMIT 1")
    suspend fun getLatest(): CareerInsights?

    @Insert
    suspend fun insert(insights: CareerInsights)

    @Query("DELETE FROM career_insights WHERE generatedDate < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)
}
```

---

## Migrations — template for v1 → v2+

Replace `fallbackToDestructiveMigration()` with explicit migrations before any schema
change ships to users. Template:

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Example: add a new nullable column
        database.execSQL(
            "ALTER TABLE job_applications ADD COLUMN newField TEXT DEFAULT NULL"
        )
    }
}

// In DatabaseModule:
Room.databaseBuilder(...)
    .openHelperFactory(factory)
    .addMigrations(MIGRATION_1_2)
    .build()
```

> **Rule:** Every entity field change requires a migration. Room's `exportSchema = true`
> generates a diff-friendly JSON in `app/schemas/` — review it before every release build.

---

## Duplicate detection — enforced from Phase 2 onward

`SaveJobApplicationUseCase` must call `findDuplicate` before every insert. This is a
cross-cutting rule that never gets removed:

```kotlin
class SaveJobApplicationUseCase @Inject constructor(
    private val dao: JobApplicationDao
) {
    suspend fun execute(job: JobApplication): SaveResult {
        val existing = dao.findDuplicate(job.companyName, job.roleTitle)
        if (existing != null) return SaveResult.Duplicate(existing)
        dao.insert(job)
        return SaveResult.Saved
    }
}

sealed class SaveResult {
    object Saved : SaveResult()
    data class Duplicate(val existing: JobApplication) : SaveResult()
}
```

---

## ProGuard rules — required for release builds

Add to `proguard-rules.pro` (Phase 9, but note early):

```
-keep class com.jobassistant.data.local.entity.** { *; }
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }
```
