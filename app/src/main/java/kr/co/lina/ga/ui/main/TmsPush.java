package kr.co.lina.ga.ui.main;

import android.content.Context;
import android.os.Message;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.gson.JsonObject;
import com.tms.sdk.TMS;
import com.tms.sdk.ITMSConsts;
import com.tms.sdk.api.APIManager;
import com.tms.sdk.api.request.DeviceCert;
import com.tms.sdk.api.request.Login;
import com.tms.sdk.api.request.Logout;
import com.tms.sdk.api.request.SetConfig;
import com.tms.sdk.common.util.NotificationConfig;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import kr.co.lina.ga.R;
import kr.co.lina.ga.ServerUrls;
import kr.co.lina.ga.ui.push.NotificationClickReceiver;
import kr.co.lina.ga.ui.push.NotificationEventReceiver;

public class TmsPush {

    String FIREBASE_SENDER_ID = "106906648660";

    public Context tmsContext;

    void initTms(Context context) {
        //sdk setting
        tmsContext = context;

        TMS sdk = new TMS.Builder()
                .setDebugEnabled(true)
                .setDebugTag("TAG")
                .setPrivateEnabled(false)
                .setFirebaseSenderId(FIREBASE_SENDER_ID) // FCM 발신자 ID 등록
                .setServerAppKey(ServerUrls.APP_KEY)     // 앱키 등록
                .setServerUrl(ServerUrls.PUSH_URL) // MSG-API URL 등록
                .setNotificationConfig(
                        new NotificationConfig.Builder()
                                .setColor(ContextCompat.getColor(context, R.color.colorPush))
                                .setExpandable(true)
                                .setRing(true)
                                .setVibrate(true)
                                .setGroupable(true)
                                .setStackable(true)
                                .setWakeLockScreen(true)
                                //.setPopupActivity(MainActivity.class)
                                //.setShowPopupActivity(true)
                                //.setLargeIcon(R.drawable.ic_notification)
                                .setClickListener("clickAction", NotificationClickReceiver.class)
                                .setEventListener("eventAction", NotificationEventReceiver.class)
                                //.setActivityToMoveWhenClick("kr.co.lina.ga",MainActivity.class)
                                //.setActivityToMoveWhenClickWithBackStack("kr.co.lina.ga",MainActivity.class)
                                .setExpandablePlaceholder("두손가락을 이용해 아래로 당겨주세요.")
                                .setExpandable(true)
                                .create()
                )
                .build(context);

        doTmsDeviceCert(context);
    }

    // Device 등록
    void doTmsDeviceCert(Context context) {
        new DeviceCert(context).request(new APIManager.APICallback() {
            public void response(String code, JSONObject json) {
                if (ITMSConsts.CODE_SUCCESS.equals(code)) {
                    //${callback_process}
                    // to do ...
                    Log.d("TMS PUSH 1", code);

                    //doSetConfig(context);
                }
            }
        });
    }

    // setConfig
    void doSetConfig(Context context) {
        new SetConfig(context).request("Y", "Y", new APIManager.APICallback() {
            @Override
            public void response (String code, JSONObject json) {
                if(ITMSConsts.CODE_SUCCESS.equals(code)){
                    //${callback_process}
                    Log.d("TMS PUSH 2", code);
                }
            }
        });
    }

    public static void setTmsId(Context context, String custId) throws JSONException {

        // get : previous login id
        String getId = TMS.getInstance(context).getCustId();

        if (getId != "" && custId != getId) {
            new Logout(context).request(new APIManager.APICallback() {
                @Override
                public void response(String code, JSONObject json) {
                    //if (code.equals(ITMSConsts.CODE_SUCCESS)) {}   //응답이 성공일 경우
                    // logout의 결과에 관계없이 로그인 실행 !!!
                    doTmsLogin(context, custId);
                }
            });
        }
        else {
            doTmsLogin(context, custId);
        }
    }

    public static void doTmsLogin(Context context, String custId)  {

        // set new login id
        TMS.getInstance(context).setCustId(custId);

        // 로그인하기
        //String custId = TMS.getInstance(context).getCustId();
        //Toast.makeText(context, "getCustId = < " + custId + " > ", Toast.LENGTH_LONG).show();

        //JSONObject userData = new JSONObject();
        //userData.put("custName", "이정규");
        //userData.put("custPhone", "01058804063");

        new Login(context).request(custId, new APIManager.APICallback() {
            public void response(String code, JSONObject json) {
                if(ITMSConsts.CODE_SUCCESS.equals(code)){
                    // ${callback_process}
                    Log.d("TMS PUSH 3", code);
                }
                // RETURN !!!
                JsonObject jjson = new JsonObject();
                jjson.addProperty("result", "0000");
                String strJson = json.toString();

                String strReturn = "javascript:gnx_app_callback('setTmsId'," + strJson + ");";

                WebView webView = ((MainActivity)context).mWebView;
                String finalStrReturn = strReturn;
                webView.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        webView.loadUrl(finalStrReturn);
                    }
                });
            }
        });
    }
}
