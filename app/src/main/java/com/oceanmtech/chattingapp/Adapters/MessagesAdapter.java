package com.oceanmtech.chattingapp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.oceanmtech.chattingapp.Models.MessageModel;
import com.oceanmtech.chattingapp.R;
import com.oceanmtech.chattingapp.databinding.RowReceiverBinding;
import com.oceanmtech.chattingapp.databinding.RowSenderBinding;

import java.util.ArrayList;

public class MessagesAdapter extends RecyclerView.Adapter{

    Context context;
    ArrayList<MessageModel> messageList;

    final int ITEM_SEND = 1;
    final int ITEM_RECEIVE = 2;

    String senderRoom, receiverRoom;
    FirebaseRemoteConfig remoteConfig;

    public MessagesAdapter(Context context, ArrayList<MessageModel> messageList, String senderRoom, String receiverRoom) {
        remoteConfig = FirebaseRemoteConfig.getInstance();
        this.context = context;
        this.messageList = messageList;
        this.senderRoom = senderRoom;
        this.receiverRoom = receiverRoom;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == ITEM_SEND) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_sender, parent, false);
            return new SenderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_receiver, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        MessageModel currentMessage = messageList.get(position);
        if(FirebaseAuth.getInstance().getUid().equals(currentMessage.getSenderId())){
            return ITEM_SEND;
        }else{
            return ITEM_RECEIVE;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageModel currentMessage = messageList.get(position);

        int reactions[] = new int[]{
                R.drawable.ic_like,
                R.drawable.ic_love,
                R.drawable.ic_laugh,
                R.drawable.ic_wow,
                R.drawable.ic_sad,
                R.drawable.ic_angry
        };

        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(reactions)
                .build();

        ReactionPopup popup = new ReactionPopup(context, config, (pos) -> {

            if(pos < 0)
                return false;

            if(holder.getClass() == SenderViewHolder.class) {
                SenderViewHolder viewHolder = (SenderViewHolder) holder;
                viewHolder.binding.ivFeeling.setImageResource(reactions[pos]);
                viewHolder.binding.ivFeeling.setVisibility(View.VISIBLE);
            }else{
                ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
                viewHolder.binding.ivFeeling.setImageResource(reactions[pos]);
                viewHolder.binding.ivFeeling.setVisibility(View.VISIBLE);
            }
            currentMessage.setFeeling(pos);
            FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(senderRoom)
                    .child("message")
                    .child(currentMessage.getMessageId()).setValue(currentMessage);
            FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(receiverRoom)
                    .child("message")
                    .child(currentMessage.getMessageId()).setValue(currentMessage);
            return true; // true is closing popup, false is requesting a new selection
        });

        if(holder.getClass() == SenderViewHolder.class){
            SenderViewHolder viewHolder = (SenderViewHolder) holder;

            if(currentMessage.getMessage().equals("photo")){
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.tvSender.setVisibility(View.GONE);
                Glide.with(context).load(currentMessage.getImageUrl()).placeholder(R.drawable.img_placeholder).into(viewHolder.binding.image);
            }

            viewHolder.binding.tvSender.setText(currentMessage.getMessage());

            if(currentMessage.getFeeling() >= 0) {
                viewHolder.binding.ivFeeling.setImageResource(reactions[currentMessage.getFeeling()]);
                viewHolder.binding.ivFeeling.setVisibility(View.VISIBLE);
            } else {
                viewHolder.binding.ivFeeling.setVisibility(View.GONE);
            }

            viewHolder.binding.tvSender.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
//                    boolean isFeelingsEnabled = remoteConfig.getBoolean("isFeelingsEnabled");
//                    if(isFeelingsEnabled)
                        popup.onTouch(view, motionEvent);
//                    else
//                        Toast.makeText(context, "This feature is disabled temporarily.", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });
            viewHolder.binding.image.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    popup.onTouch(view, motionEvent);
                    return false;
                }
            });

        }else{
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;

            if(currentMessage.getMessage().equals("photo")){
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.tvReceiver.setVisibility(View.GONE);
                Glide.with(context).load(currentMessage.getImageUrl()).placeholder(R.drawable.img_placeholder).into(viewHolder.binding.image);
            }

            viewHolder.binding.tvReceiver.setText(currentMessage.getMessage());

            if(currentMessage.getFeeling() >= 0) {
                viewHolder.binding.ivFeeling.setImageResource(reactions[currentMessage.getFeeling()]);
                viewHolder.binding.ivFeeling.setVisibility(View.VISIBLE);
            } else {
                viewHolder.binding.ivFeeling.setVisibility(View.GONE);
            }

            viewHolder.binding.tvReceiver.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    popup.onTouch(view, motionEvent);
                    return false;
                }
            });
            viewHolder.binding.image.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    popup.onTouch(view, motionEvent);
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class SenderViewHolder extends RecyclerView.ViewHolder {
        RowSenderBinding binding;
        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RowSenderBinding.bind(itemView);
        }
    }
    public class ReceiverViewHolder extends RecyclerView.ViewHolder {
        RowReceiverBinding binding;
        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RowReceiverBinding.bind(itemView);
        }
    }
}
