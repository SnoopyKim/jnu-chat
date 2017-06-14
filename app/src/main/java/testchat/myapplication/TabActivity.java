package testchat.myapplication;

import android.*;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TabActivity extends AppCompatActivity {

    private long lastPressed;
    private Fragment fragment;
    Toolbar toolbar;
    EditText etSearch;

    FirebaseUser user;
    DatabaseReference loginRef;

    //Navigation에서 Icon클릭시 해당 fragment로 이동
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_friends:
                    fragment = new FriendsFragment();
                    findViewById(R.id.searchbox_ll).setVisibility(View.VISIBLE);
                    etSearch.setVisibility(View.VISIBLE);
                    switchFragment(fragment);
                    getSupportActionBar().setTitle("친구");
                    return true;
                case R.id.navigation_chats:
                    fragment = new RoomsFragment();
                    findViewById(R.id.searchbox_ll).setVisibility(View.VISIBLE);
                    etSearch.setVisibility(View.VISIBLE);
                    switchFragment(fragment);
                    getSupportActionBar().setTitle("채팅");
                    return true;
                case R.id.navigation_profile:
                    fragment = new ProfileFragment();
                    findViewById(R.id.searchbox_ll).setVisibility(View.GONE);
                    etSearch.setVisibility(View.GONE);
                    switchFragment(fragment);
                    getSupportActionBar().setTitle("프로필");
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_tab);

        etSearch = (EditText) findViewById(R.id.etSearch);

        //FragmentTransaction의 인스턴스를 Activity로부터 가져옴
        FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        //toolbar로 activity별 이름 지정 및 icon추가 위함
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("JNU chat");
        toolbar.setTitleTextColor(Color.WHITE);

        user = FirebaseAuth.getInstance().getCurrentUser();
        loginRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("profile").child("login");

        //삽입할 뷰에 Friendsfragment를 추가하고(add), 이를 적용(commit) -> 첫화면은 친구 리스트
        if(fragment == null) {
            fragment = new FriendsFragment();
            fragmentTransaction.add(R.id.content, fragment);
            fragmentTransaction.commit();
            getSupportActionBar().setTitle("친구");
        }


        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //검색 텍스트에 변화에 따른 이벤트 리스너
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) {}
            //검색 텍스트 바뀔때마다 fragment에 string 보내기
            @Override
            public void afterTextChanged(Editable s) {
                String current = getSupportActionBar().getTitle().toString();
                switch (current) {
                    case "친구":
                        ((FriendsFragment) fragment).ChangeET(s.toString()); break;
                    case "채팅":
                        ((RoomsFragment) fragment).ChangeET(s.toString()); break;
                    default:
                        break;
                }
            }
        });
        //String token = PushFirebaseInstanceID.getInstance.getToken();
    }

    //fragment 이동 함수(transaction 수행)
    public void switchFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        //Activity는 그대로, 안의 Fragment만 바꿈
        transaction.replace(R.id.content, fragment);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        //2초안에 뒤로가기 버튼을 두번 누르면 종료
        if (System.currentTimeMillis() - lastPressed < 2000) {
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = df.format(c.getTime());

            loginRef.setValue(formattedDate);

            moveTaskToBack(true);
        }
        Toast.makeText(this, "한번 더 누르면 종료됩니다.",Toast.LENGTH_SHORT).show();
        lastPressed = System.currentTimeMillis();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(c.getTime());

        loginRef.setValue(formattedDate);

    }

    @Override
    protected void onResume() {
        super.onResume();

        loginRef.setValue("on");

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("onActivityResult:","resultCode:"+resultCode);
        switch(resultCode) {
            case 1:
                fragment = new FriendsFragment();
                findViewById(R.id.searchbox_ll).setVisibility(View.VISIBLE);
                etSearch.setVisibility(View.VISIBLE);
                switchFragment(fragment);
                getSupportActionBar().setTitle("친구");
                break;
            case 2:
                fragment = new RoomsFragment();
                findViewById(R.id.searchbox_ll).setVisibility(View.VISIBLE);
                etSearch.setVisibility(View.VISIBLE);
                switchFragment(fragment);
                getSupportActionBar().setTitle("채팅");
                break;
            default:
                fragment = new ProfileFragment();
                findViewById(R.id.searchbox_ll).setVisibility(View.GONE);
                etSearch.setVisibility(View.GONE);
                switchFragment(fragment);
                getSupportActionBar().setTitle("프로필");
                break;

        }
    }


}
