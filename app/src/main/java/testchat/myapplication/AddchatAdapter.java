package testchat.myapplication;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by snoopy on 2017-04-01.
 */

//FriendsFragment에서 RecyclerView의 커스텀Adapter (View에서 한 row마다의 데이터나 이벤트 적용)
public class AddchatAdapter extends RecyclerView.Adapter<AddchatAdapter.ViewHolder> {

    //친구 데이터 리스트 두개 (하나는 백업용)
    List<Friend> mFriend;
    List<Friend> mFilter;
    List<Friend> mResult;
    Context context;

    //리스트로 된 View들을 통합적으로 보관하는 객체
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvEmail;
        public ImageView ivUser;
        public CheckBox cbAdd;


        //imageview 동그랗게
        //ivUser.setBackground(new ShapeDrawable(new OvalShape()));

        //순서대로 칸, 이름, 이미지를 레이아웃에서 불러와 생성
        public ViewHolder(View itemView) {
            super(itemView);
            tvEmail = (TextView) itemView.findViewById(R.id.tvEmail);
            ivUser = (ImageView) itemView.findViewById(R.id.ivUser);
            cbAdd = (CheckBox) itemView.findViewById(R.id.cbAdd);
        }
    }

    // 커스텀 생성자로 친구 데이터 리스트를 받음
    public AddchatAdapter(List<Friend> mFriend, Context context) {
        this.mFriend = mFriend;
        this.mFilter = new ArrayList<>();
        this.mFilter.addAll(mFriend);
        this.mResult = new ArrayList<>();
        this.context = context;
    }

    //VIew생성 및 레이아웃 설정
    @Override
    public AddchatAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.add_chat_view, parent, false);

        //set the view's size, margin, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    //친구 데이터 리스트에 따라 row들의 이미지, 텍스트, 이벤트 관리(설정)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        //이름과 이미지를 친구 데이터 리스트에서와 같은 순서로 설정(그림)
        holder.tvEmail.setText(mFriend.get(position).getName());

        String stPhoto = mFriend.get(position).getPhoto();
        if(stPhoto.equals("None")) {
            //친구의 이미지 정보가 없을 경우 지정해둔 기본 이미지로
            Drawable defaultImg = context.getResources().getDrawable(R.drawable.ic_person_black_24dp);
            holder.ivUser.setImageDrawable(defaultImg);
        } else {
            Glide.with(context).load(stPhoto).into(holder.ivUser);

        }

        holder.cbAdd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    mResult.add(mFriend.get(position));

                } else {
                    mResult.remove(mFriend.get(position));
                }
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

    //결과 친구 데이터 리스트 반환
    public List<Friend> getResult() {
        return mResult;
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {

        return mFriend.size();
    }
}