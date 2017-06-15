package testchat.myapplication;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.List;
import java.util.UUID;

/**
 * Created by snoopy on 2017-04-01.
 */

/**
 * @Name    ChatAdapter
 * @Usage   Check chat property(left/right)
 *           File download
 *           manage custom viewholder
 * @Layout  left_text_view.xml/right_text_view.xml
 * */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    List<Chat> mChat;

    String stUid;
    Context context;
    boolean isYou;

    String roomKey;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;
        public TextView tvName;
        public TextView tvTime;
        public ImageView ivUser;
        public ImageView ivDownload;
        public Button btnDownload;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.mTextView);
            tvName = (TextView) itemView.findViewById(R.id.tvName);
            tvTime = (TextView) itemView.findViewById(R.id.tvChatTime);
            ivUser = (ImageView) itemView.findViewById(R.id.ivUser);
            ivDownload = (ImageView) itemView.findViewById(R.id.ivDownload);
            btnDownload = (Button) itemView.findViewById(R.id.btnDownload);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ChatAdapter(List<Chat> aChat, String uid, String roomKey, Context context) {
        this.mChat = aChat;
        this.stUid = uid;
        this.roomKey = roomKey;
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
            isYou=true;
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.right_text_view, parent, false);
        } else {
            isYou=false;
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
        //text chat
        if (mChat.get(position).getFile().equals("false")) {
            holder.mTextView.setVisibility(View.VISIBLE);
            holder.ivDownload.setVisibility(View.GONE);
            holder.btnDownload.setVisibility(View.GONE);

            holder.mTextView.setText(mChat.get(position).getText());

        }
        //image chat
        else if (mChat.get(position).getFile().contains("image")) {
            holder.mTextView.setVisibility(View.GONE);
            holder.ivDownload.setVisibility(View.VISIBLE);
            holder.btnDownload.setVisibility(View.GONE);

            Glide.with(context).load(Uri.parse(mChat.get(position).getText()))
                    .override(800,800)
                    .fitCenter()
                    .placeholder(R.drawable.ic_photo_placeholder_24dp)
                    .into(holder.ivDownload);

            //이미지 파일 클릭 시 다운로드 수행
            holder.ivDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String fileName = mChat.get(position).getTime();
                    StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://myapplication-89783.appspot.com")
                            .child("chats").child(roomKey).child(mChat.get(position).getUid()).child(fileName);

                    File storagePath = new File(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)));
                    if (!storagePath.exists())
                        storagePath.mkdirs();

                    final File image = new File(storagePath, UUID.randomUUID().toString()+".jpg");

                    storageReference.getFile(image).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(context,"다운로드가 완료됐습니다",Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context,"다운로드에 실패했습니다 : "+e.getMessage(),Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    });
                }
            });

        }
        //file chat
        else {
            holder.mTextView.setVisibility(View.GONE);
            holder.ivDownload.setVisibility(View.GONE);
            holder.btnDownload.setVisibility(View.VISIBLE);

            final String fileName = mChat.get(position).getTime();
            final String fileType = MimeTypeMap.getSingleton().getExtensionFromMimeType(mChat.get(position).getFile());
            if(fileType != null)
                holder.btnDownload.setText(fileType);
            else {
                holder.btnDownload.setText("UNKNOWN");
                holder.btnDownload.setEnabled(false);
            }

            final StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://myapplication-89783.appspot.com")
                    .child("chats").child(roomKey).child(mChat.get(position).getUid()).child(fileName);

            //그 외 파일 클릭 시 다운로드 수행행
            holder.btnDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    File storagePath = new File(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)));
                    if (!storagePath.exists())
                        storagePath.mkdirs();

                    File file = new File(storagePath, UUID.randomUUID().toString() +"."+ fileType);
                    storageReference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(context,"다운로드가 완료됐습니다",Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context,e.getMessage(),Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    });
                }
            });
        }

        //if right text view = user
        if(isYou)
            holder.tvName.setText("나");
        else
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