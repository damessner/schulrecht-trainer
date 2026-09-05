package at.schulrecht.trainer.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

class ContentApi {
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val manifestAdapter = moshi.adapter(ManifestDto::class.java)
    private val moduleAdapter = moshi.adapter(ModuleDto::class.java)
    private val questionListAdapter = moshi.adapter<List<QuestionDto>>(
        Types.newParameterizedType(List::class.java, QuestionDto::class.java)
    )

    suspend fun fetchManifest(): ManifestDto = withContext(Dispatchers.IO) {
        val body = get("$CONTENT_BASE/manifest.json")
        manifestAdapter.fromJson(body) ?: throw IOException("Leeres Manifest")
    }

    suspend fun fetchModule(path: String): ModuleDto = withContext(Dispatchers.IO) {
        val body = get("$CONTENT_BASE/$path/module.json")
        moduleAdapter.fromJson(body) ?: throw IOException("Leeres Modul: $path")
    }

    suspend fun fetchQuestions(modulePath: String, levelFile: String): List<QuestionDto> =
        withContext(Dispatchers.IO) {
            val body = get("$CONTENT_BASE/$modulePath/$levelFile")
            questionListAdapter.fromJson(body) ?: emptyList()
        }

    private fun get(url: String): String {
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("HTTP ${response.code} für $url")
            return response.body?.string() ?: throw IOException("Leerer Body: $url")
        }
    }

    companion object {
        const val CONTENT_BASE =
            "https://raw.githubusercontent.com/damessner/schulrecht-content/main"
    }
}
