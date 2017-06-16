package testchat.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.List;
import java.util.Locale;

/**
 * @Name    FriendsFragment
 * @Usage   Init variables and add adapter to recyclerview
 * @Layout  fragment_chats.xml
 * @Comment Iterate all of room for find my room
 * TODO add if no chatting room, show tvNoChat
 * */
public class RoomsFragment extends Fragment {
    //채팅방 리스트 화면 Fragment
    String TAG = getClass().getSimpleName();

    RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;
    EditText etSearch;

    List<Room> mRoom;

    FirebaseUser user;
    FirebaseDatabase database;
    RoomAdapter mRAdapter;

    DatabaseReference myRef;

    TextView tvChat;
    TextView tvNoChat;

    //Fragment생성시 View (FriendsFragment와 동일)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_chats, container, false);

        user = FirebaseAuth.getInstance().getCurrentUser();

        etSearch = (EditText) v.findViewById(R.id.etSearch);
        //tvNoChat = (TextView) v.findViewById(R.id.text_noFriend);
        tvChat = (TextView) v.findViewById(R.id.text_Room);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.Chat_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRoom = new ArrayList<>();

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("chats");

        //Firebase에 DB 정보 호출 (처음 한번만)
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                if(dataSnapshot.getValue() != null) {
                    mRoom.clear();
                    //DB에 존재하는 채팅방 중 참여자에 자신이 있는 경우에만 추가
                    for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {
                        //방에 내가 없으면 패스
                        if(dataSnapshot2.child("people").child(user.getUid()).getValue() == null) continue;

                        List <String> roomPeople = new ArrayList<String>();
                        List <String> chatInfo = new ArrayList<String>();
                        String roomKey = dataSnapshot2.getKey();
                        String myTime = dataSnapshot2.child("people").child(user.getUid()).child("in").getValue().toString();
                        //채팅방에 채팅 기록이 없으면 패스
                        for(DataSnapshot chat : dataSnapshot2.child("chatInfo").getChildren()) {
                            chatInfo.add(chat.getKey());
                        }
                        if (chatInfo.size() == 0) continue;
                        //채팅방의 마지막 채팅시간이 자신의 입장 시간보다 빠르면 패스
                        String lastTime = chatInfo.get(chatInfo.size()-1);
                        if (myTime.compareTo(lastTime) > 0) continue;
                        //앞 모든 패스 경우에 속하지 않을 때 룸리스트에 추가
                        for(DataSnapshot roomPerson : dataSnapshot2.child("people").getChildren()) {
                            roomPeople.add(roomPerson.child("name").getValue().toString());
                        }
                        //참여자, 채팅방 고유키, 존재여부, 사진정보를 가지고 Room형식의 데이터를 생성한 뒤 리스트에 추가

                        Room room = new Room(roomPeople, roomKey);
                        mRoom.add(room);

                    }
                } else {
                    //empty room list
                }
                //채팅방 데이터 리스트를 완성한 뒤 어댑터에 넣고 RecyclerView에 어댑터를 장착
                mRAdapter = new RoomAdapter(mRoom, getActivity());
                mRecyclerView.setAdapter(mRAdapter);
                mRAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Failed to read value

                Toast.makeText(getContext(),"정보를 불러들이는데 실패했습니다. 잠시후 다시 시도해 주세요",Toast.LENGTH_SHORT).show();

            }
        });

        //Chat Fragment 우측하단의 +버튼 (채팅방 추가 화면으로 넘어감)
        FloatingActionButton addRoomButton = (FloatingActionButton) v.findViewById(R.id.floatingActionButton);
        addRoomButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    Intent intent = new Intent(v.getContext(), AddchatActivity.class);
                    startActivityForResult(intent, 0);
                }

                return false;
            }
        });

        return v;
    }

    /**
     * @Name    ChangeET
     * @Usage   Change UI : tvChat's contents
     * @Param   search string, <- Tabactivity's etSearch
     * @return  void
     * */
    public void ChangeET(String s){
        if(s.length()==0)
        {
            tvChat.setText("대화");
            mRAdapter.filter(s.toLowerCase(Locale.getDefault()));
        }
        else {
            tvChat.setText("검색 결과");
            mRAdapter.filter(s.toLowerCase(Locale.getDefault()));
        }
    }

    /**
     * @Name    onResume
     * @Usage   real time update -> order/time/image etc...
     * @return  void
     * @Comment Override life-cycle function
     *           Same as this.onCreateView -> myRef.addListenerForSingleValueEvent -> ValueEventListener
     * */

    @Override
    public void onResume() {
        //앱이 채팅화면에서 다시 돌아올 때 갱신된 데이터로 정렬 (새로 생긴 방은 X)
        super.onResume();
        if (mRAdapter!=null)
            mRAdapter.sortRoom();

    }
}
