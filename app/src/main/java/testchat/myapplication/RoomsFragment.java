package testchat.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

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

    //Fragment생성시 View (FriendsFragment와 동일)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_chats, container, false);

        user = FirebaseAuth.getInstance().getCurrentUser();

        etSearch = (EditText) v.findViewById(R.id.etSearch);

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
                        //roomText는 임시로 존재여부만 설정 (이후 마지막 채팅내용으로 할 예정)
                        boolean roomText = dataSnapshot2.child("chatInfo").hasChildren();
                        for(DataSnapshot roomPerson : dataSnapshot2.child("people").getChildren()) {
                            if(roomPerson.getKey().equals(user.getUid())) {
                                myRoom = true;
                            }
                            roomPeople.add(roomPerson.child("name").getValue().toString());
                        }
                        //참여자, 채팅방 고유키, 존재여부를 가지고 Room형식의 데이터를 생성한 뒤 리스트에 추가
                        if(myRoom) {
                            Room room = new Room(roomPeople,roomKey,roomText);
                            mRoom.add(room);
                        }
                    }
                } else {

                    Log.d(TAG, "ChatList is Empty");
                }
                //채팅방 데이터 리스트를 완성한 뒤 어댑터에 넣고 RecyclerView에 어댑터를 장착
                mRAdapter = new RoomAdapter(mRoom, getActivity());
                mRecyclerView.setAdapter(mRAdapter);
                mRAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Failed to read value
                Log.w(TAG,"Failed to read value", databaseError.toException());

            }
        });

        //검색버튼의 텍스트 변화 시
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) {


            }

            @Override
            public void afterTextChanged(Editable s) {
                //텍스트가 바뀐 후 해당 텍스트로 filter함수 호출
                mRAdapter.filter(etSearch.getText().toString().toLowerCase(Locale.getDefault()));
            }
        });

        //Chat Fragment 우측하단의 +버튼 (채팅방 추가 화면으로 넘어감)
        FloatingActionButton addRoomButton = (FloatingActionButton) v.findViewById(R.id.floatingActionButton);
        addRoomButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    Intent intent = new Intent(v.getContext(), AddchatActivity.class);
                    getContext().startActivity(intent);
                    getActivity().finish();
                }

                return false;
            }
        });

        return v;
    }

}
