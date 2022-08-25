package kr.co.lina.ga.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * 앱내 저장소 관리
 * @param context
 * @property tag Log 태그
 * @property Log Log
 * @property FILE_NAME 파일명
 * @property prefer SharedPreferences
 */
class WaSharedPreferences(context: Context) {

    private val tag = "WaSharedPreferences"
    private val Log = WaLog

    private val FILE_NAME = "prefer"
    private var prefer: SharedPreferences? = null

    /** 초기화 */
    init {
        prefer = context.getSharedPreferences(FILE_NAME, 0)
    }

    /**
     *  SharedPreferences 값 쓰기
     *  @param key
     *  @param value
     */
    fun writePrefer(key: String, value: String) {
        prefer?.edit()?.putString(key, value)?.apply()
    }

    /**
     * SharedPreferences 값 읽기
     * @param key
     * @return String
     */
    fun readPrefer(key: String): String? {
        val value = prefer?.getString(key, "")
        return value
    }
}