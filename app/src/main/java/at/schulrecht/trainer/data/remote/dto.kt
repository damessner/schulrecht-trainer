package at.schulrecht.trainer.data.remote

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ManifestDto(
    val name: String,
    val version: String,
    val stand: String,
    val base_raw: String,
    val batches: List<String>,
    val modules: List<ModuleRefDto>
)

@JsonClass(generateAdapter = true)
data class ModuleRefDto(
    val id: String,
    val titel: String,
    val saeule: String,
    val path: String
)

@JsonClass(generateAdapter = true)
data class ModuleDto(
    val id: String,
    val titel: String,
    val saeule: String,
    val status: String,
    val quelle: List<String>,
    val levels: List<String>
)

@JsonClass(generateAdapter = true)
data class QuestionDto(
    val id: String,
    val modul_id: String,
    val level: String,
    val typ: String,
    val schulart: List<String>?,
    val situation: String,
    val optionen: List<String>,
    val richtig: List<Int>,
    val pro_option_feedback: List<String>,
    val aufloesung: String,
    val hauptquelle: String,
    val zusatzquellen: List<String>?,
    val stand: String
)
