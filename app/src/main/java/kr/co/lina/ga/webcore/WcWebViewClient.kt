package kr.co.lina.ga.webcore

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import android.webkit.*
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kr.co.lina.ga.ui.main.MainActivity
import kr.co.lina.ga.utils.WaLog


/**
 * 웹뷰 클라이언트
 * @param context
 * @property tag Log 태그
 * @property Log Log
 */
class WcWebViewClient constructor(private val context: Context) : WebViewClient() {
    private val tag = "WcWebViewClient"
    private val Log = WaLog

    /**
     * 웹뷰 페이지 로딩 시작
     * @param view
     * @param url
     * @param favicon
     */
    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        Log.i(tag, "onPageStarted: url:$url")
    }

    /**
     * 웹뷰 페이지 로딩 완료
     * @param view
     * @param url
     */
    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        Log.i(tag, "onPageFinished: url:$url")
        (context as? MainActivity)?.onPagefinished()
    }

    /** API 24 이하 */
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        Log.i(tag, "shouldOverrideUrlLoading: url (API 24) : ${url}")

        if (url?.startsWith("https")!! || url.startsWith("http")) {
            WcWeb.loadUrlPage(context, view!!, url)
            return true
        }
        else if (url.startsWith("intent:kakaolink:")) {
            // 참고 문서 : https://devtalk.kakao.com/t/topic/70935
            val url2 = url.substring(7)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url2))
            try {
                context.startActivity(intent)
                return true
            } catch (e: Exception) {
                Log.e(tag, "Exception: ${e.message}")

                try {
                    val intent2 = Intent.parseUri(url2, Intent.URI_INTENT_SCHEME)
                    val existPackage =
                        this.context.packageManager.getLaunchIntentForPackage(intent2.getPackage()!!)
                    if (existPackage != null) {
                        this.context.startActivity(intent2)
                    } else {
                        val marketIntent = Intent(Intent.ACTION_VIEW)
                        marketIntent.data = Uri.parse("market://details?id=" + intent.getPackage()!!)
                        this.context.startActivity(marketIntent)
                    }
                    return true
                } catch (e: Exception) {
                    Log.e(tag, "Exception ${e.message}")
                }

            }
            return false
        }
        else if (url.startsWith("intent://")) {
            try {
                val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                val existPackage =
                    this.context.packageManager.getLaunchIntentForPackage(intent.getPackage()!!)
                if (existPackage != null) {
                    this.context.startActivity(intent)
                } else {
                    val marketIntent = Intent(Intent.ACTION_VIEW)
                    marketIntent.data = Uri.parse("market://details?id=" + intent.getPackage()!!)
                    this.context.startActivity(marketIntent)
                }
                return true
            } catch (e: Exception) {
                Log.e(tag, "Exception ${e.message}")
            }
            // 실패
            return false
        } else if (url.startsWith("market:")) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.data = Uri.parse(url)
            this.context.startActivity(intent)
        } else if (url.startsWith("tel:")) {
            this.context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(url)))
            return true
        } else if (url.startsWith("mailto:") || url.startsWith("sms:")) {
            this.context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse(url)))
            return true
        } else {
            // super
            return super.shouldOverrideUrlLoading(view, url)
        }
        return false
    }

    /**
     * 웹뷰 Url 이동 (API 24 이상)
     * @param view
     * @param request
     * @return Boolean
     */
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        //return super.shouldOverrideUrlLoading(view, request)
        Log.i(tag, "shouldOverrideUrlLoading: url:${request?.url.toString()}")
        val url = request?.url.toString()

        if (url.startsWith("https") || url.startsWith("http")) {
            WcWeb.loadUrlPage(context, view!!, url)
            return true
        }
        else if (url.startsWith("intent:kakaolink:")) {
            // 참고 문서 : https://devtalk.kakao.com/t/topic/70935
            val url2 = url.substring(7)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url2))
            try {
                context.startActivity(intent)
                return true
            } catch (e: Exception) {
                Log.e(tag, "Exception: ${e.message}")


                try {
                    val intent2 = Intent.parseUri(url2, Intent.URI_INTENT_SCHEME)
                    val existPackage =
                        this.context.packageManager.getLaunchIntentForPackage(intent2.getPackage()!!)
                    if (existPackage != null) {
                        this.context.startActivity(intent2)
                    } else {
                        val marketIntent = Intent(Intent.ACTION_VIEW)
                        marketIntent.data = Uri.parse("market://details?id=" + intent.getPackage()!!)
                        this.context.startActivity(marketIntent)
                    }
                    return true
                } catch (e: Exception) {
                    Log.e(tag, "Exception ${e.message}")
                }

            }
            return false
        }
        else if (url.startsWith("intent://")) {
            try {
                val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                val existPackage =
                    this.context.packageManager.getLaunchIntentForPackage(intent.getPackage()!!)
                if (existPackage != null) {
                    this.context.startActivity(intent)
                } else {
                    val marketIntent = Intent(Intent.ACTION_VIEW)
                    marketIntent.data = Uri.parse("market://details?id=" + intent.getPackage()!!)
                    this.context.startActivity(marketIntent)
                }
                return true
            } catch (e: Exception) {
                Log.e(tag, "Exception ${e.message}")
            }
            // 실패
            return false
        } else if (url.startsWith("market:")) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.data = Uri.parse(url)
            this.context.startActivity(intent)
        } else if (url.startsWith("tel:")) {
            this.context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(url)))
            return true
        } else if (url.startsWith("mailto:") || url.startsWith("sms:")) {
            this.context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse(url)))
            return true
        } else {
            // super
            return super.shouldOverrideUrlLoading(view, request)
        }
        return false
    }

    /**
     * 웹뷰 히스토리 업데이트
     * @param view
     * @param url
     * @return isReload
     */
    override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
        super.doUpdateVisitedHistory(view, url, isReload)
        Log.i(tag, "doUpdateVisitedHistory: url:$url")
        WcWeb.referrer = url.toString()
    }

    override fun onLoadResource(view: WebView?, url: String?) {
        super.onLoadResource(view, url)
        // Log.i(tag, "onLoadResource url : $url")
    }

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        // Log.i(tag, "shouldInterceptRequest() url : ${request!!.url.toString()}")
        return super.shouldInterceptRequest(view, request)
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError
    ) {
        //Your code to do
        //(context as MainActivity).view_reload.visibility = View.VISIBLE
    }
}