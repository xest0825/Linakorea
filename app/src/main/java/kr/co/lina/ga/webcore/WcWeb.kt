package kr.co.lina.ga.webcore

import android.content.Context
import android.webkit.WebView
import androidx.collection.ArrayMap
import kr.co.lina.ga.utils.WaLog

/**
 * WebView Referer 관리
 * @property tag Log 태그
 * @property Log Log
 * @property requestHeaders 웹뷰 호출 시 POST Header 설정
 * @property referrer Referer 정의 변수
 */
object WcWeb {

    private val tag = "WcWeb"
    private val Log = WaLog

    private val requestHeaders = ArrayMap<String, String>()

    var referrer = String()

    /**
     * Url Load
     * @param context
     * @param webview
     * @param url
     */
    fun loadUrlPage (context: Context, webview: WebView, url: String) {
        Log.i(tag, "loadUrlPage:")
        requestHeaders["Referer"] = referrer
        webview.loadUrl(url, WcWeb.requestHeaders)
    }
}