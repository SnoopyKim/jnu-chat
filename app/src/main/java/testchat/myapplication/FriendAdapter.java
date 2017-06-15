package testchat.myapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

/**
 * Created by snoopy on 2017-04-01.
 */
/**
 * @Name    FriendAdapter
 * @Usage   Friend list adapter
 *           manage each view
 *           search list
 *           show info dialog
 * @Layout  my_friend_view.xml
 * */
public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {

    FragmentManager mFragmentManager;

    //친구 데이터 리스트 두개 (하나는 백업용)
    //@Comment search results are dynamic element. So, Friends list back up to mFilter
    List<Friend> mFriend;
    List<Friend> mFilter;
    Context context;

    //Firebase관련
    FirebaseDatabase database;
    DatabaseReference userReference;
    DatabaseReference chatReference;
    FirebaseUser user;

    private String stFriendUid;
    private String stFriendEmail;
    private String stFriendname;
    private String stFriendPhoto;
    String roomKey;
    String beforeFirstName;

    InfoDialogFragment infoDialog;

    /**
     * @Name    ViewHolder
     * @Usage   Save views in Recycler view and link between variable and layout view(tag)
     * */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvEmail;
        public TextView tvLogin;
        public ImageView ivUser;
        public ImageView ivLogin;
        public LinearLayout overall;
        public TextView tvFirstname;

        //순서대로 칸, 이름, 이미지를 레이아웃에서 불러와 생성
        public ViewHolder(View itemView) {
            super(itemView);
            overall = (LinearLayout) itemView.findViewById(R.id.friend_overall);
            tvEmail = (TextView) itemView.findViewById(R.id.tvEmail);
            tvLogin = (TextView) itemView.findViewById(R.id.tvLoginTime);
            ivUser = (ImageView) itemView.findViewById(R.id.ivUser);
            ivLogin = (ImageView) itemView.findViewById(R.id.ivLogin);
            tvFirstname = (TextView) itemView.findViewById(R.id.tvFirstname);
        }
    }

    // 커스텀 생성자로 친구 데이터 리스트를 받음
    public FriendAdapter(List<Friend> aFriend, Context context, FragmentManager aFragmentManager) {
        Comparator<Friend> cmpAsc = new Comparator<Friend>() {
            @Override
            public int compare(Friend o1, Friend o2) {
                return o1.getName().compareTo(o2.getName());
            }
        };
        Collections.sort(aFriend, cmpAsc);
        this.mFriend = aFriend;
        this.mFilter = new ArrayList<>();
        this.mFilter.addAll(aFriend);
        this.context = context;
        this.mFragmentManager = aFragmentManager;

        beforeFirstName = new String("");
        database = FirebaseDatabase.getInstance();
        userReference = database.getReference("users");
        chatReference = database.getReference("chats");
        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    //VIew생성 및 레이아웃 설정
    @Override
    public FriendAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.my_friend_view, parent, false);

        //set the view's size, margin, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }


    /**
     * @Name    onBindViewHolder
     * @Usage   set holder's element to Firebase data
     * @Param   holder : custom viewholder , position : Friend's index in Friend list
     * @return  void
     * @Comment Because of hover event about viewholder, define setOnTouchListener. but can't implement
     * */
    //친구 데이터 리스트에 따라 row들의 이미지, 텍스트, 이벤트 관리(설정)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        //이름과 이미지를 친구 데이터 리스트에서와 같은 순서로 설정(그림)
        holder.tvEmail.setText(mFriend.get(position).getName());
        holder.tvFirstname.setText(mFriend.get(position).getName().substring(0, 1));

        //first name only show having first user
        if (beforeFirstName.equals(holder.tvFirstname.getText())) {
            holder.tvFirstname.setVisibility(View.GONE);
        } else {
            beforeFirstName = holder.tvFirstname.getText().toString();
            holder.tvFirstname.setVisibility(View.VISIBLE);
        }

        //접속중인 친구
        //all change of data -> refresh
        userReference.child(mFriend.get(position).getUid()).child("profile").child("login")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            String login = dataSnapshot.getValue().toString();
                            if(login.equals("on")) {
                                holder.tvLogin.setVisibility(View.GONE);
                                holder.ivLogin.setVisibility(View.VISIBLE);
                            } else {
                                holder.tvLogin.setVisibility(View.VISIBLE);
                                holder.ivLogin.setVisibility(View.GONE);

                                holder.tvLogin.setText(login.substring(5,16));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        String stPhoto = mFriend.get(position).getPhoto();
        if (stPhoto.equals("None")) {
            //친구의 이미지 정보가 없을 경우 지정해둔 기본 이미지로
            Drawable defaultImg = context.getResources().getDrawable(R.drawable.ic_person_black_24dp);
            holder.ivUser.setImageDrawable(defaultImg);
        } else {
            Glide.with(context).load(stPhoto)
                    .placeholder(R.drawable.ic_person_black_24dp)
                    .into(holder.ivUser);
        }

        //View(칸) 클릭 시
        holder.overall.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                switch (event.getAction()) {
                    //마우스를 눌렀을 때
                    case MotionEvent.ACTION_DOWN:
                        //holder.overall.setBackgroundColor(Color.parseColor("#F5F5F5"));

                        break;
                    //마우스를 땠을 때
                    case MotionEvent.ACTION_UP:
                        //set color back to default
                        holder.overall.setBackgroundColor(Color.WHITE);

                        //변수들의 값을 설정
                        stFriendUid = mFriend.get(position).getUid();
                        stFriendEmail = mFriend.get(position).getEmail();
                        stFriendname = mFriend.get(position).getName();
                        stFriendPhoto = mFriend.get(position).getPhoto();

                        //info dialog show friend's info and offer send message/delete friend function
                        infoDialog = showInfoDialog(v, position, stFriendUid, stFriendEmail, stFriendname, stFriendPhoto);
                        break;
                }
                return true;
            }
        });
    }

    /**
     * @Name    filter
     * @Usage   search friends list
     * @Param   charText : search text <- Tabactivity's changeET's event catch value
     * @return  void
     * @Comment mFilter : backup, mFilter : showing at user
     * */
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        //친구 데이터 리스트를 하나 비운 뒤, 입력한 문자에 따라 백업용으로 다시 친구 데이터 리스트를 만듬
        mFriend.clear();
        if (charText.length() == 0) {
            mFriend.addAll(mFilter);
        } else {
            for (Friend friend : mFilter) {
                String name = friend.getName();
                if (name.toLowerCase().contains(charText)) {
                    mFriend.add(friend);
                }
            }
        }
        //Communicate list view with adapter. Saying "data set Changed!"
        notifyDataSetChanged();
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mFriend.size();
    }

    /**
     * @Name    newInstance
     * @Usage   send user' information to dialog, show dialog and manage (send message/delete friend) function
     * @Param   user data
     * @return  InfoDialogFragment
     * */
    //send user info to dialog fragment
    public InfoDialogFragment newInstance(final int position, final String aID, final String aEmail, final String aName, final String aPhoto) {
        Bundle args = new Bundle();
        args.putString("paramID", aID);
        args.putString("paramEmail", aEmail);
        args.putString("paramName", aName);
        args.putString("paramPhoto", aPhoto);

        InfoDialogFragment fragment = new InfoDialogFragment();
        fragment.setArguments(args);
        //DismissListener : return result of infoDialogFragment
        fragment.setDissmissListner(new DialogDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (getValueForBool("MESSAGE", true)) {
                    //Firebase에 users 부분 데이터 불러오기
                    userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() != null) {
                                //해당 칸의 친구와 대화를 한적이 있는지 없는지
                                if (dataSnapshot.child(user.getUid()).child("room").child(stFriendUid).getValue() != null) {
                                    roomKey = dataSnapshot.child(user.getUid()).child("room").child(stFriendUid).getValue().toString();

                                    chatReference.child(roomKey).child("people").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            for(DataSnapshot person : dataSnapshot.getChildren()) {
                                                if (person.getKey().equals(user.getUid())) {
                                                    Intent in = new Intent(context, ChatActivity.class);
                                                    in.putExtra("friendName", stFriendname);
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

                                } else {
                                    //한 적이 없다면 입장 당시 시간 표시
                                    roomKey = chatReference.push().getKey();

                                    userReference.child(user.getUid()).child("room").child(stFriendUid).setValue(roomKey);
                                    userReference.child(stFriendUid).child("room").child(user.getUid()).setValue(roomKey);

                                    Calendar c = Calendar.getInstance();
                                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                                    String formattedDate = df.format(c.getTime()).replace(".",":");

                                    //DB에 새로운 채팅방을 하나 생성하고 그 고유키를 가지고 ChatActivity로 이동
                                    Hashtable<String, String> myInfo = new Hashtable<String, String>();
                                    myInfo.put("name", user.getDisplayName());
                                    myInfo.put("in", formattedDate);
                                    chatReference.child(roomKey).child("people").child(user.getUid()).setValue(myInfo);

                                    Hashtable<String, String> friendInfo = new Hashtable<String, String>();
                                    friendInfo.put("name", stFriendname);
                                    friendInfo.put("in", formattedDate);
                                    chatReference.child(roomKey).child("people").child(stFriendUid).setValue(friendInfo);

                                    Intent in = new Intent(context, ChatActivity.class);
                                    in.putExtra("friendName", stFriendname);
                                    in.putExtra("roomKey", roomKey);
                                    in.putExtra("in", formattedDate);
                                    context.startActivity(in);
                                }

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                //if click delete button
                else if (getValueForBool("Delete",true)) {
                    String friendUID = getValueForStr("friendUID");
                    userReference.child(user.getUid()).child("friends").child(friendUID).removeValue();
                    userReference.child(friendUID).child("friends").child(user.getUid()).removeValue();
                }
            }
        });
        return fragment;
    }

    //show dialog
    public InfoDialogFragment showInfoDialog(View view, int position, String aID, String aEmail, String aName, String aPhoto) {
        InfoDialogFragment infoDialog = newInstance(position, aID, aEmail, aName, aPhoto);
        infoDialog.show(mFragmentManager, "infoDialog");
        return infoDialog;
    }


}