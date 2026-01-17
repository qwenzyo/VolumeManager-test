package moe.chensi.volume.system

import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import moe.chensi.volume.EnableBinderProxy
import moe.chensi.volume.ToggleableBinderProxy
import org.joor.Reflect
import java.util.WeakHashMap

class PackageManagerProxy private constructor(context: Context) {
    companion object {
        private val cache = WeakHashMap<Context, PackageManagerProxy>()

        fun get(context: Context): PackageManagerProxy {
            return cache.getOrPut(context) { PackageManagerProxy(context) }
        }
    }

    private val userManager = UserManagerProxy(context)

    private val packageManager = context.packageManager
    private val reflect = Reflect.on(packageManager)

    init {
        val service =
            Reflect.onClass("android.app.ActivityThread").call("getPackageManager").get<Any>()
        ToggleableBinderProxy.wrap(service)
    }

    val defaultActivityIcon by lazy { packageManager.defaultActivityIcon }

    val defaultActivityIconImageBitmap by lazy {
        defaultActivityIcon.toBitmap(128, 128).asImageBitmap()
    }

    @EnableBinderProxy
    fun getInstalledApplicationsForAllUsers(): List<ApplicationInfo> {
        val apps = mutableMapOf<String, ApplicationInfo>()

        for (userId in userManager.getUserIds()) {
            for (app in reflect.call("getInstalledApplicationsAsUser", 0, userId)
                .get<List<ApplicationInfo>>()) {
                apps[app.packageName] = app
            }
        }

        return apps.values.toList()
    }

    @EnableBinderProxy
    fun getDrawable(packageName: String, resId: Int, appInfo: ApplicationInfo): Drawable? {
        return packageManager.getDrawable(packageName, resId, appInfo)
    }

    @EnableBinderProxy
    fun loadLabel(appInfo: ApplicationInfo): String {
        return appInfo.loadLabel(packageManager).toString()
    }
}