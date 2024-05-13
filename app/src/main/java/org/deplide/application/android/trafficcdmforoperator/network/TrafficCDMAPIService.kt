package org.deplide.application.android.trafficcdmforoperator.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.deplide.application.android.trafficcdmforoperator.network.dto.tcmf.version_0_0_7.TCMFMessage
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

private const val BASE_URL = "https://arkady.deplide.org"

val interceptor : HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
}

val client : OkHttpClient = OkHttpClient.Builder().apply {
    addInterceptor(interceptor)
}.build()

val moshi: Moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

val retrofitBuilder: Retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .client(client)
    .build()

interface TrafficCDMAPIService {
    @POST("/cdm/update")
    suspend fun submitMessage(
        @Header("Authorization") token: String,
        @Header("Content-Type") accept: String,
        @Body message: TCMFMessage
    )
}

object TrafficCDMApi {
    val retrofit: TrafficCDMAPIService by lazy {
        retrofitBuilder.create(TrafficCDMAPIService::class.java)
    }
}
