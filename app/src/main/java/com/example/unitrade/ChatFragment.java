package com.example.unitrade;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private List<Chat> chatList;

    private FirebaseFirestore db;
    private String currentUserId;

    private boolean fromExternal = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // required for back button
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();

        if (getArguments() != null) {
            fromExternal = getArguments().getBoolean("fromExternal", false);
        }

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        chatList = new ArrayList<>();
        adapter = new ChatAdapter(chatList, chat -> {
            Intent intent = new Intent(requireActivity(), ConversationActivity.class);
            intent.putExtra("chat", chat);
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        loadChats();

        return view;
    }

    private void loadChats() {
        if (currentUserId == null)
            return;

        db.collection("chats")
                .whereArrayContains("participants", currentUserId)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null)
                        return;

                    if (snapshots != null) {
                        chatList.clear();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                            List<String> participants = (List<String>) doc.get("participants");
                            if (participants != null && participants.size() > 1) {
                                String otherUserId = participants.get(0).equals(currentUserId)
                                        ? participants.get(1)
                                        : participants.get(0);

                                String lastMsg = doc.getString("lastMessage");
                                Long timeVal = doc.getLong("lastMessageTime");
                                long time = timeVal != null ? timeVal : 0;

                                // Defaulting isBookmarked to false for now
                                chatList.add(new Chat(otherUserId, lastMsg, time, false));
                            }
                        }

                        // Sort by newest first
                        Collections.sort(chatList,
                                (c1, c2) -> Long.compare(c2.getLastMessageTime(), c1.getLastMessageTime()));

                        adapter.notifyDataSetChanged();
                    }
                });
    }

    // ---------------------------------------------------------
    // Toolbar Logic
    // ---------------------------------------------------------
    @Override
    public void onResume() {
        super.onResume();

        AppCompatActivity act = (AppCompatActivity) requireActivity();

        if (act.getSupportActionBar() != null) {
            act.getSupportActionBar().show();
            act.getSupportActionBar().setTitle("Chat");

            act.getSupportActionBar()
                    .setDisplayHomeAsUpEnabled(fromExternal);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        AppCompatActivity act = (AppCompatActivity) requireActivity();
        if (act.getSupportActionBar() != null) {
            act.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    // ---------------------------------------------------------
    // Handle Back Press (Action Bar)
    // ---------------------------------------------------------
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home && fromExternal) {
            requireActivity().finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
