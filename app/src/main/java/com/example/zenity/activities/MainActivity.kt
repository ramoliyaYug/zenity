package com.example.zenity.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.example.zenity.R
import com.example.zenity.utils.FirebaseManager
import com.example.zenity.utils.PreferenceManager

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var prefManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefManager = PreferenceManager(this)

        // Check if user is logged in
        if (!prefManager.isLoggedIn()) {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView: NavigationView = findViewById(R.id.nav_view)

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

        // Default to therapist list for patients, sessions for therapists
        if (savedInstanceState == null) {
            if (prefManager.getUserType() == "patient") {
                navigationView.setCheckedItem(R.id.nav_therapists)
                startActivity(Intent(this, TherapistListActivity::class.java))
            } else {
                navigationView.setCheckedItem(R.id.nav_sessions)
                startActivity(Intent(this, SessionsActivity::class.java))
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
            }
            R.id.nav_therapists -> {
                startActivity(Intent(this, TherapistListActivity::class.java))
            }
            R.id.nav_sessions -> {
                startActivity(Intent(this, SessionsActivity::class.java))
            }
            R.id.nav_chat -> {
                startActivity(Intent(this, ChatListActivity::class.java))
            }
            R.id.nav_forum -> {
                startActivity(Intent(this, ForumActivity::class.java))
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
}
