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
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.LoggingBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;


public class FriendsFragment extends Fragment {

    String TAG = getClass().getSimpleName();

    TextView tvFriendcnt;

    RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;

    List<Friend> mFriend;

    FirebaseUser user;
    FirebaseDatabase database;
    FriendAdapter mFAdapter;

    DatabaseReference myRef;

    Intent in;
    String providerId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_friends, container, false);

        user = FirebaseAuth.getInstance().getCurrentUser();
        in = getActivity().getIntent();
        providerId = in.getStringExtra("providerId");

        tvFriendcnt = (TextView) v.findViewById(R.id.text_friend_num);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.Friend_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mFriend = new ArrayList<>();

        // specify an adapter (see also next example)
        mFAdapter = new FriendAdapter(mFriend, getActivity());
        mRecyclerView.setAdapter(mFAdapter);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                if(dataSnapshot.getValue() != null) {
                    if (providerId.equals("facebook")) {
                        FacebookSdk.addLoggingBehavior(LoggingBehavior.REQUESTS);
                        GraphRequest request = GraphRequest.newMyFriendsRequest(AccessToken.getCurrentAccessToken(),
                                new GraphRequest.GraphJSONArrayCallback() {
                                    @Override
                                    public void onCompleted(JSONArray objects, GraphResponse response) {
                                        if (objects!=null) {
                                            for (int i = 0; i < objects.length(); i++) {
                                                try {
                                                    JSONObject f_info = objects.getJSONObject(i);

                                                    Hashtable<String, String> friend = new Hashtable<String, String>();
                                                    for (DataSnapshot users : dataSnapshot.getChildren()) {
                                                        if (users.child("profile").child("facebook_id").getValue().equals(f_info.getString("id"))) {
                                                            String friendUid = users.getKey();
                                                            String friendName = users.child("profile").child("name").getValue().toString();
                                                            String friendEmail = users.child("profile").child("email").getValue().toString();
                                                            String friendFacebookid = users.child("profile").child("facebook_id").getValue().toString();
                                                            String friendPhoto = users.child("profile").child("photo").getValue().toString();

                                                            friend.put("email", friendEmail);
                                                            friend.put("name", friendName);
                                                            friend.put("facebook_id", friendFacebookid);
                                                            friend.put("photo", friendPhoto);
                                                            myRef.child(user.getUid()).child("friends").child(friendUid).setValue(friend);
                                                        }
                                                    }

                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            Log.d(TAG, "GraphRequest for friend : Success");
                                        }
                                    }
                                });
                        Bundle param = new Bundle();
                        param.putString("fields","id");
                        request.setParameters(param);
                        request.executeAsync();
                    }

                    mFriend.clear();
                    for (DataSnapshot dataSnapshot2 : dataSnapshot.child(user.getUid()).child("friends").getChildren()) {
                        Friend friend = dataSnapshot2.getValue(Friend.class);

                        // [START_EXCLUDE]
                        // Update RecyclerView

                        mFriend.add(friend);
                        mFAdapter.notifyItemInserted(mFriend.size() - 1);
                    }
                    tvFriendcnt.setText(mFAdapter.getItemCount()+"명");

                } else {
                    tvFriendcnt.setText(mFAdapter.getItemCount()+"명");
                    Log.d(TAG, "FriendsList is Empty");

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Failed to read value
                Log.w(TAG,"Failed to read value", databaseError.toException());

            }
        });

        //Friend Fragment 우측하단의 +버튼
        FloatingActionButton addFriendButton = (FloatingActionButton) v.findViewById(R.id.floatingActionButton);
        addFriendButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    Intent intent = new Intent(v.getContext(), AddfriendActivitiy.class);
                    getContext().startActivity(intent);
                }

                return false;
            }
        });


        return v;
    }


}
