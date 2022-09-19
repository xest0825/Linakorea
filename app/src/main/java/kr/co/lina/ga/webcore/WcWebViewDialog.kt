package kr.co.lina.ga.webcore

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Message
import android.view.*
import android.webkit.*
import android.webkit.WebSettings.PluginState
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import kr.co.lina.ga.R
import kr.co.lina.ga.WaConfig
import kr.co.lina.ga.ui.main.MainActivity
import kr.co.lina.ga.utils.WaLog
import kr.co.lina.ga.utils.WaSharedPreferences
import kr.co.lkins.EHB.permission.PermissionFactory
import kr.co.lkins.EHB.webcore.WcWebBridge
import java.io.File
import java.net.URLDecoder

/**
 * 웹뷰 다이얼로그
 * @property tag Log 태그
 * @property Log Log
 * @property locale 언어 정보
 * @property mSubWebView 웹뷰
 * @property mParentContext Context
 * @property mCustomView 뷰
 * @property mCustomViewCallback 뷰 콜백
 * @property mOrientation Int
 * @property mFsContainer FrameLayout
 * @property COVER_SCREEN_PARAMS FrameLayout Params
 */
class WcWebViewDialog(context: Context, attributeSet: Int) : Dialog(context, attributeSet) {
    private val tag = "WcWebViewDialog"
    private val Log = WaLog
    private lateinit var locale: String
    private lateinit var mSubWebView: WebView
    private var mParentContext: Context = context
    private var mCustomView: View? = null
    private var mCustomViewCallback: WebChromeClient.CustomViewCallback? = null
    private var mOrientation: Int? = 0
    private var mFsContainer: FrameLayout? = null
    private val COVER_SCREEN_PARAMS = FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )

    companion object {
    }

    /**
     * 웹뷰 생성
     * @return WebView
     */
    fun getSubWebView(): WebView {
        return mSubWebView
    }

    override fun onCreate(saveInstanceState: Bundle?) {
        super.onCreate(saveInstanceState)
        Log.i(tag, "onCreate:")

        setContentView(R.layout.dialog_webview)
        mSubWebView = findViewById<WebView>(R.id.SubWebView)

        locale = WaSharedPreferences(context).readPrefer("language").toString()

        // 웹뷰 설정
        initializeWebView(mSubWebView)
        webViewDownload()

        val closeBtn = findViewById<Button>(R.id.SubWebViewCloseBtn);
        closeBtn.setOnClickListener {
            dismiss()
        }
    }

    /**
     * 키 입력 이벤트
     * @param keyCode 키코드
     * @param event 키이벤트
     * @return Boolean
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        Log.i(tag, "onKeyDown: keyCode:$keyCode event:$event")
        if (event.keyCode == KeyEvent.KEYCODE_BACK) {
            if (mSubWebView.canGoBack()) {
                mSubWebView.goBack()
                return true
            } else {
                mSubWebView.removeAllViews()
                mSubWebView.destroy()
                dismiss()
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    /** 웹뷰 해제 */
    override fun dismiss() {
        super.dismiss()
        Log.i(tag, "dismiss:")
        mSubWebView.removeAllViews()
        mSubWebView.destroy()
    }

    val INTENT_PROTOCOL_START = "intent:"
    val INTENT_PROTOCOL_INTENT = "#Intent;"
    val INTENT_PROTOCOL_END = ";end;"
    val GOOGLE_PLAY_STORE_PREFIX = "market://details?id="

    /**
     * 웹뷰 설정 셋팅
     * @param webview
     */
    @SuppressLint("SetJavaScriptEnabled")
    private fun initializeWebView(webview: WebView) {
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
        // ZOOM 설정
        wvs.setSupportZoom(true)
        wvs.builtInZoomControls = true
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
        initializeWebViewClient()
        initializeChromeClient(mParentContext)
        webview.addJavascriptInterface(WcWebBridge(context), WaConfig.WEB_BRIDGE_NAME)

        if (WaConfig.WEB_DEBUG)
            WebView.setWebContentsDebuggingEnabled(true)
    }

    /** 웹뷰 클라이언트 초기화 */
    private fun initializeWebViewClient() {
        mSubWebView.webViewClient = object : WebViewClient() {
            /** API 24 이하 */
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                url: String?
            ): Boolean {
                Log.i(tag, "shouldOverrideUrlLoading url (API 24) :${url}")
                val url = url

                if (url?.contains("docs.google.com/gview") == true) {
                    return false
                }
                else if (url?.contains(".pdf") == true) {
                    //WcWeb.loadUrlPage(context, view!!, "http://drive.google.com/viewerng/viewer?embedded=true&url=" + url);
                    view!!.loadUrl("http://docs.google.com/gview?embedded=true&url=" + url);
                    return false
                }
                else if (url?.startsWith("https")!! || url.startsWith("http")) {
                    // SNS 수정함  load 시 opener 가 없어지는 현상 수정함.
                    //WcWeb.loadUrlPage(context, view!!, url)
                    //return true
                    return false
                } else if (url.startsWith("intent://")) {
                    try {
                        val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                        val existPackage =
                            context.packageManager.getLaunchIntentForPackage(intent.getPackage()!!)
                        if (existPackage != null) {
                            context.startActivity(intent)
                        } else {
                            val marketIntent = Intent(Intent.ACTION_VIEW)
                            marketIntent.data =
                                Uri.parse("market://details?id=" + intent.getPackage()!!)
                            context.startActivity(marketIntent)
                        }
                        return true
                    } catch (e: Exception) {
                        Log.e(tag, "Exception ${e.message}")
                    }
                    // 실패
                    return false
                } else if (url.startsWith(INTENT_PROTOCOL_START)) {
                    val customUrlStartIndex = INTENT_PROTOCOL_START.length
                    val customUrlEndIndex = url.indexOf(INTENT_PROTOCOL_INTENT)
                    if (customUrlEndIndex < 0) {
                        return false;
                    } else {
                        val customUrl = url.substring(customUrlStartIndex, customUrlEndIndex)
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(customUrl)))
                        } catch (e: Exception) {
                            val packageStartIndex = customUrlEndIndex + INTENT_PROTOCOL_INTENT.length
                            val packageEndIndex = url.indexOf(INTENT_PROTOCOL_END)

                            var lenUrl = 0
                            if (packageEndIndex < 0 ) {
                                lenUrl = url.length
                            }
                            else {
                                lenUrl = packageEndIndex
                            }
                            val packageName = url.substring(packageStartIndex, lenUrl)
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GOOGLE_PLAY_STORE_PREFIX + packageName)));
                        }
                        return true
                    }
                } else if (url.startsWith("market:")) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.addCategory(Intent.CATEGORY_DEFAULT)
                    intent.data = Uri.parse(url)
                    context.startActivity(intent)
                } else if (url.startsWith("tel:")) {
                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(url)))
                    return true
                } else if (url.startsWith("mailto:") || url.startsWith("sms:")) {
                    context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse(url)))
                    return true
                } else {
                    // super
                    return super.shouldOverrideUrlLoading(view, url)
                }
                return false
            }

            /** API 24 이상 */
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                Log.i(tag, "shouldOverrideUrlLoading url:${request!!.url.toString()}")
                val url = request.url.toString()

                if (url.contains("docs.google.com/gview")) {
                    return false
                }
                else if (url.contains(".pdf")) {
                    //WcWeb.loadUrlPage(context, view!!, "http://drive.google.com/viewerng/viewer?embedded=true&url=" + url);
                    if(url.contains("download?filePathName")){
                        return false
                    } else {
                        view!!.loadUrl("http://docs.google.com/gview?embedded=true&url=" + url);
                    }
                    return false
                }
                else if (url.startsWith("https") || url.startsWith("http")) {
                    // SNS 수정함  load 시 opener 가 없어지는 현상 수정함.
                    //WcWeb.loadUrlPage(context, view!!, url)
                    //return true
                    return false
                }
                else if (url.startsWith("intent://")) {
                    try {
                        val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                        val existPackage =
                            context.packageManager.getLaunchIntentForPackage(intent.getPackage()!!)
                        if (existPackage != null) {
                            context.startActivity(intent)
                        } else {
                            val marketIntent = Intent(Intent.ACTION_VIEW)
                            marketIntent.data =
                                Uri.parse("market://details?id=" + intent.getPackage()!!)
                            context.startActivity(marketIntent)
                        }
                        return true
                    } catch (e: Exception) {
                        Log.e(tag, "Exception ${e.message}")
                    }
                    // 실패
                    return false
                } else if (url.startsWith(INTENT_PROTOCOL_START)) {
                    val customUrlStartIndex = INTENT_PROTOCOL_START.length
                    val customUrlEndIndex = url.indexOf(INTENT_PROTOCOL_INTENT)
                    if (customUrlEndIndex < 0) {
                        return false;
                    } else {
                        val customUrl = url.substring(customUrlStartIndex, customUrlEndIndex)
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(customUrl)))
                        } catch (e: Exception) {
                            val packageStartIndex = customUrlEndIndex + INTENT_PROTOCOL_INTENT.length
                            val packageEndIndex = url.indexOf(INTENT_PROTOCOL_END)

                            var lenUrl = 0
                            if (packageEndIndex < 0 ) {
                                lenUrl = url.length
                            }
                            else {
                                lenUrl = packageEndIndex
                            }
                            val packageName = url.substring(packageStartIndex, lenUrl)
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GOOGLE_PLAY_STORE_PREFIX + packageName)));
                        }
                        return true
                    }
                } else if (url.startsWith("market:")) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.addCategory(Intent.CATEGORY_DEFAULT)
                    intent.data = Uri.parse(url)
                    context.startActivity(intent)
                } else if (url.startsWith("tel:")) {
                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(url)))
                    return true
                } else if (url.startsWith("mailto:") || url.startsWith("sms:")) {
                    context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse(url)))
                    return true
                } else {
                    // super
                    return super.shouldOverrideUrlLoading(view, request)
                }
                return false
            }

            override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                super.doUpdateVisitedHistory(view, url, isReload)
                Log.i(tag, "doUpdateVisitedHistory url:$url")
                WcWeb.referrer = url.toString()
            }

        }
    }
    /**
     * 크롬 클라이언트 초기화
     * @param context
     */
    private fun initializeChromeClient(context: Context) {
        Log.i(tag, "initializeChromeClient:")

        mSubWebView.webChromeClient = object : WebChromeClient() {

            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                //return super.onConsoleMessage(consoleMessage)
                Log.i(tag,"onConsoleMessage: consoleMessage:${consoleMessage!!.message()} [${consoleMessage.lineNumber()}]"
                )
                return true
            }

            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message?
            ): Boolean {
                Log.i(tag, "onCreateWindow: Not Supported")
                return false
            }

            override fun onCloseWindow(window: WebView?) {
                super.onCloseWindow(window)
                Log.i(tag, "onCloseWindow:")
                if (window != null) {
                    window.removeAllViews()
                    window.destroy()
                }
                dismiss()
            }

            override fun onJsAlert(
                view: WebView?,
                url: String?,
                message: String?,
                result: JsResult?
            ): Boolean {
                Log.i(tag, "onJsAlert:")
                //return super.onJsAlert(view, url, message, result)
                val alertDialog = AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.common_title))
                    .setMessage(message)
                    .setPositiveButton(context.getString(R.string.common_confirm)) { _, _ ->
                        result?.confirm()
                    }
                    .create()
                alertDialog.show()
                return true
            }

            override fun onJsConfirm(
                view: WebView?,
                url: String?,
                message: String?,
                result: JsResult?
            ): Boolean {
                Log.i(tag, "onJsConfirm:")
                val alertDialog = AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.common_title))
                    .setMessage(message)
                    .setPositiveButton(context.getString(R.string.common_confirm)) { _, _ ->
                        result?.confirm()
                    }
                    .setNegativeButton(context.getString(R.string.common_cancel)) { _, _ ->
                        result?.cancel()
                    }
                    .create()
                alertDialog.show()
                return true
            }

            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                Log.i(tag, "onShowCustomView:")
                if (mCustomView != null) {
                    callback?.onCustomViewHidden()
                    hide()
                    return
                }
                mOrientation = (context as Activity).requestedOrientation
                val deco = context.window.decorView as FrameLayout
                mFsContainer = FsHolder(context)

                mFsContainer?.addView(view, COVER_SCREEN_PARAMS)
                deco.addView(mFsContainer, COVER_SCREEN_PARAMS)
                mCustomView = view
                setFs(context, true)
                mCustomViewCallback = callback
                // 다이얼로그 처리
                hide()
                super.onShowCustomView(view, callback)
            }

            override fun onHideCustomView() {
                super.onHideCustomView()
                Log.i(tag, "onHideCustomView:")
                mCustomView?.let {
                    setFs(context, false)
                    val deco = (context as Activity).window.decorView as FrameLayout
                    deco.removeView(mFsContainer)
                    mFsContainer = null
                    mCustomView = null
                    mCustomViewCallback?.onCustomViewHidden()
                    context.requestedOrientation = mOrientation!!
                    // 다이얼로그 처리
                    show()
                }
            }
        }
    }

    /**
     * 커스텀웹뷰 설정
     * @param context
     * @param enabled
     */
    private fun setFs(context: Context, enabled: Boolean) {
        Log.i(tag, "setFs: enable:$enabled")
        val win = (context as Activity).window
        val winParams: WindowManager.LayoutParams = win.attributes
        val bits = WindowManager.LayoutParams.FLAG_FULLSCREEN
        if (enabled) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
            if (mCustomView != null) {
                mCustomView!!.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            }
        }
        win.attributes = winParams
    }

    /**
     * 커스텀웹뷰 셋팅
     * @param ctx Context
     */
    private class FsHolder(ctx: Context?) : FrameLayout(ctx!!) {
        init {
            setBackgroundColor(ContextCompat.getColor(ctx!!, android.R.color.black))
        }

        override fun onTouchEvent(evt: MotionEvent): Boolean {
            return true
        }
    }

    fun isAppInstalled(activity: Activity, packageName: String?): Boolean {
        val pm = activity.packageManager
        try {
            pm.getPackageInfo(packageName!!, PackageManager.GET_ACTIVITIES)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
        }
        return false
    }

    private fun webViewDownload() {
        // 다운로드 기능
        // Java : JavaScriptInterface.java
        mSubWebView.getSettings().setDefaultTextEncodingName("utf-8");
        mSubWebView.getSettings().setJavaScriptEnabled(true)
        mSubWebView.getSettings()
            .setAppCachePath(mParentContext.getCacheDir().getAbsolutePath())
        mSubWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT)
        mSubWebView.getSettings().setDatabaseEnabled(true)
        mSubWebView.getSettings().setDomStorageEnabled(true)
        mSubWebView.getSettings().setUseWideViewPort(true)
        mSubWebView.getSettings().setLoadWithOverviewMode(true)
        mSubWebView.addJavascriptInterface(JavaScriptInterface(context), "Android")
        mSubWebView.getSettings().setPluginState(PluginState.ON)

        mSubWebView.setDownloadListener(DownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
            /*mSubWebView.loadUrl(
                JavaScriptInterface.getBase64StringFromBlobUrl(url)
            )*/
            downloadProcess(url, userAgent, contentDisposition, mimeType)
        })
    }

    /** 다운로드 기능 적용 진행 */
    private fun downloadProcess(
        url: String,
        userAgent: String,
        contentDisposition: String,
        mimeType: String
    ) {
        // 파싱 후 URL 획득 필요함. 확인 필요함.
        dnUrl = url
        dnContentDisposition = contentDisposition
        dnMimeType = mimeType
        dnUserAgent = userAgent

        if (PermissionFactory().checkPermissionStorage(mParentContext as MainActivity)) {
            downloadMangerProcess(true)
        }
    }

    private var dnUrl: String? = null
    private var dnContentDisposition: String? = null
    private var dnMimeType: String? = null
    private var dnUserAgent: String? = null

    private var DOWNLOAD_FILENAME_PREFIX = "attachmentfilename="

    /** 다운로드 매니저 */
    fun downloadMangerProcess(bPermission: Boolean) {
        Log.i(tag, "downloadProcess:")
        if (bPermission) {
            // attachment;fileName="개인정보동의서.pdf";,
            val content = dnContentDisposition!!.replace("""[\";,"]""".toRegex(), "")
            Log.i(tag, "DownloadListener content: $content")

            val filename = URLDecoder.decode(content.substring(DOWNLOAD_FILENAME_PREFIX.length, content.length))
            Log.i(tag, "DownloadListener fileName:$filename")

            val file =
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + "/" + filename)
            Log.i(tag, "DownloadListener file:${file.absolutePath}")
            val cookies = CookieManager.getInstance().getCookie(dnUrl)
            val request: DownloadManager.Request = DownloadManager.Request(Uri.parse(dnUrl))
//            request.setDescription(Strings.getString(this, locale).get("file_download_start"))
            request.setDescription(mParentContext.getString(R.string.file_download_start))
            request.setTitle(filename)
            request.addRequestHeader("Cookie", cookies)
            request.addRequestHeader("User-Agent", dnUserAgent)
            request.allowScanningByMediaScanner()
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
            request.setDestinationUri(Uri.fromFile(file))
            request.setAllowedOverMetered(true)
            request.setAllowedOverRoaming(false)
            request.setVisibleInDownloadsUi(true)

            val downloadManger = mParentContext.getSystemService(AppCompatActivity.DOWNLOAD_SERVICE) as DownloadManager?
            val dnID = downloadManger!!.enqueue(request)
            Toast.makeText(
                mParentContext,
                "$filename " + mParentContext.getString(R.string.file_downloading),
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(mParentContext, R.string.file_permission_desc, Toast.LENGTH_LONG).show()
        }
    }
}