package com.example.zenity.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.zenity.R

/**
 * Base activity that provides common functionality for all activities
 */
open class BaseActivity : AppCompatActivity() {

    private var loadingAnimation: LottieAnimationView? = null
    private var contentView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    /**
     * Setup loading animation view
     */
    protected fun setupLoadingAnimation(loadingAnimView: LottieAnimationView, content: View) {
        loadingAnimation = loadingAnimView
        contentView = content
    }

    /**
     * Show loading animation and hide content
     */
    protected fun showLoading() {
        loadingAnimation?.visibility = View.VISIBLE
        loadingAnimation?.playAnimation()
        contentView?.visibility = View.GONE
    }

    /**
     * Hide loading animation and show content
     */
    protected fun hideLoading() {
        loadingAnimation?.visibility = View.GONE
        loadingAnimation?.pauseAnimation()
        contentView?.visibility = View.VISIBLE
    }

    /**
     * Show a toast message
     */
    protected fun showToast(message: String, isError: Boolean = false) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun finish() {
        super.finish()
        // Apply exit animation
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
