package ipt.lei.dam.ncrapp.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import ipt.lei.dam.ncrapp.R

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_NcrAPP)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        //Carregar imagem de logo e imagem de loading da splashScreen
        val logoView = findViewById<View>(R.id.logo_view)
        val loadingImageView = findViewById<View>(R.id.loading_image)

        //Carregar as animações da imagem e do logo
        val logoAnimation = AnimationUtils.loadAnimation(this, R.anim.logo_animation)
        val rotateLoadingAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_loading)

        //Ativar a animação do logo
        logoView.startAnimation(logoAnimation)

        //Limpeza do login guardado em cache de forma a resetar o login do utilizador
        val userInfo = getSharedPreferences(getString(R.string.userInfo), Context.MODE_PRIVATE)
        val keepLogin = userInfo.getBoolean("keepLogin", false)
        if(!keepLogin)
            userInfo.edit().clear().apply()


        //Implementado um listener para quando a imagem do logo acabar a sua animação, o loading começar
        logoAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                loadingImageView.startAnimation(rotateLoadingAnimation)

                // Após dois segundos, entrar na atividade principal
                Handler(Looper.getMainLooper()).postDelayed({
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                }, 2000)//Dois segundos
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
    }
}