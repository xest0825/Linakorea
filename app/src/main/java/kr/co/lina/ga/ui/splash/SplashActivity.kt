package kr.co.lina.ga.ui.splash

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import com.akexorcist.localizationactivity.ui.LocalizationActivity
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.co.lina.ga.BuildConfig
import kr.co.lina.ga.R
import kr.co.lina.ga.retrofit.RetrofitClient
import kr.co.lina.ga.retrofit.RtfModel
import kr.co.lina.ga.ui.main.MainActivity
import kr.co.lina.ga.ui.permission.PermissionActivity
import kr.co.lina.ga.vestpin.VestPinActivity
import kr.co.lina.ga.utils.WaLog
import kr.co.lina.ga.utils.WaSharedPreferences
import kr.co.lina.ga.utils.WaUtils
import kr.co.lina.ga.vaccine.Vaccine
import kr.co.lina.ga.vestpin.BioAuthActivity
import kr.co.lina.ga.vestpin.VestPinActivity2
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


/**
 * 메인 진입전 로딩화면
 * @property tag Log 태그
 * @property Log Log
 * @property locale 언어 정보
 * layout : activity_splash.xml
 */
var newUrl = ""
var testUrl = "file:///android_asset/test1.html"
var mainContext: Context? = null

class SplashActivity : LocalizationActivity()  {
    private val tag = "SplashActivity"
    private val Log = WaLog
    private lateinit var context: Context
    private lateinit var locale: String
    private var setLanguageFlag: Boolean = false

    private var bTestPage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)
        Log.i(tag, "onCreate:")
        context = this

        getWindow().setStatusBarColor(Color.TRANSPARENT)

        languageCheck()

        if (bTestPage == false) {
            splash_title.visibility = View.GONE
            textViewUrl.visibility = View.GONE
            splash_url.visibility = View.GONE

            btnGo.visibility = View.GONE
            btnVestPin2.visibility = View.GONE
            btnBioAuth.visibility = View.GONE
            btnTestPage.visibility = View.GONE

            splash_img_1.visibility = View.VISIBLE
            splash_img_2.visibility = View.VISIBLE
            splash_img_3.visibility = View.VISIBLE
            splash_img_4.visibility = View.VISIBLE
        }
        else {
            splash_title.visibility = View.VISIBLE
            textViewUrl.visibility = View.VISIBLE
            splash_url.visibility = View.VISIBLE

            btnGo.visibility = View.VISIBLE
            btnVestPin2.visibility = View.VISIBLE
            btnBioAuth.visibility = View.VISIBLE
            btnTestPage.visibility = View.VISIBLE

            splash_img_1.visibility = View.GONE
            splash_img_2.visibility = View.GONE
            splash_img_3.visibility = View.GONE
            splash_img_4.visibility = View.GONE
        }

        btnGo.setOnClickListener {
            val url = findViewById(R.id.splash_url) as EditText
            val value = url.text.toString()
            newUrl = value
            processMain()
        }

        btnVestPin2.setOnClickListener {
            //startActivity(Intent(this@SplashActivity, VestPinActivity::class.java))
            startActivity(Intent(this@SplashActivity, VestPinActivity2::class.java))
        }

        btnBioAuth.setOnClickListener {
            startActivity(Intent(this@SplashActivity, BioAuthActivity::class.java))
        }

        btnTestPage.setOnClickListener {
            newUrl = testUrl
            processMain()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.i(tag, "onResume:")
    }

    /** Activity 종료전 호출 */
    override fun onDestroy() {
        super.onDestroy()
        Log.i(tag, "onDestroy:")
    }

    private fun processMain() {
        Log.i(tag, "processMain:")
        GlobalScope.launch {
            delay(1000L)
            startMain()
        }
    }

    /** 메인 실행 */
    private fun startMain() {
        Log.i(tag, "startMain:")

        if (WaSharedPreferences(this).readPrefer("show_permission")?.isEmpty()!!) {
            startActivity(Intent(this@SplashActivity, PermissionActivity::class.java))
        } else {
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        }
        // 좌
        //overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_left)
        // 우
        //overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_right)
        overridePendingTransition(R.anim.activity_fade, R.anim.activity_hold)
        finish()
    }

    /** 언어 체크 */
    private fun languageCheck() {
        Log.i(tag, "languageCheck:")
        //Log.d(tag, "@@@@@" + Strings.getString(this, locale).strings[""])
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                locale = getResources().getConfiguration().getLocales().get(0).getLanguage()
//            }
//            else {
//                locale = Resources.getSystem().getConfiguration().locale.getLanguage()
//            }
        // SharedPreferendces 확인 후 언어 정보 없을때 기기 언어 정보 셋팅
        if (WaSharedPreferences(this).readPrefer("language")?.isEmpty()!!) {
            val lang = Resources.getSystem().getConfiguration().locale.getLanguage()
            WaSharedPreferences(this).writePrefer("language", lang)
            Log.d(tag, "최초 1회 언어 셋팅 : ${lang}")
        }
        val deviceLang = Resources.getSystem().getConfiguration().locale.getLanguage()
        locale = WaSharedPreferences(this).readPrefer("language").toString()
        Log.d(tag, "기기 언어 : ${deviceLang}, 사용자 언어 : ${locale}")

        // 앱 언어 셋팅
        if(!setLanguageFlag) {
            setLanguage(locale)
            setLanguageFlag = true
            Log.d(tag, "setLanguageFlag true")
        }

        //requestVersionCheck()

        if (bTestPage == false) {
            processMain()
        }
    }

    /** 앱버전 체크 */
    private fun requestVersionCheck() {
        Log.i(tag, "requestSaveDevice:")
        if (!WaUtils.checkNetworkState(context)) {
            alertNetworkError()
            return
        }

        // TODO API 파라메터 설정
        val jsonData = JSONObject()
        jsonData.put("device_uniqueKey", "A-${WaUtils.getUUID(context)}")
        jsonData.put("device_type", Build.MODEL)
        jsonData.put("device_os", "AOS-${Build.VERSION.SDK_INT}")
        jsonData.put("app_version", WaUtils.getVersionInfo(context))
        jsonData.put("app_os", "aos")
        jsonData.put("lang", locale)

        Log.i(tag, "jsonData:$jsonData")

        val paramReqSeq: RequestBody = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            jsonData.toString()
        )

        RetrofitClient.getService().appVersionCheck(paramReqSeq)
            .enqueue(object : Callback<RtfModel.AppVersionCheck> {
                override fun onFailure(call: Call<RtfModel.AppVersionCheck>, t: Throwable) {
                    Log.e(tag, "onFailure: t:${t.message}")
                    processMain()
                }

                override fun onResponse(
                    call: Call<RtfModel.AppVersionCheck>,
                    response: Response<RtfModel.AppVersionCheck>
                ) {
                    Log.i(tag, "onResponse: response:${response}")
                    //Log.i(tag, "onResponse:${response.isSuccessful}")
                    if (response.isSuccessful) {
                        val dat = response.body()
                        Log.i(tag, "dat:$dat")
                        if (dat!!.status == "OK") {

                            val serverVersion = dat.result.app_version
                            val updateType = dat.result.app_update_type
                            //val appSize = dat?.result?.app_version_size

                            val app = WaUtils.getVersionInfo(context)
                            if (WaUtils.compareVersion(app!!, serverVersion)) {
                                // 서버 버전이 높은 경우
                                when (updateType) {
                                    "U03" -> {
                                        // U03 : 강제 업데이트
                                        goForceMarket(context, true)
                                    }
                                    "U02" -> {
                                        // U02 : 권장 업데이트
                                        goForceMarket(context, false)
                                        WaSharedPreferences(context).writePrefer(
                                            "version_update",
                                            "Y"
                                        )
                                    }
                                    "U01" -> {
                                        // U01 : 사용안함
                                        processMain()
                                    }
                                }
                            } else {
                                processMain()
                            }
                        } else {
                            processMain()
                        }
                    } else {
                        processMain()
                    }
                }
            })
    }

    /** 플레이스토어 이동 */
    fun goForceMarket(context: Context, update: Boolean) {
        Log.i(tag, "goForceMarket:")
        if (update) {
            val title = this.getString(R.string.common_update_desc_1)
            val strOk = this.getString(R.string.common_update)
            WaUtils.showAlertDialog(this, WaUtils.TYPE_ALERT_OK, title, "", strOk, "",
                {
                    WaUtils.goMarket(context)
                    finish()
                },
                {
                })
        } else {
            val title = this.getString(R.string.common_update_desc_2)
            val strOk = this.getString(R.string.common_update)
            val strCancel = this.getString(R.string.common_confirm)
            WaUtils.showAlertDialog(this, WaUtils.TYPE_ALERT_OKCANCEL, title, "", strOk, strCancel,
                {
                    WaUtils.goMarket(context)
                    finish()
                },
                {
                    processMain()
                })
        }
    }

    /** 네트워크 오류발생시 알림 */
    private fun alertNetworkError() {
        Log.i(tag, "alertNetworkError:")

        val title = this.getString(R.string.common_network_error)
        WaUtils.showAlertDialog(this, WaUtils.TYPE_ALERT_OK, title, "", "확인", "취소",
            {
                requestVersionCheck()
            },
            {
            })

    }

}