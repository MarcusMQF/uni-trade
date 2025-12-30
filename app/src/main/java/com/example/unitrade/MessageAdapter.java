package com.example.unitrade;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private List<Message> messageList;
    private String currentUserId;

    public MessageAdapter(List<Message> messageList, String currentUserId) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        Message msg = messageList.get(position);
        if (msg.getSenderId() != null && msg.getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_SENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
        }
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private ImageView messageImageView;
        private TextView messageTextView;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageImageView = itemView.findViewById(R.id.messageImageView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
        }

        public void bind(Message message) {
            if (message.getMediaUrl() != null && !message.getMediaUrl().isEmpty()) {
                messageImageView.setVisibility(View.VISIBLE);
                Glide.with(itemView.getContext())
                        .load(message.getMediaUrl())
                        .into(messageImageView);
            } else {
                messageImageView.setVisibility(View.GONE);
            }

            if (message.getText() != null && !message.getText().isEmpty()) {
                messageTextView.setVisibility(View.VISIBLE);
                messageTextView.setText(message.getText());
            } else {
                messageTextView.setVisibility(View.GONE);
            }
        }
    }
}
