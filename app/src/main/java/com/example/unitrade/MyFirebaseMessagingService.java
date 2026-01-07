package com.example.unitrade;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Add this log!
        android.util.Log.d("FCM_SERVICE", "Message received! Title: " +
                (remoteMessage.getNotification() != null ? remoteMessage.getNotification().getTitle() : "Data only"));
        // Get the chatId from the 'data' payload of the notification
        String incomingChatId = remoteMessage.getData().get("chatId");
        String senderId = remoteMessage.getData().get("senderId");
        String currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();

        // CHECK: If the user is already looking at THIS specific chat, do not show notification
        if (incomingChatId != null && incomingChatId.equals(ConversationActivity.activeChatId)) {
            return; // Exit without showing notification
        }

        // 3. Block Check: Did I block this person?
        if (currentUserId != null && senderId != null) {
            String blockId = currentUserId + "_" + senderId;

            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("blocks")
                    .document(blockId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // SILENT EXIT: The sender is blocked by the current user
                            android.util.Log.d("FCM_SERVICE", "Blocked message suppressed.");
                        } else {
                            // Not blocked, proceed to show
                            processNotification(remoteMessage);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // If DB check fails, default to showing the notification to be safe
                        processNotification(remoteMessage);
                    });
        } else {
            processNotification(remoteMessage);
        }
    }

    private void processNotification(RemoteMessage remoteMessage) {
        String title, body;

        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
        } else {
            title = remoteMessage.getData().get("title");
            body = remoteMessage.getData().get("body");
        }

        if (title != null) {
            showNotification(title, body);
        }
    }

    private void showNotification(String title, String body) {
        String channelId = "chat_messages";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Create Channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Chat Notifications", NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(true);
            channel.setLightColor(android.graphics.Color.RED);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }

        // What happens when you tap the notification
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.stat_notify_chat) // Ensure you have a chat icon in res/drawable
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL) // Adds sound and vibration
                .setContentIntent(pendingIntent);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        // When the token is refreshed by Google, update it in your database
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .update("fcmToken", token);
        }
    }


}
