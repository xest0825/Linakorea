package kr.co.lina.ga.ui.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.JsonObject
import com.softsecurity.transkey.TransKeyActivity
import com.softsecurity.transkey.TransKeyCipher

object SecureText {

    private val tag = "SecureText"

    fun processSecureText(requestCode: Int, resultCode: Int, data: Intent?):String {
        var json = JsonObject()
        if (resultCode == AppCompatActivity.RESULT_CANCELED) {
//            if (data != null) {
//                var errMsg = data.getStringExtra(TransKeyActivity.mTK_PARAM_ERROR_MESSAGE)
//                if (errMsg == null) errMsg = ""
//                if (Global.debug) android.util.Log.e("Error", errMsg)
//                Toast.makeText(this, "ErrorMsg : $errMsg", Toast.LENGTH_LONG).show()
//            }
//            setDummy(onClickIndex, "")
//            TransKeyResultActivity.clearData(onClickIndex)
            json.addProperty("state","canceled")

            return json.toString()
        } else {
            val iRealDataLength = data!!.getIntExtra(TransKeyActivity.mTK_PARAM_DATA_LENGTH, 0)
            if (iRealDataLength == 0) {
                json.addProperty("state", "ok")
                json.addProperty("value","")
                return json.toString()
            }
            val cipherData = data!!.getStringExtra(TransKeyActivity.mTK_PARAM_CIPHER_DATA)
            val cipherDataEx = data!!.getStringExtra(TransKeyActivity.mTK_PARAM_CIPHER_DATA_EX)
            val cipherDataExWithPadding =
                data!!.getStringExtra(TransKeyActivity.mTK_PARAM_CIPHER_DATA_EX_PADDING)
            val dummyData = data!!.getStringExtra(TransKeyActivity.mTK_PARAM_DUMMY_DATA)
            val secureKey = data!!.getByteArrayExtra(TransKeyActivity.mTK_PARAM_SECURE_KEY)
            val secureData = data!!.getStringExtra(TransKeyActivity.mTK_PARAM_SECURE_DATA)
            android.util.Log.d(tag, "secureData : $secureData")
            val secureDataWithTimestamp =
                data!!.getStringExtra(TransKeyActivity.mTK_PARAM_SECURE_DATA_WITH_TIMESTAMP)
            android.util.Log.d(tag, "secureDataWithTimestamp : $secureDataWithTimestamp")
            val decryptSecureData = TransKeyCipher.decryptSecureData(secureData)
            android.util.Log.d(tag, "decrypted secure data : $decryptSecureData") // *******
            val rsaData = data!!.getStringExtra(TransKeyActivity.mTK_PARAM_RSA_DATA)

            json.addProperty("state", "ok")
            json.addProperty("value",decryptSecureData)
            return json.toString()
        }
    }
}