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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Created by snoopy on 2017-04-01.
 */

/**
 * @Name    RoomAdapter
 * @Usage   Friend list adapter
 *           manage each view
 *           search list
 * @Layout  my_chatlist_view.xml
 * */
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

    /**
     * @Name    ViewHolder
     * @Usage   Save views in Recycler view and link between variable and layout view(tag)
     * */
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

        //채팅방 이름 (참여자들 중 본인빼고 이름 이어붙임)
        List<String> listNames = mRoom.get(position).getPeople();
        final String allFriendName;
        String stName = "";
        for (String name : listNames) {
            if (!name.equals(user.getDisplayName()))
                stName += (name+", ");
        }

        //Because of jumping  user name in data, user's name room-name is empty
        if(stName.equals("")) {
            allFriendName = "";
            holder.tvName.setText("나");
        } else {
            allFriendName = stName.substring(0,(stName.length()-2));
            holder.tvName.setText(allFriendName);
        }

        //개인 톡방이면 이미지를 상대 프로필 사진으로
        if (listNames.size()<=2) {
            chatReference.child(mRoom.get(position).getKey()).child("people").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot person : dataSnapshot.getChildren()) {
                        String stUid = person.getKey();
                        if (stUid.equals(user.getUid())) continue;
                        userReference.child(person.getKey()).child("profile").child("photo").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue().toString().equals("None")) {
                                    //친구의 이미지 정보가 없을 경우 지정해둔 기본 이미지로
                                    Drawable defaultImg = context.getResources().getDrawable(R.drawable.ic_person_black_24dp);
                                    holder.ivUser.setImageDrawable(defaultImg);
                                } else {
                                    Glide.with(context).load(dataSnapshot.getValue().toString()).into(holder.ivUser);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            //단톡방이면 이미지를 지정해둔 Drawble로
        } else {
            Drawable drawable = context.getResources().getDrawable(R.drawable.ic_people_black_24dp);
            holder.ivUser.setImageDrawable(drawable);

        }
        //최근 챗 시간과 표시내용 그려줌
        chatReference.child(mRoom.get(position).getKey()).child("chatInfo").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> chatInfo = new ArrayList<String>();
                for(DataSnapshot chat : dataSnapshot.getChildren()) {
                    chatInfo.add(chat.getKey());
                }
                String lastTime = chatInfo.get(chatInfo.size()-1);
                holder.tvChatTime.setText(lastTime.substring(11,16));
                mRoom.get(position).setlastTime(lastTime);
                if (dataSnapshot.child(lastTime).child("file").getValue().toString().equals("false")) {
                    holder.tvLastChat.setText(dataSnapshot.child(lastTime).child("text").getValue().toString());
                } else if (dataSnapshot.child(lastTime).child("file").getValue().toString().contains("image")) {
                    holder.tvLastChat.setText("이미지");
                } else {
                    holder.tvLastChat.setText("첨부 파일");
                }
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
                        chatReference.child(roomKey).child("people").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for(DataSnapshot person : dataSnapshot.getChildren()) {
                                    if (person.getKey().equals(user.getUid())) {
                                        Intent in = new Intent(context, ChatActivity.class);
                                        in.putExtra("friendName", allFriendName);
                                        in.putExtra("roomKey", roomKey);
                                        in.putExtra("in", person.child("in").getValue().toString());
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

        //각 칸의 삭제버튼을 누를 시의 이벤트
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roomKey = mRoom.get(position).getKey();
                int friendCnt = mRoom.get(position).getPeople().size();
                if(friendCnt <= 2) {
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String formattedDate = df.format(c.getTime());

                    chatReference.child(roomKey).child("people").child(user.getUid()).child("in").setValue(formattedDate);
                } else {
                    chatReference.child(roomKey).child("people").child(user.getUid()).removeValue();
                }
                mFilter.remove(position);
                mRoom.remove(position);

                notifyDataSetChanged();
            }
        });

    }

    //Sort room by recent chat time
    //정렬하고 순서가 바뀐 데이터 정보를 알림 (onBindView 호출 = 다시 그림)
    public void sortRoom() {
        Comparator<Room> cmpAsc = new Comparator<Room>() {
            @Override
            public int compare(Room o1, Room o2) {
                return o2.getLastTime().compareTo(o1.getLastTime());
            }
        };
        Collections.sort(mRoom, cmpAsc);
        notifyDataSetChanged();
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