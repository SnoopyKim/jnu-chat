package testchat.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.Hashtable;
import java.util.regex.Pattern;

/**
 * Created by TH-home on 2017-05-08.
 */

public class SigninActivity extends AppCompatActivity {
    String TAG = this.getClass().getSimpleName();
    String stEmail;
    String stPassword;
    String stName;
    //gender = -1 : not select , gender = 0 : male , gender = 1 : female
    int gender;

    EditText etEmail;
    EditText etPassword;
    EditText etName;
    EditText etPasswordCheck;
    EditText etBirth;
    TextView tvPasswordError;
    Toolbar toolbar;
    ToggleButton tb_m;
    ToggleButton tb_f;


    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference myRef;
    FirebaseAuth.AuthStateListener authListener;
    FirebaseUser user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("회원가입");
        toolbar.setTitleTextColor(Color.WHITE);

        etEmail = (EditText) findViewById(R.id.edit_email);
        etPassword = (EditText) findViewById(R.id.edit_password);
        etPasswordCheck = (EditText) findViewById(R.id.edit_password_check);
        etName = (EditText) findViewById(R.id.edit_name);
        etBirth = (EditText) findViewById(R.id.edit_birth);
        tvPasswordError = (TextView) findViewById(R.id.text_error);

        //select gender toggle
        tb_f = (ToggleButton) findViewById(R.id.button_gender_f);
        tb_f.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gender=1;
                toggleButtonProcess(tb_f,tb_m);
            }
        });
        tb_m = (ToggleButton) findViewById(R.id.button_gender_m);
        tb_m.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gender=0;
                toggleButtonProcess(tb_m,tb_f);
            }
        });

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");
        mAuth = FirebaseAuth.getInstance();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull final FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if(user != null) {

                    Intent intent = new Intent(SigninActivity.this, MainActivity.class);
                    startActivity(intent);
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

                if(!checkBirthForm(etBirth.getText().toString()))
                    Toast.makeText(SigninActivity.this, "생년월일을 확인해주세요", Toast.LENGTH_SHORT).show();

                //이메일, 비밀번호, 이름 부분은 필수 입력
                else if (stEmail.isEmpty() || stEmail.equals("") || stPassword.isEmpty() || stPassword.equals("") || stName.isEmpty() || stName.equals("")) {
                    Toast.makeText(SigninActivity.this, "이메일이나 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();

                } else {
                    if(stPassword.equals(etPasswordCheck.getText().toString()))
                        registerUser(stEmail, stPassword);
                    else
                        tvPasswordError.setVisibility(View.VISIBLE);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_close_button,menu);
        return true;
    }

    //toolbar에서의 x 버튼 클릭 시 종료
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_closebutton:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void toggleButtonProcess(ToggleButton tb_checking,ToggleButton tb_checkout){
        if(tb_checking.isChecked()){
            if(tb_checkout.isChecked()){
                tb_checkout.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_light_gray));
                tb_checkout.setTextColor(getResources().getColor(R.color.colorDarkGray));
                tb_checkout.setPadding(0,0,0,0);
                tb_checking.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_dark_gray));
                tb_checking.setTextColor(getResources().getColor(R.color.colorLightGray));
                tb_checking.setPadding(0,0,0,0);
                gender=1;
            }
            else {
                tb_checking.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_dark_gray));
                tb_checking.setTextColor(getResources().getColor(R.color.colorLightGray));
                tb_checking.setPadding(0,0,0,0);
                gender=1;
            }
        }
        else{
            tb_checking.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_light_gray));
            tb_checking.setTextColor(getResources().getColor(R.color.colorDarkGray));
            tb_checking.setPadding(0,0,0,0);
            gender=-1;
        }
    }
    public boolean checkBirthForm(String str){
        if(str.length()!=8){
            return false;
        }
        else{
            if(!Pattern.matches("^[0-9]+$", str)){
                //숫자아님
                return false;
            }
            int birth = Integer.parseInt(str);
            int month = (birth%10000)/100;
            int day = (birth%100);
            if(month<1 || month>12) return false;
            if(day<1 || day>31)     return false;
            return  true;
        }
    }
}
