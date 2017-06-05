package testchat.myapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Created by snoopy on 2017-04-01.
 */

//FriendsFragment에서 RecyclerView의 커스텀Adapter (View에서 한 row마다의 데이터나 이벤트 적용)
public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {

    //th
    FragmentManager mFragmentManager;
    //th fin

    //친구 데이터 리스트 두개 (하나는 백업용)
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

    //리스트로 된 View들을 통합적으로 보관하는 객체
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvEmail;
        public ImageView ivUser;
        public LinearLayout overall;
        public TextView tvFristname;

        //순서대로 칸, 이름, 이미지를 레이아웃에서 불러와 생성
        public ViewHolder(View itemView) {
            super(itemView);
            overall = (LinearLayout) itemView.findViewById(R.id.friend_overall);
            tvEmail = (TextView) itemView.findViewById(R.id.tvEmail);
            ivUser = (ImageView) itemView.findViewById(R.id.ivUser);
            tvFristname = (TextView) itemView.findViewById(R.id.tvFirstname);
        }
    }

    // 커스텀 생성자로 친구 데이터 리스트를 받음
    public FriendAdapter(List<Friend> aFriend, Context context) {
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

        beforeFirstName = new String("");
    }    // 커스텀 생성자로 친구 데이터 리스트를 받음

    public FriendAdapter(List<Friend> aFriend, Context context, FragmentManager aFragmentManager) {
        Comparator<Friend> cmpAsc = new Comparator<Friend>() {
            @Override
            public int compare(Friend o1, Friend o2) {
                //korean -> english sort
                /*String left = o1.getName();
                String right = o2.getName();
                boolean isKoreanLeft=Pattern.matches("[가-힣]",left.substring(0,1));
                boolean isKoreanRight=Pattern.matches("[가-힣]",right.substring(0,1));

                if(isKoreanLeft & isKoreanRight) return o1.getName().compareTo(o2.getName());
                else if(isKoreanLeft)           return o1.getName().compareTo(o2.getName())-10000;
                else if(isKoreanRight)           return o1.getName().compareTo(o2.getName())+10000;
                else                            return o1.getName().compareTo(o2.getName());*/
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
    }

    //VIew생성 및 레이아웃 설정
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

    //친구 데이터 리스트에 따라 row들의 이미지, 텍스트, 이벤트 관리(설정)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        //이름과 이미지를 친구 데이터 리스트에서와 같은 순서로 설정(그림)
        holder.tvEmail.setText(mFriend.get(position).getName());
        holder.tvFristname.setText(mFriend.get(position).getName().substring(0, 1));
        if (beforeFirstName.equals(holder.tvFristname.getText())) {
            holder.tvFristname.setVisibility(View.GONE);
        } else {
            beforeFirstName = holder.tvFristname.getText().toString();
            holder.tvFristname.setVisibility(View.VISIBLE);
        }
        final String stPhoto = mFriend.get(position).getPhoto();
        if (stPhoto.equals("None")) {
            //친구의 이미지 정보가 없을 경우 지정해둔 기본 이미지로
            Drawable defaultImg = context.getResources().getDrawable(R.drawable.ic_person_black_24dp);
            holder.ivUser.setImageDrawable(defaultImg);
        } else {
            Glide.with(context).load(stPhoto).into(holder.ivUser);
        }
        //View(칸) 클릭 시
        holder.overall.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                switch (event.getAction()) {
                    //마우스를 눌렀을 때
                    case MotionEvent.ACTION_DOWN:
                        holder.overall.setBackgroundColor(Color.parseColor("#F5F5F5"));

                        database = FirebaseDatabase.getInstance();
                        userReference = database.getReference("users");
                        chatReference = database.getReference("chats");
                        user = FirebaseAuth.getInstance().getCurrentUser();

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

                        infoDialog = showInfoDialog(v, stFriendUid, stFriendEmail, stFriendname, stFriendPhoto);
                        break;
                }
                return true;
            }
        });
    }

    //리스트 검색 시 호출되는 함수
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
        notifyDataSetChanged();
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mFriend.size();
    }

    //send user info to dialog fragment
    public InfoDialogFragment newInstance(String aID, String aEmail, String aName, String aPhoto) {
        Bundle args = new Bundle();
        args.putString("paramID", aID);
        args.putString("paramEmail", aEmail);
        args.putString("paramName", aName);
        args.putString("paramPhoto", aPhoto);

        InfoDialogFragment fragment = new InfoDialogFragment();
        fragment.setArguments(args);
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

                                } else {
                                    roomKey = chatReference.push().getKey();
                                }
                                if (roomKey != null) {
                                    //채팅을 한적이 없다면 양쪽 사람에게 표시를하고
                                    userReference.child(user.getUid()).child("room").child(stFriendUid).setValue(roomKey);
                                    userReference.child(stFriendUid).child("room").child(user.getUid()).setValue(roomKey);

                                    //DB에 새로운 채팅방을 하나 생성하고 그 고유키를 가지고 ChatActivity로 이동
                                    Hashtable<String, String> myInfo = new Hashtable<String, String>();
                                    myInfo.put("name", user.getDisplayName());
                                    myInfo.put("photo", dataSnapshot.child(user.getUid()).child("profile").child("photo").getValue().toString());
                                    chatReference.child(roomKey).child("people").child(user.getUid()).setValue(myInfo);

                                    Hashtable<String, String> friendInfo = new Hashtable<String, String>();
                                    friendInfo.put("name", stFriendname);
                                    friendInfo.put("photo", stFriendPhoto);
                                    chatReference.child(roomKey).child("people").child(stFriendUid).setValue(friendInfo);

                                    //chatReference.child(roomKey).child("chatInfo").setValue("");

                                    Intent in = new Intent(context, ChatActivity.class);
                                    in.putExtra("pre", 0);
                                    in.putExtra("friendName", stFriendname);
                                    in.putExtra("roomKey", roomKey);
                                    context.startActivity(in);
                                }

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        });
        return fragment;
    }

    //show dialog
    public InfoDialogFragment showInfoDialog(View view, String aID, String aEmail, String aName, String aPhoto) {
        InfoDialogFragment infoDialog = newInstance(aID, aEmail, aName, aPhoto);
        infoDialog.show(mFragmentManager, "infoDialog");
        return infoDialog;
    }


}