package com.example.unitrade;

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chatList.get(position);
        holder.bind(chat, listener);
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
            nameTextView.setText(chat.getName());
            lastMessageTextView.setText(chat.getLastMessage());
            timestampTextView.setText(chat.getTimestamp());
            Glide.with(itemView.getContext())
                .load(chat.getAvatarUrl())
                .placeholder(R.drawable.profile_pic_2)
                .into(avatarImageView);
            itemView.setOnClickListener(v -> listener.onItemClick(chat));

            // Set the initial bookmark state
            updateBookmarkIcon(chat.isBookmarked());

            saveButton.setOnClickListener(v -> {
                chat.setBookmarked(!chat.isBookmarked());
                updateBookmarkIcon(chat.isBookmarked());
                String message = chat.isBookmarked() ? "Chat bookmarked!" : "Chat un-bookmarked!";
                Toast.makeText(itemView.getContext(), message, Toast.LENGTH_SHORT).show();
            });
        }

        private void updateBookmarkIcon(boolean isBookmarked) {
            if (isBookmarked) {
                saveButton.setImageResource(R.drawable.ic_bookmark);
            } else {
                saveButton.setImageResource(R.drawable.ic_bookmark_border);
            }
        }
    }
}
