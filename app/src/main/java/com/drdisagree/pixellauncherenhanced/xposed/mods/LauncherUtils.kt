package com.drdisagree.pixellauncherenhanced.xposed.mods

import android.content.Context
import android.widget.Toast
import com.drdisagree.pixellauncherenhanced.xposed.HookEntry.Companion.enqueueProxyCommand
import com.drdisagree.pixellauncherenhanced.xposed.ModPack
import com.drdisagree.pixellauncherenhanced.xposed.mods.toolkit.XposedHook.Companion.findClass
import com.drdisagree.pixellauncherenhanced.xposed.mods.toolkit.callStaticMethod
import com.drdisagree.pixellauncherenhanced.xposed.utils.BootLoopProtector.resetCounter
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LauncherUtils(context: Context) : ModPack(context) {

    override fun updatePrefs(vararg key: String) {}

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        ThemesClass = findClass("com.android.launcher3.util.Themes")
        GraphicsUtilsClass = findClass("com.android.launcher3.icons.GraphicsUtils")
    }

    companion object {

        private var ThemesClass: Class<*>? = null
        private var GraphicsUtilsClass: Class<*>? = null
        private var lastRestartTime = 0L

        fun getAttrColor(context: Context, resID: Int): Int {
            return try {
                ThemesClass.callStaticMethod(
                    "getAttrColor",
                    context,
                    resID
                )
            } catch (_: Throwable) {
                try {
                    ThemesClass.callStaticMethod(
                        "getAttrColor",
                        resID,
                        context
                    )
                } catch (_: Throwable) {
                    try {
                        GraphicsUtilsClass.callStaticMethod(
                            "getAttrColor",
                            context,
                            resID
                        )
                    } catch (_: Throwable) {
                        GraphicsUtilsClass.callStaticMethod(
                            "getAttrColor",
                            resID,
                            context
                        )
                    }
                }
            } as Int
        }

        fun restartLauncher(context: Context) {
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastRestartTime >= 500) {
                lastRestartTime = currentTime
                resetCounter(context.packageName)

                Toast.makeText(context, "Restarting Launcher...", Toast.LENGTH_SHORT).show()

                CoroutineScope(Dispatchers.IO).launch {
                    delay(1000)
                    enqueueProxyCommand { proxy ->
                        proxy.runCommand("killall ${context.packageName}")
                    }
                }
            }
        }
    }
}