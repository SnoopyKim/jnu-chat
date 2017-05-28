package testchat.myapplication;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by snoopy on 2017-04-01.
 */

//말풍선 View에서의 데이터 및 레이아웃 관리
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    List<Chat> mChat;
    String stEmail;
    Context context;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;
        public ViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.mTextView);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ChatAdapter(List<Chat> mChat, String email, Context context) {
        this.mChat = mChat;
        this.stEmail = email;
        this.context = context;
    }

    //말풍선이 자기꺼면 1, 다른사람꺼면 2를 반환
    @Override
    public int getItemViewType(int position) {
        if (mChat.get(position).getEmail().equals(stEmail)) {
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
    public void onBindViewHolder(ViewHolder holder, final int position) {
        //말풍선 누르면 해당 칸의 순서 팝업으로 출력 (처음 코딩 시 동작 매커니즘 확인을 위해 해둔 부분)
        holder.mTextView.setText(mChat.get(position).getText());
        holder.mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, String.valueOf(position),Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mChat.size();
    }
}