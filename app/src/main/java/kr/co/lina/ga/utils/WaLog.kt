package kr.co.lina.ga.utils

import android.util.Log
import kr.co.lina.ga.WaConfig

/**
 * 로그 처리 유틸
 * @property debug 웹 디버그 모드 설정
 */
object WaLog {

    val debug = WaConfig.WEB_DEBUG

    /**
     * 경고 로그
     * @param tag
     * @param msg
     */
    fun w(tag: String, msg: String) {
        if (debug)
            Log.w(tag, msg)
    }

    /**
     * 상세 로그
     * @param tag
     * @param msg
     */
    fun v(tag: String, msg: String) {
        if (debug)
            Log.v(tag, msg)
    }

    /**
     * 정보 로그
     * @param tag
     * @param msg
     */
    fun i(tag: String, msg: String) {
        if (debug)
            Log.i(tag, msg)
    }

    /**
     * 디버그 로그
     * @param tag
     * @param msg
     */
    fun d(tag: String, msg: String) {
        if (debug)
            Log.d(tag, msg)
    }

    /**
     * 에러 로그
     * @param tag
     * @param msg
     */
    fun e(tag: String, msg: String) {
        if (debug)
            Log.e(tag, msg)
    }
}