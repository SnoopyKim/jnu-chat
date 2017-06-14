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
//create push message and showing
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
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        pushTitle="";
        pushBody="";
        //get data -> 백그라운드일 때 사용
        //get notification -> 백그라운드일때 메세지 리시브 안거치고 바로 알람 띄움
        //안드로이드 : 포그라운드 메시지
        //node.js : 백그라운드 알람 이엇네
        //String title = dataNoti.get("title");
        //String msg = dataNoti.get("body");
        //Log.d(TAG,title);
        Intent serviceIntent = new Intent(getApplicationContext(),MyService.class);
        getApplicationContext().startService(serviceIntent);

        serviceIntent = new Intent(getApplicationContext(),notiListener.class);
        getApplicationContext().startService(serviceIntent);

        RemoteMessage.Notification noti = remoteMessage.getNotification();

        Map<String,String> data = remoteMessage.getData();
        String title = noti.getTitle();
        String msg = noti.getBody();

        pushTitle=title;
        pushBody=msg;
        Log.d(TAG,title);
        sendPushNotification(title,msg);
    }

    private void sendPushNotification(String title,String message){
        System.out.println("received message : " + message);
        Log.d(TAG,"Title:"+title);
        Log.d(TAG,"Message:"+message);
//        //up MainActivity
//        //flag activity clear top : set piriorty 1st
//        Intent intent = new Intent(this, MainActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        //define intent work
//        PendingIntent pendingIntent = PendingIntent.getActivity(this,
//                0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);
//
//        //ringing class
//        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//
//        //alarm service setting
//        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
//                .setSmallIcon(R.drawable.ic_jnu_chat).setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.ic_jnu_chat) )
//                .setContentTitle(title)
//                .setContentText(message)
//                .setAutoCancel(true)
//                .setSound(defaultSoundUri).setLights(000000255,500,2000)
//                .setContentIntent(pendingIntent);
//
//        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());


        //** Intent와 PendingIntent를 추가해 주는 것으로 헤드업 알림이 가능
        //** 없을 경우 이전 버전의 Notification과 동일
        NotificationManager nm;
        Notification.Builder builder;
        Intent push;
        PendingIntent fullScreenPendingIntent;

        push = new Intent();
        push.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        push.setClass(this, MainActivity.class);

        fullScreenPendingIntent = PendingIntent.getActivity(this, 0, push, PendingIntent.FLAG_CANCEL_CURRENT);
        //** 여기까지 헤드업 알림을 사용하기 위한 필수 조건!

        builder = new Notification.Builder(this)
            .setSmallIcon(R.mipmap.ic_launcher).setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.ic_jnu_chat) )
            .setTicker("Test1") //** 이 부분은 확인 필요
            .setWhen(System.currentTimeMillis())
            .setContentTitle(title) //** 큰 텍스트로 표시
            .setContentText(message) //** 작은 텍스트로 표시
            .setAutoCancel(true)
            .setPriority(Notification.PRIORITY_MAX) //** MAX 나 HIGH로 줘야 가능함
            .setFullScreenIntent(fullScreenPendingIntent, true);

        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(0, builder.build());

        //nm.cancel(0);
    }

}
