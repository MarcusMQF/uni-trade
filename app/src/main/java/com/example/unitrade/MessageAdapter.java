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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_image_sent, parent,
                    false);
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
        } else if (holder instanceof ImageViewHolder) {
            // If it's the image holder, cast it and bind
            ((ImageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private android.widget.ImageView messageImageView;
        private android.widget.TextView messageTextView;
        private android.view.View imageContainer;
        private android.widget.ImageView playIcon;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageImageView = itemView.findViewById(R.id.messageImageView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            imageContainer = itemView.findViewById(R.id.imageContainer);
            playIcon = itemView.findViewById(R.id.playIcon);
        }

        public void bind(Message message) {
            if (message.getMediaUrl() != null && !message.getMediaUrl().isEmpty()) {
                // Show container if it exists, otherwise show image directly
                if (imageContainer != null) {
                    imageContainer.setVisibility(View.VISIBLE);
                } else {
                    messageImageView.setVisibility(View.VISIBLE);
                }

                Glide.with(itemView.getContext())
                        .load(message.getMediaUrl())
                        .into(messageImageView);

                String mediaUrl = message.getMediaUrl();
                boolean isVideo = mediaUrl != null && (mediaUrl.contains(".mp4")
                        || (message.getText() != null && message.getText().contains("[video]")));

                if (playIcon != null) {
                    playIcon.setVisibility(isVideo ? View.VISIBLE : View.GONE);
                }

                messageImageView.setOnClickListener(v -> {
                    if (mediaUrl == null || mediaUrl.isEmpty())
                        return;

                    android.content.Intent intent = new android.content.Intent(itemView.getContext(),
                            MediaViewerActivity.class);
                    intent.putExtra(MediaViewerActivity.EXTRA_MEDIA_URL, mediaUrl);

                    if (isVideo) {
                        intent.putExtra(MediaViewerActivity.EXTRA_MEDIA_TYPE, "video");
                    } else {
                        intent.putExtra(MediaViewerActivity.EXTRA_MEDIA_TYPE, "image");
                    }

                    itemView.getContext().startActivity(intent);
                });

            } else {
                if (imageContainer != null) {
                    imageContainer.setVisibility(View.GONE);
                } else {
                    messageImageView.setVisibility(View.GONE);
                }
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
        ImageView playIcon;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageImageView = itemView.findViewById(R.id.messageImageView);
            playIcon = itemView.findViewById(R.id.playIcon);
        }

        public void bind(Message message) {
            Glide.with(itemView.getContext())
                    .load(message.getMediaUrl())
                    .into(messageImageView);

            String mediaUrl = message.getMediaUrl();
            boolean isVideo = mediaUrl != null && (mediaUrl.contains(".mp4")
                    || (message.getText() != null && message.getText().contains("[video]")));

            // Show play icon if it's a video and the view exists
            if (playIcon != null) {
                playIcon.setVisibility(isVideo ? View.VISIBLE : View.GONE);
            }

            messageImageView.setOnClickListener(v -> {
                if (mediaUrl == null || mediaUrl.isEmpty())
                    return;

                android.content.Intent intent = new android.content.Intent(itemView.getContext(),
                        MediaViewerActivity.class);
                intent.putExtra(MediaViewerActivity.EXTRA_MEDIA_URL, mediaUrl);

                if (isVideo) {
                    intent.putExtra(MediaViewerActivity.EXTRA_MEDIA_TYPE, "video");
                } else {
                    intent.putExtra(MediaViewerActivity.EXTRA_MEDIA_TYPE, "image");
                }

                itemView.getContext().startActivity(intent);
            });
        }
    }

}
