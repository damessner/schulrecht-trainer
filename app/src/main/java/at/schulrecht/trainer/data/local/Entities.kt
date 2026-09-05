package at.schulrecht.trainer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "modules")
data class ModuleEntity(
    @PrimaryKey val id: String,
    val titel: String,
    val saeule: String,
    val status: String
)

@Entity(
    tableName = "questions",
    indices = [androidx.room.Index("modulId")]
)
data class QuestionEntity(
    @PrimaryKey val id: String,
    val modulId: String,
    val level: String,
    val typ: String,
    val schulartCsv: String,
    val situation: String,
    val optionenJson: String,
    val richtigCsv: String,
    val feedbacksJson: String,
    val aufloesung: String,
    val hauptquelle: String,
    val zusatzCsv: String,
    val stand: String
)

@Entity(
    tableName = "attempts",
    indices = [androidx.room.Index("questionId")]
)
data class AttemptEntity(
    @PrimaryKey(autoGenerate = true) val uid: Long = 0,
    val questionId: String,
    val modulId: String,
    val level: String,
    val score: Float,
    val correct: Boolean,
    val createdAt: Long
)

@Entity(tableName = "review_states")
data class ReviewStateEntity(
    @PrimaryKey val questionId: String,
    val modulId: String,
    val level: String,
    val box: Int,
    val nextDue: Long,
    val fails: Int,
    val passes: Int
)

data class ModuleCount(
    val modulId: String,
    val total: Int
)
