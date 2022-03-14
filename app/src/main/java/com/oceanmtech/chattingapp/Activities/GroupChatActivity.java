package com.oceanmtech.chattingapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.oceanmtech.chattingapp.Adapters.GroupMessagesAdapter;
import com.oceanmtech.chattingapp.Adapters.MessagesAdapter;
import com.oceanmtech.chattingapp.Models.MessageModel;
import com.oceanmtech.chattingapp.R;
import com.oceanmtech.chattingapp.databinding.ActivityGroupChatBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class GroupChatActivity extends AppCompatActivity {

    ActivityGroupChatBinding binding;
    GroupChatActivity context = GroupChatActivity.this;

    ArrayList<MessageModel> messageList;
    GroupMessagesAdapter mAdapter;

    FirebaseDatabase database;
    FirebaseStorage storage;

    ProgressDialog dialog;

    String senderUid, filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setTitle("Group Chat");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   // To display back arrow in action bar.

        if(FirebaseAuth.getInstance() !=null){
            senderUid = FirebaseAuth.getInstance().getUid();
        }

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        messageList = new ArrayList<>();

        mAdapter = new GroupMessagesAdapter(context, messageList);
        binding.rvChat.setLayoutManager(new LinearLayoutManager(context));
        binding.rvChat.setAdapter(mAdapter);
       // binding.rvChat.getLayoutManager().scrollToPosition(messageList.size()-1);
       // binding.rvChat.scrollToPosition(messageList.size()-1);

        database.getReference().child("public")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messageList.clear();
                        for(DataSnapshot snapshot1 : snapshot.getChildren()) {
                            MessageModel currentMessage = snapshot1.getValue(MessageModel.class);
                            currentMessage.setMessageId(snapshot1.getKey());
                            messageList.add(currentMessage);
                        }
                        mAdapter.notifyDataSetChanged();
                        binding.rvChat.getLayoutManager().scrollToPosition(messageList.size()-1);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        dialog = new ProgressDialog(context);
        dialog.setMessage("Uploading image...");
        dialog.setCancelable(false);

        binding.ivSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String typedMsg = binding.etMessage.getText().toString();
                Date date = new Date();
                MessageModel messageModel = new MessageModel(typedMsg, senderUid, date.getTime());
                binding.etMessage.setText("");

                database.getReference().child("public").push().setValue(messageModel);
            }
        });
        binding.ivAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setAction(Intent.ACTION_GET_CONTENT);
                i.setType("image/*");
                startActivityForResult(i, 301);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 301){
            if(data != null && data.getData() != null){
                Uri selectedImage = data.getData();
                Calendar calendar = Calendar.getInstance();
                StorageReference reference = storage.getReference().child("chats").child(calendar.getTimeInMillis()+"");
                dialog.show();
                reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        dialog.dismiss();
                        if(task.isSuccessful()){
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    filePath = uri.toString();

                                    String typedMsg = binding.etMessage.getText().toString();
                                    Date date = new Date();
                                    MessageModel messageModel = new MessageModel(typedMsg, senderUid, date.getTime());
                                    messageModel.setMessage("photo");
                                    messageModel.setImageUrl(filePath);
                                    binding.etMessage.setText("");

                                    database.getReference().child("public").push().setValue(messageModel);
                                    //Toast.makeText(mContext, filePath, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                });
            }
        }
    }
    @Override
    public boolean onSupportNavigateUp() {   // To go back on click of back arrow.
        finish();
        return super.onSupportNavigateUp();
    }
}