package testchat.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

public class AddchatActivity extends AppCompatActivity {
    String TAG = this.getClass().getSimpleName();

    EditText etSearch;
    TextView tvAdded;
    Button btnAddChat;

    RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;
    AddchatAdapter mAAdapter;

    List<Friend> mFriend;
    List<Friend> mResult;

    DatabaseReference myRef;
    DatabaseReference chatRef;
    FirebaseUser user;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addchat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("채팅 초대");

        etSearch = (EditText) findViewById(R.id.etSearch);
        tvAdded = (TextView) findViewById(R.id.tvAdded);
        btnAddChat = (Button) findViewById(R.id.btnAddChat);

        //RecyclerView 사용하기 위한 사전 작업 (크기 고정, 어댑터 설정 등등)
        mRecyclerView = (RecyclerView) findViewById(R.id.Friend_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mFriend = new ArrayList<>();

        user = FirebaseAuth.getInstance().getCurrentUser();
        myRef = FirebaseDatabase.getInstance().getReference("users");
        //친구 데이터 리스트의 정보를 추가하기 위해 처음에 한번만 FirebaseDB호출
        myRef.child(user.getUid()).child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null) {
                    //친구 데이터 리스트를 초기화 해주고 Firebase내 자신의 친구 리스트 목록을 가져와 추가
                    mFriend.clear();
                    for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {
                        String uid = dataSnapshot2.getKey();
                        String email = dataSnapshot2.child("email").getValue().toString();
                        String name = dataSnapshot2.child("name").getValue().toString();
                        String photo = dataSnapshot2.child("photo").getValue().toString();
                        Friend friend = new Friend(uid,email,name,photo);

                        // [START_EXCLUDE]
                        // Update RecyclerView

                        mFriend.add(friend);
                    }

                } else {
                    Log.d(TAG, "FriendsList is Empty");
                }

                //친구 데이터 리스트 작업이 다 끝나고 나면 어댑터에 리스트를 집어놓고 RecyclerView에 적용
                mAAdapter = new AddchatAdapter(mFriend, AddchatActivity.this);
                mRecyclerView.setAdapter(mAAdapter);
                mAAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Failed to read value
                Log.w(TAG,"Failed to read value", databaseError.toException());

            }
        });

        //검색 텍스트에 변화에 따른 이벤트 리스너
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                mAAdapter.filter(s.toString().toLowerCase(Locale.getDefault()));
            }
        });

        //추가 버튼 설정 (클릭 시 추가 된 친구들로 채팅방 생성하고 ChatActivity로 넘어감
        btnAddChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mResult = mAAdapter.getResult();
                if(mResult.size() == 0) {
                    Toast.makeText(AddchatActivity.this,"친구를 추가하세요",Toast.LENGTH_SHORT).show();

                } else {
                    chatRef = FirebaseDatabase.getInstance().getReference("chats");
                    String roomKey = chatRef.push().getKey();
                    String roomName = "";
                    for (Friend friend : mResult) {
                        roomName += (friend.getName()+", ");
                    }

                    Hashtable<String, String> myInfo = new Hashtable<String, String>();
                    myInfo.put("name", user.getDisplayName());
                    if (user.getPhotoUrl() != null) {
                        myInfo.put("photo", user.getPhotoUrl().toString());
                    } else {
                        myInfo.put("photo", "None");
                    }
                    chatRef.child(roomKey).child("people").child(user.getUid()).setValue(myInfo);
                    for (Friend friend : mResult) {
                        Hashtable<String, String> friendInfo = new Hashtable<String, String>();
                        friendInfo.put("name", friend.getName());
                        friendInfo.put("photo", friend.getPhoto());
                        chatRef.child(roomKey).child("people").child(friend.getUid()).setValue(friendInfo);
                    }

                    Intent intent = new Intent(AddchatActivity.this, ChatActivity.class);
                    intent.putExtra("friendName", roomName.substring(0,roomName.length()-2));
                    intent.putExtra("roomKey", roomKey);
                    startActivity(intent);
                    setResult(2);
                    finish();

                }
            }
        });
    }

    //툴바에서 뒤로가기 버튼 클릭 시 TabActivity(FriendsFragment)화면 재실행
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_backbutton:
                setResult(2);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    //폰의 뒤로가기 버튼 클릭 시 TabActivity(FriendsFragment)화면 재실행
    @Override
    public void onBackPressed() {
        setResult(2);
        finish();
    }

}
