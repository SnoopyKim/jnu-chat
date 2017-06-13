package testchat.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

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

    boolean myRoom;
    String photo;
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
                        myRoom = false;
                        List <String> roomPeople = new ArrayList<String>();
                        String roomKey = dataSnapshot2.getKey();
                        for(DataSnapshot roomPerson : dataSnapshot2.child("people").getChildren()) {
                            if(roomPerson.getKey().equals(user.getUid())) {
                                myRoom = true;
                            }
                            roomPeople.add(roomPerson.child("name").getValue().toString());
                            if(!roomPerson.child("name").getValue().toString().equals(user.getDisplayName()))
                                photo = roomPerson.child("photo").getValue().toString();
                        }
                        //참여자, 채팅방 고유키, 존재여부, 사진정보, 최근채팅시간을 가지고 Room형식의 데이터를 생성한 뒤 리스트에 추가
                        if(myRoom) {
                            Room room = new Room(roomPeople,roomKey,photo);
                            mRoom.add(room);
                        }
                    }
                } else {

                    Log.d(TAG, "ChatList is Empty");
                }
                //채팅방 데이터 리스트를 완성한 뒤 어댑터에 넣고 RecyclerView에 어댑터를 장착
                mRAdapter = new RoomAdapter(mRoom, getActivity());
                mRecyclerView.setAdapter(mRAdapter);
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null) {
                            Comparator<Room> cmpAsc = new Comparator<Room>() {
                                @Override
                                public int compare(Room o1, Room o2) {
                                    return o2.getLastTime().compareTo(o1.getLastTime());
                                }
                            };
                            Collections.sort(mRoom, cmpAsc);
                            mRAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d("Notify:","Failed");
                    }
                });
                myRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null) {
                            Comparator<Room> cmpAsc = new Comparator<Room>() {
                                @Override
                                public int compare(Room o1, Room o2) {
                                    return o2.getLastTime().compareTo(o1.getLastTime());
                                }
                            };
                            Collections.sort(mRoom, cmpAsc);
                            mRAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d("Notify:","Failed");
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Failed to read value
                Log.w(TAG,"Failed to read value", databaseError.toException());

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

    @Override
    public void onStart() {
        super.onStart();

    }
}
