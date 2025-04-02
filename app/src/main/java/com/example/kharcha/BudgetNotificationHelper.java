package com.example.kharcha;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class BudgetNotificationHelper {
    private static final String CHANNEL_ID = "budget_alert_channel";
    private static final String CHANNEL_NAME = "Budget Alerts";
    private static final String CHANNEL_DESC = "Notifications for budget threshold alerts";
    private static final int NOTIFICATION_ID = 2001;

    // Create notification channel
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESC);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    // Show budget threshold notification
    public static void showBudgetThresholdNotification(Context context, float spentAmount, float budgetAmount) {
        // Create intent to open the app when notification is tapped
        Intent intent = new Intent(context, TransactionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        // Calculate the percentage of budget used
        int percentageUsed = (int) ((spentAmount / budgetAmount) * 100);

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.kharcha_logo) // Use your app icon
                .setContentTitle("Budget Alert")
                .setContentText("You've spent " + percentageUsed + "% of your monthly budget")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("You've spent ₹" + spentAmount + " out of your monthly budget of ₹" + budgetAmount + ". That's " + percentageUsed + "% of your budget."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        try {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        } catch (SecurityException e) {
            // Handle missing notification permission
            e.printStackTrace();
        }
    }

    // Check if budget threshold is reached
    public static void checkBudgetThreshold(Context context, float currentTotalExpense) {
        float budgetAmount = BudgetActivity.getBudget(context);

        // Only proceed if a budget has been set
        if (budgetAmount > 0) {
            float thresholdPercentage = 0.8f; // 80%
            float thresholdAmount = budgetAmount * thresholdPercentage;

            // If expenses have exceeded the threshold
            if (currentTotalExpense >= thresholdAmount) {
                // Send notification
                showBudgetThresholdNotification(context, currentTotalExpense, budgetAmount);
            }
        }
    }
}