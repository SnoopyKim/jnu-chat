package testchat.myapplication;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by TH-home on 2017-06-03.
 */
// reference http://www.programkr.com/blog/MQTMxEDMwYT2.html
public class InfoDialogFragment  extends DialogFragment
{
    private String stFriendUID;
    private String stFriendName;
    private String stFriendEmail;
    private String stFriendPhoto;

    Context context;

    TextView tvName;
    TextView tvEmail;
    ImageView ivUser;
    Button btnMessage;

    private DialogDismissListener onDismissListener=null;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            stFriendUID = getArguments().getString("paramID");
            stFriendEmail = getArguments().getString("paramEmail");
            stFriendName = getArguments().getString("paramName");
            stFriendPhoto = getArguments().getString("paramPhoto");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        //dailog creator = builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_info, null);
        tvName = (TextView) view.findViewById(R.id.tvUser);         tvName.setText(stFriendName);
        tvEmail = (TextView) view.findViewById(R.id.tvUsermail);   tvEmail.setText(stFriendEmail);
        ivUser = (ImageView) view.findViewById(R.id.ivUser);
        btnMessage = (Button) view.findViewById(R.id.btnMessage);
        btnMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onDismissListener!=null){
                    onDismissListener.setValue("MESSAGE",true);
                }
                dismiss();
            }
        });
        context = getContext();

        if(stFriendPhoto.equals("None")) {
            Drawable defaultImg = context.getResources().getDrawable(R.drawable.ic_person_black_24dp);
            ivUser.setImageDrawable(defaultImg);
        } else {
            Glide.with(context).load(stFriendPhoto).into(ivUser);
        }
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().setOnDismissListener(onDismissListener);
    }

    @Override
    public void onStart() {
        super.onStart();
        // safety check
        if (getDialog() == null)
            return;

        int dialogWidth = 900; // specify a value here
        int dialogHeight = 1600; // specify a value here

        getDialog().getWindow().setLayout(dialogWidth, dialogHeight);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        dismiss();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void setDissmissListner(DialogDismissListener listener){
        onDismissListener=listener;
    }
}

