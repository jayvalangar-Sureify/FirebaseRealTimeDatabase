package com.example.firebaserealtimedatabase;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.jetbrains.annotations.NotNull;

import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;

public class MyWorker extends Worker {
    @NotNull
    public static final String CHANNEL_ID = "channel_id";
    public static final int NOTIFICATION_ID = 1;
    @NotNull
    public static final MyWorker.Companion Companion = new MyWorker.Companion((DefaultConstructorMarker) null);

    public MyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NotNull
    public Result doWork() {
        Log.i("test_response", "DoWork : Success function called");
        this.issueNotification();
        Result var10000 = Result.success();
        Intrinsics.checkNotNullExpressionValue(var10000, "Result.success()");
        return var10000;
    }

    private void issueNotification()
    {
        // Create the notification channel for Android 8.0+
        if (Build.VERSION.SDK_INT
                >= Build.VERSION_CODES.O) {
            makeNotificationChannel(
                    "CHANNEL_1", "Example channel",
                    NotificationManager.IMPORTANCE_DEFAULT);
        }

        // Creating the notification builder
        NotificationCompat.Builder notificationBuilder
                = new NotificationCompat.Builder(getApplicationContext(), "CHANNEL_1");

        // Setting the notification's properties
        notificationBuilder
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Notification!")
                .setContentText("This is an Oreo notification!")
                .setNumber(3);

        // Getting the notification manager and send the
        // notification
        NotificationManager notificationManager
                = (NotificationManager) getApplicationContext().getSystemService(
                NOTIFICATION_SERVICE);

        // it is better to not use 0 as notification id, so
        // used 1.
        notificationManager.notify(
                1, notificationBuilder.build());
    }

    // Helper method to create a notification channel for
    // Android 8.0+
    private void makeNotificationChannel(String channelId,
                                         String channelName,
                                         int importance)
    {
        NotificationChannel channel
                = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(
            channelId, channelName, importance);
        }
        NotificationManager notificationManager
                = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static final class Companion {
        private Companion() {
        }

        // $FF: synthetic method
        public Companion(DefaultConstructorMarker $constructor_marker) {
            this();
        }
    }
}
