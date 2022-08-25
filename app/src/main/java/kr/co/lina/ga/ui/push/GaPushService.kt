//package kr.co.lina.ga.ui.push
//
//import android.app.NotificationManager
//import android.app.PendingIntent
//import android.content.Context
//import android.content.Intent
//import android.media.RingtoneManager
//import android.util.Log
//import androidx.core.app.NotificationCompat
//import com.google.firebase.messaging.FirebaseMessaging
//import com.google.firebase.messaging.FirebaseMessagingService
//import com.google.firebase.messaging.RemoteMessage
//import kr.co.lina.ga.R
//import kr.co.lina.ga.ui.main.MainActivity
//import kr.co.lina.ga.utils.WaSharedPreferences
//
//object Push {
//    fun initFirebase() {
//        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//                // success !!!
//            }
//        }
//    }
//}
//
//class GaPushService: FirebaseMessagingService() {
//    private val TAG = "GaPushService"
//
//    /**
//     * FirebaseInstanceIdService is deprecated.
//     * this is new on firebase-messaging:17.1.0
//     */
//    override fun onNewToken(token: String) {
//        Log.d(TAG, "new Token: $token")
//        //------------------------------
//        // send to server !!!!!
//        //------------------------------
//        sendRegistrationToServer(token)
//    }
//
//    /**
//     * this method will be triggered every time there is new FCM Message.
//     */
//    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//        Log.d(TAG, "From: " + remoteMessage.from)
//
////        if(remoteMessage.notification != null) {
////            Log.d(TAG, "Notification Message Body: ${remoteMessage.notification?.body}")
////            val title = remoteMessage.notification!!.title ?: ""
////            val body = remoteMessage.notification!!.body ?: ""
////            sendNotification(body, title)
////        }
////        else if (remoteMessage.data.size > 0) {
//////            var jsonStr: String = remoteMessage.data.get("_data").toString()
//////            val pushJson: JSONObject = JSONObject(jsonStr)
////            var title :String = remoteMessage.data.get("title")!!
////            var body :String = remoteMessage.data.get("body")!!
//////            goActivity = pushJson.getString("source")
////            sendNotification(body, title)
////        }
//    }
//
//    /**
//     * Persist token to third-party servers.
//     *
//     * Modify this method to associate the user's FCM registration token with any server-side account
//     * maintained by your application.
//     *
//     * @param token The new token.
//     */
//    private fun sendRegistrationToServer(token: String) {
//        // TODO: Implement this method to send token to your app server.
//        Log.d(TAG, "Save Push Token($token)")
//        WaSharedPreferences(this).writePrefer("push_token", token)
//    }
//
//    private fun sendNotification(body: String?, messageTitle: String) {
//        val intent = Intent(this, MainActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//            putExtra("Notification", body)
//        }
//        .0
//
//        var pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
//        val notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
//
//        var notificationBuilder = NotificationCompat.Builder(this,"Notification")
//            .setSmallIcon(R.mipmap.ic_launcher_foreground)
//            .setContentTitle(messageTitle)
//            .setContentText(body)
//            .setAutoCancel(true)
//            .setSound(notificationSound)
//            .setContentIntent(pendingIntent)
//
//        var notificationManager: NotificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        notificationManager.notify(0, notificationBuilder.build())
//    }
//
//}