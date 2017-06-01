package testchat.myapplication;

import android.graphics.Color;
import android.os.Bundle;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.regex.Pattern;

/**
 * Created by th on 2017-04-12.
 */

//비밀번호 찾기 화면
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
    FirebaseDatabase database;
    DatabaseReference myRef;
    FirebaseUser user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_findinfo);

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
                    Toast.makeText(FindinfoActivity.this, "당신의 id는 ******입니다", Toast.LENGTH_SHORT).show();
                }

            }
        });
        Button btnFindPW = (Button) findViewById(R.id.button_passwordfind);
        btnFindID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stID = etID.getText().toString();
                stPhone = etPhone.getText().toString();

                if(stID.isEmpty() || stID.equals("") || stPhone.isEmpty() || stPhone.equals(""))
                    tvPwError.setText("아이디와 휴대폰 번호를 입력해주세요");
                else{
                    Toast.makeText(FindinfoActivity.this, "당신의 비밀번호는는 ******입니다", Toast.LENGTH_SHORT).show();
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
