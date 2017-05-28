package testchat.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.FacebookSdk;

public class TabActivity extends AppCompatActivity {

    private long lastPressed;
    private Fragment fragment;
    Toolbar toolbar;

    //Navigation에서 Icon클릭시 해당 fragment로 이동
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_friends:
                    fragment = new FriendsFragment();
                    switchFragment(fragment);
                    getSupportActionBar().setTitle("친구");
                    return true;
                case R.id.navigation_chats:
                    fragment = new RoomsFragment();
                    switchFragment(fragment);
                    getSupportActionBar().setTitle("채팅");
                    return true;
                case R.id.navigation_profile:
                    fragment = new ProfileFragment();
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

        //FragmentTransaction의 인스턴스를 Activity로부터 가져옴
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        //toolbar로 activity별 이름 지정 및 icon추가 위함
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("JNU chat");
        toolbar.setTitleTextColor(Color.WHITE);

        //삽입할 뷰에 Friendsfragment를 추가하고(add), 이를 적용(commit) -> 첫화면은 친구 리스트
        fragment = new FriendsFragment();
        fragmentTransaction.add(R.id.content, fragment);
        fragmentTransaction.commit();
        getSupportActionBar().setTitle("친구");


        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
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
            finish();
        }
        Toast.makeText(this, "한번 더 누르면 종료됩니다.",Toast.LENGTH_SHORT).show();
        lastPressed = System.currentTimeMillis();
    }

}
