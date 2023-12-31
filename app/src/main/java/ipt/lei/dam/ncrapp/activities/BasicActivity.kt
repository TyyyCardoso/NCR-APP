package ipt.lei.dam.ncrapp.activities

import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ipt.lei.dam.ncrapp.R
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException

open class BaseActivity : AppCompatActivity() {

    var toast: Toast? = null
    private lateinit var loadingImage: ImageView
    private lateinit var rotationAnimation: Animation

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        setupLoadingAnimation()
    }

    //Serve para definir a imagem de loading nas activies
    private fun setupLoadingAnimation() {
        loadingImage = findViewById(R.id.loading_image)
        rotationAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_loading)
    }

    /**
     *
     * Função usada para mostrar ou deixar de mostrar a imagem de loading (cotonete)
     *
     */
    protected fun setLoadingVisibility(visible: Boolean) {
        if (visible) {
            loadingImage.visibility = View.VISIBLE
            loadingImage.startAnimation(rotationAnimation)
        } else {
            loadingImage.visibility = View.GONE
            loadingImage.clearAnimation()
        }
    }

    /**
     *
     * Função que valida se o email inserido está correto e dentro do padrão
     *
     */
    fun isEmailValid(email: String) : Boolean {
        val emailRegex = ("^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$")
        return email.matches(emailRegex.toRegex())
    }

    /**
     *
     * Função que faz as chamadas ao retrofit.
     * É usada esta função para não se repetir código extensivo ao longo das atividades
     * Recebe três parâmetros:
     *  requestCall -> Que é onde vai ser inserida a chamada
     *  onSuccess -> Que é o que acontece quando a chamada dá sucesso
     *  onError -> Usado quando a chamada retorna erro
     *
     */
    fun <T> makeRequestWithRetries(requestCall: () -> Response<T>, onSuccess: (T) -> Unit, onError: (String) -> Unit, maxAttempts: Int = 3
    ) {
        //É usado este método para incorporar a imagem de loading que está a ser reutilizada em praticamente quase todas as atividades
        setLoadingVisibility(true)
        //É iniciada uma thread para executar a chamada, isto é necessário devido a não bloquear o UI
        Thread {
            var attemptCount = 0
            var successful = false

            // Este é um método implementado com sistema de retries caso a chamada não funcione à primeira, por isso um while que irá iterar enquanto
            // não esgotar as tentativas ou não ter dado sucesso
            while (attemptCount < maxAttempts && !successful) {
                try {
                    //Tratado o requestCall que é definido na atividade (é a chamada a fazer)
                    val response = requestCall()
                    //Se a resposta foi com sucesso
                    if (response.isSuccessful) {
                        runOnUiThread {
                            onSuccess(response.body()!!)
                            setLoadingVisibility(false)
                        }
                        successful = true
                    } else {
                        val errorBody = response.errorBody()!!.string()
                        val errorMessage = JSONObject(errorBody).getString("message")
                        runOnUiThread {
                            onError(errorMessage)
                            setLoadingVisibility(false)
                        }
                        break // Não tentar novamente em caso de erro de resposta HTTP
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    attemptCount++
                    if (attemptCount >= maxAttempts) {
                        runOnUiThread {
                            onError("Erro ao tentar conectar. Por favor, tente novamente.")
                            setLoadingVisibility(false)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread {
                        onError("Erro inesperado. Por favor, tente novamente.")
                        setLoadingVisibility(false)
                    }
                    break
                }
            }
        }.start()
    }
}