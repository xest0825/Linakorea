package kr.co.lina.ga.vestpin;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import kr.co.lina.ga.R;

public class WebViewActivity extends AppCompatActivity {
    //private String authority = "com.yettiesoft.VPContentProvider";
    private String authority = "kr.co.lina.ga.VPContentProvider";
    private String serviceName = "gadev.lina.co.kr";    //"172.16.10.205:9090";
    private VP_AdapterWebView sample = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vestpin_webview);

        ConstraintLayout layout = findViewById(R.id.layout);
        String host = this.getIntent().getStringExtra("host");

        sample = new VP_AdapterWebView(WebViewActivity.this, layout, authority, serviceName, host);
        sample.setFingerprintCnt(3);
        sample.createVestPIN();

        String flag = getIntent().getStringExtra("flag");
        String id = getIntent().getStringExtra("id");
        String nickName = getIntent().getStringExtra("nickName");
        switch (flag) {
            case "register":
                register(id, nickName);
                break;
            case "auth":
                auth();
                break;
            case "reRegister":
                reRegister(id, nickName);
                break;
        }
    }

    private void register(String id, String nickName) {
        sample.register(id, nickName, new VP_AdapterWebView.Callback() {
            @Override
            public void onResponse(int errCode, String message) {
                showMessage(errCode, message);
                finishCustom();
            }
        });
        sample.setWebViewVisible();
    }

    private void auth() {
        sample.auth(new VP_AdapterWebView.Callback() {
            @Override
            public void onResponse(int errCode, String message) {
                showMessage(errCode, message);
                finishCustom();
            }
        });
        sample.setWebViewVisible();
    }

    private void reRegister(String id, String nickName) {
        sample.reRegister(id, nickName, new VP_AdapterWebView.Callback() {
            @Override
            public void onResponse(int errCode, String message) {
                showMessage(errCode, message);
                finishCustom();
            }
        });
        sample.setWebViewVisible();
    }

    private void showMessage(final int code, final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(WebViewActivity.this, "errorCode = " + code + ", message = " + message, Toast.LENGTH_LONG).show();
                Log.d("SAMPLE", "errorCode = " + code + ", message = " + message);
            }
        });
    }

    private void finishCustom() {
        finish();
        overridePendingTransition(R.anim.out_right, android.R.anim.fade_out);
    }

    @Override
    public void onBackPressed() {
        if(sample != null && sample.isWebViewVisible() && sample.canWebViewGoBack()) {
            sample.webViewGoBack();
        }else {
            super.onBackPressed();
        }
    }
}
