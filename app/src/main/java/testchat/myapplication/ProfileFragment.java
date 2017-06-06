package testchat.myapplication;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class ProfileFragment extends Fragment {
    //개인정보 및 설정 Fragment 화면
    String TAG = getClass().getSimpleName();

    ImageView ivUser;
    TextView tvUser;
    private StorageReference mStorageRef;
    FirebaseUser user;
    Bitmap bitmap;

    String stUid;
    String stEmail;
    Uri uriPhoto;

    ProgressBar pbLogin;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        pbLogin = (ProgressBar)v.findViewById(R.id.pbLogin);

        //프로필 사진과 이름 layout 객체 지정
        ivUser = (ImageView) v.findViewById(R.id.ivUser);
        tvUser = (TextView)v.findViewById(R.id.tvUser);

        //Firebase 내 저장소 부분 호출
        mStorageRef = FirebaseStorage.getInstance().getReference();

        //처음 유저의 정보로 그림
        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null) {
            stUid = user.getUid();
            stEmail = user.getEmail();
            //유저 이메일 설정
            tvUser.setText(user.getDisplayName());
            //자신의 프로필 정보에서 사진URL 정보가 없다면 기본 Drawble로, 있다면 해당 사진으로 그림
            uriPhoto = user.getPhotoUrl();
            if (uriPhoto == null) {
                Drawable defaultImg = getContext().getResources().getDrawable(R.drawable.ic_person_black_24dp);
                ivUser.setImageDrawable(defaultImg);
                pbLogin.setVisibility(View.GONE);
            } else {
                Glide.with(getContext()).load(user.getPhotoUrl()).into(ivUser);
                pbLogin.setVisibility(View.GONE);
            }
        } else {
            Toast.makeText(getActivity(),"로그인 정보를 불러들이지 못했습니다.",Toast.LENGTH_SHORT).show();
        }

        ivUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //사진 클릭 시 기기 내의 갤러리로 연결
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i,1);

            }
        });

        //저장소 허용 동의 부분
        if (ContextCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        //로그아웃 버튼 클릭 시 계정 로그아웃하고 MainActivity로 넘어감
        TextView btnLogout = (TextView)v.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(v.getContext(),MainActivity.class);
                startActivity(in);
                FirebaseAuth.getInstance().signOut();
                LoginManager.getInstance().logOut();
                getActivity().finish();
            }
        });

        return v;
    }

    //자신의 갤러리에서 선택한 사진을 그릴 때 DB에 해당 사진을 올림
    public void uploadImage() {

        StorageReference mountainRef = mStorageRef.child("users").child(stUid);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        final LinearLayout container = (LinearLayout) getActivity().findViewById(R.id.container);
        final LinearLayout option = (LinearLayout) getView().findViewById(R.id.optionLinear);
        container.setEnabled(false);
        option.setEnabled(false);
        ivUser.setVisibility(View.INVISIBLE);
        pbLogin.setVisibility(View.VISIBLE);

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
                String photoUri =  String.valueOf(downloadUrl);
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("users");

                myRef.child(stUid).child("profile").child("photo").setValue(photoUri);

                //선택했던 사진을 Firebase 계정에 PhotoUri로 설정
                UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                        .setPhotoUri(downloadUrl).build();
                user.updateProfile(profileUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Glide.with(getContext()).load(user.getPhotoUrl()).into(ivUser);
                            container.setEnabled(true);
                            option.setEnabled(true);
                            pbLogin.setVisibility(View.GONE);
                            ivUser.setVisibility(View.VISIBLE);
                            Toast.makeText(getContext(), "사진 업로드가 잘 됐습니다.", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }
        });
    }

    //사진 클릭 시 넘어갔던 화면에서 다시 돌아올 때 호출되는 함수
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //저장소(갤러리)에서 선택한 사진을 bitmap형식으로 바꿔 그려주고 uploadImage함수 호출
        if(data!=null) {
            Uri image = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), image);
                ivUser.setImageBitmap(bitmap);

                uploadImage();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    //저장소 허용 동의 부분에서 결과 처리 부분인데 아직 아무것도 없음 (딱히 필요한 이벤트가 없을 듯?)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

}

