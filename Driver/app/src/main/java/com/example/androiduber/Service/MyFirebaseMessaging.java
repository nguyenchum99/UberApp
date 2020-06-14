package com.example.androiduber.Service;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.androiduber.CustommerCall;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessaging  extends FirebaseMessagingService {


    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Intent intent = new Intent(getBaseContext(), CustommerCall.class);

//        if(remoteMessage.getNotification().getTitle().equals("destinaton")){
//            LatLng customer_destination = new Gson().fromJson(remoteMessage.getNotification().getBody(), LatLng.class);
//            intent.putExtra("lat_des", customer_destination.latitude);
//            intent.putExtra("lng_des", customer_destination.longitude);
//        }

        LatLng customer_location = new Gson().fromJson(remoteMessage.getNotification().getBody(), LatLng.class);
        intent.putExtra("lat", customer_location.latitude);
        intent.putExtra("lng", customer_location.longitude);
        intent.putExtra("customer", remoteMessage.getNotification().getTitle());

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);


    }
}
