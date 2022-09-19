package kr.co.lina.ga.ui.main

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.View
import android.view.WindowManager
import android.webkit.*
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.doOnNextLayout
import com.akexorcist.localizationactivity.core.OnLocaleChangedListener
import com.akexorcist.localizationactivity.ui.LocalizationActivity
import com.google.gson.JsonObject
import com.secureland.smartmedic.core.Constants
import com.yettiesoft.oscar.service.Oscar
import com.yettiesoft.vestpin.service.VestPinLibImpl
import kr.co.lina.ga.BuildConfig
import kr.co.lina.ga.Model.MultiUploadModel
import kr.co.lina.ga.R
import kr.co.lina.ga.ServerUrls
import kr.co.lina.ga.WaConfig
import kr.co.lina.ga.scanner.ImageCropActivity
import kr.co.lina.ga.ui.setting.SettingActivity
import kr.co.lina.ga.ui.splash.newUrl
import kr.co.lina.ga.utils.WaLog
import kr.co.lina.ga.utils.WaSharedPreferences
import kr.co.lina.ga.utils.WaUtils
import kr.co.lina.ga.vaccine.Vaccine
import kr.co.lina.ga.webcore.*
import kr.co.lkins.EHB.permission.PermissionFactory
import kr.co.lkins.EHB.permission.PermissionFactory.Companion.REQUEST_PERMISSION_CAMERA
import kr.co.lkins.EHB.permission.PermissionFactory.Companion.REQUEST_PERMISSION_GALLERY
import kr.co.lkins.EHB.webcore.WcWebBridge
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.*

/**
 * 메인화면
 * @property tag Log 태그
 * @property Log Log
 * @property mWebView 웹뷰
 * @property locale 언어 정보
 * @property REQUEST_SINGLE 카메라 싱글 업로드 결과 코드
 * layout : activity_main.xml
 */

class MainActivity : LocalizationActivity(), OnLocaleChangedListener {
    private val tag = "MainActivity"
    private val Log = WaLog
    lateinit var mWebView: WcNsWebView
    private lateinit var locale: String

    enum class IMAGE_TYPE { FILE_CHOOSER, GALLERY_UPLOAD, CAMERA_UPLOAD }

    private var mGalleryMode = IMAGE_TYPE.FILE_CHOOSER

    companion object {
        // mVaccine 제품 RequestCode
        const val VACCINE_REQ = 777
        // UI 간소화 모드 검사진행 Notification
        const val MESSAGE_ID = 12345
        // UI 간소화 모드 검사결과 Notification
        const val MESSAGE_ID1 = 123456
        var mInstance: MainActivity? = null

        fun mainActivity(): Context {
            return mInstance!!
        }
    }

    // set MainActivity context
    init {
        mInstance = this
    }

    private val REQUEST_SINGLE = 5001

    // new scan
    private val REQ_PHOTO  = 5002
    private val REQ_CAMERA = 5003

    var singleUploadData: MultiUploadModel.MultiUpload? = null
    var mUploadData: MultiUploadModel.MultiUpload? = null

    lateinit var photoPath: String

    // secure Text
    val REQ_SECURE_TEXT = 5005
    var secureText = ""
    private val TAG = "Main"

    var isForeground = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val PACKAGE_NAME = getPackageName();

        setContentView(R.layout.activity_main)
        Log.i(tag, "onCreate: ")
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        locale = WaSharedPreferences(this).readPrefer("language").toString()

        // 웹뷰 쿠키 설정
        var lang = WaSharedPreferences(this).readPrefer("language").toString()

        CookieManager.getInstance().flush()

        val main_webview = findViewById<WcNsWebView>(R.id.main_webview)
        mWebView = main_webview as WcNsWebView
        WcWebManager(this, mWebView!!)
        webViewDownload()

        // 백신
        //Vaccine.initVaccine()

        // 앱 위변조
        //AppAuth.authApp()

        // Fido
        initVestPin()

        // Push
        //Push.initFirebase()
        TmsPush().initTms(this)

        checkKeyboardShow()

        //---------------------------------------------------------------
        // load main page
        //---------------------------------------------------------------
        if (newUrl == "") {
            newUrl = ServerUrls.MAIN_URL
        }
        WaConfig.URL_MAIN = ServerUrls.MAIN_URL
        WaConfig.URL_API = ServerUrls.MAIN_URL

        //---------------------------------------------------------------
        // 인터넷 연결 안될 때 : <재시도> 버튼 생성, 기능 추가
        //---------------------------------------------------------------
        if (checkNetworkStatus() == true) {
            WcWeb.loadUrlPage(this, mWebView!!, newUrl)
            Log.d(tag, "url@2 : ${newUrl}")
            //WcWeb.loadUrlPage(this, mWebView!!, WaConfig.URL_MAIN + "?lang=${lang}")

            val bPush = intent.getBooleanExtra("push", false)
            if (bPush == true) {
                intent.putExtra("push", false)
                // goto Notice page
                gotoNoticePage("onCreate")
            }
            else {
                val strDeepLink = intent?.dataString
                val uri = intent.data;
                if (uri != null) {
                    var targetUrl = uri.getQueryParameter("targetUrl")
                    var reqPlatformTyp= uri.getQueryParameter("reqPlatformTyp")
                    var targetPlatformTyp= uri.getQueryParameter("targetPlatformTyp")

                    if(targetUrl == null ) {
                        targetUrl = "";
                    }

                    if(reqPlatformTyp == null ) {
                        reqPlatformTyp = "";
                    }

                    if(targetPlatformTyp == null ) {
                        targetPlatformTyp = "";
                    }

                    gotoGateWay(targetPlatformTyp.toString(), reqPlatformTyp.toString(), targetUrl.toString())
                }
                //main_webview.loadUrl(newUrl)
                Log.d("PUSH", "onCreate")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isForeground = true
        Log.i(tag, "onResume:")
    }

    override fun onPause() {
        super.onPause()
        isForeground = false
        Log.i(tag, "onPause:")
    }

    /** Activity 종료전 호출 */
    override fun onDestroy() {
        super.onDestroy()
        Log.i(tag, "onDestroy:")

        Vaccine.quitVaccine()
    }

    /** BackKey 동작 */
    override fun onBackPressed() {
        Log.i(tag, "onBackPressed")

        if (mWebView != null) {
            //if (mWebView!!.canGoBack()) {
            //    mWebView!!.goBack()
            //} else {
            //    appQuit()
            //}
            mWebView.evaluateJavascript("javascript:BackKeyPressed('" + "TEST" + "');",
                ValueCallback<String?> { value -> // value is the result returned by the Javascript as JSON
                    // Receive newpid here
                    if (value == "true") {
                        if (mWebView!!.canGoBack()) {
                            mWebView!!.goBack()
                        } else {
                            appQuit()
                        }
                    }
                    else {
                        Log.d("JS=======", value!!)
                        //Toast.makeText(this, "Return = [$value]", Toast.LENGTH_SHORT).show()
                    }
                })
        } else {
            super.onBackPressed()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        val bPush = intent?.getBooleanExtra("push", false)
        Log.d("PUSH", "onNewIntent = [$bPush]")
        if (bPush == true) {
            //if (isForeground == true) {
                //val alertDialog = android.app.AlertDialog.Builder(this)
                //    .setTitle(this.getString(R.string.common_title))
                //    .setMessage(this.getString(R.string.common_goto_notice))
                //    .setPositiveButton(this.getString(R.string.common_confirm)) { _, _ ->
                //        // goto Notice page
                        gotoNoticePage("onNewIntent [Foreground]")
                //    }
                //    .setNegativeButton(this.getString(R.string.common_cancel)) { _, _ ->
                //    }
                //    .create()
                //alertDialog.show()
            //}
            //else {
            //    // goto Notice page
            //    gotoNoticePage("onNewIntent [Background]")
            //}

        }
        else {
            // deeplink or not
            if (intent?.data != null) {
                val strDeepLink = intent?.dataString
                if (strDeepLink != null && strDeepLink != "" && strDeepLink.startsWith("linaga")) {
                    // 2022-09-13 딥링크 분기처리
                    if(strDeepLink.contains("url=")) {
                        val arr = strDeepLink.split("url=")
                        mWebView?.post {
                            val str = "javascript:redirectUrl('" + arr[1] + "');"
                            mWebView.loadUrl(str)
                        }
                    }

                    // 2022-09-13 딥링크 분기처리
                    if (strDeepLink.contains("targetPlatformTyp=")) {
                        val arr = strDeepLink.split("main_page?")
                        val newUrl = ServerUrls.MAIN_URL+"?"+arr[1]
                        mWebView?.post {
                            mWebView?.loadUrl(newUrl)
                        }

                    }
                }
            } else {
            }
        }
    }

    fun gotoGateWay(target: String, req: String, url: String, ) {
        mWebView?.post {
            mWebView?.loadUrl(ServerUrls.MAIN_URL+ "?targetPlatformTyp="+target+"&reqPlatformTyp="+req+"&targetUrl="+url)
        }
    }

    fun gotoNoticePage(msg: String) {
        mWebView?.post {
            Log.d("PUSH", "$msg ---> 공지사항")
            mWebView?.loadUrl(ServerUrls.NOTICE_URL)
        }
    }

    /** 페이지 로딩 완료 */
    fun onPagefinished() {
        Log.i(tag, "onPagefinished:")
        //쿠키 수동 싱크
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().sync()
        }
        else {
            CookieManager.getInstance().flush()
        }
    }

    /** 앱 종료 */
    fun appQuit() {
        Log.i(tag, "appQuit:")

        val title = this.getString(R.string.common_quit_desc)
        WaUtils.showAlertDialog(this, WaUtils.TYPE_ALERT_OKCANCEL, title, "", "확인", "취소",
            {
                Vaccine.quitVaccine()
                //finish();

                //android.os.Process.killProcess(android.os.Process.myPid());

                //finishAndRemoveTask();

                finishAffinity();
                System.exit(0);
            },
            {
            })
    }

    fun checkNetworkStatus() : Boolean {
        val view_reload = findViewById<View>(R.id.view_reload)
        val btn_reload = findViewById<Button>(R.id.btn_reload)

        btn_reload.setOnClickListener {
            view_reload.visibility = View.INVISIBLE
            if (WaUtils.checkNetworkState(this) == true) {
                //view_reload.visibility = View.INVISIBLE
                WcWeb.loadUrlPage(this, mWebView!!, WaConfig.URL_MAIN)
            }
            else {
                Handler().postDelayed({
                    view_reload.visibility = View.VISIBLE
                }, 1000)
            }
        }

        if (WaUtils.checkNetworkState(this) == false) {
            view_reload.visibility = View.VISIBLE
            return false
        }
        else {
            return true
        }
    }
    /**
     * 위치정보 권한 동의 팝업
     * @param origin
     * @param callback 권한 동의 결과
     */
    fun onGeolocationPermissionsShowPrompt(
        origin: String?,
        callback: GeolocationPermissions.Callback?
    ) {
        Log.i(tag, "onGeolocationPermissionsShowPrompt")
        PermissionFactory().processGeolocationPermission(this, origin, callback)
    }

    /**
     * 기능정의
     * @param filePathCallback
     */
    fun onShowFileChooser(filePathCallback: ValueCallback<Array<Uri>>?) {
        Log.i(tag, "onShowFileChooser")
        PermissionFactory().processShowFileChooser(this, filePathCallback)
    }

    /**
     * 액티비티 결과 처리
     * @param requestCode
     * @param resultCode
     * @param data
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i(tag, "OnActivityResult: requestCode:$requestCode, resultCode:$resultCode ")

        // 사진 & 갤러리 결과
        var type = 0 //0:fromCamera, 1:fromGallery
        if (requestCode == REQ_PHOTO || requestCode == REQ_CAMERA) {
            if (resultCode == Activity.RESULT_OK) {
                var path = ""
                if (requestCode == REQ_PHOTO) {
                    if (data != null) {
                        path = data.data.toString()
                        type = 1
                    }
                }
                else {  // REQ_CAMERA
                    path = photoPath
                    type = 0
                }

                var bValid = 0
                contentResolver.openInputStream(Uri.parse(path)).run {
                    bValid = this!!.read()
                }
                if (bValid > 0) {
                    val uri = data!!.data
                    val cursor = uri?.let {
                        contentResolver.query(it, null, null, null, null)
                    }
                    val nameIndex = cursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor!!.getColumnIndex(OpenableColumns.SIZE)
                    cursor!!.moveToFirst()

                    val name = cursor!!.getString(nameIndex)
                    val size = cursor!!.getLong(sizeIndex)

                    val intent = Intent(this@MainActivity, ImageCropActivity::class.java)
                    intent.putExtra("upload_data", singleUploadData)
                    intent.putExtra("file_dir", path)
                    intent.putExtra("type", type)

                    var compress = 40
                    if (size > 16*1024*1024) {   // picture size is over 16MB
                        compress = 5
                    }
                    else if (size > 8*1024*1024) {   // picture size is over 8MB
                        compress = 10
                    }
                    else if (size > 4*1024*1024) {   // picture size is over 4MB
                        compress = 20
                    }
                    intent.putExtra("compress", compress)
                    startActivityForResult(intent, REQUEST_SINGLE)
                }
                else {
                    val title = this.getString(R.string.gallery_bad_file)
                    WaUtils.showAlertDialog(this, WaUtils.TYPE_ALERT_OK, title, "", "확인", "", {}, {})
                }
            }

        }

        // 싱글 업로드 결과 처리
        else if (requestCode == REQUEST_SINGLE) {
            if (resultCode == Activity.RESULT_OK) {

                val type = data?.getIntExtra("type",0)
                val uploadState = data?.getStringExtra("upload_state")

                val webview = mWebView

                val json = JsonObject()
                json.addProperty("result", uploadState)
                val data = json.toString()

                var returnStr = ""
                if(type == 0){
                    returnStr = "javascript:gnx_app_callback('uploadCameraImg',"+ data + ");"
                }else{
                    returnStr = "javascript:gnx_app_callback('uploadGalleryImg',"+ data + ");"
                }

                webview?.post{
                    webview?.loadUrl(returnStr)
                }


//                if (!uploadYN.isNullOrEmpty()) {
//                    if (uploadYN == "Y") {
//                        jsUploadResult()
//                    } else {
//                        jsUploadResult()
//                    }
//                }
//                if (!uploadState.isNullOrEmpty()) {
//                    if (uploadState == "Y") {
//                        jsUploadResult()
//                    } else {
//                        jsUploadResult()
//                    }
//                }

            }
//            else if (resultCode == Activity.RESULT_CANCELED){
//                getImages()
//            }

        }
        else if (requestCode == REQ_SECURE_TEXT) {

            val jsonResult = SecureText.processSecureText (requestCode, resultCode, data)

            val jsonData = JSONObject(jsonResult)
            var value = ""
            val state = if (jsonData.getString("state") == "ok") "0" else "1"
            if(state == "0") {
                value = jsonData.getString("value")
            }
            val webview = mWebView

            val json = JsonObject()
            json.addProperty("state", state)
            json.addProperty("result", value)
//            val msg = data!!.getStringExtra("secureText")
//            json.addProperty("secureText", msg)
            val data = json.toString()

            var returnStr = "javascript:gnx_app_callback('getSecureText',"+ data + ");"
            webview?.post{
                webview?.loadUrl(returnStr)
            }
        }
        else if (requestCode == VACCINE_REQ) {
            when (resultCode) {
                Constants.ROOTING_EXIT_APP,     // 1200
                Constants.ROOTING_YES_OR_NO,    // 1210
                Constants.V_DB_FAIL -> {        // 1240
                    //this.finish()
                }
                Constants.EMPTY_VIRUS -> {      // 1000
                }
                Constants.EXIST_VIRUS_CASE1 -> { // 1010
                }
                Constants.EXIST_VIRUS_CASE2 -> { // 1100
                }
            }
            val json = JsonObject()
            json.addProperty("result", resultCode.toString())
            val data = json.toString()

            var returnStr = "javascript:gnx_app_callback('runVaccine',"+ data + ");"

            mWebView?.post{
                mWebView?.loadUrl(returnStr)
            }
        }
    }

    /**
     * 권한체크 결과
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

//        if (requestCode == PermissionFactory.REQUEST_PERMISSION_CAMERAGALLERY
//            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            // <촬영하기>
//            if (mGalleryMode == IMAGE_TYPE.CAMERA_UPLOAD) {
//                getCropImage()
//            }
//            // <사진 가져오기>
//            else if (mGalleryMode == IMAGE_TYPE.GALLERY_UPLOAD) {
//                getImages()
//            }
//        }

        if (grantResults.isEmpty()) {
            throw RuntimeException("Empty permission result")
        }

        // 카메라
        if (requestCode == REQUEST_PERMISSION_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCropImage()
            }
            else {  // 거부됨 (PERMISSION_DENIED)
                Toast.makeText(this, "승인이 거부되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
        // 갤러리
        else if (requestCode == REQUEST_PERMISSION_GALLERY) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getImages()
            } else {  // 거부됨 (PERMISSION_DENIED)
                Toast.makeText(this, "승인이 거부되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** 설정화면 */
    fun calltoSetting() {
        startActivity(Intent(this, SettingActivity::class.java))
    }

    /** 캐쉬삭제 */
    fun calltoClearCache() {
        Log.i(tag, "calltoClearCache:")
        mWebView?.clearCache(true)
    }

    /** 갤러리 사진 가져오기 */
    fun calltoGalleryUpload(uploadData: MultiUploadModel.MultiUpload) {
        Log.i(tag, "calltoGalleryUpload:")

        mGalleryMode = IMAGE_TYPE.GALLERY_UPLOAD
        singleUploadData = uploadData

        val result = PermissionFactory().checkPermissionGallery(this)
        if (result == true) {
            getImages()
        }
        else {
            PermissionFactory().requestPermissionGallery(this)
        }
    }

    /** 카메라 싱글 업로드 */
    fun calltoCameraUpload(uploadData: MultiUploadModel.MultiUpload) {
        Log.i(tag, "calltoCameraUpload:")
        // PermissionFactory().processShowCamera(this, uploadData)

        mGalleryMode = IMAGE_TYPE.CAMERA_UPLOAD
        singleUploadData = uploadData

        val result = PermissionFactory().checkPermissionCamera(this)
        if (result == true) {
            getCropImage()
        }
        else {
            PermissionFactory().requestPermissionCamera(this)
        }
    }

    // 2022.7.27 by Gagamel
    // camera 요청용 Launcher : startActivityForResult() --> Launcher.launch()
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {
            // 사진 & 갤러리 결과
            var type = 0 //0:fromCamera, 1:fromGallery
            if (it.resultCode == Activity.RESULT_OK) {
                var path =  photoPath
                var bValid = 0
                contentResolver.openInputStream(Uri.parse(path)).run {
                    bValid = this!!.read()
                }
                if (bValid > 0) {
                    val intent = Intent(this@MainActivity, ImageCropActivity::class.java)
                    intent.putExtra("upload_data", singleUploadData)
                    intent.putExtra("file_dir", path)
                    intent.putExtra("type", type)
                    startActivityForResult(intent, REQUEST_SINGLE)
                }
                else {
                    val title = this.getString(R.string.gallery_bad_file)
                    WaUtils.showAlertDialog(this, WaUtils.TYPE_ALERT_OK, title, "", "확인", "", {}, {})
                }
            }
        }

    fun getCropImage() {

        var photoFile: File? = createImageFile()

        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this,
                BuildConfig.APPLICATION_ID +".provider",
                it
            )
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            takePictureLauncher.launch(intent)
        }
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        )
        val image = File.createTempFile(
            imageFileName, // prefix
            ".jpg", // suffix
            storageDir      // directory
        )

        // Save a file: path for use with ACTION_VIEW intents
        photoPath = "file:" + image.absolutePath
        return image
    }

    fun  getImages() {
//        WaImage().showMultiGallery(this, mUploadData!!)
        val intent = Intent("android.intent.action.GET_CONTENT")
        intent.type = "image/*"     // 모든 이미지
        startActivityForResult(intent, REQ_PHOTO)
    }

    /** 화면 갱신 */
    fun jsUploadResult() {
        Log.i(tag, "jsUploadResult:")
        mWebView?.loadUrl("javascript:getFileList()")
    }

    /** 언어 설정 */
    fun changeLanguage(language: String) {
        Log.i(tag, "jsUploadResult:")
        locale = WaSharedPreferences(this).readPrefer("language").toString()

        // 언어 설정 적용
//        runOnUiThread(  Runnable() {
//            setLanguage(language)
//        })
    }

    /** 웹뷰 다운로드 기능 (별도 브라우저 호출) */
    private fun webViewDownload() {
        // 다운로드 기능
        mWebView?.setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
            Log.i(
                tag,
                "DownloadListener url:$url,\n userAgent:$userAgent,\n contentDisposition:$contentDisposition,\n mimeType:$mimeType,\n contentLength:$contentLength"
            )
            // 브라우저 호출
            //val i = Intent(Intent.ACTION_VIEW)
            //i.data = Uri.parse(url)
            //startActivity(i)

            downloadProcess(url, userAgent, contentDisposition, mimeType)
        }
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

        if (PermissionFactory().checkPermissionStorage(this)) {
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

            // attachmentfileName=개인정보동의서.pdf
            //if (content.indexOf("attachmentfilename=") == -1) {
            //    Toast.makeText(this, R.string.file_not_found, Toast.LENGTH_LONG).show()
            //    return
            //}
            /* 20220906 수정 : 한글 깨짐 오류 수정 */
            val filename = URLDecoder.decode(content.substring(DOWNLOAD_FILENAME_PREFIX.length, content.length))
            Log.i(tag, "DownloadListener fileName:$filename")

            val file =
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + "/" + filename)
            Log.i(tag, "DownloadListener file:${file.absolutePath}")
            val cookies = CookieManager.getInstance().getCookie(dnUrl)
            val request: DownloadManager.Request = DownloadManager.Request(Uri.parse(dnUrl))
//            request.setDescription(Strings.getString(this, locale).get("file_download_start"))
            request.setDescription(this.getString(R.string.file_download_start))
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

            val downloadManger = getSystemService(DOWNLOAD_SERVICE) as DownloadManager?
            val dnID = downloadManger!!.enqueue(request)
            Toast.makeText(
                this,
//                        "$filename " + Strings.getString(this, locale).get("file_downloading"),
                "$filename " + this.getString(R.string.file_downloading),
                Toast.LENGTH_LONG
            ).show()

//            val alertDialog = AlertDialog.Builder(this)
//                .setTitle(this.getString(R.string.common_title))
//                .setMessage("$filename " + this.getString(R.string.file_downloading))
//                .setPositiveButton(this.getString(R.string.file_down_title)) { _, _ ->
//                    val downloadManger = getSystemService(DOWNLOAD_SERVICE) as DownloadManager?
//                    val dnID = downloadManger!!.enqueue(request)
//                    Toast.makeText(
//                        this,
//                        "$filename " + this.getString(R.string.file_downloading),
//                        Toast.LENGTH_LONG
//                    ).show()
//                }
//                .setNegativeButton(this.getString(R.string.file_down_cancel)) { _, _ ->
//                }
//
//                .setCancelable(false)
//                .create()
//            alertDialog.show()

        } else {
//            Toast.makeText(this, Strings.getString(this, locale).get("file_permission_desc"), Toast.LENGTH_LONG).show()
            Toast.makeText(this, R.string.file_permission_desc, Toast.LENGTH_LONG).show()
        }
    }

    override fun onAfterLocaleChanged() {
        Log.d(tag, "onAfterLocaleChanged:")
    }

    override fun onBeforeLocaleChanged() {
        Log.d(tag, "onBeforeLocaleChanged:")
    }

    fun initVestPin() {
        android.util.Log.d("VestPIN", "VestPIN version is " + VestPinLibImpl.getVersion())
        val authority = "kr.co.lina.ga.VPContentProvider"

        val options = HashMap<String, String>()
        options["FINGERPRINT_CNT"] = "3"

        val webview = VestPinLibImpl(this, mWebView, authority, options)
        webview.webViewInit()


        val wvs: WebSettings = mWebView!!.settings
        wvs.javaScriptEnabled = true
        wvs.domStorageEnabled = true
        wvs.setSupportMultipleWindows(true)
        wvs.javaScriptCanOpenWindowsAutomatically = true

        // 클라이언트 설정
        mWebView!!.webChromeClient = WcWebChromeClient(this)
        mWebView!!.webViewClient = WcWebViewClient(this)
        mWebView!!.addJavascriptInterface(WcWebBridge(this), WaConfig.WEB_BRIDGE_NAME)

        // Oscar
        android.util.Log.d("OSCAR", "version = " + Oscar.getVersion())
    }

    var activityHeight = 0
    var bKeyboardShow = false
    private fun checkKeyboardShow() {

        // HW Key
//        mWebView.setOnKeyListener { _, keyCode, _ ->
//            if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
//                mWebView.goBack()
//            }
//            return@setOnKeyListener true
//        }

        //------------------------------------------------------------
        // library <KeyboardVisibilityEvent>
        //------------------------------------------------------------
//        KeyboardVisibilityEvent.setEventListener(
//        this, object : KeyboardVisibilityEventListener() {
//            fun onVisibilityChanged(isOpen: Boolean) {
//                // Ah... at last. do your thing :)
//            }
//        })

        // 4번
        this.window.decorView.doOnNextLayout {
            val displayFrame : Rect = Rect()
            this.window.decorView.getWindowVisibleDisplayFrame(displayFrame)
            activityHeight = displayFrame.height()
        }

        /* Check for keyboard open/close */
        this.window.decorView.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            val drawFrame : Rect = Rect()
            this.window.decorView.getWindowVisibleDisplayFrame(drawFrame)
            val currentSize = drawFrame.height()

            bKeyboardShow = currentSize < activityHeight
            Log.v("keyboard1","$bKeyboardShow $currentSize - $activityHeight")

            if (bKeyboardShow == true) {
                //Toast.makeText(this, "Keyboard : Show", Toast.LENGTH_SHORT).show()
                mWebView?.post{
                    mWebView.loadUrl("javascript:KeyboardShow()")
                }
            }
            else {
                //Toast.makeText(this, "Keyboard : Hide", Toast.LENGTH_SHORT).show()
                mWebView?.post{
                    mWebView.loadUrl("javascript:KeyboardHide()")
                }
            }
        }
    }
}
