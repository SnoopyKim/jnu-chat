package testchat.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Hashtable;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;


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
    FirebaseUser user;

    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        //layout objects 생성 및 초기화
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        RLinput = (RelativeLayout) findViewById(R.id.RLinput);
        pbLogin = (ProgressBar) findViewById(R.id.pbLogin);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");

        //FirebaseAuth.getInstance().signOut();
        //LoginManager.getInstance().logOut();

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
                startActivity(intent);
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
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getDisplayName());

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

                                intent.putExtra("providerId","facebook");
                                startActivity(intent);
                                finish();
                            }


                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    } else {
                        intent.putExtra("providerId","email");

                        startActivity(intent);
                        finish();
                    }

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                pbLogin.setVisibility(GONE);
                RLinput.setVisibility(VISIBLE);
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
        fbtnLogin.setReadPermissions(Arrays.asList("public_profile","email","user_friends"));
        fbtnLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());

            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplicationContext(), "cancel",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
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

    //계정 로그인 감지의 시작과 종료 호출 함수
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
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
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

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
                                        profile.put("email", object.getString("email"));
                                        profile.put("photo", object.getJSONObject("picture").getJSONObject("data").getString("url"));
                                        profile.put("uid", user.getUid());
                                        profile.put("facebook_id", object.getString("id"));
                                        myRef.child(user.getUid()).child("profile").setValue(profile);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                    //API 호출 시 필요한 인자들 설정 (이메일, 이름, id, 프로필 사진)
                    Bundle param = new Bundle();
                    param.putString("fields","email,name,id,picture");
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
        callbackManager.onActivityResult(requestCode,resultCode,data);
    }
}
