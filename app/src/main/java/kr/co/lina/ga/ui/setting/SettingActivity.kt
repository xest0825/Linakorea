package kr.co.lina.ga.ui.setting

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.akexorcist.localizationactivity.core.OnLocaleChangedListener
import com.akexorcist.localizationactivity.ui.LocalizationActivity
import kr.co.lina.ga.R
import kr.co.lina.ga.utils.WaLog
import kr.co.lina.ga.utils.WaSharedPreferences

/**
 * 설정 화면
 * @property tag Log 태그
 * @property Log Log
 * @property locale 언어 정보
 * layout : activity_setting.xml
 */
class SettingActivity : LocalizationActivity(), OnLocaleChangedListener {

    val tag = "SettingActivity"
    val Log = WaLog
    private lateinit var locale: String
    private lateinit var setting_back:Button
    private lateinit var notification_box_title:TextView
    private lateinit var settingHome:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        Log.i(tag, "onCreate")

        setting_back = findViewById(R.id.setting_back)
        notification_box_title = findViewById(R.id.notification_box_title)
        settingHome = findViewById(R.id.settingHome)
        locale = WaSharedPreferences(this).readPrefer("language").toString()

//        setting_back.setText(Strings.getString(this, locale).get("setting_back"))
//        notification_box_title.setText(Strings.getString(this, locale).get("setting_title"))
//        settingHome.setText(Strings.getString(this, locale).get("setting_home"))
        setting_back.setText(this.getString(R.string.setting_back))
        notification_box_title.setText(this.getString(R.string.setting_title))
        settingHome.setText(this.getString(R.string.setting_home))

    }

    override fun onResume() {
        super.onResume()
        Log.i(tag, "onResume:")
    }

}