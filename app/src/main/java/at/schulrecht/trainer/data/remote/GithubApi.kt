package at.schulrecht.trainer.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

class GithubApi {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val adapter = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
        .adapter(ReleaseDto::class.java)

    suspend fun latestRelease(): ReleaseDto = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://api.github.com/repos/damessner/schulrecht-trainer/releases/latest")
            .header("Accept", "application/vnd.github+json")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
            adapter.fromJson(response.body?.string() ?: "") ?: throw IOException("Leeres Release")
        }
    }
}
