package ipt.lei.dam.ncrapp.models

data class LoginResponse(val token: String, val isValidated: Boolean, val email: String?)
