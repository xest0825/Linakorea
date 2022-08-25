package kr.co.lina.ga.ui.permission

import android.content.Intent
import android.os.Bundle
import android.text.Html
import com.akexorcist.localizationactivity.core.OnLocaleChangedListener
import com.akexorcist.localizationactivity.ui.LocalizationActivity
import kr.co.lina.ga.R
import kr.co.lina.ga.ui.main.MainActivity
import kr.co.lina.ga.utils.WaLog
import kr.co.lina.ga.utils.WaSharedPreferences
import kotlinx.android.synthetic.main.activity_permission.*

/**
 * 접근 권한 안내 화면
 * @property tag Log 태그
 * @property Log Log
 * @property locale 언어 정보
 * layout : activity_permission.xml
 */
class PermissionActivity : LocalizationActivity(), OnLocaleChangedListener {
    val tag = "PermissionActivity"
    val Log = WaLog
    //private lateinit var locale: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)
        Log.i(tag, "onCreate:")

        //locale = WaSharedPreferences(this).readPrefer("language").toString()

        //permission_icon_bg_1
        permission_confirm.setOnClickListener {
            WaSharedPreferences(this).writePrefer("show_permission", "Y")
            startActivity(Intent(this@PermissionActivity, MainActivity::class.java))
            overridePendingTransition(R.anim.activity_fade, R.anim.activity_hold)
            finish()
        }
        permission_btn_close.setOnClickListener {
            WaSharedPreferences(this).writePrefer("show_permission", "Y")
            startActivity(Intent(this@PermissionActivity, MainActivity::class.java))
            overridePendingTransition(R.anim.activity_fade, R.anim.activity_hold)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.i(tag, "onResume:")
    }

}