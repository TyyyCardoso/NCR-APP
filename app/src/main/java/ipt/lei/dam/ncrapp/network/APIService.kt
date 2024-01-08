
import ipt.lei.dam.ncrapp.models.DefaultResponse
import ipt.lei.dam.ncrapp.models.didyouknow.DidYouKnowAddRequest
import ipt.lei.dam.ncrapp.models.didyouknow.DidYouKnowEditRequest
import ipt.lei.dam.ncrapp.models.didyouknow.DidYouKnowResponse
import ipt.lei.dam.ncrapp.models.events.EventResponse
import ipt.lei.dam.ncrapp.models.events.GetEventsRequest
import ipt.lei.dam.ncrapp.models.events.SubscribeEventRequest
import ipt.lei.dam.ncrapp.models.login.BiometricLoginRequest
import ipt.lei.dam.ncrapp.models.login.LoginRequest
import ipt.lei.dam.ncrapp.models.login.LoginResponse
import ipt.lei.dam.ncrapp.models.otp.SendOTPRequest
import ipt.lei.dam.ncrapp.models.otp.ValidateOTPRequest
import ipt.lei.dam.ncrapp.models.otp.ValidateOTPResponse
import ipt.lei.dam.ncrapp.models.password.ChangePasswordRequest
import ipt.lei.dam.ncrapp.models.password.RecoverPasswordRequest
import ipt.lei.dam.ncrapp.models.schedule.ScheduleResponse
import ipt.lei.dam.ncrapp.models.signup.SignUpRequest
import ipt.lei.dam.ncrapp.models.signup.SignUpResponse
import ipt.lei.dam.ncrapp.models.staff.StaffMemberResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface APIService {
        /**
         * Chamada de login
         */
        @POST("auth/login")
        fun login(@Body request: LoginRequest): Call<LoginResponse>
        /**
         * Chamada de login biométrico
         */
        @POST("auth/biometricLogin")
        fun biometricLogin(@Body request: BiometricLoginRequest): Call<LoginResponse>
        /**
         * Chamada de recuperação de password
         */
        @POST("auth/recover")
        fun recoverPassword(@Body request: RecoverPasswordRequest): Call<ResponseBody>
        /**
         * Chamada de validar o código
         */
        @POST("auth/validate")
        fun validateOTP(@Body request: ValidateOTPRequest): Call<ValidateOTPResponse>
        /**
         * Chamada de mudar a palavra-passe
         */
        @POST("auth/change")
        fun changePassword(@Body request: ChangePasswordRequest): Call<ResponseBody>
        /**
         * Chamada de criar conta
         */
        @POST("auth/signup")
        fun signUp(@Body request: SignUpRequest): Call<SignUpResponse>
        /**
         * Chamada de enviar código
         */
        @POST("auth/send")
        fun sendOTP(@Body request: SendOTPRequest): Call<ResponseBody>
        /**
         * Chamada de editar perfil
         */
        @Multipart
        @POST("profile/edit")
        fun editProfile(
                @Part("name") name: RequestBody,
                @Part("about") about: RequestBody,
                @Part("email") email: RequestBody,
                @Part image: MultipartBody.Part
        ): Call<DefaultResponse>

        /**
         * Chamada de obter o staff
         */
        @GET("staff/all")
        fun getStaff(): Call<List<StaffMemberResponse>>

        /**
         * Chamada de obter os eventos
         */
        @POST("event/all")
        fun getEvents(@Body getEventsRequest: GetEventsRequest): Call<List<EventResponse>>

        /**
         * Chamada para criar um evento
         */
        @Multipart
        @POST("event")
        fun addEvent(
                @Part("name") name: RequestBody,
                @Part("description") description: RequestBody,
                @Part("initDate") initDate: RequestBody,
                @Part("endDate") endDate: RequestBody,
                @Part("location") location: RequestBody,
                @Part("transport") transport: RequestBody,
                @Part image: MultipartBody.Part
        ): Call<DefaultResponse>
        /**
         * Chamada para editar um evento
         */
        @Multipart
        @PUT("event")
        fun editEvent(
                @Part("id") id: RequestBody,
                @Part("name") name: RequestBody,
                @Part("description") description: RequestBody,
                @Part("initDate") initDate: RequestBody,
                @Part("endDate") endDate: RequestBody,
                @Part("location") location: RequestBody,
                @Part("transport") transport: RequestBody,
                @Part("createdAt") createdAt: RequestBody,
                @Part image: MultipartBody.Part,
                @Part("imageFileName") imageFileName: RequestBody,
        ) : Call<DefaultResponse>
        /**
         * Chamada para eliminar um evento
         */
        @DELETE("event/{id}")
        fun deleteEvent(@Path("id") id: Int) : Call<ResponseBody>
        /**
         * Chamada para increver num evento
         */
        @POST("event/subscribe")
        fun subscribeEvent(@Body subscribeEventRequest: SubscribeEventRequest) : Call<ResponseBody>
        /**
         * Chamada para cancelar uma inscrição
         */
        @POST("event/cancel")
        fun cancelarInscricao(@Body subscribeEventRequest: SubscribeEventRequest) : Call<ResponseBody>

        /**
         * Chamada para obter "Sabias que"
         */
        @GET("didyouknow/all")
        fun getDidYouKnow() : Call<List<DidYouKnowResponse>>
        /**
         * Chamada para criar "sabias que"
         */
        @POST("didyouknow")
        fun addDidYouKnow(@Body didYouKnowRequest: DidYouKnowAddRequest) : Call<DefaultResponse>

        /**
         * Chamada para editar "sabias que"
         */
        @PUT("didyouknow")
        fun editDidYouKnow(@Body didYouKnowRequest: DidYouKnowEditRequest) : Call<DefaultResponse>

        /**
         * Chamada para eliminar "sabias que"
         */
        @DELETE("didyouknow/{id}")
        fun deleteDidYouKnow(@Path("id") id: Int) : Call<ResponseBody>

        /**
         * Chamada obter documentos de horarios
         */
        @GET("docs/all")
        fun getSchedules() : Call<List<ScheduleResponse>>

        /**
         * Chamada para elimianr um documento de horario
         */
        @DELETE("docs/{id}")
        fun deleteSchedule(@Path("id") id: Int?) : Call<ResponseBody>

        /**
         * Chamada para criar um documento de horario
         */
        @Multipart
        @POST("docs/uploadSchedule")
        fun uploadDoc(@Part("docName") docName: RequestBody,
                      @Part("docDescription") docDescription: RequestBody,
                      @Part("docType") docType: RequestBody,
                      @Part pdf: MultipartBody.Part): Call<ResponseBody>


}
