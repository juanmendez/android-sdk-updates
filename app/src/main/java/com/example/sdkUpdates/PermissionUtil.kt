package com.example.sdkUpdates

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

enum class PermissionState {
    NONE,
    INITIAL,
    GRANTED,
    TEMPORARILY_DENIED,
    PERMANENTLY_DENIED,
    ALL_GRANTED,
}

class PermissionUtil(private val activity: AppCompatActivity, private val permissionType: String) {
    companion object {
        private const val PACKAGE = "package"
    }

    private var requestPermissionLauncher: ActivityResultLauncher<String>
    private val preferences: SharedPreferences
        get() = activity.getPreferences(Context.MODE_PRIVATE)

    private var _currentState = PermissionState.NONE

    @Suppress("unused")
    val currentState: PermissionState
        get() = _currentState

    private var permissionListener: (PermissionState) -> Unit = {}


    private val sharedKey: String
        get() = "isInitialized-$permissionType"

    init {
        requestPermissionLauncher = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            preferences.edit().putBoolean(sharedKey, true).apply()
            updateDisplay(isGranted)
        }
    }

    fun refreshUpdate(listener: (PermissionState) -> Unit) {
        val permissionValue = ContextCompat.checkSelfPermission(activity, permissionType)
        val isGranted = permissionValue == PackageManager.PERMISSION_GRANTED
        permissionListener = listener
        updateDisplay(isGranted)
    }

    private fun updateDisplay(isGranted: Boolean) {
        val lastState = _currentState

        _currentState = when {
            isGranted -> {
                PermissionState.GRANTED
            }
            ActivityCompat.shouldShowRequestPermissionRationale(activity, permissionType) -> {
                PermissionState.TEMPORARILY_DENIED
            }
            preferences.getBoolean(sharedKey, false) -> {
                PermissionState.PERMANENTLY_DENIED
            }
            else -> {
                PermissionState.INITIAL
            }
        }

        // prevent to update if both init and refreshUpdate call sequentially
        if (lastState != _currentState) {
            permissionListener(_currentState)
        }
    }

    fun request() {
        if (_currentState == PermissionState.PERMANENTLY_DENIED) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                data = Uri.fromParts(PACKAGE, activity.packageName, null)
            }
            activity.startActivity(intent)
        } else {
            requestPermissionLauncher.launch(permissionType)
        }
    }

    fun unregister() = requestPermissionLauncher.unregister()
}