package testchat.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.Hashtable;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;


public class MainActivity extends AppCompatActivity {
    String TAG = "MainActivity";
    String stEmail;
    String stPassword;

    ProgressBar pbLogin;

    EditText etEmail;
    EditText etPassword;

    TextView textbtnFindinfo;
    TextView textbtnSignin;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    DatabaseReference myRef;
    FirebaseUser user;

    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        pbLogin = (ProgressBar) findViewById(R.id.pbLogin);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");
        //user = FirebaseAuth.getInstance().getCurrentUser();

        textbtnFindinfo = (TextView) findViewById(R.id.textbtnFindinfo);
        //textbtnFindinfo.setText(Html.fromHtml("<u>"+textbtnFindinfo.getText()+"</u>"));
        textbtnFindinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FindinfoActivity.class);
                startActivity(intent);
            }
        });

        textbtnSignin = (TextView) findViewById(R.id.textbtnSignin);
        //textbtnSignin.setText(Html.fromHtml("<u>"+textbtnSignin.getText()+"</u>"));

        //FirebaseAuth.getInstance().signOut();
        //LoginManager.getInstance().logOut();

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                    /*
                    SharedPreferences sharedPreferences = getSharedPreferences("email", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("uid", user.getUid());
                    editor.putString("email", user.getEmail());
                    editor.apply();
                    */
                    Intent intent = new Intent(MainActivity.this, TabActivity.class);
                    startActivity(intent);

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                    // ...
            }
        };

            Button btnRegister = (Button) findViewById(R.id.btnRegister);
            btnRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stEmail = etEmail.getText().toString();
                    stPassword = etPassword.getText().toString();

                    //Toast.makeText(MainActivity.this, stEmail+""+stPassword, Toast.LENGTH_SHORT).show();
                    if (stEmail.isEmpty() || stEmail.equals("") || stPassword.isEmpty() || stPassword.equals("")) {
                        Toast.makeText(MainActivity.this, "이메일이나 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
                    } else {
                        registerUser(stEmail, stPassword);
                    }
                }
            });

            Button btnLogin = (Button) findViewById(R.id.btnLogin);
            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stEmail = etEmail.getText().toString();
                    stPassword = etPassword.getText().toString();

                    //Toast.makeText(MainActivity.this, "LOGIN", Toast.LENGTH_SHORT).show();
                    if (stEmail.isEmpty() || stEmail.equals("") || stPassword.isEmpty() || stPassword.equals("")) {
                        Toast.makeText(MainActivity.this, "이메일이나 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
                    } else {
                        userLogin(stEmail, stPassword);
                    }
                }
            });

            callbackManager = CallbackManager.Factory.create();
            final LoginButton fbtnLogin = (LoginButton) findViewById(R.id.facebook_login);
            fbtnLogin.setReadPermissions(Arrays.asList("email","user_friends"));
            fbtnLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    handleFacebookAccessToken(loginResult.getAccessToken());

                    pbLogin.setVisibility(GONE);
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

            Button facebookBtnLogin = (Button)findViewById(R.id.btnLoginFacebook);
            facebookBtnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pbLogin.setVisibility(VISIBLE);
                    fbtnLogin.performClick();
                }
            });

    }

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

    public void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(MainActivity.this, "Authentication success.",
                                    Toast.LENGTH_SHORT).show();
                            if(user != null) {
                                Hashtable<String, String> profile = new Hashtable<String, String>();
                                profile.put("email", user.getEmail());
                                profile.put("photo", "");
                                profile.put("key",user.getUid());
                                myRef.child(user.getUid()).child("profile").setValue(profile);
                                myRef.child(user.getUid()).child("friends").setValue(null);
                            }
                        }
                    }
                });
    }

    private void userLogin(String email, String password){
        pbLogin.setVisibility(VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        //pbLogin.setVisibility(View.INVISIBLE);
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            pbLogin.setVisibility(GONE);

                        } else {

                            Intent in = new Intent(MainActivity.this, TabActivity.class);
                            startActivity(in);
                            pbLogin.setVisibility(GONE);
                            finish();
                        }

                        // ...
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken accessToken) {
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()) {
                    //user = task.getResult().getUser();
                    Log.v(TAG,myRef.child(user.getUid()).toString());
                    //if(mAuth.) {
                        Hashtable<String, String> profile = new Hashtable<String, String>();
                        profile.put("email", user.getEmail());
                        profile.put("photo", "");
                        profile.put("key", user.getUid());
                        myRef.child(user.getUid()).child("profile").setValue(profile);

                        Hashtable<String, String> friends = new Hashtable<String, String>();
                        friends.put("email", "email");
                        friends.put("photo", "");
                        friends.put("key", "Frkey");
                        myRef.child(user.getUid()).child("friends").child(friends.get("key")).setValue(friends);

                        Log.v(TAG,"First Facebook Login");

                    //} else {
                        //Log.v(TAG,"Not first Facebook Login");

                   //}
                    Log.v(TAG,"Facebook Login task success");

                } else {
                    Log.v(TAG,"Facebook Login task fail");
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode,resultCode,data);
    }
}
