package com.oceanmtech.chattingapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.oceanmtech.chattingapp.Models.MessageModel;
import com.oceanmtech.chattingapp.Models.User;
import com.oceanmtech.chattingapp.R;
import com.oceanmtech.chattingapp.databinding.RowReceiverBinding;
import com.oceanmtech.chattingapp.databinding.RowReceiverGroupBinding;
import com.oceanmtech.chattingapp.databinding.RowSenderBinding;
import com.oceanmtech.chattingapp.databinding.RowSenderGroupBinding;

import java.util.ArrayList;

public class GroupMessagesAdapter extends RecyclerView.Adapter{

    Context context;
    ArrayList<MessageModel> messageList;

    final int ITEM_SEND = 1;
    final int ITEM_RECEIVE = 2;

    public GroupMessagesAdapter(Context context, ArrayList<MessageModel> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == ITEM_SEND) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_sender_group, parent, false);
            return new SenderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_receiver_group, parent, false);
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
                    .child("public")
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

            FirebaseDatabase.getInstance()
                    .getReference().child("users")
                    .child(currentMessage.getSenderId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()) {
                                User user = snapshot.getValue(User.class);
                                viewHolder.binding.tvName.setText("@" + user.getName());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

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

        }else{
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;

            if(currentMessage.getMessage().equals("photo")){
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.tvReceiver.setVisibility(View.GONE);
                Glide.with(context).load(currentMessage.getImageUrl()).placeholder(R.drawable.img_placeholder).into(viewHolder.binding.image);
            }
//            FirebaseDatabase.getInstance()
//                    .getReference().child("users")
//                    .child(currentMessage.getSenderId())
//                    .addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            if(snapshot.exists()) {
//                                User user = snapshot.getValue(User.class);
//                                viewHolder.binding.tvName.setText("@" + user.getName());
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//
//                        }
//                    });

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
        RowSenderGroupBinding binding;
        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RowSenderGroupBinding.bind(itemView);
        }
    }
    public class ReceiverViewHolder extends RecyclerView.ViewHolder {
        RowReceiverGroupBinding binding;
        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RowReceiverGroupBinding.bind(itemView);
        }
    }
}
