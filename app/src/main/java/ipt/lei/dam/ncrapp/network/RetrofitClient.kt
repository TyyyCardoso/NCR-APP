package ipt.lei.dam.ncrapp.network

import APIService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val authInterceptor = AuthInterceptor()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS) // Tempo de timeout para a conexão
        .readTimeout(30, TimeUnit.SECONDS)    // Tempo de timeout para a leitura de dados
        .writeTimeout(30, TimeUnit.SECONDS)   // Tempo de timeout para a escrita de dados
        .addInterceptor(authInterceptor)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: APIService = retrofit.create(APIService::class.java)

    fun setAuthToken(token: String) {
        authInterceptor.token = token
    }
}
