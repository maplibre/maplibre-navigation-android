package org.maplibre.navigation.android.example;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import org.maplibre.navigation.android.navigation.v5.navigation.notification.NavigationNotification;
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress;

import static org.maplibre.navigation.android.navigation.v5.navigation.NavigationConstants.NAVIGATION_NOTIFICATION_CHANNEL;

public class CustomNavigationNotification implements NavigationNotification {

    private static final int CUSTOM_NOTIFICATION_ID = 91234821;
    private static final String STOP_NAVIGATION_ACTION = "stop_navigation_action";

    private final Notification customNotification;
    private final NotificationCompat.Builder customNotificationBuilder;
    private final NotificationManager notificationManager;
    private BroadcastReceiver stopNavigationReceiver;
    private int numberOfUpdates;

    public CustomNavigationNotification(Context applicationContext) {
        notificationManager = (NotificationManager) applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);

        customNotificationBuilder = new NotificationCompat.Builder(applicationContext, NAVIGATION_NOTIFICATION_CHANNEL)
                .setSmallIcon(org.maplibre.navigation.android.navigation.ui.v5.R.drawable.ic_navigation)
                .setContentTitle("Custom Navigation Notification")
                .setContentText("Display your own content here!")
                .setContentIntent(createPendingStopIntent(applicationContext));

        customNotification = customNotificationBuilder.build();
    }

    @Override
    public Notification getNotification() {
        return customNotification;
    }

    @Override
    public int getNotificationId() {
        return CUSTOM_NOTIFICATION_ID;
    }

    @Override
    public void updateNotification(RouteProgress routeProgress) {
        // Update the builder with a new number of updates
        customNotificationBuilder.setContentText("Number of updates: " + numberOfUpdates++);

        notificationManager.notify(CUSTOM_NOTIFICATION_ID, customNotificationBuilder.build());
    }

    @Override
    public void onNavigationStopped(Context context) {
        context.unregisterReceiver(stopNavigationReceiver);
        notificationManager.cancel(CUSTOM_NOTIFICATION_ID);
    }

    public void register(BroadcastReceiver stopNavigationReceiver, Context applicationContext) {
        this.stopNavigationReceiver = stopNavigationReceiver;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            applicationContext.registerReceiver(stopNavigationReceiver, new IntentFilter(STOP_NAVIGATION_ACTION), Context.RECEIVER_NOT_EXPORTED);
        } else {
            applicationContext.registerReceiver(stopNavigationReceiver, new IntentFilter(STOP_NAVIGATION_ACTION));
        }
    }

    private PendingIntent createPendingStopIntent(Context context) {
        Intent stopNavigationIntent = new Intent(STOP_NAVIGATION_ACTION);
        return PendingIntent.getBroadcast(context, 0, stopNavigationIntent, PendingIntent.FLAG_IMMUTABLE);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    public void createNotificationChannel(Context context) {
        NotificationChannel chan = new NotificationChannel(NAVIGATION_NOTIFICATION_CHANNEL, "CustomNavigationNotification", NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (service != null) {
            service.createNotificationChannel(chan);
        }
    }
}
