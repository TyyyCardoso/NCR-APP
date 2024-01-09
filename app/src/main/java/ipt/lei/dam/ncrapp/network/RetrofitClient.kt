package ipt.lei.dam.ncrapp.network

import APIService
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import okhttp3.OkHttpClient
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private val environment = EnvironmentEnum.PROD// Set your current environment here

    //Diferentes IP's para a API
    val BASE_URL = when(environment) {
        EnvironmentEnum.EMULATOR -> "http://10.0.2.2:8080/ncr/" // Emulator URL
        EnvironmentEnum.DEV -> "http://localhost:8080/ncr/" // Development URL
        EnvironmentEnum.PROD -> "http://85.243.90.78:8080/ncrAPI/"   // Production URL
    }

    //Interceptor
    private val authInterceptor = AuthInterceptor()

    //Construç~ão do cliente
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS) // Tempo de timeout para a conexão
        .readTimeout(30, TimeUnit.SECONDS)    // Tempo de timeout para a leitura de dados
        .writeTimeout(30, TimeUnit.SECONDS)   // Tempo de timeout para a escrita de dados
        .addInterceptor(authInterceptor)
        .build()

    @RequiresApi(Build.VERSION_CODES.O)
    val localDateTimeDeserializer = JsonDeserializer { json, _, _ ->
        LocalDateTime.parse(json.asJsonPrimitive.asString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    val gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, localDateTimeDeserializer)
        .create()


    @RequiresApi(Build.VERSION_CODES.O)
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    @RequiresApi(Build.VERSION_CODES.O)
    val apiService: APIService = retrofit.create(APIService::class.java)

    fun setAuthToken(token: String) {
        authInterceptor.token = token
    }
}
