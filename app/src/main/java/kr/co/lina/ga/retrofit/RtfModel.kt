package kr.co.lina.ga.retrofit

/**
 * HTTP 통신 모델
 */
class RtfModel {

    /**
     * 버전 확인
     * @param msg 메시지
     * @param status 상태
     * @param result 결과
     */
    data class AppVersionCheck(
        val msg: String = "",
        val status: String = "",
        val result: AppVersionCheckResultData
    )

    /**
     * 버전 데이타 확인
     * @param app_version 앱버전
     * @param app_version_size 앱버전 크기
     * @param app_update_type 업데이트 타입
     */
    data class AppVersionCheckResultData(
        val app_version: String = "",
        val app_version_size: String = "",
        val app_update_type: String = ""
    )

    /**
     * 파일 업로드
     * @param status 상태
     * @param msg 메시지
     * @param result 결과
     */
    data class UploadFiles(
        val status: String = "",
        val msg: String = "",
        val result: String = ""
    )
}