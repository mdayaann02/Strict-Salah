package com.example.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.example.R

enum class AppLogoTheme(
    val key: String,
    val title: String,
    val subtitle: String,
    val genderLabel: String,
    val drawableRes: Int,
    val aliasName: String?
) {
    DEFAULT(
        key = "DEFAULT",
        title = "Universal Edition",
        subtitle = "Celestial Navy & Royal Gold Strict Salah Emblem",
        genderLabel = "Universal / All",
        drawableRes = R.drawable.app_logo,
        aliasName = null // Main activity
    ),
    BROTHER(
        key = "BROTHER",
        title = "Brother Edition",
        subtitle = "Emerald Green & Warm Gold Minaret Arch",
        genderLabel = "Brother (Masculine)",
        drawableRes = R.drawable.app_logo_brother,
        aliasName = "com.example.MainActivityBrother"
    ),
    SISTER(
        key = "SISTER",
        title = "Sister Edition",
        subtitle = "Rose Gold & Floral Pearl Crescent Mihrab",
        genderLabel = "Sister (Feminine)",
        drawableRes = R.drawable.app_logo_sister,
        aliasName = "com.example.MainActivitySister"
    );

    companion object {
        fun fromKey(key: String): AppLogoTheme {
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: DEFAULT
        }
    }
}

object AppIconManager {

    fun applyLogoTheme(context: Context, theme: AppLogoTheme): Boolean {
        return try {
            val pm = context.packageManager
            val packageName = context.packageName

            val mainComponent = ComponentName(packageName, "com.example.MainActivity")
            val brotherComponent = ComponentName(packageName, "com.example.MainActivityBrother")
            val sisterComponent = ComponentName(packageName, "com.example.MainActivitySister")

            when (theme) {
                AppLogoTheme.DEFAULT -> {
                    pm.setComponentEnabledSetting(
                        mainComponent,
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP
                    )
                    pm.setComponentEnabledSetting(
                        brotherComponent,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                    )
                    pm.setComponentEnabledSetting(
                        sisterComponent,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                    )
                }
                AppLogoTheme.BROTHER -> {
                    pm.setComponentEnabledSetting(
                        brotherComponent,
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP
                    )
                    pm.setComponentEnabledSetting(
                        mainComponent,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                    )
                    pm.setComponentEnabledSetting(
                        sisterComponent,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                    )
                }
                AppLogoTheme.SISTER -> {
                    pm.setComponentEnabledSetting(
                        sisterComponent,
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP
                    )
                    pm.setComponentEnabledSetting(
                        mainComponent,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                    )
                    pm.setComponentEnabledSetting(
                        brotherComponent,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                    )
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
