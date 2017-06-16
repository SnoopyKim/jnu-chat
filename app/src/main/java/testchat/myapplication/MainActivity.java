package testchat.myapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.LoggingBehavior;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Hashtable;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;


/**
 * @Name    MainActivity
 * @Usage   Checking grant options
 *           Checking Login Information
 *           Linking SinginActivity/FindinfoActivity/Tabactivity
 * @Layout  activity_main.xml
 * */
public class MainActivity extends AppCompatActivity {
    String TAG = "MainActivity";
    String stEmail;
    String stPassword;

    RelativeLayout RLinput;
    ProgressBar pbLogin;

    EditText etEmail;
    EditText etPassword;

    TextView textbtnFindinfo;
    TextView textbtnSignin;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    FirebaseDatabase database;
    DatabaseReference myRef;
    DatabaseReference tokenRef;
    FirebaseUser user;

    CallbackManager callbackManager;

    //noti listner permission string
    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
    private AlertDialog enableNotificationListenerAlertDialog;

    private BroadcastReceiver notiReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If the user did not turn the notification listener service on we prompt him to do so
        if(!isNotificationServiceEnabled()){
            enableNotificationListenerAlertDialog = buildNotificationServiceAlertDialog();
            enableNotificationListenerAlertDialog.show();
        }
        //get noti receiver/start service

        /**
         * Broadcast Receiver.
         * We use this Broadcast Receiver to notify the Main Activity when
         * a new notification has arrived, so it can properly change the
         * notification push to head up
         * */
        notiReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int receivedNotificationCode = intent.getIntExtra("Notification Code",-1);
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(this.getPackageName());
        registerReceiver(notiReceiver,intentFilter);


        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        //저장소 허용 동의 부분
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)) {

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.MANAGE_DOCUMENTS},
                        1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        final String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG,"Token:" + token);

        //layout objects 생성 및 초기화
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        RLinput = (RelativeLayout) findViewById(R.id.RLinput);
        pbLogin = (ProgressBar) findViewById(R.id.pbLogin);

        //유저들의 정보와 기기 토큰 정보를 저장하는 DB 위치 선언(생성)
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");
        tokenRef = database.getReference("fcmtoken");

        //비밀번호 찾기 버튼 클릭 시 해당 Activity로 이동
        textbtnFindinfo = (TextView) findViewById(R.id.textbtnFindinfo);
        textbtnFindinfo.setText(Html.fromHtml("<u>"+textbtnFindinfo.getText()+"</u>"));
        textbtnFindinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FindinfoActivity.class);
                startActivity(intent);
            }
        });

        //회원가입 버튼 클릭 시 해당 Activity로 이동
        textbtnSignin = (TextView) findViewById(R.id.textbtnSignin);
        textbtnSignin.setText(Html.fromHtml("<u>"+textbtnSignin.getText()+"</u>"));
        textbtnSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,SigninActivity.class);
                startActivityForResult(intent, 0);
            }
        });

        //로그인 감지(계속 활성화되있음)
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();

                //기기에서의 최근 유저가 접속중(Signed_in)이면
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    if (mAuthListener != null) {
                        //계정 감지 리스너를 끔 (이때 꺼줘도 뒤의 코드들 실행 됨)
                        mAuth.removeAuthStateListener(mAuthListener);
                    }
                    //접속 당시 유저의 기기 토큰을 업로드
                    tokenRef.child(user.getUid()).setValue(token);

                    RLinput.setVisibility(GONE);
                    pbLogin.setVisibility(VISIBLE);

                    //계정 제공업체 분류하고 TabActivity로 이동
                    final Intent intent = new Intent(MainActivity.this, TabActivity.class);
                    if (user.getProviderData().get(1).getProviderId().equals("facebook.com")) {
                        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(final DataSnapshot dataSnapshot) {
                                //만약 현유저가 페이스북 계정이면 친구 리스트를 API로 호출
                                FacebookSdk.addLoggingBehavior(LoggingBehavior.REQUESTS);
                                GraphRequest request = GraphRequest.newMyFriendsRequest(AccessToken.getCurrentAccessToken(),
                                        new GraphRequest.GraphJSONArrayCallback() {
                                            @Override
                                            public void onCompleted(JSONArray objects, GraphResponse response) {
                                                if (objects!=null) {
                                                    for (int i = 0; i < objects.length(); i++) {
                                                        try {
                                                            JSONObject f_info = objects.getJSONObject(i);

                                                            //이 앱에 동의를 한 친구들의 데이터를 Firebase내 자신의 친구 리스트에 추가
                                                            Hashtable<String, String> friend = new Hashtable<String, String>();
                                                            for (DataSnapshot users : dataSnapshot.getChildren()) {
                                                                if (users.child("profile").child("facebook_id").getValue().equals(f_info.getString("id"))) {
                                                                    String friendUid = users.getKey();
                                                                    String friendName = users.child("profile").child("name").getValue().toString();
                                                                    String friendEmail = users.child("profile").child("email").getValue().toString();
                                                                    String friendPhoto = users.child("profile").child("photo").getValue().toString();

                                                                    friend.put("email", friendEmail);
                                                                    friend.put("name", friendName);
                                                                    friend.put("photo", friendPhoto);

                                                                    myRef.child(user.getUid()).child("friends").child(friendUid).setValue(friend);
                                                                }
                                                            }

                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                    Log.d(TAG, "GraphRequest for friend : Success");
                                                }
                                            }
                                        });
                                    //API 호출 인자값은 id만
                                Bundle param = new Bundle();
                                param.putString("fields","id");
                                request.setParameters(param);
                                request.executeAsync();

                                pbLogin.setVisibility(GONE);
                                RLinput.setVisibility(VISIBLE);

                                //DB에 로그인 상태로 작성
                                myRef.child(user.getUid()).child("profile").child("login").setValue("on");

                                intent.putExtra("providerId","facebook");
                                startActivity(intent);

                                finish();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        //페이스북이 아닌 일반 이메일 유저
                    } else {
                        pbLogin.setVisibility(GONE);
                        RLinput.setVisibility(VISIBLE);

                        //DB에 로그인 상태로 작성
                        myRef.child(user.getUid()).child("profile").child("login").setValue("on");

                        intent.putExtra("providerId","email");
                        startActivity(intent);
                        finish();

                    }

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        //로그인 버튼 클릭 시
        Button btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stEmail = etEmail.getText().toString();
                stPassword = etPassword.getText().toString();

                //빈칸이 있을 경우 로그인X, 팝업 띄움
                if (stEmail.isEmpty() || stEmail.equals("") || stPassword.isEmpty() || stPassword.equals("")) {
                    Toast.makeText(MainActivity.this, "이메일이나 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
                } else {
                    userLogin(stEmail, stPassword);
                }
            }
        });

        //페이스북 콜백매니저(로그인 버튼 관련)
        callbackManager = CallbackManager.Factory.create();
        final LoginButton fbtnLogin = (LoginButton) findViewById(R.id.facebook_login);
        //페이스북 로그인하는 유저의 정보 동의를 얻는 부분 (이메일, 친구리스트)
        fbtnLogin.setReadPermissions(Arrays.asList("public_profile","email","user_friends","user_birthday"));
        fbtnLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                //로그인 성공시
                handleFacebookAccessToken(loginResult.getAccessToken());

            }

            @Override
            public void onCancel() {
                //로그인 실패 혹은 거절 시
                Toast.makeText(getApplicationContext(), "cancel",Toast.LENGTH_SHORT).show();
                pbLogin.setVisibility(GONE);
                RLinput.setVisibility(VISIBLE);
            }

            @Override
            public void onError(FacebookException error) {
                //로그인 에러 시
                Toast.makeText(getApplicationContext(), "error",Toast.LENGTH_SHORT).show();
            }
        });

        //보여지는 페이스북 로그인 버튼 클릭 시 숨겨져있는 진짜 페이스북 로그인 버튼 실행
        Button facebookBtnLogin = (Button)findViewById(R.id.btnLoginFacebook);
        facebookBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RLinput.setVisibility(GONE);
                pbLogin.setVisibility(VISIBLE);
                fbtnLogin.performClick();
            }
        });
    }

    //액티비티 실행 시 계정 감지 리스너 활성화
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    //로그인 버튼 클릭 시 Firebase에 해당 계정 로그인 on
    private void userLogin(String email, String password){
        RLinput.setVisibility(GONE);
        pbLogin.setVisibility(VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            String errorMessage = task.getException().getMessage();
                            String errorToast;
                            if(errorMessage.equals("The email address is badly formatted.")){
                                errorToast = "이메일 형식이 올바르지 않습니다.";
                            }
                            else if(errorMessage.equals("There is no user record corresponding to this identifier. The user may have been deleted.")){
                                errorToast = "아이디가 존재하지 않습니다.";
                            }
                            else if(errorMessage.equals("The password is invalid or the user does not have a password.")){
                                errorToast = "비밀번호가 일치하지 않습니다.";
                            }
                            else{
                                errorToast="로그인에 문제가 생겼습니다. 다시 한번 확인해 주세요.";
                            }

                            Toast.makeText(MainActivity.this, errorToast,Toast.LENGTH_SHORT).show();

                            RLinput.setVisibility(VISIBLE);
                            pbLogin.setVisibility(GONE);
                        }
                    }
                });
    }

    //페이스북 로그인에 성공할 시
    private void handleFacebookAccessToken(AccessToken accessToken) {
        //credential로 페이스북에서 token을 얻어 Firebase에 해당 계정 로그인 on
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    user = task.getResult().getUser();

                    //로그인 on되면 자신의 정보를 API로 불러와 Firebase내 DB에 저장
                    FacebookSdk.addLoggingBehavior(LoggingBehavior.REQUESTS);
                    GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),
                            new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(JSONObject object, GraphResponse response) {
                                    try {
                                        Log.d("GraphRequest_Me",response.toString());

                                        Hashtable<String, String> profile = new Hashtable<String, String>();
                                        profile.put("name", object.getString("name"));
                                        if (object.has("email")) {
                                            profile.put("email", object.getString("email"));
                                        } else {
                                            profile.put("email", "None");
                                        }
                                        profile.put("photo", object.getJSONObject("picture").getJSONObject("data").getString("url"));
                                        profile.put("uid", user.getUid());
                                        profile.put("facebook_id", object.getString("id"));
                                        if (object.has("birthday")) {
                                            profile.put("birth", object.getString("birthday").replace("/","-"));
                                        } else {
                                            profile.put("birth", "None");
                                        }
                                        if (object.has("gender")) {
                                            profile.put("gender", object.getString("gender"));
                                        } else {
                                            profile.put("gender", "None");
                                        }
                                        profile.put("phone","None");
                                        Log.d(TAG,profile.toString());

                                        myRef.child(user.getUid()).child("profile").setValue(profile);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                    //API 호출 시 필요한 인자들 설정 (이메일, 이름, id, 프로필 사진)
                    Bundle param = new Bundle();
                    param.putString("fields","email,name,id,picture,gender,birthday");
                    request.setParameters(param);
                    //API 호출
                    request.executeAsync();

                    Log.v(TAG,"Facebook Login task success");

                } else {
                    Log.v(TAG,"Facebook Login task fail");
                }
            }
        });
    }

    //페이스북 화면에서 돌아올 때 사용
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case 1:
                if (mAuthListener != null) {
                    mAuth.removeAuthStateListener(mAuthListener);
                } break;
            default:
                callbackManager.onActivityResult(requestCode, resultCode, data); break;
        }
    }

    //저장소 허용 동의 부분에서 결과 처리 부분인데 아직 아무것도 없음 (딱히 필요한 이벤트가 없을 듯?)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,"저장소 접근에 동의하셨습니다",Toast.LENGTH_SHORT).show();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    Toast.makeText(this,"저장소 접근을 거절하셨습니다. 어플 사용에 제한이나 에러가 발생할 수 있습니다",Toast.LENGTH_SHORT).show();

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    //폰의 뒤로가기 버튼을 클릭할 시 Task중 가장 뒤로 보냄
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
    }

    /**
     *  Broad caster turn down
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(notiReceiver);
    }

    /**
     * Is Notification Service Enabled.
     * Verifies if the notification listener service is enabled.
     * Got it from: https://github.com/kpbird/NotificationListenerService-Example/blob/master/NLSExample/src/main/java/com/kpbird/nlsexample/NLService.java
     * @return True if eanbled, false otherwise.
     */
    private boolean isNotificationServiceEnabled(){
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    /**
     * Build Notification Listener Alert Dialog.
     * Builds the alert dialog that pops up if the user has not turned
     * the Notification Listener Service on yet.
     * @return An alert dialog which leads to the notification enabling screen
     */
    private android.app.AlertDialog buildNotificationServiceAlertDialog(){
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.notification_listener_service);
        alertDialogBuilder.setMessage(R.string.notification_listener_service_explanation);
        alertDialogBuilder.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
                    }
                });
        alertDialogBuilder.setNegativeButton(R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // If you choose to not enable the notification listener
                        // the app. will not work as expected
                    }
                });
        return(alertDialogBuilder.create());
    }
}
