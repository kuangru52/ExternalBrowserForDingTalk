package com.kuangru52.externalbrowser

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.util.Log
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HookEntry : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != "com.alibaba.android.rimet") return

        Log.e("ExternalBrowser", "🔥 [Xposed] DingTalk package loaded: ${lpparam.packageName}, process: ${lpparam.processName}")

        val classLoader = lpparam.classLoader

        // 1. Hook 标准 Android WebView.loadUrl
        try {
            val webViewClass = XposedHelpers.findClass("android.webkit.WebView", classLoader)
            XposedHelpers.findAndHookMethod(webViewClass, "loadUrl", String::class.java, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val url = param.args[0] as? String
                    if (handleUrl(param.thisObject, url)) {
                        param.result = null
                    }
                }
            })
            XposedHelpers.findAndHookMethod(webViewClass, "loadUrl", String::class.java, Map::class.java, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val url = param.args[0] as? String
                    if (handleUrl(param.thisObject, url)) {
                        param.result = null
                    }
                }
            })
            Log.e("ExternalBrowser", "🔥 [Xposed] Successfully hooked android.webkit.WebView.loadUrl")
        } catch (e: Throwable) {
            Log.d("ExternalBrowser", "Android WebView hook skipped", e)
        }

        // 2. Hook UC / U4 WebView.loadUrl (钉钉实际使用的内核)
        try {
            val ucWebViewClass = XposedHelpers.findClass("com.uc.webview.export.WebView", classLoader)
            XposedHelpers.findAndHookMethod(ucWebViewClass, "loadUrl", String::class.java, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val url = param.args[0] as? String
                    if (handleUrl(param.thisObject, url)) {
                        param.result = null
                    }
                }
            })
            XposedHelpers.findAndHookMethod(ucWebViewClass, "loadUrl", String::class.java, Map::class.java, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val url = param.args[0] as? String
                    if (handleUrl(param.thisObject, url)) {
                        param.result = null
                    }
                }
            })
            Log.e("ExternalBrowser", "🔥 [Xposed] Successfully hooked com.uc.webview.export.WebView.loadUrl")
        } catch (e: Throwable) {
            Log.d("ExternalBrowser", "UC WebView hook skipped", e)
        }

        // 3. Hook 腾讯 X5 WebView.loadUrl
        try {
            val x5WebViewClass = XposedHelpers.findClass("com.tencent.smtt.sdk.WebView", classLoader)
            XposedHelpers.findAndHookMethod(x5WebViewClass, "loadUrl", String::class.java, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val url = param.args[0] as? String
                    if (handleUrl(param.thisObject, url)) {
                        param.result = null
                    }
                }
            })
            XposedHelpers.findAndHookMethod(x5WebViewClass, "loadUrl", String::class.java, Map::class.java, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val url = param.args[0] as? String
                    if (handleUrl(param.thisObject, url)) {
                        param.result = null
                    }
                }
            })
            Log.e("ExternalBrowser", "🔥 [Xposed] Successfully hooked com.tencent.smtt.sdk.WebView.loadUrl")
        } catch (e: Throwable) {
            Log.d("ExternalBrowser", "X5 WebView hook skipped", e)
        }
    }

    private fun handleUrl(webViewObj: Any?, url: String?): Boolean {
        if (url.isNullOrEmpty()) return false
        if (url.startsWith("http://") || url.startsWith("https://")) {
            // 排除钉钉内部域名
            if (!url.contains("dingtalk.com") && !url.contains("alibaba.com") && !url.contains("aliyuncs.com")) {
                Log.w("ExternalBrowser", ">>> [Intercepted URL]: $url")
                try {
                    val context = getContextFromObject(webViewObj) ?: return false
                    val externalIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(externalIntent)
                    Log.i("ExternalBrowser", ">>> [SUCCESS] Opened external browser for: $url")

                    // 关闭承载 WebView 的 Activity
                    var currContext: Context? = context
                    where@ while (currContext is ContextWrapper) {
                        if (currContext is Activity) {
                            currContext.finish()
                            break@where
                        }
                        currContext = currContext.baseContext
                    }
                    return true
                } catch (e: Throwable) {
                    Log.e("ExternalBrowser", "Error opening external browser", e)
                }
            }
        }
        return false
    }

    private fun getContextFromObject(obj: Any?): Context? {
        if (obj == null) return null
        if (obj is Context) return obj
        try {
            val getContextMethod = obj.javaClass.getMethod("getContext")
            return getContextMethod.invoke(obj) as? Context
        } catch (_: Throwable) {}
        try {
            val getContextMethod = obj.javaClass.getMethod("getConfigContext")
            return getContextMethod.invoke(obj) as? Context
        } catch (_: Throwable) {}
        return null
    }
}
