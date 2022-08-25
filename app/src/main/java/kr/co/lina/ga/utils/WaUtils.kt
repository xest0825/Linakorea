package kr.co.lina.ga.utils

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import kr.co.lina.ga.R
import kr.co.lina.ga.ui.main.MainActivity
import kr.co.lina.ga.vaccine.Vaccine
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * 앱정보 관리
 * @property tag Log 태그
 * @property Log Log
 * @property PATTERN_YYYYMMDD_KR 날짜패턴
 */
object WaUtils {

    private val tag = "waUtils"
    private val Log = WaLog

    /**
     * 버전 정보 확인
     * @param context
     * @return String
     */
    fun getVersionInfo(context: Context?): String? {
        Log.i(tag, "getVersionInfo:")
        var version = ""
        //version = BuildConfig.VERSION_NAME
        if (context == null) {
            version = ""
        } else {
            try {
                val packageInfo = context.applicationContext
                    .packageManager
                    .getPackageInfo(context.applicationContext.packageName, 0)
                version = packageInfo.versionName
            } catch (e: PackageManager.NameNotFoundException) {
                Log.e(tag, "getVersionInfo:" + e.message)
            }
        }
        Log.i(tag, "getVersionInfo version:$version")
        return version
    }

    /**
     * 버전 비교
     * @param appVersion
     * @param serverVersion
     * @return Boolean
     */
    fun compareVersion(appVersion: String, serverVersion: String): Boolean {
        Log.i(tag, "compareVersion: appVersion:$appVersion serverVersion:$serverVersion")

        val app = appVersion.replace(".", "").toInt()
        val server = serverVersion.replace(".", "").toInt()

        Log.i(tag, "compareVersion app:$app server:$server")

        return (server > app)
    }

    /**
     * 플레이스토어 이동
     * @param context
     */
    fun goMarket(context: Context) {
        Log.i(tag, "goMarket:")
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("market://details?id=" + context.applicationContext.packageName)
        context.startActivity(intent)
    }

    /**
     * 네트워크 확인
     * @param context
     * @return Boolean
     */
    fun checkNetworkState(context: Context): Boolean {
        Log.i(tag, "checkNetworkState:")
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nw = connectivityManager.activeNetwork ?: return false
            val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else -> false
            }
        } else {
            val nwInfo = connectivityManager.activeNetworkInfo ?: return false
            return nwInfo.isConnected
        }
    }

    /**
     * dp 확인
     * @param dp
     * @return Int
     */
    fun dpToPx(dp: Float): Int {
        Log.i(tag, "dpToPx: dp:$dp")
        val metrics = Resources.getSystem().displayMetrics
        return (dp * metrics.density + 0.5f).toInt()
    }

    val PATTERN_YYYYMMDD_KR = "yyyy년 MM월 dd일"
    // val PATTERN_YYYYMMDD_DASH = "yyyy-MM-dd"

    /**
     * 오늘 날짜 확인
     * @param pattern
     * @return String
     */
    fun getToday(pattern: String): String =
        SimpleDateFormat(pattern, Locale.getDefault()).format(Date())

    /**
     * 마켓팅 정보 동의 알림
     * @param context
     * @param agree
     * @return STring
     */
    fun notificationToastDiscription(context: Context, agree: Boolean): String {
        Log.i(tag, "notificationToastDiscription:")
        var result = ""
        result = if (agree) {
            getToday(PATTERN_YYYYMMDD_KR) + context.getString(R.string.notification_toast_agree)
        } else {
            getToday(PATTERN_YYYYMMDD_KR) + context.getString(R.string.notification_toast_disagree)
        }
        return result
    }

    // TODO DeviceID 읽기 및 관리
    // ADID, ANDRIOD_ID, UUID - SAVE Preferences

    /**
     * UUID 값 확인
     * @param context
     * @return String
     */
    fun getUUID(context: Context): String {
        Log.i(tag, "getUUID:")
        var uuid = ""
        if (WaSharedPreferences(context).readPrefer("deviceId")?.isEmpty()!!) {
            uuid = UUID.randomUUID().toString()
            WaSharedPreferences(context).writePrefer("deviceId", uuid)
        } else {
            uuid = WaSharedPreferences(context).readPrefer("deviceId").toString()
        }
        Log.i(tag, "getUUID: uuid:$uuid")
        return uuid
    }

    /**
     * 앱캐시 삭제
     * @param context
     */
    fun clearApplicationData(context: Context) {
        val cache: File = context.cacheDir
        val appDir = File(cache.parent)
        if (appDir.exists()) {
            val children = appDir.list()
            for (s in children!!) {
                if (s != "lib") {
                    deleteDir(File(appDir, s))
                    Log.i(tag, "File /data/data/APP_PACKAGE/$s DELETED")
                }
            }
        }
    }

    /**
     * 경로 삭제
     * @param dir
     * @return Boolean
     */
    private fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.list()
            for (i in children!!.indices) {
                val success = deleteDir(File(dir, children[i]))
                if (!success) {
                    return false
                }
            }
        }
        return dir!!.delete()
    }

    val TYPE_ALERT_OK = 0
    val TYPE_ALERT_OKCANCEL = 1

    fun showAlertDialog(context: Context, type: Int,
                        title: String = "", message: String  = "",
                        ok: String = "확인", cancel: String = "취소",
                        okHandler: () -> Unit, cancelHandler: () -> Unit) {

        val builder = AlertDialog.Builder(context).setView(R.layout.dialog_popup)
        builder.setCancelable(false)
        val dialog = builder.create()
        dialog.window?.decorView?.setBackgroundResource(R.drawable.bg_dialog) // setting the background
        dialog.show()

        val textTitle = dialog.window?.decorView?.findViewById<TextView>(R.id.dialog_popup_text)
        textTitle!!.text = title

        // show ONE buttons
        if (type == TYPE_ALERT_OK) {
            val btns = dialog.window?.decorView?.findViewById<LinearLayout>(R.id.dialog_popup_layout)
            btns!!.visibility = View.GONE

            val btnOk = dialog.window?.decorView?.findViewById<Button>(R.id.dialog_popup_ok1)
            btnOk!!.setOnClickListener {
                dialog.dismiss()
                okHandler()
            }
        }
        // show TWO buttons
        else {  // TYPE_ALERT_OKCANCEL
            val btn = dialog.window?.decorView?.findViewById<Button>(R.id.dialog_popup_ok1)
            btn!!.visibility = View.GONE

            val btnOK = dialog.window?.decorView?.findViewById<Button>(R.id.dialog_popup_ok)
            btnOK!!.setOnClickListener {
                dialog.dismiss()
                okHandler()
            }
            val btnCancel = dialog.window?.decorView?.findViewById<Button>(R.id.dialog_popup_cancel)
            btnCancel!!.setOnClickListener {
                dialog.dismiss()
                cancelHandler()
            }
        }
    }
}