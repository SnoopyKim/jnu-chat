package testchat.myapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
import java.util.Hashtable;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    //String[] myDataset = {"안녕","오늘","뭐했어","영화볼래?"};
    String email;

    EditText etText;
    Button btnSend;

    List<Chat> mChat;
    FirebaseDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        database = FirebaseDatabase.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            email = user.getEmail();

        }

        etText = (EditText) findViewById(R.id.etText);
        btnSend = (Button) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String stText = etText.getText().toString();
                if (stText.equals("") || stText.isEmpty()){

                    Toast.makeText(ChatActivity.this, "내용을 입력해주세요", Toast.LENGTH_SHORT).show();
                } else {

                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String formattedDate = df.format(c.getTime());

                    DatabaseReference myRef = database.getReference("chats").child(formattedDate);

                    Hashtable<String, String> chat = new Hashtable<String, String>();
                    chat.put("email",email);
                    chat.put("text",stText);
                    myRef.setValue(chat);
                    //Toast.makeText(ChatActivity.this, email + "," + stText, Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button btnFinish = (Button) findViewById(R.id.btnFinish);
        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                finish();
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mChat = new ArrayList<>();

        // specify an adapter (see also next example)
        mAdapter = new MyAdapter(mChat);
        mRecyclerView.setAdapter(mAdapter);

        DatabaseReference myRef = database.getReference("chats");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // A new comment has been added, add it to the displayed list
                Chat chat = dataSnapshot.getValue(Chat.class);

                // [START_EXCLUDE]
                // Update RecyclerView
                mChat.add(chat);
                mAdapter.notifyItemInserted(mChat.size() - 1);
                // [END_EXCLUDE]
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
