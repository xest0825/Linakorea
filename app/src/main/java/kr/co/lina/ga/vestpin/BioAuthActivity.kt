package kr.co.lina.ga.vestpin

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_bio_auth.*
import kr.co.lina.ga.R
import kr.co.lina.ga.databinding.ActivityMainBinding
import java.util.concurrent.Executor

class BioAuthActivity : AppCompatActivity() {

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bio_auth)

        //하드웨어가 생체인식 기능을 지원하는지 체크
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG)) {    // or DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS ->
                //Toast.makeText(this,"App can authenticate using biometrics.",Toast.LENGTH_SHORT).show()
                textStatus.setText("인증 가능")
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                //Toast.makeText(this,"No biometric features available on this device.",Toast.LENGTH_SHORT).show()
                textStatus.setText("HW 미지원")
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                //Toast.makeText(this,"Biometric features are currently unavailable.",Toast.LENGTH_SHORT).show()
                textStatus.setText("생체 인식 기능은 현재 사용할 수 없습니다.")
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                //Toast.makeText(this,"The device does not have any biometric credentials",Toast.LENGTH_SHORT).show()
                textStatus.setText("기기에 생체 인증 정보가 없음")
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED ->
                //Toast.makeText(this,"A security vulnerability has been discovered and the sensor is unavailable until a security update has addressed this issue.",Toast.LENGTH_SHORT).show()
                textStatus.setText("보안 취약점이 발견되었으며 보안 업데이트가 이 문제를 해결할 때까지 센서를 사용할 수 없습니다")
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED ->
                //Toast.makeText(this,"A given authenticator combination is not supported by the device.",Toast.LENGTH_SHORT).show()
                textStatus.setText("지정된 인증자 조합이 장치에서 지원되지 않습니다.")
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN ->
                //Toast.makeText(this,"Unable to determine whether the user can authenticate.",Toast.LENGTH_SHORT).show()
                textStatus.setText("상태 이상")
        }

        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    //Toast.makeText(this,errString.toString(),Toast.LENGTH_SHORT).show()
                    textStatus.setText(errString.toString())
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    //Toast.makeText(this,"Authentication Success",Toast.LENGTH_SHORT).show()
                    textStatus.setText("인증 성공")
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    //Toast.makeText(this,"Authentication Failed",Toast.LENGTH_SHORT).show()
                    textStatus.setText("인증 실패")
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for my app")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(BIOMETRIC_STRONG)
            .build()

        btnLogin.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }
    }
}