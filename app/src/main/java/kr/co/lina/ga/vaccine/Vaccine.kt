package kr.co.lina.ga.vaccine

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import com.TouchEn.mVaccine.b2b2c.activity.BackgroundScanActivity
import com.TouchEn.mVaccine.b2b2c.receiver.ScanReceiver
import com.TouchEn.mVaccine.b2b2c.util.Global
import com.secureland.smartmedic.SmartMedic
import com.secureland.smartmedic.core.Constants
import kotlinx.coroutines.MainCoroutineDispatcher
import kr.co.lina.ga.ui.main.MainActivity

object Vaccine {

    val VACCINE_ID = "lina_ga_mobile"
    val VACCINE_LICENSE_KEY = "e6e86b6efc0033a0de17cff4232e7dcc693ccbb1"

    var scanReceiver: BroadcastReceiver? = null
    var codeReceiver: BroadcastReceiver? = null

    fun initVaccine() {

        val mainActivity = MainActivity.mainActivity() as MainActivity

        // vaccine
        Constants.site_id = VACCINE_ID
        Constants.license_key = VACCINE_LICENSE_KEY

        try {
            SmartMedic.init(mainActivity)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        /*----------------- 디버깅 모드 설정 -------------------*/
        Constants.debug = true // 디버깅 필요 시 true 설정
        Global.debug = true // 디버깅 필요 시 true 설정

        /*----------------- 실시간검사 동적 등록 방법 -------------------*/
        val intentFilter = IntentFilter()
        intentFilter.addAction("android.intent.action.PACKAGE_ADDED")
        intentFilter.addAction("android.intent.action.PACKAGE_INSTALL")
        intentFilter.addAction("android.intent.action.PACKAGE_CHANGED")
        intentFilter.addAction("android.intent.action.PACKAGE_REPLACED")
        intentFilter.addDataScheme("package")
        scanReceiver = ScanReceiver()
        mainActivity.application.registerReceiver(scanReceiver, intentFilter)

        // Vaccine : mini 모드
        mini()
    }

    fun quitVaccine() {
        val mainActivity = MainActivity.mainActivity()
        val mNotificationManager = mainActivity.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.cancel(MainActivity.MESSAGE_ID)
        mNotificationManager.cancel(MainActivity.MESSAGE_ID1)
    }

    private fun mini() {
        val mainActivity = MainActivity.mainActivity() as MainActivity
        val i = Intent(mainActivity, BackgroundScanActivity::class.java) // BackgroundScanActivity와 통신할 Intent생성

        //BackgroundScanActivity로 넘길 옵션값 설정
        i.putExtra("useBlackAppCheck", true) // 루팅 검사를 실시하면 루팅 우회 앱 설치 여부까지 검사
        i.putExtra("showBlackAppName", true)
        i.putExtra("scan_rooting", true)
        i.putExtra("scan_package", true)
        i.putExtra("useDualEngine", false)
        i.putExtra("backgroundScan", true) // mini 전용
        i.putExtra("rootingexitapp", true)
        i.putExtra("rootingyesorno", false)
        i.putExtra("rootingyes", false)
        i.putExtra("rooting_delay_time", 0)
        i.putExtra("show_update", false)
        i.putExtra("show_license", false)
        i.putExtra("show_notify", true) // mini 전용
        i.putExtra("notifyClearable", false) // mini 전용
        i.putExtra("notifyAutoClear", false) // mini 전용
        i.putExtra("show_toast", true)
        i.putExtra("show_warning", false)
        i.putExtra("show_scan_ui", true) // mini 전용
        i.putExtra("show_badge", false) // mini 전용
        i.putExtra("bg_rooting", false) // mini 전용
        i.putExtra("show_about", true) // mini 전용

        mainActivity.startActivityForResult(i, MainActivity.VACCINE_REQ) //Intent를 보내고 결과값을 얻어옴
    }
}