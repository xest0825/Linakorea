package kr.co.lina.ga.webcore

import android.content.Context
import android.webkit.WebSettings
import android.webkit.WebView
import kr.co.lina.ga.WaConfig
import kr.co.lina.ga.utils.WaLog
import kr.co.lkins.EHB.webcore.WcWebBridge

/**
 * 웹뷰 설정 셋팅
 * @property tag Log 태그
 * @property Log Log
 */
class WcWebManager (context: Context, private val webview: WebView) {
    private val tag = "WcWebManger"
    private val Log = WaLog
    //val cacheDir = ""
    //val BRIDGE_NAME = WaConfig.WEB_BRIDGE_NAME

    init {
        initWebView(context, webview)
    }

    private fun initWebView(context: Context, webview: WebView) {
        Log.i(tag, "initWebView:")
        val wvs = webview.settings
        wvs.javaScriptEnabled = true
        wvs.domStorageEnabled = true
        wvs.setSupportMultipleWindows(true)
        wvs.javaScriptCanOpenWindowsAutomatically = true
        wvs.setAppCacheEnabled(true)
        wvs.cacheMode = WebSettings.LOAD_DEFAULT
        wvs.setAppCachePath(context.applicationContext.cacheDir.absolutePath)
        wvs.loadWithOverviewMode = true
        wvs.useWideViewPort = true
        wvs.allowFileAccess = true
        // 설정
        wvs.allowFileAccessFromFileURLs = true
        wvs.allowUniversalAccessFromFileURLs = true
        wvs.allowContentAccess = true
        // 웹뷰에서 폰트 크기가 크게나오는 현상. (CSS 에서 - webkit-text-size-adjust: none 적용 안됨)
        wvs.textZoom = 100
        // CONTENT
        wvs.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        // UserAgent 설정
        wvs.userAgentString += WaConfig.WEB_USER_AGENT
        Log.i(tag, "userAgentString:" + wvs.userAgentString)
        // 클라이언트 설정
        webview.webChromeClient = WcWebChromeClient(context)
        webview.webViewClient = WcWebViewClient(context)
        webview.addJavascriptInterface(WcWebBridge(context), WaConfig.WEB_BRIDGE_NAME)

        if (WaConfig.WEB_DEBUG)
            WebView.setWebContentsDebuggingEnabled(true)
    }
}