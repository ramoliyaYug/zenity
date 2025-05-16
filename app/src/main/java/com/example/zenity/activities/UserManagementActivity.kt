package com.example.zenity.activities

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.zenity.R
import com.example.zenity.adapters.UserAdapter
import com.example.zenity.models.User
import com.example.zenity.utils.FirebaseManager
import com.example.zenity.utils.PreferenceManager
import com.google.android.material.tabs.TabLayout

class UserManagementActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingAnimation: LottieAnimationView
    private lateinit var emptyView: TextView
    private lateinit var emptyStateContainer: View
    private lateinit var adapter: UserAdapter
    private lateinit var prefManager: PreferenceManager

    private var currentTab = 0 // 0 = Therapists, 1 = Patients

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_management)

        prefManager = PreferenceManager(this)

        // Check if user is admin
        if (prefManager.getUserType() != "admin") {
            finish()
            return
        }

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "User Management"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        tabLayout = findViewById(R.id.tab_layout)
        recyclerView = findViewById(R.id.user_recycler_view)
        loadingAnimation = findViewById(R.id.progress_bar)
        emptyView = findViewById(R.id.empty_view)
        emptyStateContainer = findViewById(R.id.empty_state_container)

        setupTabLayout()
        setupRecyclerView()
        loadUsers()
    }

    private fun setupTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setText("Therapists"))
        tabLayout.addTab(tabLayout.newTab().setText("Patients"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentTab = tab.position
                loadUsers()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun setupRecyclerView() {
        adapter = UserAdapter { user ->
            // Handle user selection if needed
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadUsers() {
        loadingAnimation.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyStateContainer.visibility = View.GONE

        if (currentTab == 0) {
            // Load therapists
            FirebaseManager.getAllTherapists { therapists ->
                displayUsers(therapists)
            }
        } else {
            // Load patients
            FirebaseManager.getAllPatients { patients ->
                displayUsers(patients)
            }
        }
    }

    private fun displayUsers(users: List<User>) {
        loadingAnimation.visibility = View.GONE

        if (users.isEmpty()) {
            emptyStateContainer.visibility = View.VISIBLE
            emptyView.text = "No ${if (currentTab == 0) "therapists" else "patients"} found"
            recyclerView.visibility = View.GONE
        } else {
            emptyStateContainer.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            adapter.setUsers(users)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
