package testchat.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by snoopy on 2017-04-01.
 */

//채팅방 리스트 View관리 어댑터
public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.ViewHolder> {

    //FriendAdapter와 동일 (두개의 채팅방 데이터 리스트)
    List<Room> mRoom;
    List<Room> mFilter;
    Context context;

    FirebaseDatabase database;
    DatabaseReference userReference;
    DatabaseReference chatReference;
    FirebaseUser user;

    String roomKey;

    //리스트로 된 View들을 통합적으로 보관하는 객체
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView tvName;
        public ImageView ivUser;
        public LinearLayout overall;
        public TextView tvChatTime;
        public TextView tvLastChat;
        public Button btnDelete;
        //imageview 동그랗게
        //ivUser.setBackground(new ShapeDrawable(new OvalShape()));

        //View에서 쓰이는 layout객체 생성
        public ViewHolder(View itemView) {
            super(itemView);
            overall = (LinearLayout) itemView.findViewById(R.id.room_overall) ;
            tvName = (TextView) itemView.findViewById(R.id.tvName);
            ivUser = (ImageView) itemView.findViewById(R.id.ivUser);
            tvChatTime = (TextView) itemView.findViewById(R.id.tvChattime);
            tvLastChat = (TextView) itemView.findViewById(R.id.tvLatestChat);
            btnDelete = (Button) itemView.findViewById(R.id.btnDelete);
        }
    }

    //커스텀 생성자 (리스트 초기값 설정)
    public RoomAdapter(List<Room> mRoom, Context context) {
        this.mRoom = mRoom;
        this.mFilter = new ArrayList<>();
        this.mFilter.addAll(mRoom);
        this.context = context;
    }

    //View생성 layout파일 지정
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

    //각 View마다 layout객체를 설정 | 데이터 및 이벤트 관리
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        database = FirebaseDatabase.getInstance();
        userReference = database.getReference("users");
        chatReference = database.getReference("chats");
        user = FirebaseAuth.getInstance().getCurrentUser();

        //처음 값 설정하고 넣어줄 때의 roomKey
        roomKey = mRoom.get(position).getKey();

        //채팅방 이름 (참여자들 중 본인빼고 이름 이어붙임)
        List<String> listNames = mRoom.get(position).getPeople();
        final String allFriendName;
        final String stPhoto = mRoom.get(position).getPhoto();

        String stName = "";
        for (String name : listNames) {
            if (!name.equals(user.getDisplayName()))
                stName += (name+", ");
        }
        if(stName.equals("")) {
            allFriendName = "";
            holder.tvName.setText("나");
        } else {
            allFriendName = stName.substring(0,(stName.length()-2));
            holder.tvName.setText(allFriendName);
        }

        if (stPhoto.equals("None")) {
            //친구의 이미지 정보가 없을 경우 지정해둔 기본 이미지로
            Drawable defaultImg = context.getResources().getDrawable(R.drawable.ic_person_black_24dp);
            holder.ivUser.setImageDrawable(defaultImg);
        } else {
            Glide.with(context).load(stPhoto).into(holder.ivUser);
        }
        try {
            holder.tvChatTime.setText(mRoom.get(position).getLastTime().substring(11, 16));
        } catch (java.lang.StringIndexOutOfBoundsException e) {
            holder.tvChatTime.setText(mRoom.get(position).getLastTime());
        }
        holder.tvLastChat.setText(mRoom.get(position).getLastText());
        chatReference.child(roomKey).child("chatInfo").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot != null) {
                    mRoom.get(position).setlastTime(dataSnapshot.getKey());
                    mRoom.get(position).setLastText(dataSnapshot.child("text").getValue().toString());
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //View에서 각 칸을 누를 시의 이벤트
        holder.overall.setOnTouchListener(new View.OnTouchListener()
        {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                switch(event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        //holder.overall.setBackgroundColor(Color.parseColor("#F5F5F5"));

                        break;
                    case MotionEvent.ACTION_UP:
                        holder.overall.setBackgroundColor(Color.WHITE);

                        //채팅방 고유키를 받아 ChatActivity에 넘겨주면서 이동
                        roomKey = mRoom.get(position).getKey();

                        if (user.getPhotoUrl()!=null)
                            chatReference.child(roomKey).child("people").child(user.getUid()).child("photo").setValue(user.getPhotoUrl().toString());
                        else
                            chatReference.child(roomKey).child("people").child(user.getUid()).child("photo").setValue("None");

                        Intent intent = new Intent(context, ChatActivity.class);
                        intent.putExtra("friendName",allFriendName);
                        intent.putExtra("roomKey",roomKey);
                        context.startActivity(intent);

                        break;
                }
                return true;
            }
        });

        //각 칸의 삭제버튼을 누를 시의 이벤트
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatReference.child(roomKey).child("people").child(user.getUid()).removeValue();
                mFilter.remove(position);
                mRoom.remove(position);

                notifyItemRemoved(position);
            }
        });

    }

    //채팅방 검색 함수 (FriendAdapter에서의 filter함수와 동일)
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        mRoom.clear();
        if (charText.length() == 0) {
            mRoom.addAll(mFilter);
        } else {
            for (Room room : mFilter) {
                String name = "";
                for (String fName : room.getPeople()) {
                    if (!fName.equals(user.getDisplayName()))
                        name += fName;
                }
                if (name.toLowerCase().contains(charText)) {
                    mRoom.add(room);
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mRoom.size();
    }
}