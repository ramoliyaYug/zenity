package com.example.zenity.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.navigation.NavigationView
import com.example.zenity.R
import com.example.zenity.utils.FirebaseManager
import com.example.zenity.utils.PreferenceManager
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var prefManager: PreferenceManager
    private lateinit var dashboardContainer: LinearLayout
    private lateinit var welcomeText: TextView
    private lateinit var loadingAnimation: LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply splash screen
        installSplashScreen()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefManager = PreferenceManager(this)

        // Check if user is logged in
        if (!prefManager.isLoggedIn()) {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }

        // Initialize views
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        dashboardContainer = findViewById(R.id.dashboard_container)
        welcomeText = findViewById(R.id.welcome_text)
        loadingAnimation = findViewById(R.id.loading_animation)

        // Setup drawer toggle
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener(this)

        // Set user info in navigation header
        val headerView = navigationView.getHeaderView(0)
        val navUsername: TextView = headerView.findViewById(R.id.nav_header_username)
        val navUserType: TextView = headerView.findViewById(R.id.nav_header_user_type)

        navUsername.text = prefManager.getUserName()
        navUserType.text = prefManager.getUserType()?.capitalize()

        // Show appropriate menu items based on user type
        val menu = navigationView.menu
        if (prefManager.getUserType() == "patient") {
            menu.findItem(R.id.nav_therapists).isVisible = true
        } else {
            menu.findItem(R.id.nav_therapists).isVisible = false
        }

        // Show loading animation
        loadingAnimation.visibility = View.VISIBLE
        dashboardContainer.visibility = View.GONE

        // Simulate loading delay
        Handler(Looper.getMainLooper()).postDelayed({
            loadingAnimation.visibility = View.GONE
            dashboardContainer.visibility = View.VISIBLE

            // Animate dashboard items
            animateDashboard()

            // Set welcome message
            welcomeText.text = "Welcome, ${prefManager.getUserName()}"
        }, 1500)

        // Setup dashboard card clicks
        setupDashboardCards()
    }

    private fun animateDashboard() {
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)

        welcomeText.startAnimation(fadeIn)

        // Animate each card with a delay
        for (i in 0 until dashboardContainer.childCount) {
            val card = dashboardContainer.getChildAt(i)
            card.startAnimation(slideUp)
            slideUp.startOffset = (i * 150).toLong()
        }
    }

    private fun setupDashboardCards() {
        val profileCard: CardView = findViewById(R.id.profile_card)
        val sessionsCard: CardView = findViewById(R.id.sessions_card)
        val messagesCard: CardView = findViewById(R.id.messages_card)
        val forumCard: CardView = findViewById(R.id.forum_card)
        val findTherapistCard: CardView = findViewById(R.id.find_therapist_card)

        profileCard.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        sessionsCard.setOnClickListener {
            startActivity(Intent(this, SessionsActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        messagesCard.setOnClickListener {
            startActivity(Intent(this, ChatListActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        forumCard.setOnClickListener {
            startActivity(Intent(this, ForumActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        findTherapistCard.setOnClickListener {
            startActivity(Intent(this, TherapistListActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        // Show/hide therapist card based on user type
        if (prefManager.getUserType() == "therapist") {
            findTherapistCard.visibility = View.GONE
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            R.id.nav_therapists -> {
                startActivity(Intent(this, TherapistListActivity::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            R.id.nav_sessions -> {
                startActivity(Intent(this, SessionsActivity::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            R.id.nav_chat -> {
                startActivity(Intent(this, ChatListActivity::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            R.id.nav_forum -> {
                startActivity(Intent(this, ForumActivity::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            R.id.nav_logout -> {
                FirebaseManager.logout()
                prefManager.clearUserSession()
                startActivity(Intent(this, AuthActivity::class.java))
                finish()
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
