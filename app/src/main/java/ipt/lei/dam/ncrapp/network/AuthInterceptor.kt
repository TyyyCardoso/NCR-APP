package ipt.lei.dam.ncrapp.network

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    var token: String? = null

    /**
     *
     * Este interceptor serve para apanhar o token de autorização que está a ser retornado pela API
     *
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val requestBuilder = original.newBuilder().apply {
            token?.let { header("Authorization", "Bearer $it") }
        }

        return chain.proceed(requestBuilder.build())
    }
}