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

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private List<Chat> chatList;

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
            @Nullable Bundle savedInstanceState
    ) {

        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        if (getArguments() != null) {
            fromExternal = getArguments().getBoolean("fromExternal", false);
        }

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // ---------------------------------------------------------
        // Chat data (NEW MODEL)
        // ---------------------------------------------------------
        chatList = new ArrayList<>();

        chatList.add(new Chat(
                "u2",                                   // userId
                "[Rating Card] Please rate...",          // last message
                System.currentTimeMillis() - 5 * 60_000, // 5 min ago
                false
        ));

        chatList.add(new Chat(
                "u3",
                "Chat has ended. If you need further...",
                System.currentTimeMillis() - 2 * 60 * 60_000, // 2 hours ago
                true
        ));

        adapter = new ChatAdapter(chatList, chat -> {
            Intent intent = new Intent(requireActivity(), ConversationActivity.class);
            intent.putExtra("chat", chat);
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        return view;
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
