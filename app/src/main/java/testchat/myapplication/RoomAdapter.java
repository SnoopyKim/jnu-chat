package testchat.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

/**
 * Created by snoopy on 2017-04-01.
 */

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.ViewHolder> {

    List<Room> mRoom;
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
        public TextView tvName;
        public ImageView ivUser;
        public LinearLayout overall;

        //imageview 동그랗게
        //ivUser.setBackground(new ShapeDrawable(new OvalShape()));

        public ViewHolder(View itemView) {
            super(itemView);
            overall = (LinearLayout) itemView.findViewById(R.id.room_overall) ;
            tvName = (TextView) itemView.findViewById(R.id.tvName);
            ivUser = (ImageView) itemView.findViewById(R.id.ivUser);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public RoomAdapter(List<Room> mRoom, Context context) {
        this.mRoom = mRoom;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RoomAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.my_chatlist_view, parent, false);

        //set the view's size, margin, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.tvName.setText(mRoom.get(position).getKey());

        String stKey = mRoom.get(position).getKey();

        database = FirebaseDatabase.getInstance();
        userReference = database.getReference("users");
        chatReference = database.getReference("chats");
        user = FirebaseAuth.getInstance().getCurrentUser();
        roomCheck = true;

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

                        break;
                }
                return true;
            }
        });

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mRoom.size();
    }
}