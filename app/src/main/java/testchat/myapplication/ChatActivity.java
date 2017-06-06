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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

//채팅방 화면 Activity
public class ChatActivity extends AppCompatActivity{
    String TAG = this.getClass().getSimpleName();

    int pre;
    String allFriendName;

    private RecyclerView mRecyclerView;
    private ChatAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    EditText etText;
    EditText etSearch;
    Button btnSend;
    Button btnSearch;

    List<Chat> mChat;
    FirebaseDatabase database;
    DatabaseReference myRef;
    FirebaseUser user;

    //생성 시
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(toolbar);

        //전 화면에서 넘겨준 데이터(채팅방 고유키, 상대방 이름)를 받음
        Intent in = getIntent();
        pre = in.getIntExtra("pre",0);
        allFriendName = in.getStringExtra("allfriendName");
        final String friendName = in.getStringExtra("friendName");
        final String roomKey = in.getStringExtra("roomKey");
        Log.d("roomKey",roomKey);
        if(allFriendName.equals(""))
            getSupportActionBar().setTitle("나");
        else
            getSupportActionBar().setTitle(allFriendName.substring(0,(allFriendName.length()-2)));

        //사용자의 이메일과 이름 초기화
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("chats").child(roomKey);
        user = FirebaseAuth.getInstance().getCurrentUser();

        etText = (EditText) findViewById(R.id.etText);
        etSearch = (EditText) findViewById(R.id.etSearch);
        btnSearch = (Button) findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchString = etSearch.getText().toString();
                if(searchString.length()==0)
                {
                    Toast.makeText(ChatActivity.this, "검색할 내용을 입력해주세요", Toast.LENGTH_SHORT).show();
                    //mAdapter.filter(searchString.toLowerCase(Locale.getDefault()));
                }
                else {
                    mAdapter.filter(searchString.toLowerCase(Locale.getDefault()));
                }
            }
        });

        //보내기 버튼 클릭 시 EditText에 적힌 내용을 DB로 보냄
        btnSend = (Button) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String stText = etText.getText().toString();
                if (stText.equals("") || stText.isEmpty()){

                    Toast.makeText(ChatActivity.this, "내용을 입력해주세요", Toast.LENGTH_SHORT).show();
                } else {
                    //보낼 당시의 시각을 DB 내 child로 하고 채팅 정보를 업로드(추가)하고 EditText초기화
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String formattedDate = df.format(c.getTime());

                    Hashtable<String, String> chat = new Hashtable<String, String>();
                    chat.put("uid",user.getUid());
                    chat.put("name",user.getDisplayName());
                    chat.put("text",stText);
                    myRef.child("chatInfo").child(formattedDate).setValue(chat);
                    etText.setText("");
                }
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mChat = new ArrayList<>();
        mAdapter = new ChatAdapter(mChat,user.getUid(),ChatActivity.this);
        mRecyclerView.setAdapter(mAdapter);

        //해당 고유키의 채팅방 DB에서 child가 추가될 때마다 데이터 리스트에 추가하고 어댑터로 RecyclerView에 그려짐
        myRef.child("chatInfo").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // A new comment has been added, add it to the displayed list
                String uid = dataSnapshot.child("uid").getValue().toString();
                String name = dataSnapshot.child("name").getValue().toString();
                String text = dataSnapshot.child("text").getValue().toString();
                String time = dataSnapshot.getKey();
                Chat chat = new Chat(uid,name,text,time);

                // [START_EXCLUDE]
                // Update RecyclerView
                mChat.add(chat);
                mRecyclerView.scrollToPosition(mChat.size()-1);
                mAdapter.notifyItemInserted(mChat.size() - 1);
                // [END_EXCLUDE]
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_back_button,menu);
        return true;
    }

    //폰에서의 뒤로가기 버튼 클릭 시 종료
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_backbutton:
                setResult(pre);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    //폰의 뒤로가기 버튼 클릭 시 TabActivity(FriendsFragment)화면 재실행
    @Override
    public void onBackPressed() {
        setResult(pre);
        finish();
    }
}
