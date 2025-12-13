package com.example.unitrade;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<Chat> chatList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Chat chat);
    }

    public ChatAdapter(List<Chat> chatList, OnItemClickListener listener) {
        this.chatList = chatList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        holder.bind(chatList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {

        private ImageView avatarImageView;
        private TextView nameTextView;
        private TextView lastMessageTextView;
        private TextView timestampTextView;
        private ImageButton saveButton;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImageView = itemView.findViewById(R.id.avatarImageView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            lastMessageTextView = itemView.findViewById(R.id.lastMessageTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
            saveButton = itemView.findViewById(R.id.saveButton);
        }

        public void bind(final Chat chat, final OnItemClickListener listener) {

            // 🔹 Resolve user dynamically (auto-update avatar & name)
            User user = SampleData.getUserById(itemView.getContext(), chat.getUserId());

            if (user != null) {
                nameTextView.setText(user.getUsername());

                Glide.with(itemView.getContext())
                        .load(user.getProfileImageUrl())
                        . signature(new ObjectKey(user.getProfileImageVersion()))
                        .placeholder(R.drawable.profile_pic_2)
                        .into(avatarImageView);
            } else {
                nameTextView.setText("Unknown User");
                avatarImageView.setImageResource(R.drawable.profile_pic_2);
            }

            // 🔹 Last message
            lastMessageTextView.setText(chat.getLastMessage());

            // 🔹 Timestamp (from long → readable text)
            timestampTextView.setText(
                    DateUtils.getRelativeTimeSpanString(
                            chat.getLastMessageTime(),
                            System.currentTimeMillis(),
                            DateUtils.MINUTE_IN_MILLIS
                    )
            );

            // 🔹 Click to open chat
            itemView.setOnClickListener(v -> listener.onItemClick(chat));

            // 🔹 Bookmark
            updateBookmarkIcon(chat.isBookmarked());

            saveButton.setOnClickListener(v -> {
                chat.setBookmarked(!chat.isBookmarked());
                updateBookmarkIcon(chat.isBookmarked());

                Toast.makeText(
                        itemView.getContext(),
                        chat.isBookmarked() ? "Chat bookmarked!" : "Chat un-bookmarked!",
                        Toast.LENGTH_SHORT
                ).show();
            });
        }

        private void updateBookmarkIcon(boolean isBookmarked) {
            saveButton.setImageResource(
                    isBookmarked ? R.drawable.ic_bookmark : R.drawable.ic_bookmark_border
            );
        }
    }
}
