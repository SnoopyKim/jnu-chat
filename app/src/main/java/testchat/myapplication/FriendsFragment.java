package testchat.myapplication;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class FriendsFragment extends Fragment {

    String TAG = getClass().getSimpleName();

    RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;

    List<Friend> mFriend;

    FirebaseUser user;
    FirebaseDatabase database;
    FriendAdapter mFAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_friends, container, false);

        user = FirebaseAuth.getInstance().getCurrentUser();

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
        DatabaseReference myRef = database.getReference("users");
        myRef.child(user.getUid()).child("friends").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                if(dataSnapshot.getValue() != null) {
                    String value = dataSnapshot.getValue().toString();
                    Log.d(TAG, "FriendsList is: " + value);

                    mFriend.clear();
                    for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {

                        String value2 = dataSnapshot2.getValue().toString();
                        Log.d(TAG, "Friend is: " + value2);
                        Friend friend = dataSnapshot2.getValue(Friend.class);

                        // [START_EXCLUDE]
                        // Update RecyclerView

                        mFriend.add(friend);
                        mFAdapter.notifyItemInserted(mFriend.size() - 1);
                    }
                } else {

                    Log.d(TAG, "FriendsList is Empty");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Failed to read value
                Log.w(TAG,"Failed to read value", databaseError.toException());

            }
        });

        //페이스북 Graph API 받아오기
        FacebookSdk.addLoggingBehavior(LoggingBehavior.REQUESTS);
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                Log.d("GraphRequest",object.toString() + " " + response.toString());
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields","id,name,link");
        request.setParameters(parameters);
        request.executeAsync();

        return v;
    }


}
