package testchat.myapplication.fcm;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by th on 2017-06-13.
 */
// get instance id listener -> token refresh
public class MyInstanceIDListenerService extends InstanceIDListenerService{
    private static final String TAG = "MyInstanceIDLS";

    @Override
    public void onTokenRefresh() {
        Intent intent = new Intent(this,RegistrationIntentService.class);
        startService(intent);
    }
}
