
import ipt.lei.dam.ncrapp.models.ChangePasswordRequest
import ipt.lei.dam.ncrapp.models.DidYouKnowRequest
import ipt.lei.dam.ncrapp.models.DidYouKnowResponse
import ipt.lei.dam.ncrapp.models.EventRequest
import ipt.lei.dam.ncrapp.models.EventResponse
import ipt.lei.dam.ncrapp.models.GetEventsRequest
import ipt.lei.dam.ncrapp.models.LoginRequest
import ipt.lei.dam.ncrapp.models.LoginResponse
import ipt.lei.dam.ncrapp.models.RecoverPasswordRequest
import ipt.lei.dam.ncrapp.models.SendOTPRequest
import ipt.lei.dam.ncrapp.models.SignUpRequest
import ipt.lei.dam.ncrapp.models.SignUpResponse
import ipt.lei.dam.ncrapp.models.SubscribeEventRequest
import ipt.lei.dam.ncrapp.models.ValidateOTPRequest
import ipt.lei.dam.ncrapp.models.ValidateOTPResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface APIService {
        @POST("auth/login")
        fun login(@Body request: LoginRequest): Call<LoginResponse>

        @POST("auth/recover")
        fun recoverPassword(@Body request: RecoverPasswordRequest): Call<ResponseBody>

        @POST("auth/validate")
        fun validateOTP(@Body request: ValidateOTPRequest): Call<ValidateOTPResponse>

        @POST("auth/change")
        fun changePassword(@Body request: ChangePasswordRequest): Call<ResponseBody>

        @POST("auth/signup")
        fun signUp(@Body request: SignUpRequest): Call<SignUpResponse>

        @POST("auth/send")
        fun sendOTP(@Body request: SendOTPRequest): Call<ResponseBody>

        /**
         * Events
         */
        @POST("event/all")
        fun getEvents(@Body getEventsRequest: GetEventsRequest): Call<List<EventResponse>>

        @POST("event")
        fun addEvent(@Body eventRequest: EventRequest) : Call<ResponseBody>

        @PUT("event")
        fun editEvent(@Body eventRequest: EventRequest) : Call<ResponseBody>

        @DELETE("event/{id}")
        fun deleteEvent(@Path("id") id: Int) : Call<ResponseBody>

        @POST("event/subscribe")
        fun subscribeEvent(@Body subscribeEventRequest: SubscribeEventRequest) : Call<ResponseBody>

        @POST("event/cancel")
        fun cancelarInscricao(@Body subscribeEventRequest: SubscribeEventRequest) : Call<ResponseBody>

        /**
         * DidYouKnow
         */
        @GET("didyouknow/all")
        fun getDidYouKnow() : Call<List<DidYouKnowResponse>>

        @POST("didyouknow")
        fun addDidYouKnow(@Body didYouKnowRequest: DidYouKnowRequest) : Call<ResponseBody>

        @PUT("didyouknow")
        fun editDidYouKnow(@Body didYouKnowRequest: DidYouKnowRequest) : Call<ResponseBody>

        @DELETE("didyouknow/{id}")
        fun deleteDidYouKnow(@Path("id") id: Int) : Call<ResponseBody>

}
