package com.example.riderapp.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.example.riderapp.R;
import com.example.riderapp.RateActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class MyFirebaseMessaging  extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d("messa", "From: " + remoteMessage.getFrom());
        if(remoteMessage.getNotification().getTitle().equals("Notice")){
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Log.d("mess",""+ remoteMessage.getNotification().getBody() );
                    Toast.makeText(MyFirebaseMessaging.this, ""+ remoteMessage.getNotification().getBody(), Toast.LENGTH_LONG).show();
                }
            });
        }
        else if(remoteMessage.getNotification().getTitle().equals("Accept")){
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Log.d("mess",""+ remoteMessage.getNotification().getBody() );
                    Toast.makeText(MyFirebaseMessaging.this, ""+ remoteMessage.getNotification().getBody(), Toast.LENGTH_LONG).show();
                }
            });
        }
        else if(remoteMessage.getNotification().getTitle().equals("Arrived")){
            showArrivedNotification(remoteMessage.getNotification().getBody());
        }
        else if(remoteMessage.getNotification().getTitle().equals("DropOff")){
            openRateActivity(remoteMessage.getNotification().getBody());
        }
    }

    private void openRateActivity(String body) {

        Intent intent = new Intent(this, RateActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    private void showArrivedNotification(String body) {
        PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(), 0, new Intent(),
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext());

        builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_LIGHTS| Notification.DEFAULT_SOUND)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Arrived")
                .setContentText(body)
                .setContentIntent(contentIntent);

        NotificationManager manager = (NotificationManager) getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);

        manager.notify(1, builder.build());
    }





}
