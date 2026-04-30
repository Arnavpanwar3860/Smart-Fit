package com.example.smartfit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class)
    )

    private val requestPermissionLauncher = registerForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        if (granted.containsAll(permissions)) {
            proceedToNextScreen()
        } else {
            Toast.makeText(this, "Permission required for step tracking", Toast.LENGTH_SHORT).show()
            // Proceed anyway, but features might be limited
            proceedToNextScreen()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val nameEditText = findViewById<EditText>(R.id.nameInput)
        val loginBtn = findViewById<Button>(R.id.loginBtn)
        val loginLayout = findViewById<LinearLayout>(R.id.loginLayout)

        // Bold + Modern: Fade-in animation for the container
        loginLayout.alpha = 0f
        loginLayout.animate().alpha(1f).setDuration(1500)

        loginBtn.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            if (name.isNotEmpty()) {
                checkAndRequestPermissions()
            } else {
                Toast.makeText(this, "Enter name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkAndRequestPermissions() {
        if (HealthConnectClient.getSdkStatus(this) != HealthConnectClient.SDK_AVAILABLE) {
            Toast.makeText(this, "Health Connect is not available", Toast.LENGTH_SHORT).show()
            proceedToNextScreen()
            return
        }

        val healthConnectClient = HealthConnectClient.getOrCreate(this)
        lifecycleScope.launch {
            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            if (granted.containsAll(permissions)) {
                proceedToNextScreen()
            } else {
                requestPermissionLauncher.launch(permissions)
            }
        }
    }

    private fun proceedToNextScreen() {
        val nameEditText = findViewById<EditText>(R.id.nameInput)
        val name = nameEditText.text.toString().trim()

        val userPrefsKey = "SmartFitPrefs_${name.lowercase()}"
        val sharedPref = getSharedPreferences(userPrefsKey, Context.MODE_PRIVATE)
        val isProfileComplete = sharedPref.getBoolean("is_profile_complete", false)

        if (isProfileComplete) {
            val intent = Intent(this, DashboardActivity::class.java).apply {
                putExtra("USER_NAME", name)
                putExtra("username", name)
            }
            startActivity(intent)
        } else {
            val intent = Intent(this, ProfileActivity::class.java).apply {
                putExtra("USER_NAME", name)
                putExtra("username", name)
            }
            startActivity(intent)
        }
    }
}
