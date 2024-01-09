package ipt.lei.dam.ncrapp.models.login

import java.time.LocalDate
import org.threeten.bp.LocalDateTime

data class LoginResponse(val token: String, val isValidated: Boolean, val email: String?, val name : String, val type: String, val registrationDate: String, val image: String, val about: String)
