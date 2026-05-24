package com.hula.app

import android.app.Activity
import android.Manifest
import app.tauri.annotation.Permission
import app.tauri.annotation.TauriPlugin
import app.tauri.plugin.Plugin

/**
 * 运行时权限：相机、麦克风、媒体（相册/视频）。
 * 通过 plugin:hula-permissions|checkPermissions / requestPermissions 从前端调用。
 */
@TauriPlugin(
    permissions = [
        Permission(
            strings = [Manifest.permission.CAMERA],
            alias = "camera"
        ),
        Permission(
            strings = [Manifest.permission.RECORD_AUDIO],
            alias = "microphone"
        ),
        Permission(
            strings = [
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            ],
            alias = "media"
        ),
        Permission(
            strings = [Manifest.permission.READ_EXTERNAL_STORAGE],
            alias = "storageLegacy"
        )
    ]
)
class HulaPermissionsPlugin(private val activity: Activity) : Plugin(activity)
