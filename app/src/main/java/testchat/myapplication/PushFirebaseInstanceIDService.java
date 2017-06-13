package testchat.myapplication;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by TH-home on 2017-06-07.
 */
// create token about user
public class PushFirebaseInstanceIDService extends FirebaseInstanceIdService{
    private static final String TAG = "MyFirebaseIDService";
    //don't active onTokenRefresh -> String token = FirebaseInstanceID.getInstance.getToken();
    @Override
    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d("TAG",token);

    }
}
