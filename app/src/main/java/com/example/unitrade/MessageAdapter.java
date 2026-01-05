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

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT_TEXT = 1;
    private static final int VIEW_TYPE_RECEIVED_TEXT = 2;
    private static final int VIEW_TYPE_SENT_IMAGE = 3;
    private static final int VIEW_TYPE_RECEIVED_IMAGE = 4;

    private List<Message> messageList;
    private String currentUserId;

    public MessageAdapter(List<Message> messageList, String currentUserId) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        Message msg = messageList.get(position);
        boolean isMe = msg.getSenderId() != null && msg.getSenderId().equals(currentUserId);
        boolean isImage = msg.getMediaUrl() != null && !msg.getMediaUrl().isEmpty();

        if (isMe) {
            return isImage ? VIEW_TYPE_SENT_IMAGE : VIEW_TYPE_SENT_TEXT;
        } else {
            return isImage ? VIEW_TYPE_RECEIVED_IMAGE : VIEW_TYPE_RECEIVED_TEXT;
        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT_IMAGE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_image_sent, parent, false);
            return new ImageViewHolder(view);
        } else if (viewType == VIEW_TYPE_SENT_TEXT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
            return new MessageViewHolder(view);
        }
        // Add logic for RECEIVED_IMAGE and RECEIVED_TEXT similarly...
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
        return new MessageViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);

        if (holder instanceof MessageViewHolder) {
            // If it's the text holder, cast it and bind
            ((MessageViewHolder) holder).bind(message);
        }
        else if (holder instanceof ImageViewHolder) {
            // If it's the image holder, cast it and bind
            ((ImageViewHolder) holder).bind(message);
        }
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

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView messageImageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageImageView = itemView.findViewById(R.id.messageImageView);
        }

        public void bind(Message message) {
            Glide.with(itemView.getContext())
                    .load(message.getMediaUrl())
                    .into(messageImageView);

            messageImageView.setOnClickListener(v -> {
                String mediaUrl = message.getMediaUrl();
                if (mediaUrl == null || mediaUrl.isEmpty()) return;

                // Check if the message is a video based on your Message object's type
                // or by checking the URL extension/content
                if (mediaUrl.contains(".mp4") || (message.getText() != null && message.getText().contains("[video]"))) {
                    // ACTION: Play Video
                    android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                    intent.setDataAndType(android.net.Uri.parse(mediaUrl), "video/*");
                    itemView.getContext().startActivity(intent);
                } else {
                    // ACTION: Zoom/View Image
                    // You can create a simple FullScreenImageActivity or use a system intent
                    android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                    intent.setDataAndType(android.net.Uri.parse(mediaUrl), "image/*");
                    itemView.getContext().startActivity(intent);
                }
            });
        }
    }

}
