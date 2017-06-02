package testchat.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Hashtable;

public class AddfriendActivitiy extends AppCompatActivity {
    String TAG = getClass().getSimpleName();

    ListView mListView;

    EditText etSearch;
    Button btnSearch;
    Toolbar toolbar;

    RelativeLayout rlResult;
    ImageView ivUser;
    TextView tvUser;
    TextView tvUsermail;
    Button btnAddFriend;

    RelativeLayout rlConfirm;
    Button btnYes;
    Button btnNo;

    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference myRef;

    String personEmail;
    String personName;
    String personPhoto;
    String personUid;

    String userName;
    String userPhoto;
    String userFacebookid;

    //th ver.
    Context mContext;
    //th fin

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addfriend);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("친구 검색");
        toolbar.setTitleTextColor(Color.WHITE);

        user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");

        //layout objects 생성 및 호출
        etSearch  = (EditText) findViewById(R.id.etSearch);
        btnSearch = (Button) findViewById(R.id.btnSearch);

        rlResult = (RelativeLayout) findViewById(R.id.searchResult);
        ivUser = (ImageView) findViewById(R.id.ivUser);
        tvUser = (TextView) findViewById(R.id.tvUser);
        tvUsermail = (TextView) findViewById(R.id.tvUsermail);
        btnAddFriend = (Button) findViewById(R.id.btnAddFriend);
        btnAddFriend.setEnabled(false);

        rlConfirm = (RelativeLayout) findViewById(R.id.confirmPop);
        btnYes = (Button) findViewById(R.id.btnYes);
        btnNo = (Button) findViewById(R.id.btnNo);
        //th ver.
        mContext = this;
        //th fin

        etSearch.setOnKeyListener(new View.OnKeyListener(){
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
                    btnSearch.callOnClick();
                }
                return false;
            }
        });
        //검색 버튼 클릭 이벤트
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //검색한 아이디를 받음
                rlConfirm.setVisibility(View.GONE);
                rlResult.setVisibility(View.INVISIBLE);
                final String searchEmail = etSearch.getText().toString();

                //Firebase의 users db목록을 읽음
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //유저의 사진URL을 받음
                        userName = dataSnapshot.child(user.getUid()).child("profile").child("name").getValue().toString();
                        userPhoto = dataSnapshot.child(user.getUid()).child("profile").child("photo").getValue().toString();

                        //모든 유저들 중 검색한 아이디와 동일한 계정을 찾음
                        for (DataSnapshot person : dataSnapshot.getChildren()) {
                            personEmail = person.child("profile").child("email").getValue().toString();
                            //동일할 시 결과창의 이미지뷰와 텍스트뷰를 설정해준 뒤 활성화
                            if(personEmail.equals(searchEmail)) {
                                personName = person.child("profile").child("name").getValue().toString();
                                personPhoto = person.child("profile").child("photo").getValue().toString();
                                personUid = person.child("profile").child("uid").getValue().toString();
                                if (dataSnapshot.child(user.getUid()).child("friends").child(personUid).getValue() == null) {
                                    if (personPhoto.equals("None")) {
                                        Drawable defaultImg = getResources().getDrawable(R.drawable.ic_person_black_24dp);
                                        ivUser.setImageDrawable(defaultImg);
                                    } else {
                                        Glide.with(AddfriendActivitiy.this).load(personPhoto).into(ivUser);
                                    }
                                    tvUser.setText(personName);

                                    rlResult.setVisibility(View.VISIBLE);
                                    btnAddFriend.setEnabled(true);

                                    break;
                                } else {
                                    //만약 이미 친구면 다시 검색
                                    Toast.makeText(AddfriendActivitiy.this,"이미 친구입니다",Toast.LENGTH_SHORT).show();
                                    etSearch.setText("");
                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        //친구추가 버튼 클릭 시 확인 창 활성화
        btnAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tvConfirmName = (TextView) findViewById(R.id.tvConfirmUser);
                tvConfirmName.setText(tvUser.getText().toString());
                ImageView ivConfirmImage = (ImageView) findViewById(R.id.ivConfirmUser);
                if(personPhoto.equals("None")) {
                    Drawable defaultImg = getResources().getDrawable(R.drawable.ic_person_black_24dp);
                    ivConfirmImage.setImageDrawable(defaultImg);
                } else {
                    Glide.with(AddfriendActivitiy.this).load(personPhoto).into(ivConfirmImage);
                }
                rlConfirm.setVisibility(View.VISIBLE);
            }
        });

        //'네'버튼 클릭 시
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //자신의 친구리스트DB에 해당 유저 추가
                Hashtable<String, String> friend = new Hashtable<String, String>();
                friend.put("email", personEmail);
                friend.put("name", personName);
                friend.put("photo", personPhoto);
                myRef.child(user.getUid()).child("friends").child(personUid).setValue(friend);

                //해당 유저의 친구리스트DB에 자신을 추가
                Hashtable<String, String> me = new Hashtable<String, String>();
                me.put("email", user.getEmail());
                me.put("name", user.getDisplayName());
                me.put("photo", userPhoto);
                myRef.child(personUid).child("friends").child(user.getUid()).setValue(me);

                //팝업과 함께 전체화면 초기화
                Toast.makeText(AddfriendActivitiy.this, "추가 되었습니다", Toast.LENGTH_SHORT).show();
                rlConfirm.setVisibility(View.GONE);
                rlResult.setVisibility(View.INVISIBLE);
                btnAddFriend.setEnabled(false);
                etSearch.setText("");

            }
        });
        //'아니오'버튼 클릭 시 확인 창 비활성화
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rlConfirm.setVisibility(View.GONE);
            }
        });
    }

    //툴바에서 뒤로가기 버튼 클릭 시 TabActivity(FriendsFragment)화면 재실행
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_backbutton:
                Intent intent = new Intent(AddfriendActivitiy.this, TabActivity.class);
                startActivity(intent);

                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    //폰의 뒤로가기 버튼 클릭 시 TabActivity(FriendsFragment)화면 재실행
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(AddfriendActivitiy.this, TabActivity.class);
        startActivity(intent);

        finish();
    }
}
