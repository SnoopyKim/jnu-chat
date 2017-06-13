package testchat.myapplication;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.IntDef;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by th on 2017-06-14.
 */

public class notiListener extends NotificationListenerService {
    private Notification mNoti;
    private Bundle mBundle;
    boolean isBound = false;
    public static notiListener mNotiService;
    private IBinder binder = new ServiceBinder();
    public class ServiceBinder extends Binder {
        notiListener getService(){
            return notiListener.this;
        }
    }

// 디바이스 설정=>일반=>보안=>알림 에서 해당 앱을 ON 시 서비스 시작됨

    @Override
    public void onCreate() {
        mNotiService=this;
        Toast.makeText(notiListener.this, "hello?", Toast.LENGTH_LONG).show();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    //푸시가 추가된 경우
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        Log.d("noti listen","hi");
        mNoti=sbn.getNotification();
        mBundle = mNoti.extras;
        String title = mBundle.getString("android.title");
        CharSequence text=mBundle.getCharSequence("android.text");
        Toast.makeText(notiListener.this, "뜸?", Toast.LENGTH_LONG).show();
    }
    @Override
    public IBinder onBind(Intent intent) {
        isBound = true;
        String action = intent.getAction();
        Log.d("NOTIF", "onBind: " + action);

        if (SERVICE_INTERFACE.equals(action)) {
            Log.d("NOTIF", "Bound by system");
            return super.onBind(intent);
        } else {
            Log.d("NOTIF", "Bound by application");
            return binder;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public StatusBarNotification[] getActiveNotifications() {
        return super.getActiveNotifications();
    }

}
