package com.example.riderapp.Common;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.example.riderapp.HomeActivity;
import com.example.riderapp.Model.FCMResponse;
import com.example.riderapp.Model.Notification;
import com.example.riderapp.Model.Sender;
import com.example.riderapp.Model.Token;
import com.example.riderapp.Model.User;
import com.example.riderapp.Remote.FCMClient;
import com.example.riderapp.Remote.IFCMService;
import com.example.riderapp.Remote.IGoogleAPI;
import com.example.riderapp.Remote.RetrofitClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Common {
    public static String currentToken = "";

    public static final String driver_tbl = "Drivers";
    public static final String user_driver_tbl = "DriversInfomation";
    public static final String user_rider_btl = "RidersInfomation";
    public static final String pickup_request_tbl = "PickupRequest";
    public static final String token_tbl = "Tokens";
    public static final String rate_detail_tbl = "RateDetails";

    public static  boolean isDriverFound = false;
    public static  String driverId = "";
    public static final int PICK_IMAGE_REQUEST = 9999;
    public static final String baseURL = "https://maps.googleapis.com";

    public static final String fcmURL="https://fcm.googleapis.com/";

    private static double base_fare = 2.55;
    private static double time_rate =  0.35;
    private static double distance_rate = 1.75;

    public static User currentUser;
    public static Location mLastLocation;
    public static Location mDestination;

    public static IFCMService getFCMService(){

        return FCMClient.getClient(fcmURL).create(IFCMService.class);

    }

    public static IGoogleAPI getGoogleAPI(){

        return RetrofitClient.getClient(baseURL).create(IGoogleAPI.class);

    }

    // 1km - 1 dollar
    public static  double getPrice(double km, double min)
    {
        return (base_fare + (time_rate*min) + distance_rate*km);
    }

    public  static  void sendRequestToDriver(String driverId, IFCMService mService,
                                             Context context, Location currentLocation) {

        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token_tbl);

        tokens.orderByKey().equalTo(driverId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapShot:dataSnapshot.getChildren())
                        {
                            Token token = postSnapShot.getValue(Token.class);//Get Token object from database with key

                            //Make raw payload - convert LatLng to json
                            String json_lat_lng = new Gson().toJson(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()));
                            String riderToken = FirebaseInstanceId.getInstance().getToken();
                            Notification data = new Notification(riderToken, json_lat_lng);//send it to driver app and we will deserialize it again
                            Sender content = new Sender(token.getToken(),data);

                            mService.sendMessage(content).enqueue(new Callback<FCMResponse>() {
                                @Override
                                public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                    if (response.body().success == 1)
                                        Toast.makeText(context, "Request sent!", Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(context, "Failed !", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Call<FCMResponse> call, Throwable t) {
                                    Log.e("ERROR",t.getMessage());

                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public  static  void sendRequestToDriverDestination(String driverId, IFCMService mService, Location destination) {

        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token_tbl);

        tokens.orderByKey().equalTo(driverId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapShot:dataSnapshot.getChildren())
                        {
                            Token token = postSnapShot.getValue(Token.class);//Get Token object from database with key
                            //Make raw payload - convert LatLng to json
                            String json_lat_lng = new Gson().toJson(new LatLng(destination.getLatitude(),destination.getLongitude()));
                            String riderToken = FirebaseInstanceId.getInstance().getToken();
                            Notification data = new Notification(riderToken,json_lat_lng);//send it to driver app and we will deserialize it again
                            Sender content = new Sender(token.getToken(),data);

                            mService.sendMessage(content).enqueue(new Callback<FCMResponse>() {
                                @Override
                                public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {

                                }

                                @Override
                                public void onFailure(Call<FCMResponse> call, Throwable t) {
                                    Log.e("ERROR",t.getMessage());

                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }



}
