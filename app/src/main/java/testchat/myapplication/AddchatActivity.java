package testchat.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class AddchatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addchat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


    }

    //툴바에서 뒤로가기 버튼 클릭 시 TabActivity(FriendsFragment)화면 재실행
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_backbutton:
                Intent intent = new Intent(AddchatActivity.this, TabActivity.class);
                startActivity(intent);

                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    //폰의 뒤로가기 버튼 클릭 시 TabActivity(FriendsFragment)화면 재실행
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(AddchatActivity.this, TabActivity.class);
        startActivity(intent);

        finish();
    }

}
