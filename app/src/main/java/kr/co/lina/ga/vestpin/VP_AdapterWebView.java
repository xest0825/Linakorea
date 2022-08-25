package kr.co.lina.ga.vestpin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.http.SslError;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yettiesoft.vestpin.exception.VFException;
import com.yettiesoft.vestpin.service.VestPinLibImpl;

import java.util.HashMap;

import kr.co.lina.ga.WaConfig;

public class VP_AdapterWebView {
    private String authority;
    private String serviceName;
    private Activity context;
    private WebView webView;
    private String host;
    private HashMap<String, String> options = new HashMap<>();
    //private String jsPrefix = "sample";  // 수정 가능
    private String jsPrefix = WaConfig.WEB_BRIDGE_NAME;    // 수정 가능
    private String callbackName = jsPrefix + ".vestPinResponse";       // 수정 가능
    private Callback callback = null;
    private VestPinLibImpl vestPin = null;

    public interface Callback {
        void onResponse(int errCode, String message);
    }

    /*
    개별 기능 테스트를 위한 생성자, 해당 객체 생성시 WebView 도 같이 생성
    activity - 해당 샘플을 적용할 Activity
    layout - 위의 activity 에서 생성한 ConstraintLayout
    authority - AndroidManifest.xml 에서 설정한 provider authority
    serviceName - 해당 vestPin 에서 사용하고 있는 service name
    host - 해당 vestPin 을 적용한 web url
     */
    public VP_AdapterWebView(Activity activity, ConstraintLayout layout, String authority, String serviceName, String host) {
        this.context = activity;
        this.authority = authority;
        this.serviceName = serviceName;
        this.host = host;
        createWebView(layout);
    }

    /*
    **********필수 API**********
    지문의 최대 시도 횟수 설정
    maxCnt - 최대 횟수(5회까지 가능)
     */
    public void setFingerprintCnt(int maxCnt) {
        options.put("FINGERPRINT_CNT", String.valueOf(maxCnt));
    }

    /*
    **********필수 API**********
    VestPIN 객체를 생성
    해당 API 를 호출하기 전에 setFingerPrintCnt API 를 호출해야 함
    return 객체 생성 성공 여부
     */
    public boolean createVestPIN() {
        if(options.size() == 0) {
            return false;
        }

        try {
            vestPin = new VestPinLibImpl(context, webView, authority, options);
            webView.addJavascriptInterface(vestPin, "vestfin");
            webView.addJavascriptInterface(this, jsPrefix);
        } catch (VFException e) {
            return false;
        }

        return true;
    }

    /*
    지문 등록 webView 생성
    해당 webView 에 대한 추가 설정은 관련 API 를 사용하여 따로 설정해야 함
    (최초 webView 생성시 visibility 는 invisible 로 설정)
    id - 사용자 id
    nickName - 사용자 nickName
    callback - 해당 API 에 대한 결과 값을 받기 위한 callback
     */
    public void register(String id, String nickName, Callback callback) {
        this.callback = callback;
        String url = host + "sgi/reg.html";
        String apiName = "register('" + id + "', '" + nickName + "', '" + callbackName + "')";

        loadWebView(url, apiName);
    }

    /*
    지문 인증 webView 생성
    해당 webView 에 대한 추가 설정은 관련 API 를 사용하여 따로 설정해야 함
    (최초 webView 생성시 visibility 는 invisible 로 설정)
    callback - 해당 API 에 대한 결과 값을 받기 위한 callback
     */
    public void auth(Callback callback) {
        try {
            this.callback = callback;
            String ul = vestPin.getUserList(serviceName);
            if (ul != null) {
                JsonParser parser = new JsonParser();
                JsonArray jsonArray = (JsonArray) parser.parse(ul);

                JsonObject object = (JsonObject) jsonArray.get(0);
                String attributes = object.get("attributes").getAsString();
                JsonObject attr = (JsonObject) parser.parse(attributes);
                String authType = attr.get("authType").getAsString();
                String hrid = object.get("HRID").getAsString();

                String url = host + "sgi/auth.html";
                String apiName = "authenticate('" + hrid + "', '" + authType + "', '" + callbackName + "')";

                loadWebView(url, apiName);
            }
        } catch (VFException e) {
            callback.onResponse(e.getErrorCode(), e.getErrorMessage());
        }
    }

    /*
    지문 삭제 webView 생성
    해당 webView 에 대한 추가 설정은 관련 API 를 사용하여 따로 설정해야 함
    (최초 webView 생성시 visibility 는 invisible 로 설정)
    callback - 해당 API 에 대한 결과 값을 받기 위한 callback
     */
    public void remove(Callback callback) {
        this.callback = callback;
        String url = host + "sgi/remove.html";
        String apiName = "remove('" + callbackName + "')";

        loadWebView(url, apiName);
    }

    /*
    지문 재등록 webView 생성
    해당 webView 에 대한 추가 설정은 관련 API 를 사용하여 따로 설정해야 함
    (최초 webView 생성시 visibility 는 invisible 로 설정)
    id - 사용자 id
    nickName - 사용자 nickName
    callback - 해당 API 에 대한 결과 값을 받기 위한 callback
     */
    public void reRegister(String id, String nickName, Callback callback) {
        this.callback = callback;
        String url = host + "sgi/rereg.html";
        String apiName = "reregister('" + id + "', '" + nickName + "', '" + callbackName + "')";

        loadWebView(url, apiName);
    }

    /*
    webView load API
     */
    private void loadWebView(final String url, final String name) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String _url) {
                        if(_url.equals(url)) {
                            webView.loadUrl("javascript:" + name + ";");
                            Log.d("WEBVIEW", "CALL Javascript, " + name);
                        }
                    }

                    @Override
                    public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
                        final AlertDialog.Builder builder;

                        builder = new AlertDialog.Builder(context);
                        builder.setMessage("이 사이트의 보안 인증서는 신뢰할 수 없습니다.\n");
                        builder.setPositiveButton("continue", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                handler.proceed();
                            }
                        });
                        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                handler.cancel();
                            }
                        });
                        final AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });

                webView.loadUrl(url);
            }
        });
    }

    /*
    webView 로부터 결과값 받아오는 API
     */
    @JavascriptInterface
    public void vestPinResponse(String response) {
        JsonObject res = (JsonObject) new JsonParser().parse(response);
        JsonElement element = res.get("errCode");
        int errCode = -1;
        if(element != null) {
            errCode = res.get("errCode").getAsInt();
        }

        String errMsg = null;
        if(errCode == -1) {
            errMsg = "return value is invalid.";
        } else if(errCode != 0) {
            element = res.get("errMsg");
            if(element != null) {
                errMsg = res.get("errMsg").getAsString();
            }
        } else {
            element = res.get("userId");
            if(element != null) {
                errMsg = element.getAsString();
            }
        }

        this.callback.onResponse(errCode, errMsg);
    }

    /*
    등록된 사용자의 수를 가져옴
    return 등록된 사용자 수
     */
    public int getUserNum() {
        if(vestPin == null) {
            return -1;
        }

        int result = 0;
        try {
            String res = vestPin.getUserList(serviceName);

            // res 가 null일 경우 user = 0
            if (res != null) {
                JsonParser parser = new JsonParser();
                JsonArray jsonArray = (JsonArray) parser.parse(res);

                result = jsonArray.size();
            }
        } catch (VFException e) {
            result = -1;
        }

        return result;
    }

    /*
    webView 에 대한 visibility 를 invisible 로 설정
     */
    public void setWebViewGone() {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webView.setVisibility(View.GONE);
            }
        });
    }

    /*
    webView 에 대한 visibility 를 visible 로 설정
     */
    public void setWebViewVisible() {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webView.setVisibility(View.VISIBLE);
            }
        });
    }

    /*
    webView 의 뒤로갈 페이지의 여부 반환
    return 뒤로갈 페이지가 있을 경우 true, 없으면 false
     */
    public boolean canWebViewGoBack() {
        return webView.canGoBack();
    }

    /*
    webView 의 뒤로가기 호출
     */
    public void webViewGoBack() {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webView.goBack();
            }
        });
    }

    /*
    webView 에 대한 visibility 정보 반환
    return visible 이면 true, invisible 이면 false
     */
    public boolean isWebViewVisible() {
        return webView.getVisibility() == View.VISIBLE;
    }

    /*
    생성한 webView 를 반환
    return webView
     */
    public WebView getWebView() {
        return webView;
    }

    /*
    destroy webView
     */
    private void destroyWebView() {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(webView != null) {
                    ((ViewGroup)webView.getParent()).removeView(webView);
                    webView.destroy();
                    webView = null;
                }
            }
        });
    }

    /*
    webView 생성과 설정후 해당 constraintLayout 에 webView 추가
     */
    private void createWebView(final ConstraintLayout layout) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webView = new WebView(context);
                ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT);
                webView.setLayoutParams(layoutParams);
                webView.setVisibility(View.GONE);
                webView.getSettings().setJavaScriptEnabled(true);
                webView.getSettings().setDomStorageEnabled(true);
                webView.getSettings().setDatabaseEnabled(true);
                webView.getSettings().setUseWideViewPort(false);
                webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
                webView.getSettings().setSupportMultipleWindows(true);
                webView.setWebChromeClient(new WebChromeClient() {
                    @Override
                    public boolean onJsAlert(WebView view, String url, String message, final android.webkit.JsResult result)
                    {
                        new AlertDialog.Builder(context)
                                .setTitle("")
                                .setMessage(message)
                                .setPositiveButton(android.R.string.ok,
                                        new AlertDialog.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                result.confirm();
                                            }
                                        })
                                .setCancelable(false)
                                .create()
                                .show();

                        return true;
                    };
                });

                webView.setWebContentsDebuggingEnabled(true);

                layout.addView(webView);
            }
        });
    }

    @Override
    public void finalize() throws Throwable {
        destroyWebView();
        super.finalize();
    }
}
