package testchat.myapplication.fcm;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import testchat.myapplication.R;

/**
 * Created by th on 2017-06-13.
 */
//instance id -> get token
public class RegistrationIntentService extends IntentService{
    private static final String TAG = "RegistrationIntentService";
    public RegistrationIntentService() { super(TAG); }
    //instance id token genration
    @SuppressLint("LongLogTag")
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(new Intent(QuickstartPreferences.REGISTRATION_GENERATION));
        InstanceID instanceID = InstanceID.getInstance(this);
        String token = null;
        try {
            synchronized (TAG) {
                //google service json -> get senderID
                String default_senderID = getString(R.string.gcm_defaultSenderId);
                //default scope = GCM
                String scope = GoogleCloudMessaging.INSTANCE_ID_SCOPE;
                token = instanceID.getToken(default_senderID, scope, null);

                Log.i(TAG,"GCM RegToken:" + token);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        //get token -> local boadcast COMPLETE
        //send token
        Intent registrationComplete = new Intent(QuickstartPreferences.REGISTRATION_COMPLETE);
        registrationComplete.putExtra("token",token);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }
}
