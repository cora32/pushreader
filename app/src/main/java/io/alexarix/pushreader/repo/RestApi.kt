package io.alexarix.pushreader.repo

import android.content.Context
import com.google.gson.GsonBuilder
import io.alexarix.pushreader.pojo.ResponseData
import io.alexarix.pushreader.repo.room.PRLogEntity
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Url
import java.io.File

class HostSelectionInterceptor: Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        val host: String = SPM.host
        val port: Int = SPM.port

        val newUrl = request.url.newBuilder()
            .scheme(SPM.protocol)
            .host(host)
            .port(port)
            .addPathSegment(SPM.path)
            .build()

        request = request.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(request)
    }
}

fun getClient(directory: File): OkHttpClient {
    val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY)
    }
//    val hostInterceptor = HostSelectionInterceptor()

    return OkHttpClient.Builder()
//        .addInterceptor(hostInterceptor)
        .addInterceptor(httpLoggingInterceptor)
        .cache(
            Cache(
                directory = File(directory, "cws_cache"),
                maxSize = 5L * 1024L * 1024L // 5 MiB
            )
        )
        .build()
}

fun getRetrofit(context: Context): Retrofit = Retrofit.Builder()
    .client(getClient(context.cacheDir))
    .addConverterFactory(
        GsonConverterFactory.create(
            GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
        )
    )
    .baseUrl("https://unknown-networks.io/")
    .build()

interface RestApi {
    @Headers(
        "Accept: application/json"
    )
    @POST()
    suspend fun sendData(
        @Url url: String,
        @Body data: PRLogEntity,
    ): retrofit2.Response<ResponseData>
}