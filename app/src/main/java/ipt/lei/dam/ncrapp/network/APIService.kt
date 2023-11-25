
import ipt.lei.dam.ncrapp.models.LoginRequest
import ipt.lei.dam.ncrapp.models.LoginResponse
import ipt.lei.dam.ncrapp.models.RecoverPasswordRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface APIService {
        @POST("auth/login")
        suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

        @POST("auth/recoverPassword")
        suspend fun recoverPassword(@Body request: RecoverPasswordRequest)
}
