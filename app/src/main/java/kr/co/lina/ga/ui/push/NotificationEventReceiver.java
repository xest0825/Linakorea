package kr.co.lina.ga.ui.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tms.sdk.bean.PushMsg;
import com.tms.sdk.push.NotificationReceiver;

public class NotificationEventReceiver extends NotificationReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        //이 라인 반드시 사용!
        super.onReceive(context,intent);

        //푸시 데이터 파싱하기
        PushMsg receivedMessage = getPushMsg();

        //로그 표시
        String message = "notification is received = "+receivedMessage.toString();
        message = message + (intent.getAction()!=null?" intent action = "+intent.getAction():"");
        //Log log = new Log(message, new Throwable().fillInStackTrace());
        //DatabaseHelper.getInstance(context).getLogDao().insert(log);
    }
}