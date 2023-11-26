
import ipt.lei.dam.ncrapp.models.EventResponse
import ipt.lei.dam.ncrapp.models.LoginRequest
import ipt.lei.dam.ncrapp.models.LoginResponse
import ipt.lei.dam.ncrapp.models.RecoverPasswordRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface APIService {
        /**
         * Authentication
         */
        @POST("/auth/login")
        fun login(@Body request: LoginRequest): Call<LoginResponse>

        @POST("/auth/recover")
        fun recoverPassword(@Body request: RecoverPasswordRequest): Call<ResponseBody>

        /**
         * Events
         */
        @GET("/event/all")
        fun getEvents(): Call<List<EventResponse>>

}
