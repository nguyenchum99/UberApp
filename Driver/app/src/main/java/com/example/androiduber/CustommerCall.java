package com.example.androiduber;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androiduber.Common.Common;
import com.example.androiduber.Model.FCMResponse;
import com.example.androiduber.Model.Notification;
import com.example.androiduber.Model.Sender;
import com.example.androiduber.Model.Token;
import com.example.androiduber.Remote.IFCMService;
import com.example.androiduber.Remote.IGoogleAPI;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustommerCall extends AppCompatActivity {

    TextView tvTime;
    TextView tvAddress;
    TextView tvDistance;

    Button btnAccept, btnCancel;

    IGoogleAPI mService;
    IFCMService mFCMService;

    String customerId;

    Double lat, lng;
    Double lat_des, lng_des;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custommer_call);

        mService = Common.getGoogleAPI();
        mFCMService = Common.getFCMService();


        tvTime = findViewById(R.id.txt_time);
        tvAddress = findViewById(R.id.txt_address);
        tvDistance = findViewById(R.id.txt_distance);

        btnAccept = findViewById(R.id.btn_accept);
        btnCancel = findViewById(R.id.btn_cancel);

        if(getIntent() != null){
            lat = getIntent().getDoubleExtra("lat", -1.0);
            lng = getIntent().getDoubleExtra("lng", -1.0);
            customerId = getIntent().getStringExtra("customer");
            getDirection(lat, lng);
        }

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!TextUtils.isEmpty(customerId)){
                    Log.d("cus", customerId);
                    cancelBooking(customerId);

                }

            }
        });

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(customerId)){
                    Log.d("cus", customerId);
                    acceptBooking(customerId);

                }
            }
        });


    }

    private void cancelBooking(String customerId) {
        Token token = new Token(customerId);

        Notification notification = new Notification("Notice", "Driver has cancelled your request");

        Sender sender = new Sender(token.getToken(), notification);
        Log.d("notice", "" + customerId);

        mFCMService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if(response.body().success == 1){
                    Toast.makeText(CustommerCall.this, "Cancelled", Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {

            }
        });

    }


    private void acceptBooking(String customerId) {
        Token token = new Token(customerId);

        Notification notification = new Notification("Accept", "Driver has accepted your request");

        Sender sender = new Sender(token.getToken(), notification);

        mFCMService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if(response.body().success == 1){
                    Intent intent = new Intent(CustommerCall.this, DriverTracking.class);
                    //send customer location to new activity
                    intent.putExtra("lat", lat);
                    intent.putExtra("lng", lng);
                    intent.putExtra("customerId", customerId);
//                    intent.putExtra("lat_des", lat_des);
//                    intent.putExtra("lng_des", lng_des);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {

            }
        });

    }


    private void getDirection(Double lat, Double lng) {

        String requestApi = null;

        try{
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=" + Common.mLastLocation.getLatitude()+ "," + Common.mLastLocation.getLongitude() + "&"+
                    "destination=" + lat +"," + lng + "&"+
                    "key=" + getResources().getString(R.string.google_direction_api);

//            requestApi="https://maps.googleapis.com/maps/api/directions/json?mode=driving&" +
//                    "transit_routing_preference=less_driving&origin="+Common.mLastLocation.getLatitude()+","+Common.mLastLocation.getLongitude()+"&" +
//                    "destination="+lat+","+lng+"&key="+getResources().getString(R.string.google_direction_api);

            Log.d("call", requestApi);
            mService.getPath(requestApi).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());

                        JSONArray routes = jsonObject.getJSONArray("routes");

                        JSONObject object = routes.getJSONObject(0);

                        JSONArray legs = object.getJSONArray("legs");

                        JSONObject legsObject = legs.getJSONObject(0);

                        JSONObject distance = legsObject.getJSONObject("distance");
                        tvDistance.setText(distance.getString("text"));

                        //get time
                        JSONObject time = legsObject.getJSONObject("duration");
                        tvTime.setText(time.getString("text"));

                        //get address
                        String address = legsObject.getString("end_address");
                        tvAddress.setText(address);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }


                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(CustommerCall.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
