package testchat.myapplication;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatsFragment extends Fragment {

    String TAG = getClass().getSimpleName();

    RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;

    List<Room> mRoom;

    FirebaseUser user;
    FirebaseDatabase database;
    RoomAdapter mRAdapter;

    DatabaseReference myRef;

    boolean myRoom;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_chats, container, false);

        user = FirebaseAuth.getInstance().getCurrentUser();

        mRecyclerView = (RecyclerView) v.findViewById(R.id.Chat_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRoom = new ArrayList<>();

        // specify an adapter (see also next example)
        mRAdapter = new RoomAdapter(mRoom, getActivity());
        mRecyclerView.setAdapter(mRAdapter);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("chats");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                if(dataSnapshot.getValue() != null) {
                    mRoom.clear();
                    for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {
                        myRoom = false;
                        List <String> roomPeople = new ArrayList<String>();
                        String roomKey = dataSnapshot2.getKey().toString();
                        boolean roomText = dataSnapshot2.child("chatInfo").hasChildren();
                        for(DataSnapshot roomPerson : dataSnapshot2.child("people").getChildren()) {
                            if(roomPerson.getValue().toString().equals(user.getDisplayName())) {
                                myRoom = true;
                            }
                            roomPeople.add(roomPerson.getValue().toString());
                        }
                        Room room = new Room(roomPeople,roomKey,roomText);

                        // [START_EXCLUDE]
                        // Update RecyclerView
                        if(myRoom) {
                            mRoom.add(room);
                            mRAdapter.notifyItemInserted(mRoom.size() - 1);
                        }
                    }
                } else {

                    Log.d(TAG, "ChatList is Empty");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Failed to read value
                Log.w(TAG,"Failed to read value", databaseError.toException());

            }
        });



        return v;
    }

}
