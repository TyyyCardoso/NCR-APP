
import ipt.lei.dam.ncrapp.models.ChangePasswordRequest
import ipt.lei.dam.ncrapp.models.LoginRequest
import ipt.lei.dam.ncrapp.models.LoginResponse
import ipt.lei.dam.ncrapp.models.RecoverPasswordRequest
import ipt.lei.dam.ncrapp.models.ValidateOTPRequest
import ipt.lei.dam.ncrapp.models.ValidateOTPResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface APIService {
        @POST("/auth/login")
        fun login(@Body request: LoginRequest): Call<LoginResponse>

        @POST("/auth/recover")
        fun recoverPassword(@Body request: RecoverPasswordRequest): Call<ResponseBody>

        @POST("/auth/validate")
        fun validateOTP(@Body request: ValidateOTPRequest): Call<ValidateOTPResponse>

        @POST("/auth/change")
        fun changePassword(@Body request: ChangePasswordRequest): Call<ResponseBody>
}
