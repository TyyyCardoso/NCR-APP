package ipt.lei.dam.ncrapp.fragments

import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import ipt.lei.dam.ncrapp.R
import org.json.JSONObject
import org.threeten.bp.format.DateTimeFormatter
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

open class BasicFragment : Fragment() {
    var toast: Toast? = null
    protected lateinit var loadingImage: ImageView
    protected lateinit var rotationAnimation: Animation

    //Formatar Datas para base de dados
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    //Formatar Datas para mostrar na aplicação
    val formatShow = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    //Formatar Datas para fazer comparações
    val formatComp = DateTimeFormatter.ISO_DATE_TIME


    /**
     *
     * Serve para definir a imagem de loading nas activies
     *
     */
    protected fun setupLoadingAnimation(view: View) {
        loadingImage = view.findViewById(R.id.loading_image)
        rotationAnimation = AnimationUtils.loadAnimation(this.context, R.anim.rotate_loading)
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
     * Função que faz as chamadas ao retrofit.
     * É usada esta função para não se repetir código extensivo ao longo das atividades
     * Recebe três parâmetros:
     *  requestCall -> Que é onde vai ser inserida a chamada
     *  onSuccess -> Que é o que acontece quando a chamada dá sucesso
     *  onError -> Usado quando a chamada retorna erro
     *
     */
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

    /**
     *
     * Função que comprime imagens reduzindo a qualidade e resolução
     * Recebe um parâmetro:
     *  imageUri -> imagem selecionada pelo utilizador (galeira ou camara)
     *
     */
    fun compressImage(imageUri: Uri): File {
        // Obtem o ficheiro da imagem
        val originalBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, imageUri)
        // Maximo tamanho de um dos lados da imagem
        val maxSize = 800
        //Obter fator de redimensionamento e verificar o lado maior
        val scaleFactor = Math.min(maxSize.toFloat() / originalBitmap.width, maxSize.toFloat() / originalBitmap.height)

        //Se o lado maior for maior que maxSize, a imagem é redimensionada mantendo o seu aspect ratio original
        val resizedBitmap = if (originalBitmap.width > maxSize || originalBitmap.height > maxSize) {
            val newWidth = (originalBitmap.width * scaleFactor).toInt()
            val newHeight = (originalBitmap.height * scaleFactor).toInt()
            Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
        } else {
            originalBitmap
        }

        //Definicao do novo ficheiro comprimido
        var file = File(requireContext().cacheDir, "compressed_image.jpg")

        //Inicia o ciclo de reduçao de qualidade até atingir o tamanho máximo de 1MB
        var quality = 100
        do {
            file.createNewFile()
            val bos = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos)
            val bitmapData = bos.toByteArray()

            FileOutputStream(file).use { fos ->
                fos.write(bitmapData)
                fos.flush()
            }

            quality -= 10 // Reduzir a qualidade em 10% se o ficheiro for maior que 1MB
        } while (file.length() > 1_048_576 && quality > 0) // 1MB

        return file
    }

}