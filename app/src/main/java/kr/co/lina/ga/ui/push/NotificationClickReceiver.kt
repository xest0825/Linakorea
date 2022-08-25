package kr.co.lina.ga.ui.push

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.tms.sdk.api.APIManager
import com.tms.sdk.bean.PushMsg
import com.tms.sdk.push.NotificationReceiver
import kr.co.lina.ga.R
import kr.co.lina.ga.ui.main.MainActivity


class NotificationClickReceiver : NotificationReceiver() {

    var mContext: Context? = null

    override fun onReceive(context: Context?, intent: Intent?) {

        mContext = context

        super.onReceive(context, intent)

        // data parsing
        val receivedMessage: PushMsg = pushMsg
        var message = "notification is clicked = "+receivedMessage.toString()
        //Toast.makeText(context, "< $message >", Toast.LENGTH_LONG).show()

        // 읽음 처리
        requestReadMsg(APIManager.APICallback { s, jsonObject ->
            //
        })

        var  activityIntent = Intent(context, MainActivity::class.java)
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        activityIntent.putExtra("push", true)
        context!!.startActivity(activityIntent)

        //Toast.makeText(context, "Click and call app", Toast.LENGTH_LONG).show()
        Log.d("PUSH", "Click event & call app")
    }
}