package testchat.myapplication;

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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Hashtable;
import java.util.regex.Pattern;

/**
 * Created by TH-home on 2017-05-08.
 */
/**
 * @Name    SigninActivity
 * @Usage   Register firebase authentication and users profile
 * @Layout  activity_singin.xml
 * */
public class SigninActivity extends AppCompatActivity {
    String TAG = this.getClass().getSimpleName();
    String stEmail;
    String stPassword;
    String stName;
    String stBirth;
    String stPhone;
    //gender =  : not select , gender = 0 : male , gender = 1 : female
    String stGender = "None";
    //id_check = 1 : 중복없음, = -1 : 중복
    int id_check;

    EditText etEmail;
    EditText etPassword;
    EditText etName;
    EditText etBirth;
    EditText etPasswordCheck;
    EditText etPhone;
    TextView tvPasswordError;
    Toolbar toolbar;
    ToggleButton tb_m;//male
    ToggleButton tb_f;//female
    ProgressBar pbRegister;
    Button btnRegister;

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
        etBirth = (EditText) findViewById(R.id.edit_birth);
        etName = (EditText) findViewById(R.id.edit_name);
        etPhone = (EditText) findViewById(R.id.edit_phone);
        tvPasswordError = (TextView) findViewById(R.id.text_error);
        pbRegister = (ProgressBar)findViewById(R.id.pbRegister);
        btnRegister = (Button) findViewById(R.id.button_signin);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");
        mAuth = FirebaseAuth.getInstance();

        //등록이 완료되면 Main으로 돌아감
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull final FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if(user != null) {
                    setResult(1);
                    if(authListener != null){
                        mAuth.removeAuthStateListener(authListener);
                    }
                    finish();
                }
            }
        };

        //ID 중복확인
        Button btnIdcheck = (Button) findViewById(R.id.button_check);
        btnIdcheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etEmail.getText().toString().contains("@") && etEmail.getText().toString().contains(".")) {
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            id_check = 1;
                            for (DataSnapshot user : dataSnapshot.getChildren()) {
                                if (user.child("profile").child("email").getValue() == null) continue;

                                if (etEmail.getText().toString().equals(user.child("profile").child("email").getValue().toString())) {
                                    id_check = -1;
                                    break;
                                } else {
                                    id_check = 1;
                                }
                            }
                            if (id_check == 1)
                                Toast.makeText(SigninActivity.this, "사용 가능한 이메일입니다", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(SigninActivity.this, "중복된 이메일입니다", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {
                    Toast.makeText(SigninActivity.this, "이메일 형식이 올바르지 않습니다", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //select gender toggle
        tb_f = (ToggleButton) findViewById(R.id.button_gender_f);
        tb_f.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stGender="Female";
                toggleButtonProcess(tb_f,tb_m);
            }
        });
        tb_m = (ToggleButton) findViewById(R.id.button_gender_m);
        tb_m.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stGender="Male";
                toggleButtonProcess(tb_m,tb_f);
            }
        });

        //회원가입 버튼 클릭 시
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stEmail = etEmail.getText().toString();
                stPassword = etPassword.getText().toString();
                stName = etName.getText().toString();
                stBirth = etBirth.getText().toString();
                stPhone = etPhone.getText().toString();
                /**
                 * Check Validity
                 * important error's error code will big number
                 * IsEmptyEtField
                 */
                int errorCode=0;
                if(stEmail.isEmpty() || stEmail.equals(""))
                    errorCode+=2;
                if(stPassword.isEmpty() || stPassword.equals(""))
                    errorCode+=4;
                if(stName.isEmpty() || stName.equals(""))
                    errorCode+=8;
                if(!checkBirthForm(stBirth))
                    errorCode+=16;
                if(stGender.equals("None"))
                    errorCode+=32;
                if(stPhone.isEmpty() || stPhone.equals(""))
                    errorCode+=64;
                if (id_check != 1)
                    errorCode+=128;
                if(!stPassword.equals(etPasswordCheck.getText().toString())) {
                    errorCode+=256;
                    tvPasswordError.setVisibility(View.VISIBLE);
                }
                else
                    tvPasswordError.setVisibility(View.INVISIBLE);

                int terrorCode=errorCode;
                if(errorCode-256>=0){
                    errorCode-=256;
                    tvPasswordError.setVisibility(View.VISIBLE);
                }
                if(errorCode-128>=0){
                    errorCode-=128;
                    Toast.makeText(SigninActivity.this, "이메일 확인버튼을 눌러 중복을 확인하세요", Toast.LENGTH_SHORT).show();
                }
                String errorToast="";
                if(errorCode-64>=0){
                    errorCode-=64;
                    errorToast= "핸드폰번호 " + errorToast;
                }
                if(errorCode-32>=0){
                    errorCode-=32;
                    errorToast= "성별 " + errorToast;
                }
                if(errorCode-16>=0){
                    errorCode-=16;
                    errorToast= "생년월일 " + errorToast;
                }
                if(errorCode-8>=0){
                    errorCode-=8;
                    errorToast= "이름 " + errorToast;
                }
                if(errorCode-4>=0){
                    errorCode-=4;
                    errorToast= "비밀번호 " + errorToast;
                }
                if(errorCode-2==0){
                    errorCode-=2;
                    errorToast= "이메일 " + errorToast;
                }
                errorToast = errorToast + "란을 확인해 주세요";
                if(terrorCode==0) {
                    registerUser(stEmail, stPassword);
                    btnRegister.setVisibility(View.GONE);
                    pbRegister.setVisibility(View.VISIBLE);
                }
                else{
                    Toast.makeText(SigninActivity.this, errorToast, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    /**
     * @Name    registerUser
     * @Usage   Regist to firebase authentication
     * @Param   user's email, user's password
     * @return  void
     * */
    //Firebase내의 계정에 해당 정보로 등록(추가)
    public void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(SigninActivity.this, "등록에 실패하였습니다\n" + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            //Reset progress bar
                            btnRegister.setVisibility(View.VISIBLE);
                            pbRegister.setVisibility(View.GONE);

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
                            profile.put("birth",stBirth);
                            profile.put("gender",stGender);
                            profile.put("phone",stPhone);

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
    public void onStart(){
        super.onStart();
        mAuth.addAuthStateListener(authListener);
    }
    @Override
    public void onStop(){
        super.onStop();
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

    /**
     * @Name    toggleButtonProcess
     * @Usage   union two toggle button
     * @Param   to check in button  , to check out button
     * @return  void
     * */
    public void toggleButtonProcess(ToggleButton tb_checking,ToggleButton tb_checkout){
        if(tb_checking.isChecked()){
            if(tb_checkout.isChecked()){
                tb_checkout.setChecked(false);
                tb_checkout.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_light_gray));
                tb_checkout.setTextColor(getResources().getColor(R.color.colorDarkGray));
                tb_checkout.setPadding(0,0,0,0);

                tb_checking.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_dark_gray));
                tb_checking.setTextColor(getResources().getColor(R.color.colorLightGray));
                tb_checking.setPadding(0,0,0,0);
                //gender="Female";
            }
            else {
                tb_checking.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_dark_gray));
                tb_checking.setTextColor(getResources().getColor(R.color.colorLightGray));
                tb_checking.setPadding(0,0,0,0);
                //gender="Female";
            }
        }
        else{
            tb_checking.setChecked(false);
            tb_checking.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_light_gray));
            tb_checking.setTextColor(getResources().getColor(R.color.colorDarkGray));
            tb_checking.setPadding(0,0,0,0);
            stGender="None";
        }
    }
    /**
     * @Name    checkBirthForm
     * @Usage   validity check - Birth
     * @Param   str = user's birth // from etBirth
     * @return  boolean check result
     * */
    public boolean checkBirthForm(String str){
        if(str.length()!=8){
            return false;
        }
        else{
            //pattern chek
            //none digit
            if(!Pattern.matches("^[0-9]+$", str)){
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
