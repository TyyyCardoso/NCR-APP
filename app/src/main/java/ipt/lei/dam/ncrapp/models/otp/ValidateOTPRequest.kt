package ipt.lei.dam.ncrapp.models.otp

data class ValidateOTPRequest(val otp: String, val email: String?, val type: String?)
