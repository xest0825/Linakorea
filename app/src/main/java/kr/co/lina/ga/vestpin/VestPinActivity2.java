package kr.co.lina.ga.vestpin;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import com.yettiesoft.vestpin.exception.VFException;
import com.yettiesoft.vestpin.service.VestPinLibImpl;

import java.util.HashMap;

import kr.co.lina.ga.R;
import kr.co.lina.ga.ServerUrls;

public class VestPinActivity2 extends AppCompatActivity {

    //private String baseUrl = "http://172.16.10.205:8080/POC/html";
    //private String url = "https://www.html5cert.kr/vestpin_mobile/sample2/index_WV.html";
    private String url =  ServerUrls.VESTPIN2_URL;

    private WebView mWebView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vest_pin2);

        Log.d("VestPIN", "VestPIN version is " + VestPinLibImpl.getVersion());

        mWebView = (WebView)this.findViewById(R.id.webView);
        String authority = "kr.co.lina.ga.VPContentProvider";

        HashMap<String, String> options = new HashMap<>();
        options.put("FINGERPRINT_CNT", "3");
        try {
            Activity context = this;
            VestPinLibImpl webView = new VestPinLibImpl(context, mWebView, authority, options);

            webView.webViewInit();

//            if (url.equals(""))
//                webView.setUrl(baseUrl);
//            else
                webView.setUrl(url);

//            HashMap<VestPinLibImpl.CustomConfig, Integer> config = new HashMap<>();
//            config.put(VestPinLibImpl.CustomConfig.XML_LAYOUT_ID, R.layout.custom_biometric);
//            config.put(VestPinLibImpl.CustomConfig.LAYOUT_ID, R.id.customLinearLayout);
//            config.put(VestPinLibImpl.CustomConfig.IMAGEVIEW_ID, R.id.fingerprint_custom_icon);
//            config.put(VestPinLibImpl.CustomConfig.TEXTVIEW_ID, R.id.fingerprint_description);
//            config.put(VestPinLibImpl.CustomConfig.CANCEL_BTN_ID, R.id.cancel_button);
//            boolean res = webView.setCustomUI(config);
//            if(!res) {
//                Log.d("CUSTOM", "CUSTOM FAIL");
//            }

            webView.loadFin();
        } catch (VFException e) {
            Toast.makeText(getApplicationContext(),"error is " + e.getErrorMessage() + ".",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        if(mWebView.canGoBack())
            mWebView.goBack();
        else {
            this.finish();
        }
    }
}