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

        Button btnRegister = (Button) findViewById(R.id.button_signin);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stEmail = etEmail.getText().toString();
                stPassword = etPassword.getText().toString();
                stName = etName.getText().toString();

                if (stEmail.isEmpty() || stEmail.equals("") || stPassword.isEmpty() || stPassword.equals("") || stName.isEmpty() || stName.equals("")) {
                    Toast.makeText(SigninActivity.this, "이메일이나 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();

                } else {
                    registerUser(stEmail, stPassword);

                }
            }
        });

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
                            Toast.makeText(SigninActivity.this, "등록에 실패하였습니다",
                                    Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(SigninActivity.this, "등록되셨습니다",
                                    Toast.LENGTH_SHORT).show();

                            String userUid = task.getResult().getUser().getUid();
                            Hashtable<String, String> profile = new Hashtable<String, String>();
                            profile.put("email", stEmail);
                            profile.put("facebook_id", "");
                            profile.put("name", stName);
                            profile.put("photo", "");
                            profile.put("key",userUid);

                            myRef.child(userUid).child("profile").setValue(profile);
                            myRef.child(userUid).child("friends").setValue("");
                            myRef.child(userUid).child("room").setValue("");

                            Intent intent = new Intent(SigninActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                    }
                });
    }

}
