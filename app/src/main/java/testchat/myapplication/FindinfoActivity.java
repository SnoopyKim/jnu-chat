package testchat.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Pattern;

/**
 * Created by th on 2017-04-12.
 */

/**
 * @Name    FindinfoActivity
 * @Usage   Find ID/Reset Password
 * @Layout  activity_findinfo.xml
 * @Comment Reset password using Email of user id part, so user must using correct and exist own Email adrress
 * */
public class FindinfoActivity extends AppCompatActivity {

    EditText etName;
    EditText etBirth;
    TextView tvIdError;
    EditText etID;
    EditText etPhone;
    TextView tvPwError;

    Toolbar toolbar;

    String stName;
    String stBirth;
    String stID;
    String stPhone;
    String TAG = this.getClass().getSimpleName();

    FirebaseAuth mAuth;
    DatabaseReference myRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_findinfo);

        mAuth = FirebaseAuth.getInstance();
        myRef = FirebaseDatabase.getInstance().getReference("users");

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("아이디/비밀번호 찾기");
        toolbar.setTitleTextColor(Color.WHITE);

        etName = (EditText) findViewById(R.id.edit_name);
        etBirth = (EditText) findViewById(R.id.edit_birth) ;
        tvIdError = (TextView) findViewById(R.id.text_warning_id);

        etID = (EditText) findViewById(R.id.edit_id);
        etPhone = (EditText) findViewById(R.id.edit_phone) ;
        tvPwError = (TextView) findViewById(R.id.text_warning_password);

        //Find ID
        Button btnFindID = (Button) findViewById(R.id.button_idfind);
        btnFindID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stName = etName.getText().toString();
                stBirth = etBirth.getText().toString();

                if(stName.isEmpty() || stName.equals("") || stBirth.isEmpty() || stBirth.equals(""))
                    tvIdError.setText("이름과 생년월일을 입력해주세요");
                else if(!checkBirthForm(stBirth))
                    tvIdError.setText("생년월일을 확인해주세요");
                else{
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //Check all user data equal stName
                            for (DataSnapshot user : dataSnapshot.getChildren()) {
                                if (user.child("profile").child("name").getValue().toString().equals(stName)
                                        && user.child("profile").child("facebook_id").getValue().toString().equals("None")) {
                                    Toast.makeText(FindinfoActivity.this, "당신의 아이디는"
                                            + user.child("profile").child("email").getValue().toString() + "입니다", Toast.LENGTH_LONG).show();
                                    etName.setText("");
                                    etBirth.setText("");
                                }
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

            }
        });
        //Reset Password
        Button btnFindPW = (Button) findViewById(R.id.button_passwordfind);
        btnFindPW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stID = etID.getText().toString();
                stPhone = etPhone.getText().toString();

                if(stID.isEmpty() || stID.equals("") || stPhone.isEmpty() || stPhone.equals(""))
                    tvPwError.setText("아이디와 휴대폰 번호를 입력해주세요");
                else{
                    /**
                     * mAuth.sendPasswordResetEmail
                     * @Reference   https://firebase.google.com/docs/reference/js/firebase.auth.Auth
                     * @Return      firebase.Promise containing void
                     * @Comment     비밀번호 찾기는 안되고 재설정은 가능함 (문제는 재설정 작업이 이메일로 링크를 보내는 식이라 계정이 없는 이메일이면 받지를 못함...)
                     *               User must correct and exist email for reset password
                     */
                    mAuth.sendPasswordResetEmail(stID).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Toast.makeText(FindinfoActivity.this, "당신의 비밀번호가 초기화 됐습니다\n" +
                                        "메일을 확인하세요", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

            }
        });
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
