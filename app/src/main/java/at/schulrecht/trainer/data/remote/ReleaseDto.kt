package at.schulrecht.trainer.data.remote

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReleaseAssetDto(
    val name: String,
    val browser_download_url: String
)

@JsonClass(generateAdapter = true)
data class ReleaseDto(
    val tag_name: String,
    val assets: List<ReleaseAssetDto>
)
