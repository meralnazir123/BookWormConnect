package com.example.bookwormconnect;

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

import java.util.Map;

/**
 * MyFMService — Firebase Cloud Messaging Service (updated)
 *
 * Enhancements over the original:
 *  • Extracts chatId, requestId, bookTitle from data payload
 *  • Tapping the notification opens ChatActivity with the correct chat
 *  • Uses a dedicated CHAT notification channel with high priority
 *  • Updates the FCM token in Firestore whenever it rotates
 */
public class MyFMService extends FirebaseMessagingService {

    private static final String CHANNEL_ID   = "bookworm_chat_channel";
    private static final String CHANNEL_NAME = "BookWorm Chat Messages";

    // ── Called when a push arrives ─────────────────────────────────────
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        String title = "BookWormConnect";
        String body  = "You have a new message";

        // Notification payload (when app is in foreground)
        if (message.getNotification() != null) {
            if (message.getNotification().getTitle() != null)
                title = message.getNotification().getTitle();
            if (message.getNotification().getBody() != null)
                body  = message.getNotification().getBody();
        }

        // Data payload — carries chatId, requestId, bookTitle
        Map<String, String> data = message.getData();
        String chatId    = data.get("chatId");
        String requestId = data.get("requestId");
        String bookTitle = data.get("bookTitle");

        showChatNotification(title, body, chatId, requestId, bookTitle);
    }

    // ── Called when the FCM token is refreshed ─────────────────────────
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        // Persist the new token to Firestore so other users can reach this device
        com.google.firebase.auth.FirebaseAuth auth =
                com.google.firebase.auth.FirebaseAuth.getInstance();
        if (auth.getUid() != null) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(auth.getUid())
                    .update("fcmToken", token);
        }
    }

    // ── Build and show the notification ───────────────────────────────
    private void showChatNotification(String title, String body,
                                      String chatId, String requestId,
                                      String bookTitle) {
        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Create channel (required on API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for new book chat messages");
            channel.enableVibration(true);
            channel.setShowBadge(true);
            manager.createNotificationChannel(channel);
        }

        // Deep-link intent → opens ChatActivity with the right chat
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("chatId",    chatId);
        intent.putExtra("requestId", requestId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                (int) System.currentTimeMillis(),   // unique request code
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_IMMUTABLE
        );

        // Build the notification
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.books)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent);

        // Use unique ID so multiple notifications don't overwrite each other
        int notifId = (chatId != null) ? chatId.hashCode()
                : (int) System.currentTimeMillis();
        manager.notify(notifId, builder.build());
    }
}
