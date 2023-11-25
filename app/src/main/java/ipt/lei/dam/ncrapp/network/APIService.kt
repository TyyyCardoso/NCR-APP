
import ipt.lei.dam.ncrapp.models.LoginRequest
import ipt.lei.dam.ncrapp.models.LoginResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface APIService {
        @POST("/auth/login")
        fun login(@Body request: LoginRequest): Call<LoginResponse>
}
