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
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
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

    RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;

    List<Friend> mFriend;

    FirebaseUser user;
    FirebaseDatabase database;
    FriendAdapter mFAdapter;

    DatabaseReference myRef;

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
        myRef = database.getReference("users");
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

        //FacebookSdk.addLoggingBehavior(LoggingBehavior.REQUESTS);
        GraphRequest request = GraphRequest.newMyFriendsRequest(AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONArrayCallback() {
                    @Override
                    public void onCompleted(JSONArray objects, GraphResponse response) {
                        for(int i=0; i<objects.length(); i++) {
                            try {
                                JSONObject f_info = objects.getJSONObject(i);

                                Hashtable<String, String> friend = new Hashtable<String, String>();
                                friend.put("name", f_info.getString("name"));
                                friend.put("facebook_id", f_info.getString("id"));
                                friend.put("photo", f_info.getJSONObject("picture").getJSONObject("data").getString("url"));

                                myRef.child(user.getUid()).child("friends").child(friend.get("facebook_id")).setValue(friend);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Log.d("GraphRequest_Friends", response.toString());
                        }
                    }
                });
        Bundle param = new Bundle();
        param.putString("fields","email,name,id");
        request.setParameters(param);
        request.executeAsync();

        //방법2
        /*
        GraphRequest request = new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/friends",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        Log.d("GraphRequest:",response.toString());
                    }
                }
        );
        request.executeAsync();
        */



        return v;
    }


}
