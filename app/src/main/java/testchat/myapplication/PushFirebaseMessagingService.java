package testchat.myapplication;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.RemoteMessage;

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

    //사용자의 정보와 DB 내 알람 관련 위치를 선언
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference alarmRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("alarm");

    /**
     * @Name    onMessageReceived
     * @Usage   callback about get noti(message from server)
     * @Param   remoteMessage : notification's data
     * @return  void
     * */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        //js에서 보낸 Notification 정보를 받음
        RemoteMessage.Notification noti = remoteMessage.getNotification();

        final String title = noti.getTitle();
        final String msg = noti.getBody();

        //알림 설정이 꺼져있으면 아예 호출X
        alarmRef.child("noti").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null &&
                        dataSnapshot.getValue().toString().equals("on")) {
                    sendPushNotification(title,msg);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
        Intent push = new Intent(this, MainActivity.class);
        push.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        final PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(this, 0, push, PendingIntent.FLAG_UPDATE_CURRENT);
        //** 여기까지 헤드업 알림을 사용하기 위한 필수 조건!

        //알림 내용 및 설정 빌드
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
            .setSmallIcon(R.drawable.ic_jnu_chat).setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.ic_jnu_chat) )
            .setTicker("메세지가 도착했습니다") //** 상단 한줄 메세지
            .setWhen(System.currentTimeMillis())
            .setContentTitle(title) //** 큰 텍스트로 표시
            .setContentText(message) //** 작은 텍스트로 표시
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX); //** MAX 나 HIGH로 줘야 가능함

        //사용자 팝업 체크에 따라 알림을 다르게 띄움
        alarmRef.child("pop").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null
                        && dataSnapshot.getValue().toString().equals("on")) {
                    builder.setFullScreenIntent(fullScreenPendingIntent, true);
                } else {
                    builder.setContentIntent(fullScreenPendingIntent);
                }
                builder.addAction(R.drawable.ic_jnu_chat,"답장",fullScreenPendingIntent);
                
                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                nm.notify(0, builder.build());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

}
