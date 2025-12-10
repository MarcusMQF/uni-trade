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
        setHasOptionsMenu(true);   // ⭐ REQUIRED for back button to work
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        if (getArguments() != null) {
            fromExternal = getArguments().getBoolean("fromExternal", false);
        }

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        chatList = new ArrayList<>();
        chatList.add(new Chat("Shop Message", "[Rating Card] Please rate...", "20/08",
                "@drawable/profile_pic_2", false, "u2"));
        chatList.add(new Chat("Delivery Driver", "Chat has ended. If you need further...",
                "15/11", "@drawable/profile_pic_2", true, "u3"));

        adapter = new ChatAdapter(chatList, chat -> {
            Intent intent = new Intent(getActivity(), ConversationActivity.class);
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

            if (fromExternal) {
                act.getSupportActionBar().setDisplayHomeAsUpEnabled(true);   // Show back button
            } else {
                act.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        // Reset toolbar for next fragment
        AppCompatActivity act = (AppCompatActivity) requireActivity();
        if (act.getSupportActionBar() != null) {
            act.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            act.getSupportActionBar().show();
        }
    }

    // ---------------------------------------------------------
    // Handle Back Press (Action Bar Button)
    // ---------------------------------------------------------
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {

            if (fromExternal) {
                requireActivity().finish();   // ⭐ Go back to CheckoutActivity
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
