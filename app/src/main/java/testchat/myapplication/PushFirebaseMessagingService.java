package testchat.myapplication;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Window;

import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Created by TH-home on 2017-06-07.
 */
/**
 * @Name    PushFirebaseMessagingService
 * @Usage   when receive message from firebase, get noti
 * @Implement FirebaseMessagingService(FCM)
 * @Comment Only foreground active, noti listener help this active anytime
 *
 * remoteMessage.getNotification -> background -> tray noti
 * remoteMessage.getData -> foreground -> head up alarm
 * android code : foreground alarm
 * node.js : background alarm
 * */
public class PushFirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService{
    private static final String TAG = "MyFirebaseMSGService";
    public static String pushTitle;
    public static String pushBody;
    public static boolean isPopup = true;
    /*헤드업 알림 쓰고싶으면
    1. data에 priority high/sound값주기
    2. data에는 sound priority 주지말기
    3. 메시지 리시브에서 notification compat builder 사용하지 말기
    **위의 사항을 안지키면 우선순위 뺏김
    */
    /**
     * @Name    onMessageReceived
     * @Usage   callback about get noti(message from server)
     * @Param   remoteMessage : notification's data
     * @return  void
     * */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        RemoteMessage.Notification noti = remoteMessage.getNotification();
        //Map<String,String> data = remoteMessage.getData();        //get data
        String title = noti.getTitle();
        String msg = noti.getBody();

        sendPushNotification(title,msg);
    }

    /**
     * @Name    sendPushNotification
     * @Usage   change push to head up
     * @Param   title : message from, message : receive text
     * @return  void
     * */
    private void sendPushNotification(String title,String message){

        //** Intent와 PendingIntent를 추가해 주는 것으로 헤드업 알림이 가능
        //** 없을 경우 이전 버전의 Notification과 동일
        NotificationManager nm;
        Notification.Builder builder;
        Intent push;
        PendingIntent fullScreenPendingIntent;

        push = new Intent();
        //alarm on diffrent app active
        push.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        push.setClass(this, MainActivity.class);

        fullScreenPendingIntent = PendingIntent.getActivity(this, 0, push, PendingIntent.FLAG_CANCEL_CURRENT);
        //** 여기까지 헤드업 알림을 사용하기 위한 필수 조건!

        builder = new Notification.Builder(this)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.ic_jnu_chat) )
            .setTicker("메시지가 도착했습니다") //** 이 부분은 확인 필요
            .setWhen(System.currentTimeMillis())
            .setContentTitle(title) //** 큰 텍스트로 표시
            .setContentText(message) //** 작은 텍스트로 표시
            .setAutoCancel(true)
            .setPriority(Notification.PRIORITY_MAX) //** MAX 나 HIGH로 줘야 가능함
            .setFullScreenIntent(fullScreenPendingIntent, true);

        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(0/*noti request code, can custom*/, builder.build());

    }

}
