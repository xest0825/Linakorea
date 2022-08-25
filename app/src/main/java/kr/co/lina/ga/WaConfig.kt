package kr.co.lina.ga

object WaConfig {

    /**
     * 메인 화면 연결
     */
    //private const val MAIN_URL = BuildConfig.server_url
    //var MAIN_URL = BuildConfig.server_url     //const val

    /**
     * 메인 URL 설정 정의
     */
//    const val URL_MAIN = "${MAIN_URL}/front/main.go"
    var URL_MAIN = "${ServerUrls.MAIN_URL}/front/test.go"  //const val
    var URL_API = ServerUrls.MAIN_URL                    //const val

    /**
     * 웹뷰 브릿지 호출
     */
    const val WEB_BRIDGE_NAME = "GA"

    /**
     * 웹뷰 유저에이전트 설정
     */
    const val WEB_USER_AGENT= "appaos"

    /**
     * 빌드 디버그 설정
     */
    val WEB_DEBUG = BuildConfig.DEBUG_WEB
}