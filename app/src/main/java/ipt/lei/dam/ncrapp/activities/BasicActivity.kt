package ipt.lei.dam.ncrapp.activities

import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import ipt.lei.dam.ncrapp.R

open class BaseActivity : AppCompatActivity() {

    private lateinit var loadingImage: ImageView
    private lateinit var rotationAnimation: Animation

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        setupLoadingAnimation()
    }

    private fun setupLoadingAnimation() {
        loadingImage = findViewById(R.id.loading_image)
        rotationAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_loading)
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

    fun isEmailValid(email: String) : Boolean {
        val emailRegex = ("(?:[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")" +
                "@(?:(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}" +
                "(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-zA-Z0-9-]*[a-zA-Z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])")
        return email.matches(emailRegex.toRegex())
    }
}