package testchat.myapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @Name    ProfileFragment
 * @Usage
 * @Layout  fragment_profile.xml
 * */
public class ProfileFragment extends Fragment {
    //개인정보 및 설정 Fragment 화면
    String TAG = getClass().getSimpleName();

    Context context;

    ImageView ivUser;
    Button btnImage;

    TextView tvUser;
    TextView tvEmail;
    TextView tvBirth;
    TextView tvPhone;

    Switch notiAlarm;
    Switch popAlarm;

    private StorageReference mStorageRef;
    FirebaseUser user;
    DatabaseReference alarmRef;
    Bitmap bitmap;

    String stUid;
    String stEmail;
    Uri uriPhoto;

    /**
     * @Name    onCreateView
     * @Usage   initialize variable
     * @Param   layout inflater,viewgroup bundle
     * @return  void
     * */
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        context = getContext();

        //프로필 관련 layout 객체 지정
        ivUser = (ImageView) v.findViewById(R.id.ivUser);
        btnImage = (Button) v.findViewById(R.id.btnImage);

        tvUser = (TextView)v.findViewById(R.id.tvUser);
        tvEmail = (TextView)v.findViewById(R.id.tvUseraccount);
        tvPhone = (TextView)v.findViewById(R.id.tvUserPhone);
        tvBirth = (TextView)v.findViewById(R.id.tvUserBirth);

        notiAlarm = (Switch) v.findViewById(R.id.switchAlarm);
        popAlarm = (Switch) v.findViewById(R.id.switchMessage);

        //Firebase 내 저장소 부분 호출
        mStorageRef = FirebaseStorage.getInstance().getReference();

        //처음 유저의 정보로 그림
        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null) {
            stUid = user.getUid();
            stEmail = user.getEmail();
            tvUser.setText(user.getDisplayName());
            //자신의 프로필 정보에서 사진URL 정보가 없다면 기본 Drawble로, 있다면 해당 사진으로 그림
            uriPhoto = user.getPhotoUrl();
            if (uriPhoto == null) {
                Drawable defaultImg = context.getResources().getDrawable(R.drawable.ic_person_black_24dp);
                ivUser.setImageDrawable(defaultImg);

            } else {
                Glide.with(context).load(user.getPhotoUrl())
                        .placeholder(R.drawable.ic_person_black_24dp)
                        .into(ivUser);

            }
            tvEmail.setText(stEmail);

            FirebaseDatabase.getInstance().getReference("users").child(stUid)
                    .child("profile").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null) {
                        if (dataSnapshot.child("phone").getValue() == null || dataSnapshot.child("phone").getValue().equals("None")) {
                            tvPhone.setText("정보 없음");
                        } else {
                            tvPhone.setText(dataSnapshot.child("phone").getValue().toString());
                        }
                        if ( dataSnapshot.child("birth").getValue() == null || dataSnapshot.child("birth").getValue().equals("None")) {
                            tvBirth.setText("정보 없음");
                        } else {
                            tvBirth.setText(dataSnapshot.child("birth").getValue().toString());
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        } else {
            Toast.makeText(getActivity(),"로그인 정보를 불러들이지 못했습니다.",Toast.LENGTH_SHORT).show();
        }
        FirebaseDatabase.getInstance().getReference("users").child(stUid)
                .child("profile").child("photo").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null || dataSnapshot.getValue().equals("None")) {
                    Drawable defaultImg = context.getResources().getDrawable(R.drawable.ic_person_black_24dp);
                    ivUser.setImageDrawable(defaultImg);

                } else {
                    Glide.with(context).load(dataSnapshot.getValue().toString())
                            .placeholder(R.drawable.ic_person_black_24dp)
                            .into(ivUser);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //사진 클릭 시 기기 내의 갤러리로 연결
                Intent imageIntent = new Intent();
                imageIntent.addCategory(Intent.CATEGORY_OPENABLE);
                imageIntent.setType("image/*");
                imageIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(imageIntent, "사진을 선택하세요"), 0);

            }
        });

        //로그아웃 버튼 클릭 시 계정 로그아웃하고 MainActivity로 넘어감
        TextView btnLogout = (TextView)v.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //토큰 정보 DB에 자신의 것 삭제 (알람이 안오기 위함)
                FirebaseDatabase.getInstance().getReference("fcmtoken").child(user.getUid()).removeValue();

                Intent in = new Intent(context,MainActivity.class);
                startActivity(in);
                FirebaseAuth.getInstance().signOut();
                LoginManager.getInstance().logOut();
                getActivity().finish();
            }
        });

        //알람 버튼 활성화 비활성화 감지 & 클릭 시 On/Off
        alarmRef = FirebaseDatabase.getInstance().getReference("users")
                .child(user.getUid()).child("alarm");
        alarmRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()!=null) {
                    if(dataSnapshot.child("noti").getValue() != null &&
                            dataSnapshot.child("noti").getValue().toString().equals("on")) {
                        notiAlarm.setChecked(true);
                        popAlarm.setEnabled(true);
                    }
                    if(dataSnapshot.child("pop").getValue() != null &&
                            dataSnapshot.child("pop").getValue().toString().equals("on")) {
                        popAlarm.setChecked(true);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //알람 스위치 버튼에서 키면 상단만 키고 팝업 버튼을 활성, 끄면 알람과 팝업 둘다 끄고 팝업 버튼 비활성
        notiAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    alarmRef.child("noti").setValue("on");
                    popAlarm.setEnabled(true);

                } else {
                    alarmRef.child("noti").setValue("off");
                    alarmRef.child("pop").setValue("off");

                    popAlarm.setChecked(false);
                    popAlarm.setEnabled(false);
                }
            }
        });
        //팝업 스위치 버튼 (팝업 알림 켜기 끄기)
        popAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    alarmRef.child("pop").setValue("on");

                } else {
                    alarmRef.child("pop").setValue("off");

                }
            }
        });

        return v;
    }

    /**
     * @Name    uploadImage
     * @Usage   upload image(JPEG) local to server
     * @Param   layout inflater,viewgroup bundle
     * @return  void
     * */
    public void uploadImage() {

        StorageReference mountainRef = mStorageRef.child("users").child(stUid);

        //mapping image to byte
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("업로드중...");
        progressDialog.show();

        //가공한 사진 데이터를 Firebase 내 저장소에 등록(올리기)
        UploadTask uploadTask = mountainRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle unsuccessful uploads

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @SuppressWarnings("VisibleForTests")
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                //올려진 사진 데이터를 저장소에서 Uri-String형식으로 받은 후 DB에 저장
                final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                final String photoUri =  String.valueOf(downloadUrl);
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("users");

                myRef.child(stUid).child("profile").child("photo").setValue(photoUri);

                //선택했던 사진을 Firebase 계정에 PhotoUri로 설정
                UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                        .setPhotoUri(downloadUrl).build();
                user.updateProfile(profileUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "사진 업로드가 잘 됐습니다", Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(context, "사진 업로드에 실패했습니다", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }
        });
    }

    /**
     * @Name    onActivityResult
     * @Usage   when select Image and back, call bitmap // catch error
     * @Param   requestCode, result code, data
     * @return  void
     * */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //저장소(갤러리)에서 선택한 사진을 bitmap형식으로 바꿔 그려주고 uploadImage함수 호출
        if(data!=null) {
            Uri image = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), image);

                uploadImage();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}

