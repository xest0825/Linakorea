package kr.co.lina.ga.vestpin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import kr.co.lina.ga.R;
import kr.co.lina.ga.ServerUrls;

public class VestPinActivity extends AppCompatActivity {
    //private String authority = "com.yettiesoft.VPContentProvider";
    private String authority = "kr.co.lina.ga.VPContentProvider";
    private String serviceName = "gadev.lina.co.kr";
    private VP_AdapterWebView sample = null;
    private String host = ServerUrls.VESTPIN1_URL;
    private Button register = null;
    private Button auth = null;
    private Button delete = null;
    private Intent webViewIntent = new Intent(VestPinActivity.this, WebViewActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vestpin);

        register = findViewById(R.id.register);
        auth = findViewById(R.id.auth);
        delete = findViewById(R.id.delete);

        auth.setOnClickListener(authEvent());
        delete.setOnClickListener(deleteEvent());

        ConstraintLayout layout = findViewById(R.id.individualLayout);
        //host = this.getIntent().getStringExtra("url");

        webViewIntent = new Intent(VestPinActivity.this, WebViewActivity.class);
        webViewIntent.putExtra("host", host);

        sample = new VP_AdapterWebView(VestPinActivity.this, layout, authority, serviceName, host);
        sample.setFingerprintCnt(3);
        sample.createVestPIN();
    }

    @Override
    public void onResume(){
        super.onResume();
        if(sample != null && sample.isWebViewVisible()) {
            sample.setWebViewGone();
        }

        int num = sample.getUserNum();

        if(num == -1) {
            register.setVisibility(View.GONE);
            auth.setVisibility(View.GONE);
            delete.setVisibility(View.GONE);

            Toast.makeText(VestPinActivity.this, "error", Toast.LENGTH_LONG).show();
        } else if(num == 0) {
            register.setVisibility(View.VISIBLE);
            auth.setVisibility(View.GONE);
            delete.setVisibility(View.GONE);

            register.setText("등록");

            register.setOnClickListener(registerEvent());
        } else {
            register.setVisibility(View.VISIBLE);
            auth.setVisibility(View.VISIBLE);
            delete.setVisibility(View.VISIBLE);

            register.setText("재등록");
            auth.setText("인증");
            delete.setText("삭제");

            register.setOnClickListener(reRegisterEvent());
        }
    }

    public View.OnClickListener registerEvent() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity("register", "1313", "yettie");
            }
        };
    }

    public View.OnClickListener authEvent() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity("auth", null, null);
            }
        };
    }

    public View.OnClickListener deleteEvent() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sample.remove(new VP_AdapterWebView.Callback() {
                    @Override
                    public void onResponse(int errCode, String message) {
                        showMessage("errorCode = " + errCode + ", message = " + message);
                    }
                });
                sample.setWebViewGone();
            }
        };
    }

    public View.OnClickListener reRegisterEvent() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity("reRegister", "1111", "yettie");
            }
        };
    }

    private void showMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(VestPinActivity.this, message, Toast.LENGTH_LONG).show();
                Log.d("SAMPLE", message);
                onResume();
            }
        });
    }

    private void startActivity(String flag, String id, String nickName) {
        webViewIntent.putExtra("flag", flag);
        if(id != null) webViewIntent.putExtra("id", id);
        if(nickName != null) webViewIntent.putExtra("nickName", nickName);
        startActivity(webViewIntent);
        overridePendingTransition(R.anim.in_right, android.R.anim.fade_in);
    }
}
