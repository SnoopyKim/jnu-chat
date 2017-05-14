package testchat.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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

import java.util.List;

/**
 * Created by snoopy on 2017-04-01.
 */

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {

    List<Friend> mFriend;
    Context context;

    FirebaseDatabase database;
    DatabaseReference userReference;
    DatabaseReference chatReference;
    FirebaseUser user;

    String stUserid;
    String stFriendid;
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

        database = FirebaseDatabase.getInstance();
        userReference = database.getReference("users");
        chatReference = database.getReference("chats");
        user = FirebaseAuth.getInstance().getCurrentUser();
        roomCheck = true;

        if(TextUtils.isEmpty(stPhoto)) {
            Drawable defaultImg = context.getResources().getDrawable(R.drawable.ic_person_black_24dp);
            holder.ivUser.setImageDrawable(defaultImg);
            //Glide.with(context).load(R.drawable.ic_person_black_24dp).into(holder.ivUser);
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

                        break;
                    case MotionEvent.ACTION_UP:
                        //set color back to default
                        holder.overall.setBackgroundColor(Color.WHITE);

                        roomKey = chatReference.push().getKey();
                        stFriendid = mFriend.get(position).getFacebook_id();
                        userReference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.getValue()!=null) {
                                    //roomCheck = false;
                                    stUserid = dataSnapshot.child("profile").child("facebook_id").getValue().toString();
                                    if(dataSnapshot.child("room").child(stFriendid).getValue() != null) {
                                        //Log.d("userListener",dataSnapshot.child("room").child(stFriendid).getKey().toString());
                                        chatReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if(dataSnapshot != null) {
                                                    //Log.d("chatListener","on");
                                                    for (DataSnapshot room : dataSnapshot.getChildren()) {
                                                        if(room.child("people").getChildrenCount()==2 &&
                                                                room.child("people").hasChild(stFriendid) &&
                                                                room.child("people").hasChild(stUserid)) {
                                                            roomCheck = true;
                                                            roomKey = room.getKey();

                                                            Intent in = new Intent(context, ChatActivity.class);
                                                            in.putExtra("userid", stUserid);
                                                            in.putExtra("roomkey", roomKey);
                                                            in.putExtra("friendid", stFriendid);
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
                                        roomCheck = false;
                                        //Log.d("userListener","off");
                                        if (!roomCheck) {
                                            userReference.child(user.getUid()).child("room").child(stFriendid).setValue("");

                                            chatReference.child(roomKey).child("people").child(stUserid).setValue("");
                                            chatReference.child(roomKey).child("people").child(stFriendid).setValue("");
                                            chatReference.child(roomKey).child("chatInfo").setValue("");

                                            Intent in = new Intent(context, ChatActivity.class);
                                            in.putExtra("userid", stUserid);
                                            in.putExtra("roomkey", roomKey);
                                            in.putExtra("friendid", stFriendid);
                                            context.startActivity(in);
                                        }
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

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mFriend.size();
    }
}