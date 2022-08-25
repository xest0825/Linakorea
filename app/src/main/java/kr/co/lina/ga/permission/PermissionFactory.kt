package kr.co.lkins.EHB.permission

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.webkit.GeolocationPermissions
import android.webkit.ValueCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kr.co.lina.ga.utils.WaLog
import kr.co.lina.ga.utils.WaSharedPreferences
import kr.co.lina.ga.utils.WaUtils


/* 권한 처리 */
class PermissionFactory {
    val tag = "PermissionFactory"
    val Log = WaLog
    private lateinit var locale: String

    companion object {
        private lateinit var mOrigin: String
        private lateinit var mCallback: GeolocationPermissions.Callback
        private var mFilePathCallback: ValueCallback<Array<Uri>>? = null

        val REQUEST_PERMISSION_LOCATION = 10000
        val REQUEST_PERMISSION_STORAGE  = 10001
        val REQUEST_PERMISSION_CAMERA   = 10002
        val REQUEST_PERMISSION_GALLERY  = 10003

        // 저장 공간
        private val mStorage  = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        private val mLocation = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        private val mCamera   = arrayOf(Manifest.permission.CAMERA,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
        private val mGallery  = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    fun checkPermissionStorage(activity: Activity): Boolean {
        val permissions = mStorage
        if (ContextCompat.checkSelfPermission(activity, permissions[0]) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(activity, permissions, REQUEST_PERMISSION_STORAGE)
            return false
        }
        else {
            return true
        }
    }

    /*  위치정보 권한 팝업  */
    fun processGeolocationPermission(activity: Activity, origin: String?, callback: GeolocationPermissions.Callback?) {
        Log.i(tag, "processGeolocationPermission:")
        if (origin != null) {
            mOrigin = origin
        }
        if (callback != null) {
            mCallback = callback
        }
        if (PermissionFactory().checkPermissionLocation(activity)) {
            callback?.invoke(origin, true, false)
        }
    }

    /* 권한 체크 */
    private fun checkPermissionLocation(activity: Activity): Boolean {
        val permissions = mLocation
        if (ContextCompat.checkSelfPermission(activity, permissions[0]) == PackageManager.PERMISSION_DENIED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[0])) {
                // 권한 허용 거부 후 권한 호출 시
                ActivityCompat.requestPermissions(activity, permissions, REQUEST_PERMISSION_LOCATION)
                return false
            }
            else {
                // 최초 권한 호출 시
                ActivityCompat.requestPermissions(activity, permissions, REQUEST_PERMISSION_LOCATION)
                return false
            }
        }
        else {
            return true
        }
    }

    /*  이미지 선택 파일 호출  */
    fun processShowFileChooser(activity: Activity, filePathCallback: ValueCallback<Array<Uri>>?) {
        Log.i(tag, "processShowFileChooser:")
        //mGalleryMode = 0
        if (mFilePathCallback != null) {
            mFilePathCallback!!.onReceiveValue(null)
        }
        mFilePathCallback = filePathCallback

        checkPermissionGallery(activity)
    }

    /*  파일경로 초기화   */
    fun resetFilePathCallback() {
        mFilePathCallback = null
    }

    fun checkPermissionCamera(activity: Activity) : Boolean {
        if (ContextCompat.checkSelfPermission(activity, mCamera[0]) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(activity, mCamera[1]) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        else {
            return false
        }
    }

    /* 갤러리 권한 체크 */
    fun checkPermissionGallery(activity: Activity) : Boolean {
        if (ContextCompat.checkSelfPermission(activity, mGallery[0]) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        else {
            return false
        }
    }

    fun requestPermissionCamera(activity: Activity) {
        var reqPerms0 = ActivityCompat.shouldShowRequestPermissionRationale(activity, mCamera[0])
        var reqPerms1 = ActivityCompat.shouldShowRequestPermissionRationale(activity, mCamera[1])

        if (reqPerms0 || reqPerms1) {
            ActivityCompat.requestPermissions(activity, mCamera, REQUEST_PERMISSION_CAMERA)
        }
        else {
            // 사용자가 이전에 승인을 '거부'한 경우, 다시 메시지창으로 알려줘야 함.
            if (WaSharedPreferences(activity).readPrefer("permission_camera")?.isEmpty()!!) {
                // 최초 실행 시 : 처음 거절된 직후 이므로, 아무것도 하지 않음.
                ActivityCompat.requestPermissions(activity, mCamera, REQUEST_PERMISSION_CAMERA)
                WaSharedPreferences(activity).writePrefer("permission_camera", "y")
            }
            else {
                showDialogToGetPermission(activity, "camera")
            }
        }
    }

    fun requestPermissionGallery(activity: Activity) {
        var reqPerms = ActivityCompat.shouldShowRequestPermissionRationale(activity, mGallery[0])

        if (reqPerms) {
            ActivityCompat.requestPermissions(activity, mGallery, REQUEST_PERMISSION_GALLERY)
        }
        else {
            // 사용자가 이전에 승인을 '거부'한 경우, 다시 메시지창으로 선택 가능하도록 해야 함.
            if (WaSharedPreferences(activity).readPrefer("permission_gallery")?.isEmpty()!!) {
                // 최초 실행 시 : 처음 거절된 직후 이므로, 아무것도 하지 않음.
                ActivityCompat.requestPermissions(activity, mGallery, REQUEST_PERMISSION_GALLERY)
                WaSharedPreferences(activity).writePrefer("permission_gallery", "y")
            }
            else {
                showDialogToGetPermission(activity, "gallery")
            }
        }
    }

    // 사용자의 '일회성 권한'으로 설정 앱으로 이동
    fun showDialogToGetPermission(activity: Activity, type: String = "gallery") {
        var title = ""
        if (type == "camera") {
            title = "카메라를 이용하려면 권한 승인이 필요합니다. 앱 설정으로 이동하시겠습니까?"
        }
        else {
            title = "사진을 이용하려면 권한 승인이 필요합니다. 앱 설정으로 이동하시겠습니까?"
        }
        WaUtils.showAlertDialog(
            activity, WaUtils.TYPE_ALERT_OKCANCEL, title, "", "확인", "취소",
            {
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", activity.packageName, null))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                activity.startActivity(intent)

                //val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)  // 생체인식 보안
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                //activity.startActivity(intent)
            },
            {
            })
    }
}