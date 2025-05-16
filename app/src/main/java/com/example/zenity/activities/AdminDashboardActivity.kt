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

class AdminDashboardActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var prefManager: PreferenceManager
    private lateinit var dashboardContainer: LinearLayout
    private lateinit var welcomeText: TextView
    private lateinit var loadingAnimation: LottieAnimationView
    private lateinit var pendingVerificationsCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        prefManager = PreferenceManager(this)

        // Check if user is logged in and is admin
        if (!prefManager.isLoggedIn() || prefManager.getUserType() != "admin") {
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
        pendingVerificationsCount = findViewById(R.id.pending_verifications_count)

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
        navUserType.text = "Administrator"

        // Show loading animation
        loadingAnimation.visibility = View.VISIBLE
        dashboardContainer.visibility = View.GONE

        // Load pending verifications count
        loadPendingVerificationsCount()

        // Simulate loading delay
        Handler(Looper.getMainLooper()).postDelayed({
            loadingAnimation.visibility = View.GONE
            dashboardContainer.visibility = View.VISIBLE

            // Animate dashboard items
            animateDashboard()

            // Set welcome message
            welcomeText.text = "Welcome, Admin ${prefManager.getUserName()}"
        }, 1500)

        // Setup dashboard card clicks
        setupDashboardCards()
    }

    private fun loadPendingVerificationsCount() {
        FirebaseManager.getPendingVerificationRequests { requests ->
            pendingVerificationsCount.text = requests.size.toString()
        }
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
        val verifyTherapistsCard: CardView = findViewById(R.id.verify_therapists_card)
        val manageUsersCard: CardView = findViewById(R.id.manage_users_card)
        val viewStatsCard: CardView = findViewById(R.id.view_stats_card)

        verifyTherapistsCard.setOnClickListener {
            startActivity(Intent(this, TherapistVerificationListActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        manageUsersCard.setOnClickListener {
            startActivity(Intent(this, UserManagementActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        viewStatsCard.setOnClickListener {
            startActivity(Intent(this, AdminStatsActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_verify_therapists -> {
                startActivity(Intent(this, TherapistVerificationListActivity::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            R.id.nav_manage_users -> {
                startActivity(Intent(this, UserManagementActivity::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            R.id.nav_view_stats -> {
                startActivity(Intent(this, AdminStatsActivity::class.java))
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

    override fun onResume() {
        super.onResume()
        loadPendingVerificationsCount()
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
