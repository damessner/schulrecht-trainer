package at.schulrecht.trainer.data

import at.schulrecht.trainer.data.local.AttemptEntity
import at.schulrecht.trainer.data.local.ModuleEntity
import at.schulrecht.trainer.data.local.QuestionEntity
import at.schulrecht.trainer.data.local.TrainerDatabase
import at.schulrecht.trainer.data.local.UserPrefs
import at.schulrecht.trainer.data.remote.ContentApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.json.JSONArray

data class ModuleUi(
    val id: String,
    val titel: String,
    val saeule: String,
    val status: String,
    val total: Int,
    val answered: Int,
    val correct: Int
)

data class QuestionUi(
    val id: String,
    val modulId: String,
    val level: String,
    val typ: String,
    val situation: String,
    val optionen: List<String>,
    val richtig: Set<Int>,
    val feedbacks: List<String>,
    val aufloesung: String,
    val hauptquelle: String,
    val zusatzquellen: List<String>,
    val stand: String
)

data class SyncProgress(val done: Int, val total: Int)

class SchulrechtRepository(
    private val db: TrainerDatabase,
    private val api: ContentApi,
    private val prefs: UserPrefs
) {
    fun observeModules(): Flow<List<ModuleEntity>> = db.moduleDao().observeModules()

    fun observeModuleUi(): Flow<List<ModuleUi>> = combine(
        db.moduleDao().observeModules(),
        db.questionDao().observeCounts(),
        db.attemptDao().observeAll()
    ) { modules, counts, attempts ->
        val totalByModule = counts.associate { it.modulId to it.total }
        val latestByQuestion = attempts
            .sortedByDescending { it.createdAt }
            .distinctBy { it.questionId }
        val answeredByModule = latestByQuestion.groupBy { it.modulId }
        modules.map { m ->
            val latest = answeredByModule[m.id].orEmpty()
            ModuleUi(
                id = m.id,
                titel = m.titel,
                saeule = m.saeule,
                status = m.status,
                total = totalByModule[m.id] ?: 0,
                answered = latest.size,
                correct = latest.count { it.correct }
            )
        }
    }

    fun observeQuestions(moduleId: String, level: String): Flow<List<QuestionUi>> =
        db.questionDao().observeQuestions(moduleId, level).map { list ->
            list.map { entity -> entity.toUi() }
        }

    suspend fun recordAttempt(
        question: QuestionUi,
        score: Float,
        correct: Boolean
    ) {
        db.attemptDao().insert(
            AttemptEntity(
                questionId = question.id,
                modulId = question.modulId,
                level = question.level,
                score = score,
                correct = correct,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun resetProgress() {
        db.attemptDao().clearAll()
    }

    suspend fun sync(onProgress: (SyncProgress) -> Unit) {
        val manifest = api.fetchManifest()
        onProgress(SyncProgress(0, manifest.modules.size))
        var done = 0
        val failed = mutableListOf<String>()
        val moduleRows = mutableListOf<ModuleEntity>()
        val questionRows = mutableListOf<QuestionEntity>()
        for (ref in manifest.modules) {
            try {
                val module = api.fetchModule(ref.path)
                moduleRows.add(
                    ModuleEntity(
                        id = module.id,
                        titel = module.titel,
                        saeule = module.saeule,
                        status = module.status
                    )
                )
                for (levelFile in module.levels) {
                    val questions = api.fetchQuestions(ref.path, levelFile)
                    questionRows.addAll(questions.map { it.toEntity() })
                }
            } catch (e: Exception) {
                failed.add(ref.id)
            }
            done++
            onProgress(SyncProgress(done, manifest.modules.size))
        }
        db.moduleDao().upsertModules(moduleRows)
        db.questionDao().upsertQuestions(questionRows)
        prefs.setManifestVersion(manifest.version)
        prefs.setLastSync(System.currentTimeMillis())
        if (failed.isNotEmpty()) throw IllegalStateException("Fehler bei: ${failed.joinToString()}")
    }

    private fun at.schulrecht.trainer.data.remote.QuestionDto.toEntity(): QuestionEntity =
        QuestionEntity(
            id = id,
            modulId = modul_id,
            level = level,
            typ = typ,
            schulartCsv = (schulart ?: listOf("alle")).joinToString(SEP),
            situation = situation,
            optionenJson = JSONArray(optionen).toString(),
            richtigCsv = richtig.joinToString(","),
            feedbacksJson = JSONArray(pro_option_feedback).toString(),
            aufloesung = aufloesung,
            hauptquelle = hauptquelle,
            zusatzCsv = (zusatzquellen ?: emptyList()).joinToString(SEP),
            stand = stand
        )

    private fun QuestionEntity.toUi(): QuestionUi {
        fun jsonStrings(raw: String): List<String> {
            val arr = JSONArray(raw)
            return List(arr.length()) { i -> arr.optString(i) }
        }
        return QuestionUi(
            id = id,
            modulId = modulId,
            level = level,
            typ = typ,
            situation = situation,
            optionen = jsonStrings(optionenJson),
            richtig = richtigCsv.split(",").mapNotNull { it.toIntOrNull() }.toSet(),
            feedbacks = jsonStrings(feedbacksJson),
            aufloesung = aufloesung,
            hauptquelle = hauptquelle,
            zusatzquellen = zusatzCsv.split(SEP).filter { it.isNotBlank() },
            stand = stand
        )
    }

    companion object {
        private const val SEP = "\u001F"
    }
}
