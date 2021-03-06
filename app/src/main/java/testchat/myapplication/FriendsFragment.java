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

/**
 * @Name    FriendsFragment
 * @Usage   Init variables and add adapter to recyclerview
 * @Layout  fragment_friends.xml
 * */
public class FriendsFragment extends Fragment {
    String TAG = getClass().getSimpleName();

    TextView tvFriendcnt;
    TextView tvNoFriend;
    TextView tvFriend;

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

        tvFriendcnt = (TextView) v.findViewById(R.id.text_friend_num);
        tvNoFriend = (TextView) v.findViewById(R.id.text_noFriend);
        tvFriend = (TextView) v.findViewById(R.id.text_friend);

        //RecyclerView 사용하기 위한 사전 작업 (크기 고정, 어댑터 설정 등등)
        mRecyclerView = (RecyclerView) v.findViewById(R.id.Friend_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mFriend = new ArrayList<>();

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");
        //친구 데이터 리스트의 정보를 관련 DB의 정보가 업데이트 될 때마다 그림
        myRef.child(user.getUid()).child("friends").addValueEventListener(new ValueEventListener() {
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
                    tvNoFriend.setVisibility(View.VISIBLE);
                }

                //친구 데이터 리스트 작업이 다 끝나고 나면 어댑터에 리스트를 집어놓고 RecyclerView에 적용
                mFAdapter = new FriendAdapter(mFriend, getActivity(), getFragmentManager());
                mRecyclerView.setAdapter(mFAdapter);
                mFAdapter.notifyDataSetChanged();
                tvFriendcnt.setText(mFAdapter.getItemCount()+"명");
                if(mFAdapter.getItemCount()==0)
                    tvNoFriend.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Failed to read value
            }
        });

        //Friend Fragment 우측하단의 +버튼 누를 시 친구 추가화면으로 넘어가고 현재화면 종료
        FloatingActionButton addFriendButton = (FloatingActionButton) v.findViewById(R.id.floatingActionButton);
        addFriendButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    Intent intent = new Intent(v.getContext(), AddfriendActivitiy.class);
                    startActivityForResult(intent, 0);
                }

                return false;
            }
        });

        return v;
    }

    /**
     * @Name    ChangeET
     * @Usage   Change UI : tvFriend's contents / tvFriendcnt's content AND Send adapter to result
     * @Param   search string, etSearch
     * @return  void
     * */
    public void ChangeET(String s){
        if(s.length()==0)
        {
            tvFriend.setText("친구");
            tvFriendcnt.setVisibility(View.VISIBLE);
            mFAdapter.filter(s.toLowerCase(Locale.getDefault()));
        }
        else {
            tvFriend.setText("검색 결과");
            tvFriendcnt.setVisibility(View.GONE);
            mFAdapter.filter(s.toLowerCase(Locale.getDefault()));
        }
    }

}
