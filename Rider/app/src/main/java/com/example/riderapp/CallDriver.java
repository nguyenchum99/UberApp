package com.example.riderapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.riderapp.Common.Common;
import com.example.riderapp.Model.User;
import com.example.riderapp.Remote.IFCMService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CallDriver extends AppCompatActivity {

    TextView tvDriverName;
    TextView tvDriverPhone;
    TextView tvDriverRate;
    Button btnCallApp;
    Button btnCallPhone;

    String driverId;
    IFCMService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_driver);

        mService = Common.getFCMService();
        tvDriverName = findViewById(R.id.tv_driver_name);
        tvDriverPhone = findViewById(R.id.tv_phone);
        tvDriverRate = findViewById(R.id.tv_rate);
        btnCallApp = findViewById(R.id.btn_call_app);
        btnCallPhone = findViewById(R.id.btn_call_phone);

        btnCallApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if(driverId != null && !driverId.isEmpty())
               // Common.sendRequestToDriver(driverId,mService, getBaseContext(), Common.mLastLocation);

            }
        });

        btnCallPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel: "+ tvDriverPhone.getText().toString()));

                startActivity(intent);
            }
        });

        //get intent
        if(getIntent() != null){
            driverId = getIntent().getStringExtra("driverId");
            double lat = getIntent().getDoubleExtra("lat", -1.0);
            double lng = getIntent().getDoubleExtra("lng", -1.0);

            Common.mLastLocation = new Location("");
            Common.mLastLocation.setLatitude(lat);
            Common.mLastLocation.setLongitude(lng);

            loadDriverInfo(driverId);
        }
    }

    private void loadDriverInfo(String driverId) {

        FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl)
                .child(driverId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User driverUser = dataSnapshot.getValue(User.class);
                       // tvDriverName.setText(driverUser.getUsername());
                        tvDriverPhone.setText(driverUser.getPhone());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
