package testchat.myapplication.fcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import testchat.myapplication.MainActivity;
import testchat.myapplication.R;

/**
 * Created by th on 2017-06-13.
 */

public class MyGcmListenerService extends GcmListenerService{
    private static final String TAG = "MyGcm";
    int badge_count;
    /*
    * @param from : senderID
    * @param data : payload
    */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        //server send TITLE and MSG key
        String title = data.getString("TITLE");
        String message = data.getString("MSG");
        Log.d(TAG,"From:"+from);
        Log.d(TAG,"Title:"+title);
        Log.d(TAG,"Message:"+message);

        sendNotification(title,message);
    }
    //alarm notify
    private void sendNotification(String title, String Message){
        //up SplashActivity
        //flag activity clear top : set piriorty 1st
        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //define intent work
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0/*request code*/,intent,
                PendingIntent.FLAG_ONE_SHOT);

        //ringing class
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        //alarm service setting
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(Message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri);
        //.setContentIntent(PendingIntent); -> display move
        //if you want (click notify -> new window), you clear .setContentIntent

        NotificationManager notificationManager=
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //show notify
        notificationManager.notify(0/* notification id */,notificationBuilder.build());
    }
}
