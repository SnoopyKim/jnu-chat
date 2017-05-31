package testchat.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Hashtable;

/**
 * Created by TH-home on 2017-05-08.
 */

public class SigninActivity extends AppCompatActivity {
    String TAG = this.getClass().getSimpleName();
    String stEmail;
    String stPassword;
    String stName;

    EditText etEmail;
    EditText etPassword;
    EditText etName;

    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference myRef;
    FirebaseAuth.AuthStateListener authListener;
    FirebaseUser user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        etEmail = (EditText) findViewById(R.id.edit_email);
        etPassword = (EditText) findViewById(R.id.edit_password);
        etName = (EditText) findViewById(R.id.edit_name);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");
        mAuth = FirebaseAuth.getInstance();
        //등록이 완료되면 Main으로 돌아감
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull final FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if(user != null) {
                    Intent intent = new Intent(SigninActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };

        //회원가입 버튼 클릭 시
        Button btnRegister = (Button) findViewById(R.id.button_signin);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stEmail = etEmail.getText().toString();
                stPassword = etPassword.getText().toString();
                stName = etName.getText().toString();

                //이메일, 비밀번호, 이름 부분은 필수 입력
                if (stEmail.isEmpty() || stEmail.equals("") || stPassword.isEmpty() || stPassword.equals("") || stName.isEmpty() || stName.equals("")) {
                    Toast.makeText(SigninActivity.this, "이메일이나 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();

                } else {
                    registerUser(stEmail, stPassword);

                }
            }
        });

    }

    //Firebase내의 계정에 해당 정보로 등록(추가)
    public void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            Toast.makeText(SigninActivity.this, "등록에 실패하였습니다",
                                    Toast.LENGTH_SHORT).show();

                        } else {
                            //작성한 이름을 Firebase 계정에 DisplayName으로 설정
                            UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(stName).build();
                            task.getResult().getUser().updateProfile(profileUpdate);

                            //Firebase DB에 정보를 저장
                            String userUid = task.getResult().getUser().getUid();
                            Hashtable<String, String> profile = new Hashtable<String, String>();
                            profile.put("email", stEmail);
                            profile.put("facebook_id", "None");
                            profile.put("name", stName);
                            profile.put("photo", "None");
                            profile.put("uid",userUid);

                            myRef.child(userUid).child("profile").setValue(profile);
                            myRef.child(userUid).child("friends").setValue("");
                            myRef.child(userUid).child("room").setValue("");

                            Toast.makeText(SigninActivity.this, "등록되셨습니다",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }
    @Override
    public void onResume(){
        super.onResume();
        mAuth.addAuthStateListener(authListener);
    }
    @Override
    public void onStop(){
        super.onStop();
        if(authListener != null){
            mAuth.removeAuthStateListener(authListener);
        }
    }

    //폰의 뒤로가기 버튼 클릭 시 TabActivity(FriendsFragment)화면 재실행
    @Override
    public void onBackPressed() {
        finish();
    }
}
