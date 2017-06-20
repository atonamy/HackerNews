package propertyguru.androidtest.com.hackernews.ui.helpers

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.support.customtabs.CustomTabsIntent
import android.support.v4.content.ContextCompat
import propertyguru.androidtest.com.hackernews.R
import android.content.ComponentName




/**
 * Created by archie on 10/6/17.
 */
class CustomTabsHelper(private val activity: Activity) {

    private val serviceAction = "android.support.customtabs.action.CustomTabsService"
    val chromePackage = "com.android.chrome"

    fun openTab(url: String, error: (message: String) -> Unit) {
        if (isChromeCustomTabsSupported()) {
            val customTabsIntent = CustomTabsIntent.Builder()
                    .setStartAnimations(activity, R.anim.slide_in_right, R.anim.slide_out_left)
                    .setExitAnimations(activity, android.R.anim.slide_in_left,
                            android.R.anim.slide_out_right)
                    .setShowTitle(true)
                    .setToolbarColor(ContextCompat.getColor(activity, R.color.colorPrimary)).setShowTitle(true)
                    .setCloseButtonIcon(
                            BitmapFactory.decodeResource(activity.resources, R.drawable.ic_arrow_back))
                    .addDefaultShareMenuItem()
                    .build()
            openCustomTab(activity, customTabsIntent, url, error)
        } else
            forceToInstallChrome()
    }

    private fun isChromeCustomTabsSupported(): Boolean {
        val serviceIntent = Intent(serviceAction)
        serviceIntent.setPackage(chromePackage)
        val resolveInfos = activity.packageManager.queryIntentServices(serviceIntent, 0)
        return !(resolveInfos == null || resolveInfos.isEmpty())
    }

    private inline fun openCustomTab(activity: Activity,
                      customTabsIntent: CustomTabsIntent,
                      uri: String, error: (message: String) -> Unit) {

        try {
            customTabsIntent.intent.`package` = chromePackage
            customTabsIntent.launchUrl(activity, Uri.parse(uri))
        } catch (ex: Exception) {
            error(ex.message ?: activity.getString(R.string.error_message))
        }
    }

    private fun forceToInstallChrome() {

        val appId = chromePackage
        val rateIntent = Intent(Intent.ACTION_VIEW,
                Uri.parse("market://details?id=" + appId))
        var marketFound = false


        val otherApps = activity.packageManager
                .queryIntentActivities(rateIntent, 0)
        for (otherApp in otherApps) {

            if (otherApp.activityInfo.applicationInfo.packageName == "com.android.vending") {

                val otherAppActivity = otherApp.activityInfo
                val componentName = ComponentName(
                        otherAppActivity.applicationInfo.packageName,
                        otherAppActivity.name
                )

                rateIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)// task reparenting if needed
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                rateIntent.component = componentName
                activity.startActivity(rateIntent)
                marketFound = true
                break

            }
        }

        if (!marketFound) {
            val webIntent = Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + appId))
            activity.startActivity(webIntent)
        }
    }
}