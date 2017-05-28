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
import android.widget.TextView;

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


public class FriendsFragment extends Fragment {

    String TAG = getClass().getSimpleName();

    TextView tvFriendcnt;
    EditText etSearch;

    RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;
    FriendAdapter mFAdapter;

    List<Friend> mFriend;

    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference myRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_friends, container, false);

        user = FirebaseAuth.getInstance().getCurrentUser();
        Log.d(TAG, user.getDisplayName());

        tvFriendcnt = (TextView) v.findViewById(R.id.text_friend_num);
        etSearch  = (EditText) v.findViewById(R.id.etSearch);

        //RecyclerView 사용하기 위한 사전 작업 (크기 고정, 어댑터 설정 등등)
        mRecyclerView = (RecyclerView) v.findViewById(R.id.Friend_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mFriend = new ArrayList<>();

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");
        //친구 데이터 리스트의 정보를 추가하기 위해 처음에 한번만 FirebaseDB호출
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null) {
                    //친구 데이터 리스트를 초기화 해주고 Firebase내 자신의 친구 리스트 목록을 가져와 추가
                    mFriend.clear();
                    for (DataSnapshot dataSnapshot2 : dataSnapshot.child(user.getUid()).child("friends").getChildren()) {
                        Friend friend = dataSnapshot2.getValue(Friend.class);

                        // [START_EXCLUDE]
                        // Update RecyclerView

                        mFriend.add(friend);
                    }

                } else {
                    Log.d(TAG, "FriendsList is Empty");

                }

                //친구 데이터 리스트 작업이 다 끝나고 나면 어댑터에 리스트를 집어놓고 RecyclerView에 적용
                mFAdapter = new FriendAdapter(mFriend, getActivity());
                mRecyclerView.setAdapter(mFAdapter);
                mFAdapter.notifyDataSetChanged();
                tvFriendcnt.setText(mFAdapter.getItemCount()+"명");
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
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) {


            }

            //검색 텍스트가 바뀌고 난 뒤 어댑터 내의 filter함수 호출
            @Override
            public void afterTextChanged(Editable s) {
                mFAdapter.filter(etSearch.getText().toString().toLowerCase(Locale.getDefault()));
            }
        });

        //Friend Fragment 우측하단의 +버튼 누를 시 친구 추가화면으로 넘어가고 현재화면 종료
        FloatingActionButton addFriendButton = (FloatingActionButton) v.findViewById(R.id.floatingActionButton);
        addFriendButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    Intent intent = new Intent(v.getContext(), AddfriendActivitiy.class);
                    getContext().startActivity(intent);
                    getActivity().finish();
                }

                return false;
            }
        });


        return v;
    }


}
