package com.example.sdkUpdates

import androidx.appcompat.app.AppCompatActivity

class MultiPermissionUtil(private val activity: AppCompatActivity, private val permissionTypes: List<String>) {
    inner class PermissionKey(val type: String, val util: PermissionUtil)

    private val permissionMap = permissionTypes.map {
        PermissionKey(it, PermissionUtil(activity, it))
    }.toList()
    private var permissionKey = permissionMap.first()

    fun refreshUpdate(listener: (String, PermissionState) -> Unit) {
        permissionKey.util.refreshUpdate { state ->
            listener(permissionKey.type, state)

            if (state == PermissionState.GRANTED) {
                // we are done with last permission
                permissionKey.util.unregister()

                val index = permissionMap.indexOf(permissionKey)
                if (index < permissionMap.size - 1) {
                    permissionKey = permissionMap[index + 1]
                    refreshUpdate(listener)
                } else {
                    listener(permissionTypes.joinToString(), PermissionState.ALL_GRANTED)
                }
            }
        }
    }

    fun request() = permissionKey.util.request()

    fun unregister() = permissionKey.util.unregister()
}