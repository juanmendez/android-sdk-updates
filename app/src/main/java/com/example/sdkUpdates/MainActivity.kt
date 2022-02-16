package com.example.sdkUpdates

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sdkUpdates.databinding.ActivityMainBinding


@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var permissionUtil: PermissionUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }

        permissionUtil = PermissionUtil(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    override fun onResume() {
        super.onResume()
        permissionUtil.refreshUpdate(::updateDisplay)
    }

    private fun updateDisplay(state: PermissionState) {
        when (state) {
            PermissionState.GRANTED -> {
                binding.activityMainTextview.text = "Granted"
                binding.activityMainTextview.setOnClickListener { }
            }
            PermissionState.TEMPORARILY_DENIED -> {
                binding.activityMainTextview.text = "Temporarily denied"

                binding.activityMainTextview.setOnClickListener {
                    permissionUtil.request()
                }
            }
            PermissionState.PERMANENTLY_DENIED -> {
                binding.activityMainTextview.text = "Permanently denied"
                binding.activityMainTextview.setOnClickListener {
                    permissionUtil.request()
                }
            }
            else -> {
                binding.activityMainTextview.text = "First time"
                binding.activityMainTextview.setOnClickListener { }
                permissionUtil.request()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        permissionUtil.unregister()
    }
}