package ipt.lei.dam.ncrapp.activities

import android.content.Context.MODE_PRIVATE
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import ipt.lei.dam.ncrapp.R
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException

open class BasicFragment : Fragment() {
    var toast: Toast? = null
    protected lateinit var loadingImage: ImageView
    protected lateinit var rotationAnimation: Animation

    protected fun setupLoadingAnimation(view: View) {
        loadingImage = view.findViewById(R.id.loading_image)
        rotationAnimation = AnimationUtils.loadAnimation(this.context, R.anim.rotate_loading)
    }


    protected fun setLoadingVisibility(visible: Boolean) {
        if (visible) {
            loadingImage.visibility = View.VISIBLE
            loadingImage.startAnimation(rotationAnimation)
        } else {
            loadingImage.visibility = View.GONE
            loadingImage.clearAnimation()
        }
    }

    protected fun <T> makeRequestWithRetries(
        requestCall: () -> Response<T>,
        onSuccess: (T) -> Unit,
        onError: (String) -> Unit,
        maxAttempts: Int = 3
    ) {
        setLoadingVisibility(true)
        Thread {
            var attemptCount = 0
            var successful = false

            while (attemptCount < maxAttempts && !successful) {
                try {
                    val response = requestCall()
                    if (response.isSuccessful) {
                        activity?.runOnUiThread {
                            onSuccess(response.body()!!)
                            setLoadingVisibility(false)
                        }
                        successful = true
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val errorMessage = JSONObject(errorBody).getString("message")
                        activity?.runOnUiThread {
                            onError(errorMessage)
                            setLoadingVisibility(false)
                        }
                        break // Não tentar novamente em caso de erro de resposta HTTP
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    attemptCount++
                    if (attemptCount >= maxAttempts) {
                        activity?.runOnUiThread {
                            onError("Erro ao tentar conectar. Por favor, tente novamente.")
                            setLoadingVisibility(false)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    activity?.runOnUiThread {
                        onError("Erro inesperado. Por favor, tente novamente.")
                        setLoadingVisibility(false)
                    }
                    break
                }
            }
        }.start()
    }

}