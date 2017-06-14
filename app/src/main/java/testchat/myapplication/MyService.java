package testchat.myapplication;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.service.notification.StatusBarNotification;
import android.support.annotation.IntDef;
import android.util.Log;
import android.widget.Toast;

public class MyService extends Service {
    private static NotificationManager NM;
    private static serviceThread thread;
    private static Notification notifi;
    notiListener ntListener;
    public MyService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ntListener = new notiListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        thread.stopForever();
        thread = null;
    }

    //background에서 할 일
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        myServiceHandler handler = new myServiceHandler();
        thread = new serviceThread(handler);
        thread.start();
        return START_STICKY;

        //return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public class myServiceHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            //super.handleMessage(msg);

            //start contentInten
            StatusBarNotification[] mStatusNoti =  ntListener.getActiveNotifications();
            if(mStatusNoti==null)   {
                Log.d("num","empty");
                return;
            }
            for(int i=0; i<= mStatusNoti.length;i++){
                Log.d("num",Integer.toString(i));
                //Log.d("num",mStatusNoti[i].getNotification().extras.getString("android.text"));
            }
//            if(mStatusNoti==null || mStatusNoti.length==0)    return;
//            Notification newNoti = mStatusNoti[0].getNotification();
//            Bundle newBundle = newNoti.extras;
//            String title = newBundle.getString("android.title");
//            String body = newBundle.getString("android.text");
            String title="11";
            String body="22";
            Log.d("title : ",title);
            Log.d("body : ",body);
            Intent intent = new Intent(MyService.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                    intent, PendingIntent.FLAG_CANCEL_CURRENT);
            notifi = new Notification.Builder(getApplicationContext())
                    .setContentTitle(title) //.setContentTitle(title) //** 큰 텍스트로 표시
                    .setContentText(body) //.setContentText(message) //** 작은 텍스트로 표시
                    .setTicker("메시지가 도착했습니다")
                    .setSmallIcon(R.mipmap.ic_launcher).setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_jnu_chat))
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true)
                    .setPriority(Notification.PRIORITY_MAX) //** MAX 나 HIGH로 줘야 가능함
                    .setFullScreenIntent(pendingIntent, true)
                    .build();

            //소리추가
            notifi.defaults = Notification.DEFAULT_SOUND;

            //알림 소리를 한번만 내도록
            notifi.flags = Notification.FLAG_ONLY_ALERT_ONCE;

            NM.notify(777, notifi);
            //Toast.makeText(MyService.this,"hello",Toast.LENGTH_SHORT).show();
            //NM.cancel(0);   //server noti
            //NM.cancel(777); //my noti
        }

    }
}
