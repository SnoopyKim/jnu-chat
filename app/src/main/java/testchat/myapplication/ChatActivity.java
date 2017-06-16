package testchat.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
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
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import static testchat.myapplication.R.drawable.ic_highlight_off_black_24dp;
import static testchat.myapplication.R.drawable.ic_photo_white_24px;

//채팅방 화면 Activity
public class ChatActivity extends AppCompatActivity{
    String TAG = this.getClass().getSimpleName();

    boolean onebyone = false;
    boolean addMode = false;

    private RecyclerView mRecyclerView;
    private RecyclerView aRecyclerView;
    private ChatAdapter mAdapter;
    private AddchatAdapter aAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.LayoutManager aLayoutManager;

    private StorageReference storageRef;

    EditText etText;
    EditText etSearch;
    Button btnSend;
    Button btnSearch;

    ImageButton btnFile;

    List<Chat> mChat;
    List<String> inFriend;
    List<Friend> aFriend;
    FirebaseDatabase database;
    DatabaseReference myRef;
    DatabaseReference addRef;
    DatabaseReference loginRef;
    FirebaseUser user;

    String roomKey;
    String myTime;
    String stText;
    String fileSelected = "false";

    //생성 시
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(toolbar);

        //Permission 재확인 (파일 업로드/다운로드 및 이미지 그리기 관련)
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.MANAGE_DOCUMENTS)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.MANAGE_DOCUMENTS)) {

            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.MANAGE_DOCUMENTS}, 1);
            }
        }

        //전 화면에서 넘겨준 데이터(채팅방 고유키, 상대방 이름)를 받음
        Intent in = getIntent();
        final String friendName = in.getStringExtra("friendName");
        roomKey = in.getStringExtra("roomKey");
        myTime = in.getStringExtra("in");
        Log.d("roomKey",roomKey);
        if(friendName.equals(""))
            getSupportActionBar().setTitle("나");
        else
            getSupportActionBar().setTitle(friendName);

        user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("chats").child(roomKey);
        addRef = database.getReference("users");

        inFriend = new ArrayList<>();
        myRef.child("people").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot user : dataSnapshot.getChildren()) {
                    inFriend.add(user.getKey());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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
                }
                else {
                    String charText = searchString.toLowerCase(Locale.getDefault());
                    for (int pos=0; pos<mChat.size(); pos++) {
                        String text = mChat.get(pos).getText();
                        if (text.toLowerCase().contains(charText)) {
                            mRecyclerView.scrollToPosition(pos);
                        }
                    }
                }
            }
        });

        //이미지 버튼 클릭 시 기기 내 저장소로 이동
        btnFile = (ImageButton) findViewById(R.id.imageButton);
        btnFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!fileSelected.equals("false")) {
                    fileSelected = "false";
                    btnFile.setImageResource(ic_photo_white_24px);
                    etText.setEnabled(true);
                    etText.setText("");

                } else {
                    Intent fileIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    fileIntent.setType("*/*");
                    fileIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivityForResult(Intent.createChooser(fileIntent, "파일을 선택하세요"), 0);

                }
            }
        });

        //보내기 버튼 클릭 시 EditText에 적힌 내용을 DB로 보냄
        btnSend = (Button) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stText = etText.getText().toString();

                if (stText.equals("") || stText.isEmpty()){
                    Toast.makeText(ChatActivity.this, "내용을 입력해주세요", Toast.LENGTH_SHORT).show();

                } else {
                    //보낼 당시의 시각을 DB 내 child로 하고 채팅 정보를 업로드(추가)하고 EditText초기화
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                    String formattedDate = df.format(c.getTime()).replace(".",":");

                    if (!fileSelected.equals("false")) {

                        uploadFile(stText, formattedDate);

                        btnFile.setImageResource(ic_photo_white_24px);
                        btnSend.setVisibility(View.GONE);
                        findViewById(R.id.pbSend).setVisibility(View.VISIBLE);
                        etText.setEnabled(true);

                    } else {
                        Hashtable<String, String> chat = new Hashtable<String, String>();
                        chat.put("uid", user.getUid());
                        chat.put("name", user.getDisplayName());
                        chat.put("text", stText);
                        chat.put("file", fileSelected);
                        myRef.child("chatInfo").child(formattedDate).setValue(chat);

                    }
                    etText.setText("");
                }
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(false);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mChat = new ArrayList<>();
        mAdapter = new ChatAdapter(mChat,user.getUid(),roomKey,ChatActivity.this);
        mRecyclerView.setAdapter(mAdapter);

        //해당 고유키의 채팅방 DB에서 child가 추가될 때마다 데이터 리스트에 추가하고 어댑터로 RecyclerView에 그려짐
        myRef.child("chatInfo").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // A new comment has been added, add it to the displayed list
                String uid = dataSnapshot.child("uid").getValue().toString();
                String name = dataSnapshot.child("name").getValue().toString();
                String text = dataSnapshot.child("text").getValue().toString();
                String file = dataSnapshot.child("file").getValue().toString();
                String time = dataSnapshot.getKey();

                if (myTime.compareTo(time) < 0) {
                    Chat chat = new Chat(uid, name, text, file, time);

                    mChat.add(chat);
                    mRecyclerView.scrollToPosition(mChat.size() - 1);
                    mAdapter.notifyItemInserted(mChat.size() - 1);
                }
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

    public void uploadFile(String stFile, final String stDate) {
        Uri file = Uri.parse(stFile);
        storageRef = FirebaseStorage.getInstance().getReference("chats")
                .child(roomKey).child(user.getUid()).child(stDate);

        UploadTask uploadTask = storageRef.putFile(file);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ChatActivity.this,"업로드에 실패했습니다",Toast.LENGTH_SHORT).show();

                fileSelected = "false";
                findViewById(R.id.pbSend).setVisibility(View.GONE);
                btnSend.setVisibility(View.VISIBLE);

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri fileUri = taskSnapshot.getDownloadUrl();

                Hashtable<String, String> chat = new Hashtable<String, String>();
                chat.put("uid", user.getUid());
                chat.put("name", user.getDisplayName());
                chat.put("text", fileUri.toString());
                chat.put("file", fileSelected);
                myRef.child("chatInfo").child(stDate).setValue(chat);

                fileSelected = "false";
                findViewById(R.id.pbSend).setVisibility(View.GONE);
                btnSend.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            Uri fileUri = data.getData();
            fileSelected = getContentResolver().getType(fileUri);
            if(fileSelected.equals("none"))
                Toast.makeText(ChatActivity.this,"알 수 없는 유형입니다", Toast.LENGTH_SHORT).show();
            else {
                btnFile.setImageResource(ic_highlight_off_black_24dp);
                Drawable imgButton = DrawableCompat.wrap(btnFile.getDrawable());
                DrawableCompat.setTint(imgButton, getResources().getColor(R.color.conceptRedHint));
                btnFile.setImageDrawable(imgButton);
                etText.setText(fileUri.toString());
                etText.setEnabled(false);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        addRef.child(user.getUid()).child("room").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot room : dataSnapshot.getChildren()) {
                    if (roomKey.equals(room.getValue().toString())) {
                        onebyone = true;
                        break;
                    }
                }
                if(!onebyone) {
                    getMenuInflater().inflate(R.menu.toolbar_add_button, menu);
                    getMenuInflater().inflate(R.menu.toolbar_back_button, menu);
                } else {
                    getMenuInflater().inflate(R.menu.toolbar_back_button, menu);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        return true;
    }

    //폰에서의 뒤로가기 버튼 클릭 시 종료
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_backbutton:
                finish();
                break;
            case R.id.action_addbutton:
                if (!addMode) {
                    addMode = true;
                    findViewById(R.id.LLsend).setVisibility(View.GONE);
                    findViewById(R.id.RLadd).setVisibility(View.VISIBLE);
                    friendAddMode();

                } else {
                    addMode = false;
                    findViewById(R.id.LLsend).setVisibility(View.VISIBLE);
                    findViewById(R.id.RLadd).setVisibility(View.GONE);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    public void friendAddMode () {
        EditText etAdd = (EditText)findViewById(R.id.etAdd);
        Button btnAdd = (Button)findViewById(R.id.btnAdd);

        aRecyclerView = (RecyclerView) findViewById(R.id.add_recycler_view);
        aRecyclerView.setHasFixedSize(true);
        aLayoutManager = new LinearLayoutManager(this);
        aRecyclerView.setLayoutManager(aLayoutManager);

        aFriend = new ArrayList<>();
        addRef.child(user.getUid()).child("friends").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null) {
                    //친구 데이터 리스트를 초기화 해주고 Firebase내 자신의 친구 리스트 목록을 가져와 추가
                    aFriend.clear();
                    for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {
                        String uid = dataSnapshot2.getKey();
                        if(!inFriend.contains(uid)) {
                            String email = dataSnapshot2.child("email").getValue().toString();
                            String name = dataSnapshot2.child("name").getValue().toString();
                            String photo = dataSnapshot2.child("photo").getValue().toString();
                            Friend friend = new Friend(uid, email, name, photo);

                            aFriend.add(friend);
                        }
                    }

                } else {
                    Log.d(TAG, "FriendsList is Empty");
                }

                //친구 데이터 리스트 작업이 다 끝나고 나면 어댑터에 리스트를 집어놓고 RecyclerView에 적용
                aAdapter = new AddchatAdapter(aFriend,ChatActivity.this);
                aRecyclerView.setAdapter(aAdapter);
                aAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Failed to read value
                Log.w(TAG,"Failed to read value", databaseError.toException());

            }
        });

        //검색 텍스트에 변화에 따른 이벤트 리스너
        etAdd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                aAdapter.filter(s.toString().toLowerCase(Locale.getDefault()));
            }
        });

        //추가 버튼 설정 (클릭 시 추가 된 친구들을 채팅방DB에 추가)
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Friend> aResult = aAdapter.getResult();
                if(aResult.size() == 0) {
                    Toast.makeText(ChatActivity.this,"친구를 추가하세요",Toast.LENGTH_SHORT).show();

                } else {
                    String newTitle = getSupportActionBar().getTitle().toString();
                    for (Friend friend : aResult) {
                        Calendar c = Calendar.getInstance();
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                        String formattedDate = df.format(c.getTime());

                        Hashtable<String, String> friendInfo = new Hashtable<String, String>();
                        friendInfo.put("name", friend.getName());
                        friendInfo.put("in", formattedDate);
                        myRef.child("people").child(friend.getUid()).setValue(friendInfo);

                        inFriend.add(friend.getUid());
                        newTitle.concat((", "+friend.getName()));
                    }
                    addMode = false;
                    findViewById(R.id.LLsend).setVisibility(View.VISIBLE);
                    findViewById(R.id.RLadd).setVisibility(View.GONE);
                    getSupportActionBar().setTitle(newTitle);

                    Toast.makeText(ChatActivity.this,"친구가 추가됐습니다",Toast.LENGTH_SHORT);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    //폰의 뒤로가기 버튼 클릭 시 TabActivity(FriendsFragment)화면 재실행
    @Override
    public void onBackPressed() {
        finish();
    }
}
