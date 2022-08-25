package kr.co.lina.ga.ui.main

import android.app.ProgressDialog
import android.os.AsyncTask
import com.barun.appiron.android.AppIron
import com.barun.appiron.android.AppIronException
import com.barun.appiron.android.AppIronResult
import com.google.gson.JsonObject
import kr.co.lina.ga.ServerUrls

object AppAuth {
    fun authApp() {
        var aAppIronTask = appIronTask()
        aAppIronTask.execute()
    }
}

class appIronTask() : AsyncTask<Void, Void, AppIronResult>() {

    // AppIron (앱 위변조) : 앱아이언 검증서버 url
    //const val APPIRON_AUTH_CHECK_URL: String = "http://inspect.appiron.com/authCheck.call"
    //val APPIRON_AUTH_CHECK_URL: String = "http://devapp.lina.co.kr:8888/authCheck.call"
    val APPIRON_AUTH_CHECK_URL: String = ServerUrls.APPIRON_URL
    val APPIRON_AUTH_DBL_CHECK_URL: String = "http://inspect.appiron.com/authDoubleCheck.jsp"

    private lateinit var mProgressDialog: ProgressDialog
    private var mAppIronException: AppIronException? = null

    override fun onPreExecute() {
        /*---------------------------------------------------------------------------*
         * 진행 다이얼로그 보이기
         *---------------------------------------------------------------------------*/
        //mProgressDialog = ProgressDialog(MainActivity.mainActivity())
        //mProgressDialog.setMessage("무결성 검증 중입니다.\n잠시만 기다려 주세요...!")
        //mProgressDialog.show()
    }

    override fun doInBackground(vararg p0: Void?): AppIronResult? {
        try {
            /*---------------------------------------------------------------------------*
             * 1차 무결성 검증 수행
             *---------------------------------------------------------------------------*/
            var aAppIron = AppIron.getInstance(MainActivity.mainActivity())
            return aAppIron.authenticateApp(MainActivity@APPIRON_AUTH_CHECK_URL, 30)
        } catch (pException: AppIronException) {
            pException.printStackTrace()
            mAppIronException = pException
            return null
        }
    }

    override fun onPostExecute(pAppIronResult: AppIronResult?) {
        var errCode = ""
        var errMessage = ""
        var sessionId = ""
        var tokenId = ""

        /*---------------------------------------------------------------------------*
         * 진행 다이얼로그 닫기
         *---------------------------------------------------------------------------*/
        //mProgressDialog.dismiss()

        /*---------------------------------------------------------------------------*
         * 1차 무결성 검증 에러 처리
         *---------------------------------------------------------------------------*/
        if (mAppIronException != null) {
            errCode = mAppIronException!!.errorCode.toString()
            errMessage = mAppIronException!!.errorMessage

            /*---------------------------------------------------------------------------*
             * 11XX 는 네트워크 에러이므로 다시 접속하도록 구성한다.
             *---------------------------------------------------------------------------*/
//            var aResultMessage = StringBuffer()
//            if (mAppIronException!!.errorCode / 100 == 11) {
//                aResultMessage.append("네트워크에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.")
//            }
//            else if (mAppIronException!!.errorCode == 8000) {
//                aResultMessage.append("등록되지 않은 앱입니다.")
//            }
//            else if (mAppIronException!!.errorCode == 9001) {
//                aResultMessage.append("앱이 위변조되었습니다.")
//            }
//            else if (mAppIronException!!.errorCode == 9002) {
//                aResultMessage.append("루팅한 장치에서 앱이 실행되었습니다.")
//            }
//            else if (mAppIronException!!.errorCode == 9003) {
//                aResultMessage.append("라이브러리가 위변조되었습니다.")
//            }
//            else {
//                aResultMessage.append("앱이 위변조되었습니다.")
//            }
//
//            aResultMessage.append(String.format("\n[%d][%s]",
//                mAppIronException!!.errorCode,
//                mAppIronException!!.errorMessage))

            /*---------------------------------------------------------------------------*
             * 에러 출력
             *---------------------------------------------------------------------------*/
//            var aAlert = AlertDialog.Builder(MainActivity.mainActivity())
//            aAlert.setTitle("[무결성 검증 실패]");
//            aAlert.setMessage(aResultMessage.toString())
//            aAlert.setCancelable(false)
//            aAlert.setPositiveButton("close", null)
//            aAlert.show()
        }
        else {
            /*---------------------------------------------------------------------------*
             * 1차 무결성 검증의(클라이언트 대 서버 간 검증) 결과값이 정상인 경우, 발급된 세션아이디와 토큰을 가져와
             * 2차 무결성 검증 시(서버 대 서버 간 검증) 사용한다.
             *---------------------------------------------------------------------------*/
//            var aAlert = AlertDialog.Builder(MainActivity.mainActivity())
//            aAlert.setTitle("[무결성 검증 성공]");
//            if (pAppIronResult != null) {
//                aAlert.setMessage(
//                    String.format(
//                        "세션아이디 [%s]\n토큰 [%s]",
//                        pAppIronResult.sessionId, pAppIronResult.token
//                    )
//                )
//            }
//            aAlert.setCancelable(false)
//            aAlert.setPositiveButton("close", null)
//            aAlert.show()

            errCode = "0000"
            if (pAppIronResult != null) {
                sessionId = pAppIronResult.sessionId
                tokenId = pAppIronResult.token
            }
        }

        // send errCode, errMessage, sessionId, tokenId
        //
        //

        val context = MainActivity.mainActivity() as MainActivity
        val webview = context.mWebView
        val json = JsonObject()
        if (true) {
            json.addProperty("errCode", errCode)
            json.addProperty("errMsg", errMessage)
            json.addProperty("sessionId", sessionId)
            json.addProperty("tokenId", tokenId)
            json.addProperty("url", "10.2.13.65")   // for Test
        }
        else {
            json.addProperty("errCode", "0000")
            json.addProperty("errMsg", "Success")
            json.addProperty("sessionId", "lina-sessionId-lina-sessionId")
            json.addProperty("tokenId", "lina-token-lina-token")
        }
        val data = json.toString()

        var returnStr = "javascript:gnx_app_callback('runAppIron',"+ data + ");"
        webview?.post{
            webview?.loadUrl(returnStr)
        }
    }
}