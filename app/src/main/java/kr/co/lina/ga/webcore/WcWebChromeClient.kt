package kr.co.lina.ga.webcore

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.graphics.Color
import android.net.Uri
import android.os.Message
import android.view.*
import android.webkit.*
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import kr.co.lina.ga.R
import kr.co.lina.ga.ui.main.MainActivity
import kr.co.lina.ga.utils.WaLog
import kr.co.lina.ga.utils.WaSharedPreferences
import kr.co.lina.ga.utils.WaUtils
import kr.co.lina.ga.vaccine.Vaccine


/**
 * 생성 타입 정의 생성자
 * @property tag Log 태그
 * @property Log Log
 */
class WcWebChromeClient(private val context: Context) : WebChromeClient() {
    private val tag = "WcWebChromeClient"
    private val Log = WaLog

    private var locale: String = WaSharedPreferences(context).readPrefer("language").toString()

    override fun onJsAlert(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        //return super.onJsAlert(view, url, message, result)
        Log.i(tag, "onJsAlert:")

        val title = message
        WaUtils.showAlertDialog(context, WaUtils.TYPE_ALERT_OK, title!!, "", "확인", "취소",
            {
                result?.confirm()
            },
            {
            })

        return true
    }

    override fun onJsConfirm(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        //return super.onJsConfirm(view, url, message, result)
        Log.i(tag, "onJsConfirm:")

        val title = message
        WaUtils.showAlertDialog(context, WaUtils.TYPE_ALERT_OKCANCEL, title!!, "", "확인", "취소",
            {
                result?.confirm()
            },
            {
                result?.cancel()
            })

        return true
    }

    /** 메인에 생성한 함수를 호출한다. */
    // private val mainActivity = context as? MainActivity
    @SuppressLint("SetJavaScriptEnabled")
    override fun onShowFileChooser(
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: FileChooserParams?
    ): Boolean {
        Log.i(tag, "onShowFileChooser:")
        //mainActivity?.onShowFileChooser(filePathCallback)

        (context as? MainActivity)?.onShowFileChooser(filePathCallback)
        return true
    }

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
    }

    override fun onCreateWindow(
        view: WebView?,
        isDialog: Boolean,
        isUserGesture: Boolean,
        resultMsg: Message?
    ): Boolean {
        // return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg)

        Log.i(tag, "onCreateWindow:")

        val mDlg = WcWebViewDialog(view!!.context, R.style.AppDialog)
        mDlg.show()
        val subWebView = mDlg.getSubWebView()

        resultMsg?.apply {
            (obj as WebView.WebViewTransport).webView = subWebView
        }?.sendToTarget()

        return true
    }

    override fun onCloseWindow(window: WebView?) {
        super.onCloseWindow(window)
        Log.i(tag, "onCloseWindow:")
    }

    override fun onPermissionRequest(request: PermissionRequest?) {
        super.onPermissionRequest(request)
        Log.i(tag, "onPermissionRequest:")
    }

    // GEO
    override fun onGeolocationPermissionsShowPrompt(
        origin: String?,
        callback: GeolocationPermissions.Callback?
    ) {
        Log.i(tag, "onGeolocationPermissionsShowPrompt:")
        //super.onGeolocationPermissionsShowPrompt(origin, callback)
        //mainActivity?.onGeolocationPermissionsShowPrompt(origin, callback)
        (context as? MainActivity)?.onGeolocationPermissionsShowPrompt(origin, callback)

        //callback?.invoke(origin,true, false)
    }


    // 비디오 플레이어 구현
    private var mCustomView: View? = null
    private var mCustomViewCallback: CustomViewCallback? = null
    private var mOrientation: Int? = 0
    private var mFsContainer: FrameLayout? = null
    private val COVER_SCREEN_PARAMS = FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )

    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
        Log.i(tag, "onShowCustomView:")
        if (mCustomView != null) {
            callback?.onCustomViewHidden()
            return
        }
        mOrientation = (context as Activity).requestedOrientation
        val deco = context.window.decorView as FrameLayout
        mFsContainer = FsHolder(context)

        mFsContainer?.addView(view, COVER_SCREEN_PARAMS)
        deco.addView(mFsContainer, COVER_SCREEN_PARAMS)
        mCustomView = view
        setFs(true)
        mCustomViewCallback = callback
        super.onShowCustomView(view, callback)
    }

    override fun onHideCustomView() {
        super.onHideCustomView()
        Log.i(tag, "onHideCustomView:")
        mCustomView?.let {
            setFs(false)
            val deco = (context as Activity).window.decorView as FrameLayout
            deco.removeView(mFsContainer)
            mFsContainer = null
            mCustomView = null
            mCustomViewCallback?.onCustomViewHidden()
            context.requestedOrientation = mOrientation!!
        }
    }

    private fun setFs(enabled: Boolean) {
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

    private class FsHolder(ctx: Context?) : FrameLayout(ctx!!) {
        init {
            setBackgroundColor(ContextCompat.getColor(ctx!!, android.R.color.black))
        }

        override fun onTouchEvent(evt: MotionEvent): Boolean {
            return true
        }
    }

}
