package com.example.sdkUpdates

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.sdkUpdates.databinding.ActivityMainBinding


@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    // Register the permissions callback, which handles the user's response to the
    // system permissions dialog. Save the return value, an instance of
    // ActivityResultLauncher. You can use either a val, as shown in this snippet,
    // or a lateinit var in your onAttach() or onCreate() method.
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private val preferences: SharedPreferences
        get() = getPreferences(Context.MODE_PRIVATE)

    private val sharedKey: String
        get() = "isInitialized-${Manifest.permission.WRITE_EXTERNAL_STORAGE}"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }

        // we check permission has been set for the first time
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            // storing if the user has made at least one time decision came from
            // https://medium.com/@begalesagar/method-to-detect-if-user-has-selected-dont-ask-again-while-requesting-for-permission-921b95ded536
            // there is not a way to track if the user has made a decision yet from Android
            preferences.edit().putBoolean(sharedKey, true).apply()
        }
    }

    override fun onResume() {
        super.onResume()

        val permissionValue = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val isGranted = permissionValue == PackageManager.PERMISSION_GRANTED
        updateDisplay(isGranted)
    }

    private fun updateDisplay(isGranted: Boolean) {
        // Explain to the user that the feature is unavailable because the
        // features requires a permission that the user has denied. At the
        // same time, respect the user's decision. Don't link to system
        // settings in an effort to convince the user to change their
        // decision.
        when {
            isGranted -> {
                binding.activityMainTextview.text = "Granted"
                binding.activityMainTextview.setOnClickListener { }
            }
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                binding.activityMainTextview.text = "Temporarily denied"

                binding.activityMainTextview.setOnClickListener {
                    requestPermission()
                }
            }
            preferences.getBoolean(sharedKey, false) -> {
                binding.activityMainTextview.text = "Permanently denied"
                binding.activityMainTextview.setOnClickListener {
                    launchAppPermissions()
                }
            }
            else -> {
                binding.activityMainTextview.text = "First time"
                binding.activityMainTextview.setOnClickListener { }
                requestPermission()
            }
        }
    }

    private fun requestPermission() {
        requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private fun launchAppPermissions() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        requestPermissionLauncher.unregister()
    }
}