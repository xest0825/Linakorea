package kr.co.lkins.EHB.webcore

import android.content.Context
import android.content.Intent
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG
import android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import android.net.Uri
import android.os.Build
import android.webkit.JavascriptInterface
import androidx.biometric.BiometricManager.from
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.softsecurity.transkey.TransKeyActivity
import kotlinx.android.synthetic.main.activity_bio_auth.*
import kr.co.lina.ga.Model.MultiUploadModel
import kr.co.lina.ga.ui.main.AppAuth
import kr.co.lina.ga.ui.main.MainActivity
import kr.co.lina.ga.ui.main.TmsPush
import kr.co.lina.ga.utils.WaLog
import kr.co.lina.ga.utils.WaSharedPreferences
import kr.co.lina.ga.utils.WaUtils
import kr.co.lina.ga.vaccine.Vaccine
import kr.co.lina.ga.vestpin.RunOscar
import kr.co.lina.ga.webcore.WcInterfaceModel
import org.json.JSONObject


/**
 * WebView 와 Native 연결
 * @property tag Log 태그
 * @property Log Log
 * @param context Context
 */
class WcWebBridge(private val context: Context) {
    private val tag = "WcWebBridge"
    private val Log = WaLog
    // 메인에 생성한 함수를 호출한다.
    // private val mainActivity = context as? MainActivity

    /**
     * WebView 에서 Native 기능 호출
     * @property msg
     */
    @JavascriptInterface
    fun callApp(msg: String) {
        val cmd = Gson().fromJson(msg, WcInterfaceModel::class.java)
        Log.i(tag, "JSON: $msg")
        Log.i(tag, "function: ${cmd.command} action: ${cmd.action} values: ${cmd.value}")

        when (cmd.command) {
            "ui" -> {
                when (cmd.action) {
                    "setting" -> setting()
                    //"webview" -> cmd.value.url.let { webview(it) }
                    "clearcache" -> clearcache()

                    // 카메라  싱글 업로드
                    "singleUpload" -> singleUpload(
                        cmd.value.totalSize,
                        cmd.value.totalCount,
                        cmd.value.limitSize,
                        cmd.value.limitCount,
                        cmd.value.ref_seq,
                        cmd.value.claim_seq,
                        cmd.value.attach_gbn,
                        cmd.value.JSON_WEB_TOKEN,
                        cmd.value.mb_id
                    )

                    // 갤러리 멀티 업로드
                    "multiUpload" -> multiUpload(
                        cmd.value.totalSize,
                        cmd.value.totalCount,
                        cmd.value.limitSize,
                        cmd.value.limitCount,
                        cmd.value.ref_seq,
                        cmd.value.claim_seq,
                        cmd.value.attach_gbn,
                        cmd.value.JSON_WEB_TOKEN,
                        cmd.value.mb_id
                    )

                    // 언어 변경
                    "languageChange" -> changeLanguage(
                        cmd.value.lang
                    )
                }
            }
        }
    }

    /* App Version */
    @JavascriptInterface
    fun getAppVersion(msg: String) {
//        val versionName = BuildConfig.VERSION_NAME;
        val versionName = WaUtils.getVersionInfo(context)

        val webview = (context as? MainActivity)?.mWebView
        val json = JsonObject()
        json.addProperty("appVersion",versionName)
        val data = json.toString()

        var returnStr = "javascript:gnx_app_callback('getAppVersion',"+ data + ");"
        webview?.post{
            webview?.loadUrl(returnStr)
        }
    }

    /* OS Version */
    @JavascriptInterface
    fun getOSVersion(msg: String) {
        val osVersion = Build.VERSION.SDK_INT

        val webview = (context as? MainActivity)?.mWebView
        val json = JsonObject()
        json.addProperty("os", "android")
        json.addProperty("version",osVersion)
        val data = json.toString()

        var returnStr = "javascript:gnx_app_callback('getOSVersion',"+ data + ");"
        webview?.post{
            webview?.loadUrl(returnStr)
        }
    }


    /* Device Id */
    @JavascriptInterface
    fun getDeviceId(msg: String) {
        val deviceId = WaUtils.getUUID(context)

        val webview = (context as? MainActivity)?.mWebView
        val json = JsonObject()
        json.addProperty("deviceId", deviceId)
        val data = json.toString()

        var returnStr = "javascript:gnx_app_callback('getDeviceId',"+ data + ");"
        webview?.post{
            webview?.loadUrl(returnStr)
        }
    }

    @JavascriptInterface
    fun runVaccine(msg: String){
        Vaccine.initVaccine()
    }
    /* getLoginType */
    @JavascriptInterface
    fun getLoginType(msg: String){
        var loginType = WaSharedPreferences(context).readPrefer("loginType")
        if(loginType == ""){
            loginType = "0"
        }

        val webview = (context as? MainActivity)?.mWebView
        val json = JsonObject()
        json.addProperty("type", loginType)
        val data = json.toString()

        var returnStr = "javascript:gnx_app_callback('getLoginType',"+ data + ");"
        webview?.post{
            webview?.loadUrl(returnStr)
        }
    }

    /* setLoginType */
    @JavascriptInterface
    fun setLoginType(msg: String){
        val json = JSONObject(msg)
        val value = json.getString("type")
        WaSharedPreferences(context).writePrefer("loginType", value)

        val webview = (context as? MainActivity)?.mWebView
        val json1 = JsonObject()
        json1.addProperty("type", "0")
        val data = json.toString()

        var returnStr = "javascript:gnx_app_callback('setLoginType',"+ data + ");"
        webview?.post{
            webview?.loadUrl(returnStr)
        }
    }

    /* Device Auth info */
    @JavascriptInterface
    fun getDeviceAuthInfo(msg: String) {
        val webview = (context as? MainActivity)?.mWebView
        var retValue = "0"

        val biometricManager = androidx.biometric.BiometricManager.from(context)
        when (biometricManager.canAuthenticate(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG)) {    // or DEVICE_CREDENTIAL)) {
            // HW 미지원 : 0
            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Log.e("GA Bio Auth", "No biometric features available on this device.")
                retValue = "0"
            }
            // 기기에 생체 인증 정보가 없음 : 1
            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Log.e("GA Bio Auth", "The device does not have any biometric credentials")
                retValue = "1"
            }
            // 인증 가능 : 2
            androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.e("GA Bio Auth", "App can authenticate using biometrics.")
                retValue = "2"
            }
//            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
//                // 생체 인식 기능은 현재 사용할 수 없습니다.
//                Log.e("GA Bio Auth","Biometric features are currently unavailable.")
//            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED ->
//                // 보안 취약점이 발견되었으며 보안 업데이트가 이 문제를 해결할 때까지 센서를 사용할 수 없습니다
//                Log.e("GA Bio Auth","A security vulnerability has been discovered and the sensor is unavailable until a security update has addressed this issue.")
//            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED ->
//                // 지정된 인증자 조합이 장치에서 지원되지 않습니다.
//                Log.e("GA Bio Auth","A given authenticator combination is not supported by the device.")
//            androidx.biometric.BiometricManager.BIOMETRIC_STATUS_UNKNOWN ->
//                // 상태 이상
//                Log.e("GA Bio Auth","Unable to determine whether the user can authenticate.")
        }

        val json = JsonObject()
        json.addProperty("deviceSupportAuthType", retValue)
        val data = json.toString()

        var returnStr = "javascript:gnx_app_callback('getDeviceAuthInfo',"+ data + ");"
        webview?.post{
            webview?.loadUrl(returnStr)
        }
    }

    @JavascriptInterface
    fun runAppIron(msg:String){
        AppAuth.authApp()
    }

    /* get Secure Text */
    @JavascriptInterface
    fun getSecureText(msg: String) {
        // call intent
        val newIntent = Intent(context, TransKeyActivity::class.java)

        newIntent.putExtra(TransKeyActivity.mTK_PARAM_KEYPAD_TYPE, TransKeyActivity.mTK_TYPE_KEYPAD_QWERTY_LOWER)
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_INPUT_TYPE, TransKeyActivity.mTK_TYPE_TEXT_PASSWORD_LAST_IMAGE)
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_NAME_LABEL, "암호입력 32 자")
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_SET_HINT, "텍스트입력 32 자 입력하세요")
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_LICENSE_FILE_NAME, "TouchEn_mTranskey_license")
        (context as MainActivity).startActivityForResult(newIntent, (context as MainActivity).REQ_SECURE_TEXT)


//        val webview = (context as? MainActivity)?.mWebView
//        val json = JsonObject()
//        json.addProperty("secureText", msg)
//        val data = json.toString()
//
//        var returnStr = "javascript:gnx_app_callback('getSecureText',"+ data + ");"
//        webview?.post{
//            webview?.loadUrl(returnStr)
//        }
    }

    @JavascriptInterface
    fun uploadCameraImg(msg: String){
        val totalSize = "3000000"
        val totalCount =  "10"
        val limitSize = "5000000"
        val limitCount ="5"
        val ref_seq = "1"
        val claim_seq = "1"
        val attach_gbn = "1"
        val JSON_WEB_TOKEN = "23432"
        val mb_id = "1"
        singleUpload(totalSize,totalCount, limitSize, limitCount, ref_seq, claim_seq, attach_gbn, JSON_WEB_TOKEN, mb_id)
    }

    @JavascriptInterface
    fun uploadGalleryImg(msg: String){
        val totalSize = "3000000"
        val totalCount =  "10"
        val limitSize = "5000000"
        val limitCount ="5"
        val ref_seq = "1"
        val claim_seq = "1"
        val attach_gbn = "1"
        val JSON_WEB_TOKEN = "23432"
        val mb_id = "1"

        val uploadData = MultiUploadModel.MultiUpload(
            totalSize,
            totalCount,
            limitSize,
            limitCount,
            ref_seq,
            claim_seq,
            attach_gbn,
            JSON_WEB_TOKEN,
            mb_id
        )
        (context as? MainActivity)?.calltoGalleryUpload(uploadData)
    }

    @JavascriptInterface
    fun getPushToken(msg: String){
        val token = "push token here..."
        val webview = (context as? MainActivity)?.mWebView
        val json = JsonObject()
        json.addProperty("token", token)
        val data = json.toString()

        var returnStr = "javascript:gnx_app_callback('getPushToken',"+ data + ");"
        webview?.post{
            webview?.loadUrl(returnStr)
        }
    }

    @JavascriptInterface
    fun closeApp(msg: String){
        (context as? MainActivity)?.appQuit()
        //System.exit(0)
    }

    @JavascriptInterface
    fun clearCache(msg: String){
        clearcache()
    }

    @JavascriptInterface
    fun connectOscar(msg: String){
        val json = JSONObject(msg)
        val value = json.getString("code")

        // connect to Oscar server
        RunOscar.connectOscar(context, value)
    }

    @JavascriptInterface
    fun writeOscar(msg: String){
        val json = JSONObject(msg)
        val value = json.getString("sign")

        // write to Oscar server
        RunOscar.writeOscar(context, value)
    }

    @JavascriptInterface
    fun setTmsId(msg: String){
        val json = JSONObject(msg)
        val value = json.getString("custId")

        // write to Oscar server
        TmsPush.setTmsId(context, value)
    }

    @JavascriptInterface
    fun goBenepia(msg: String){
        val json = JSONObject(msg)
        val value = json.getString("data")
        Log.i(tag, "베네피아");
        val inResult = context.getPackageManager().getLaunchIntentForPackage("com.sk.benepia.linacare");
        if(inResult != null) {
            val intent = Intent(inResult)
            intent.putExtra("data", value)
            context.startActivity(intent);
        } else {
            val returnJson = JsonObject()
            val webview = (context as? MainActivity)?.mWebView
            returnJson.addProperty("installed", "false")
            val returnStr = "javascript:gnx_app_callback('goBenepia',"+ returnJson + ");"
            webview?.post{
                webview?.loadUrl(returnStr);
            }
        }
    }

    @JavascriptInterface
    fun openExternalLink(msg: String) {
        val json = JSONObject(msg)
        val link = json.getString("link")

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(link)
        context.startActivity(intent)
    }

    /** 설정화면 이동 */
    fun setting() {
        Log.i(tag, "setting")
        (context as? MainActivity)?.calltoSetting()
    }

    /** 캐쉬 삭제 */
    fun clearcache() {
        Log.i(tag, "clearcache")
        (context as? MainActivity)?.calltoClearCache()
    }

    /** 웹뷰 호출 */
    fun webview(url: String) {
        Log.i(tag, "webview")
        // (context as? MainActivity)?.calltoWebview(url)
    }

    /** 카메라 싱글 업로드 */
    fun singleUpload(
        totalSize: String,
        totalCount: String,
        limitSize: String,
        limitCount: String,
        ref_seq: String,
        claim_seq: String,
        attach_gbn: String,
        JSON_WEB_TOKEN: String,
        mb_id: String
    ) {
        Log.i(tag, "singleUpload:")

        val uploadData = MultiUploadModel.MultiUpload(
            totalSize,
            totalCount,
            limitSize,
            limitCount,
            ref_seq,
            claim_seq,
            attach_gbn,
            JSON_WEB_TOKEN,
            mb_id
        )

        (context as? MainActivity)?.calltoCameraUpload(uploadData)
    }

    /** 갤러리 멀티 업로드 */
    fun multiUpload(
        totalSize: String,
        totalCount: String,
        limitSize: String,
        limitCount: String,
        ref_seq: String,
        claim_seq: String,
        attach_gbn: String,
        JSON_WEB_TOKEN: String,
        mb_id: String
    ) {
        Log.i(tag, "multiUpload:")
        val uploadData = MultiUploadModel.MultiUpload(
            totalSize,
            totalCount,
            limitSize,
            limitCount,
            ref_seq,
            claim_seq,
            attach_gbn,
            JSON_WEB_TOKEN,
            mb_id
        )

        (context as? MainActivity)?.calltoGalleryUpload(uploadData)
    }

    // 언어 설정 변경
    fun changeLanguage (
        language: String
    ) {
        Log.i(tag, "changeLanguage: ${language}")
        WaSharedPreferences(context).writePrefer("language", language)
        Log.d(tag, "SharedPreferences 언어 : ${WaSharedPreferences(context).readPrefer("language")}")

        (context as? MainActivity)?.changeLanguage(language)
    }

}