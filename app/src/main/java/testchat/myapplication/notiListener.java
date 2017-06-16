package testchat.myapplication;

import android.content.Intent;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

/**
 * Created by th on 2017-06-14.
 * Refrence https://github.com/Chagall/notification-listener-service-example
 */

/**
 * @Name    SigninActivity
 * @Usage   Register firebase authentication and users profile
 * @Layout  activity_singin.xml
 * */
public class notiListener extends NotificationListenerService {
    /*
    These are the package names of the apps. for which we want to
    listen the notifications
 */
    private static final class ApplicationPackageNames {
        public static final String JNU_PACK_NAME = "testchat.myapplication";
    }
    /*
        These are the return codes we use in the method which intercepts
        the notifications, to decide whether we should do something or not
     */
    public static final class InterceptedNotificationCode {
        public static final int JNU_CODE = 1;
        public static final int OTHER_NOTIFICATIONS_CODE = 2; // We ignore all notification with code == 4
    }
    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    //푸시가 추가된 경우
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        int notificationCode = matchNotificationCode(sbn);
        if(notificationCode == InterceptedNotificationCode.JNU_CODE){
            Intent intent = new  Intent(ApplicationPackageNames.JNU_PACK_NAME);
            intent.putExtra("Notification Code", notificationCode);
            sendBroadcast(intent);
        }
    }
    //푸시를 없앴을 경우
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn){
        int notificationCode = matchNotificationCode(sbn);

        if(notificationCode == InterceptedNotificationCode.JNU_CODE) {
            StatusBarNotification[] activeNotifications = this.getActiveNotifications();
            if(activeNotifications != null && activeNotifications.length > 0) {
                for (int i = 0; i < activeNotifications.length; i++) {
                    if (notificationCode == matchNotificationCode(activeNotifications[i])) {
                        Intent intent = new  Intent(getPackageName());
                        intent.putExtra("Notification Code", notificationCode);
                        //sendBroadcast(intent);
                        break;
                    }
                }
            }
        }
    }
    private int matchNotificationCode(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        if(packageName.equals(ApplicationPackageNames.JNU_PACK_NAME)){
            return(InterceptedNotificationCode.JNU_CODE);
        }
        else{
            return(InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE);
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    //START_STICKY : if service turn down, after a while - turn on
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
    @Override
    public StatusBarNotification[] getActiveNotifications() {
        return super.getActiveNotifications();
    }

}
