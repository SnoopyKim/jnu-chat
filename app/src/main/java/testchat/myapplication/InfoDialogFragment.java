package testchat.myapplication;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

/**
 * Created by TH-home on 2017-06-03.
 */

/**
 * @Name    InfoDialogFragment
 * @Usage   show friend's detail data,  select menu : send message/delete friend
 * @Layout  dialog_info.xml
 * @Comment base dialog : alert dialog. But don't use left/right button.
 * @Reference  http://www.programkr.com/blog/MQTMxEDMwYT2.html
 * */
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
    Button btnDelete;

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
        //dailog creator : builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_info, null);
        context = getContext();
        tvName = (TextView) view.findViewById(R.id.tvUser);         tvName.setText(stFriendName);
        tvEmail = (TextView) view.findViewById(R.id.tvUsermail);   tvEmail.setText(stFriendEmail);
        ivUser = (ImageView) view.findViewById(R.id.ivUser);

        //click btns -> dismiss
        btnMessage = (Button) view.findViewById(R.id.btnMessage);
        btnMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onDismissListener!=null){
                    onDismissListener.setValue("MESSAGE",true);
                    onDismissListener.setValue("Delete",false);
                }
                dismiss();
            }
        });
        btnDelete = (Button) view.findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onDismissListener!=null) {
                    onDismissListener.setValue("MESSAGE",false);
                    onDismissListener.setValue("Delete",true);
                    onDismissListener.setValue("friendUID",stFriendUID);
                }
                dismiss();
            }
        });

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

    //@usage specify size of dialog
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

    //set dismisslistener -> must clarify dismiss in onCancel etc...
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

