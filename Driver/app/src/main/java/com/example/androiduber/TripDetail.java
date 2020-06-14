package com.example.androiduber;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.example.androiduber.Common.Common;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Calendar;
import java.util.TimeZone;

public class TripDetail extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TextView tvDate, tvFee, tvBaseFare, tvTime, tvDistance, tvTotal, tvFrom, tvTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail);

        tvDate = findViewById(R.id.tv_date);
        tvBaseFare = findViewById(R.id.tv_base_fare);
        tvFee = findViewById(R.id.tv_fee);
        tvDistance = findViewById(R.id.tv_distance);
        tvTime = findViewById(R.id.tv_time);
        tvFrom = findViewById(R.id.tv_from);
        tvTo = findViewById(R.id.tv_to);
        tvTotal = findViewById(R.id.tv_total);
        settingInfomation();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
       settingInfomation();
    }

    private void settingInfomation() {
        if(getIntent() != null){
            Calendar calendar = Calendar.getInstance(TimeZone.getDefault());

            int currentYear = calendar.get(Calendar.YEAR);
            int currentMonth = calendar.get(Calendar.MONTH) + 1;
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

            tvDate.setText(""  + currentDay + "/" + currentMonth + "/" + currentYear);
            tvFee.setText(String.format("$ %.2f", getIntent().getDoubleExtra("total", 0.0)));
            tvTotal.setText(String.format("$ %.2f", getIntent().getDoubleExtra("total", 0.0)));
            tvBaseFare.setText(String.format("$ %.2f", Common.base_fare));
            tvTime.setText(String.format("%s ", getIntent().getStringExtra("time")));
            tvDistance.setText(String.format("%s ", getIntent().getStringExtra("distance")));
            tvFrom.setText("From: " + getIntent().getStringExtra("end_address"));
            tvTo.setText("To: " + getIntent().getStringExtra("end_address"));

        }
    }

    private String convertToDayOfWeek(int day) {
        switch (day){
            case Calendar.MONDAY:
                return "Monday";
            case Calendar.TUESDAY:
                return "Tuesday";
            case Calendar.WEDNESDAY:
                return "Wednesday";
            case Calendar.THURSDAY:
                return "Thursday";
            case Calendar.FRIDAY:
                return "Friday";
            case Calendar.SATURDAY:
                return "Saturday";
            case Calendar.SUNDAY:
                return "Sunday";
                default:
                    return "UNK";
        }
    }
}
