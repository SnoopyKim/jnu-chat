package testchat.myapplication;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

/**
 * Created by snoopy on 2017-04-01.
 */

//말풍선 View에서의 데이터 및 레이아웃 관리
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    List<Chat> mChat;
    String stUid;
    Context context;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;
        public TextView tvName;
        public TextView tvTime;
        public ImageView ivUser;
        public ViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.mTextView);
            tvName = (TextView) itemView.findViewById(R.id.tvName);
            tvTime = (TextView) itemView.findViewById(R.id.tvChatTime);
            ivUser = (ImageView) itemView.findViewById(R.id.ivUser);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ChatAdapter(List<Chat> mChat, String uid, Context context) {
        this.mChat = mChat;
        this.stUid = uid;
        this.context = context;
    }

    //말풍선이 자기꺼면 1, 다른사람꺼면 2를 반환
    @Override
    public int getItemViewType(int position) {
        if (mChat.get(position).getUid().equals(stUid)) {
            return 1;
        } else {
            return 2;
        }
    }

    //View 생성 시
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        //만약 자신의 말풍선이면 오른쪽으로 보이는 layout, 아니면 왼쪽으로 보이는 layout로 설정
        View v;
        if (viewType == 1) {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.right_text_view, parent, false);
        } else {
            v = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.left_text_view, parent, false);
        }
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    //각 말풍선 View의 데이터 및 이벤트 관리
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        //이미지, 텍스트, 이름, 시간 layout에 값 설정
        holder.mTextView.setText(mChat.get(position).getText());
        holder.tvName.setText(mChat.get(position).getName());
        holder.tvTime.setText(mChat.get(position).getTime().substring(11,16));
        FirebaseDatabase.getInstance().getReference("users").child(mChat.get(position).getUid()).child("profile").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String stPhoto = dataSnapshot.child("photo").getValue().toString();
                if (stPhoto.equals("None")) {
                    //친구의 이미지 정보가 없을 경우 지정해둔 기본 이미지로
                    Drawable defaultImg = context.getResources().getDrawable(R.drawable.ic_person_black_24dp);
                    holder.ivUser.setImageDrawable(defaultImg);

                } else {
                    Glide.with(context).load(stPhoto).into(holder.ivUser);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mChat.size();
    }
}