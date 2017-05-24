package testchat.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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
 * Created by snoopy on 2017-04-01.
 */

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {

    List<Friend> mFriend;
    List<Friend> mFilter;
    Context context;

    FirebaseDatabase database;
    DatabaseReference userReference;
    DatabaseReference chatReference;
    FirebaseUser user;

    String stUserEmail;
    String stFriendEmail;
    String stFriendname;
    String roomKey;

    boolean roomCheck;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView tvEmail;
        public ImageView ivUser;
        public LinearLayout overall;

        //imageview 동그랗게
        //ivUser.setBackground(new ShapeDrawable(new OvalShape()));

        public ViewHolder(View itemView) {
            super(itemView);
            overall = (LinearLayout) itemView.findViewById(R.id.friend_overall) ;
            tvEmail = (TextView) itemView.findViewById(R.id.tvEmail);
            ivUser = (ImageView) itemView.findViewById(R.id.ivUser);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public FriendAdapter(List<Friend> mFriend, Context context) {
        this.mFriend = mFriend;
        this.mFilter = new ArrayList<Friend>();
        this.mFilter.addAll(mFriend);
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public FriendAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.my_friend_view, parent, false);

        //set the view's size, margin, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.tvEmail.setText(mFriend.get(position).getName());

        String stPhoto = mFriend.get(position).getPhoto();

        if(TextUtils.isEmpty(stPhoto)) {
            Drawable defaultImg = context.getResources().getDrawable(R.drawable.ic_person_black_24dp);
            holder.ivUser.setImageDrawable(defaultImg);
        } else {
            Glide.with(context).load(stPhoto).into(holder.ivUser);

        }
        holder.overall.setOnTouchListener(new View.OnTouchListener()
        {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                switch(event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        holder.overall.setBackgroundColor(Color.parseColor("#F5F5F5"));

                        database = FirebaseDatabase.getInstance();
                        userReference = database.getReference("users");
                        chatReference = database.getReference("chats");
                        user = FirebaseAuth.getInstance().getCurrentUser();

                        break;
                    case MotionEvent.ACTION_UP:
                        //set color back to default
                        holder.overall.setBackgroundColor(Color.WHITE);

                        roomKey = chatReference.push().getKey();
                        stUserEmail = user.getEmail().replace(".","|");
                        stFriendEmail = mFriend.get(position).getEmail().replace(".","|");
                        stFriendname = mFriend.get(position).getName();
                        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.getValue()!=null) {
                                    //roomCheck = false;
                                    if(dataSnapshot.child(user.getUid()).child("room").child(stFriendEmail).getValue() != null) {
                                        chatReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if(dataSnapshot != null) {
                                                    for (DataSnapshot room : dataSnapshot.getChildren()) {
                                                        if(room.child("people").getChildrenCount()==2 &&
                                                                room.child("people").hasChild(stFriendEmail) &&
                                                                room.child("people").hasChild(stUserEmail)) {
                                                            roomKey = room.getKey();

                                                            Intent in = new Intent(context, ChatActivity.class);
                                                            in.putExtra("friendName",stFriendname);
                                                            in.putExtra("roomKey", roomKey);
                                                            context.startActivity(in);

                                                            break;
                                                        }
                                                    }
                                                } else {
                                                    //Log.d("chatListener","off");
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    } else {
                                        userReference.child(user.getUid()).child("room").child(stFriendEmail).setValue(stFriendname);

                                        for (DataSnapshot userUid : dataSnapshot.getChildren()) {
                                            if(userUid.child("profile").child("email").getValue().toString().equals(stFriendEmail.replace("|","."))) {
                                                userUid.child("room").child(stUserEmail).getRef().setValue(user.getDisplayName());
                                            }
                                        }

                                        chatReference.child(roomKey).child("people").child(stUserEmail).setValue(user.getDisplayName());
                                        chatReference.child(roomKey).child("people").child(stFriendEmail).setValue(stFriendname);
                                        chatReference.child(roomKey).child("chatInfo").setValue("");

                                        Intent in = new Intent(context, ChatActivity.class);
                                        in.putExtra("friendName",stFriendname);
                                        in.putExtra("roomKey", roomKey);
                                        context.startActivity(in);

                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        break;
                }
                return true;
            }
        });


    }

    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        mFriend.clear();
        if (charText.length() == 0) {
            mFriend.addAll(mFilter);
        } else {
            for (Friend friend : mFilter) {
                String name = friend.getName();
                Log.d("friendName for filter:",name);
                if (name.toLowerCase().contains(charText)) {
                    mFriend.add(friend);
                }
            }
        }
        notifyDataSetChanged();
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {

        return mFriend.size();
    }
}