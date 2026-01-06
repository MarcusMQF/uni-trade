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

    private final List<Chat> chatList;
    private final OnItemClickListener listener;

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

        private final ImageView avatarImageView;
        private final TextView nameTextView;
        private final TextView lastMessageTextView;
        private final TextView timestampTextView;
        private final ImageButton saveButton;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImageView = itemView.findViewById(R.id.avatarImageView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            lastMessageTextView = itemView.findViewById(R.id.lastMessageTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
            saveButton = itemView.findViewById(R.id.saveButton);
        }

        public void bind(final Chat chat, final OnItemClickListener listener) {

            // 🔹 Set last message
            lastMessageTextView.setText(chat.getLastMessage());

            // 🔹 Set timestamp
            timestampTextView.setText(DateUtils.getRelativeTimeSpanString(
                    chat.getLastMessageTime(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS));

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
                        Toast.LENGTH_SHORT).show();
            });

            // 🔹 Initial State (Loading)
            nameTextView.setText("...");
            avatarImageView.setImageResource(R.drawable.profile_pic_2);

            // 🔹 Async user fetch
            UserRepository.getUserByUid(chat.getUserId(), new UserRepository.UserCallback() {
                @Override
                public void onSuccess(User user) {
                    if (user != null) {
                        // Check if username is valid, otherwise use full name
                        String name = user.getUsername();
                        if (name == null || name.isEmpty()) {
                            name = user.getFullName();
                        }
                        if (name == null || name.isEmpty()) {
                            name = "Unknown User";
                        }
                        nameTextView.setText(name);

                        Glide.with(itemView.getContext())
                                .load(user.getProfileImageUrl())
                                .signature(new ObjectKey(user.getProfileImageVersion()))
                                .placeholder(R.drawable.profile_pic_2)
                                .circleCrop()
                                .into(avatarImageView);
                    } else {
                        setUnknownUser();
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    setUnknownUser();
                }

                private void setUnknownUser() {
                    nameTextView.setText("Unknown User");
                    avatarImageView.setImageResource(R.drawable.profile_pic_2);
                }
            });
        }

        private void updateBookmarkIcon(boolean isBookmarked) {
            saveButton.setImageResource(
                    isBookmarked ? R.drawable.ic_bookmark : R.drawable.ic_bookmark_border);
        }
    }
}
