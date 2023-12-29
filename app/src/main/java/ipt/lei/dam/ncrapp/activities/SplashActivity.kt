package ipt.lei.dam.ncr.activities

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
import ipt.lei.dam.ncrapp.activities.Event
import ipt.lei.dam.ncrapp.activities.MainActivity
import ipt.lei.dam.ncrapp.activities.authentication.LoginActivity
import ipt.lei.dam.ncrapp.activities.navigation.EventsFragmento

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_NcrAPP)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logoView = findViewById<View>(R.id.logo_view)
        val loadingImageView = findViewById<View>(R.id.loading_image) // Your loading image view

        // Load the logo animation
        val logoAnimation = AnimationUtils.loadAnimation(this, R.anim.logo_animation)

        // Load the rotation animation
        val rotateLoadingAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_loading)

        // Start the logo animation
        logoView.startAnimation(logoAnimation)

        // To clear all SharedPreferences data
        val sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        // Listener for logo animation
        logoAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                // Start rotation animation after logo animation ends
                loadingImageView.startAnimation(rotateLoadingAnimation)



                // Delay for 2 seconds before starting MainActivity
                Handler(Looper.getMainLooper()).postDelayed({
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                }, 2000)  // 2000 milliseconds delay
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
    }
}