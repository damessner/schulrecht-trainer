package at.schulrecht.trainer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ModuleDao {
    @Upsert
    suspend fun upsertModules(modules: List<ModuleEntity>)

    @Query("SELECT * FROM modules ORDER BY saeule, id")
    fun observeModules(): Flow<List<ModuleEntity>>
}

@Dao
interface QuestionDao {
    @Upsert
    suspend fun upsertQuestions(questions: List<QuestionEntity>)

    @Query("SELECT * FROM questions WHERE modulId = :moduleId AND level = :level ORDER BY id")
    fun observeQuestions(moduleId: String, level: String): Flow<List<QuestionEntity>>

    @Query("SELECT modulId AS modulId, COUNT(*) AS total FROM questions GROUP BY modulId")
    fun observeCounts(): Flow<List<ModuleCount>>
}

@Dao
interface AttemptDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attempt: AttemptEntity)

    @Query("SELECT * FROM attempts")
    fun observeAll(): Flow<List<AttemptEntity>>

    @Query("DELETE FROM attempts")
    suspend fun clearAll()
}
