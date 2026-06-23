package com.example.bookwormconnect;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class chatwin extends AppCompatActivity {

    String receiverimg,receiverUID,receiverName,SenderUID;
    CircleImageView profile;
    TextView receiverNName;
    CardView sendbtn;
    EditText textmsg;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase database;
    public static String senderImg;
    public static String receiverIImg;
    String senderRoom, receiverRoom;
    RecyclerView messagesAdapter;
    ArrayList<msgModelclass> messageArrayList;
    messagesAdpter messagesAdpter;
    String bookId, questionId;
    LinearLayout questionContainer;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        bookId = getIntent().getStringExtra("bookId");
        loadQuestions();
        setContentView(R.layout.activity_chatwin);

        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                profile = findViewById(R.id.profileimgg);
                receiverNName = findViewById(R.id.receivername);

                questionContainer = findViewById(R.id.questionContainer);

                DatabaseReference questionRef = FirebaseDatabase.getInstance().getReference("BookQuestions").child(bookId);
                questionRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds:snapshot.getChildren()){
                            String question = ds.child("question").getValue(String.class);
                            Button btn = new Button(chatwin.this);
                            btn.setText(question);
                            questionContainer.addView(btn);
                            btn.setOnClickListener(v -> {
                                sendQuestion(question);
                            });
                        }
                    }

                    private void sendQuestion(String question) {
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });
    }

    private void loadQuestions() {
        DatabaseReference questionRef = FirebaseDatabase.getInstance().getReference("BookQuestions").child(bookId);
        questionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    String question = ds .child("question").getValue(String.class);
                    Toast.makeText(chatwin.this,question,Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    protected void onChildTitleChanged(Activity childActivity, CharSequence title) {
        super.onChildTitleChanged(childActivity, title);
    }

    @SuppressLint("MissingInflatedId")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chatwin);
        getSupportActionBar().hide();
        database = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        messageArrayList = new ArrayList<>();
        messagesAdapter = findViewById(R.id.msgadapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        messagesAdapter.setLayoutManager(linearLayoutManager);
        messagesAdpter = new messagesAdpter(chatwin.this,messageArrayList);
        messagesAdapter.setAdapter(messagesAdpter);



        receiverName = getIntent().getStringExtra( "namee");
        receiverimg = getIntent().getStringExtra( "receiverImg");
        receiverUID = getIntent().getStringExtra("uid");



        sendbtn = findViewById(R.id.sendbtn);
        textmsg = findViewById(R.id.textmsg);

        profile = findViewById(R.id.profileimgg);
        receiverNName = findViewById(R.id.receivername);

        Picasso.get().load(receiverimg).into(profile);
        receiverNName.setText(""+receiverName);

        SenderUID = firebaseAuth.getUid();
        senderRoom = SenderUID+receiverUID;
        receiverRoom = receiverUID+SenderUID;

        DatabaseReference reference = database.getReference().child("user").child(firebaseAuth.getUid());
        DatabaseReference chatreference = database.getReference().child("user").child(senderRoom).child("messages");


        chatreference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    msgModelclass message = dataSnapshot.getValue(msgModelclass.class);
                    messageArrayList.add(message);
                }
                messagesAdpter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                senderImg = snapshot.child("profilepic").getValue().toString();
                receiverIImg = receiverimg;


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = textmsg.getText().toString();
                if (message.isEmpty()){
                    Toast.makeText(chatwin.this, "Enter The Message First", Toast.LENGTH_SHORT).show();
                }
                textmsg.setText("");
                Date date = new Date();
                msgModelclass messages = new msgModelclass(message,SenderUID,date.getTime());
                database = FirebaseDatabase.getInstance();
                database.getReference().child("chats").child(senderRoom).child("messages")
                        .push().setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                            }

                            public void onCanceled(@NonNull Task<Void> task) {
                                database.getReference().child("chats").child(receiverRoom).child("messages")
                                        .push().setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                            }
                                        });

                            }
                        });
            }
        });

    }

    private void loadAnswers(String questionId){
        DatabaseReference answerRef = FirebaseDatabase.getInstance().getReference("BookQuestions").child(bookId).child(questionId).child("answers");
        answerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    String answer = ds.getValue(String.class);
                    Toast.makeText(chatwin.this,answer,Toast.LENGTH_SHORT).show();
                    sendbtn.setOnClickListener(v-> {
                        loadAnswers(questionId);
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void sendQuestion(String question){

        msgModelclass msg = new msgModelclass(question,SenderUID,System.currentTimeMillis()
        );
        msg.setBookId(bookId);
        msg.setMessageType("QUESTION");

        database.getReference().child("chats").child(senderRoom).child("messages").push().setValue(msg);
        database.getReference().child("chats").child(receiverRoom).child("messages").push().setValue(msg);

    }


}