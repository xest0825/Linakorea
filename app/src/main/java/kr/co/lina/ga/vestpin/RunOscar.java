package kr.co.lina.ga.vestpin;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.webkit.WebView;

import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.yettiesoft.oscar.service.Oscar;
import com.yettiesoft.oscar.service.OscarCallback;
import com.yettiesoft.oscar.type.OSCARError;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import kr.co.lina.ga.ServerUrls;
import kr.co.lina.ga.WaConfig;
import kr.co.lina.ga.ui.main.MainActivity;

public class RunOscar {

    public static String oscarUrl = ServerUrls.OSCAR_URL;
    public static Oscar mOscar = null;

    private static int OSCAR_CONNECT = 0;
    private static int OSCAR_WRITE = 1;

    public static void connectOscar(@NotNull Context context, @NotNull String authCode) {

        // thread 새롭게 생성
        new Thread(new Runnable() {
            @Override
            public void run() {

                // 생성자 필수 요소 -  RETRY_CNT, WAIT_SECOND, SERVERURL
                HashMap<Oscar.OscarConfig, String> config = new HashMap<>();
                config.put(Oscar.OscarConfig.RETRY_CNT, "10");
                config.put(Oscar.OscarConfig.WAIT_SECOND, "3");
                config.put(Oscar.OscarConfig.SERVERURL, oscarUrl);

                // oscar 를 진행할 때는 mainThread 가 아닌 다른 thread 에서 진행해야한다. (connect, read, write)
                mOscar = new Oscar(context, config);

                // connect 필수 요소 - AUTHCODE, SERVERID
                HashMap<Oscar.OscarConfig, String> data = new HashMap<>();
                data.put(Oscar.OscarConfig.AUTHCODE, authCode);
                data.put(Oscar.OscarConfig.SERVERID, "oscarSVRID_test");

                Log.e("---> OSCAR-AUTHCODE", authCode);
                Log.e("---> OSCAR-SERVERURL", oscarUrl);
                mOscar.connect(data, new OscarCallback() {
                    @Override
                    public void onError(int errorCode, String errorMessage) {
                        //errorFinish(errorMessage, dialog);
                        Log.d("Error", "CONNECT error");
                        onRespOscar(OSCAR_CONNECT, context, errorCode, errorMessage);
                    }

                    @Override
                    public void onSuccess(String result) {
                        Log.d("SUCCESS", "CONNECT");

                        //---------------------------------------------------
                        // CONNECT 성공시 READ 를 통해 SIGN 할 PLAIN 값 받아오기
                        //---------------------------------------------------
                        mOscar.read(new OscarCallback() {
                            @Override
                            public void onError(int errorCode, String errorMessage) {
                                //errorFinish(errorMessage, dialog);
                                Log.d("Error", "Read error");
                                onRespOscar(OSCAR_CONNECT, context, errorCode, errorMessage);
                            }

                            @Override
                            public void onSuccess(String result) {
                                Log.d("SUCCESS", "result = " + result);
                                onRespOscar(OSCAR_CONNECT, context, 0, result);
                            }
                        });
                    }
                });
            }
        }).start();
    }

    public static void writeOscar(@NotNull Context context, @NotNull String value) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HashMap<Oscar.OscarConfig, String> data = new HashMap<>();
                data.put(Oscar.OscarConfig.WRITE_DATA, value);
                mOscar.write(data, new OscarCallback() {
                    @Override
                    public void onError(int errorCode, String errorMessage) {
                        //errorFinish(errorMessage, dialog);
                        Log.d("Error", "WRITE error");
                        onRespOscar(OSCAR_WRITE, context, errorCode, errorMessage);
                    }

                    @Override
                    public void onSuccess(String result) {
                        Log.d("SUCCESS", "success");

                        onRespOscar(OSCAR_WRITE, context, 0, result);

                        // OSCAR 를 끝냈을 경우 CLOSE 를 통해 종료
                        int res = mOscar.close();

                        if(res != 0) {
                            //errorFinish(OSCARError.getErrorCode(res).getMessage(), dialog);
                            Log.d("Error", "Close error");
                        } else {
                            Log.d("SUCCESS", "WRITE");
                        }
                    }
                });
            }
        }).start();
    }

    public static void onRespOscar (int type, Context context, int err, String result) {
        // 받아온 PLAIN 값을 VestPIN SIGN 수행을 위해 서버로 전달!!
        String errCode = Integer.toString(err);

        JsonObject json = new JsonObject();
        json.addProperty("result", errCode);
        json.addProperty("message", result);
        String strJson = json.toString();

        String strReturn = "";

        if (type == OSCAR_CONNECT) {
            strReturn = "javascript:gnx_app_callback('connectOscar'," + strJson + ");";
        }
        else if (type == OSCAR_WRITE) {
            strReturn = "javascript:gnx_app_callback('writeOscar'," + strJson + ");";
        }

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
}

