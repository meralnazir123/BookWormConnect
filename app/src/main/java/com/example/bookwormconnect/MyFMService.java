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

public class MyFMService extends FirebaseMessagingService {

    private static final String CHANNEL_ID   = "bookworm_chat_channel";
    private static final String CHANNEL_NAME = "BookWorm Chat Messages";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        String title = "BookWormConnect";
        String body  = "You have a new message";

        if (message.getNotification() != null) {
            if (message.getNotification().getTitle() != null)
                title = message.getNotification().getTitle();
            if (message.getNotification().getBody() != null)
                body  = message.getNotification().getBody();
        }

        Map<String, String> data = message.getData();
        String chatId    = data.get("chatId");
        String requestId = data.get("requestId");
        String bookTitle = data.get("bookTitle");

        showChatNotification(title, body, chatId, requestId, bookTitle);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        com.google.firebase.auth.FirebaseAuth auth =
                com.google.firebase.auth.FirebaseAuth.getInstance();
        if (auth.getUid() != null) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(auth.getUid())
                    .update("fcmToken", token);
        }
    }

    private void showChatNotification(String title, String body,
                                      String chatId, String requestId,
                                      String bookTitle) {
        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

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

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.books)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent);

        int notifId = (chatId != null) ? chatId.hashCode()
                : (int) System.currentTimeMillis();
        manager.notify(notifId, builder.build());
    }
}
