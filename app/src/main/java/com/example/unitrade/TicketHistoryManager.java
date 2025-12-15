package com.example.unitrade;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TicketHistoryManager {

    private static final String PREFS_NAME = "TicketHistory";
    private static final String KEY_TICKETS = "Tickets";

    public static void addTicket(Context context, Ticket ticket) {
        List<Ticket> tickets = getTickets(context);
        tickets.add(0, ticket); // Add to the top of the list
        saveTickets(context, tickets);
    }

    public static List<Ticket> getTickets(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_TICKETS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<ArrayList<Ticket>>() {}.getType();
        return new Gson().fromJson(json, type);
    }

    private static void saveTickets(Context context, List<Ticket> tickets) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String json = new Gson().toJson(tickets);
        editor.putString(KEY_TICKETS, json);
        editor.apply();
    }
}
