package com.example.zenity.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.zenity.R

class SplashActivity : AppCompatActivity() {

    private val SPLASH_DELAY = 3000L // 3 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Animate logo and app name
        val logoImage = findViewById<ImageView>(R.id.splash_logo)
        val appNameText = findViewById<TextView>(R.id.splash_app_name)
        val taglineText = findViewById<TextView>(R.id.splash_tagline)
        val loadingAnimation = findViewById<LottieAnimationView>(R.id.splash_loading_animation)

        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        val pulse = AnimationUtils.loadAnimation(this, R.anim.pulse)

        logoImage.startAnimation(fadeIn)
        appNameText.startAnimation(slideUp)
        taglineText.startAnimation(fadeIn)
        loadingAnimation.playAnimation()
        logoImage.startAnimation(pulse)

        // Navigate to main activity after delay
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }, SPLASH_DELAY)
    }
}
