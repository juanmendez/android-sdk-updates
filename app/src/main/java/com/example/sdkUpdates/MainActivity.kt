package com.example.sdkUpdates

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sdkUpdates.databinding.ActivityMainBinding


@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var permissionUtil: MultiPermissionUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }

        permissionUtil = MultiPermissionUtil(this, listOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ))
    }

    override fun onResume() {
        super.onResume()
        permissionUtil.refreshUpdate(::updateDisplay)
    }

    private fun updateDisplay(permission: String, state: PermissionState) {
        when (state) {
            PermissionState.GRANTED -> {
                binding.activityMainTextview.text = "Granted - $permission"
                binding.activityMainTextview.setOnClickListener { }
            }
            PermissionState.TEMPORARILY_DENIED -> {
                binding.activityMainTextview.text = "Temporarily denied - $permission"

                binding.activityMainTextview.setOnClickListener {
                    permissionUtil.request()
                }
            }
            PermissionState.PERMANENTLY_DENIED -> {
                binding.activityMainTextview.text = "Permanently denied - $permission"
                binding.activityMainTextview.setOnClickListener {
                    permissionUtil.request()
                }
            }
            PermissionState.INITIAL -> {
                binding.activityMainTextview.text = "First time - $permission"
                binding.activityMainTextview.setOnClickListener { }
                permissionUtil.request()
            }
            else -> {
                binding.activityMainTextview.text = "All granted - $permission"
                binding.activityMainTextview.setOnClickListener { }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        permissionUtil.unregister()
    }
}