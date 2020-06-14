package com.example.androiduber;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.Toast;

import com.example.androiduber.Common.Common;
import com.example.androiduber.Helper.DirectionJSONParser;
import com.example.androiduber.Model.FCMResponse;
import com.example.androiduber.Model.Notification;
import com.example.androiduber.Model.Sender;
import com.example.androiduber.Model.Token;
import com.example.androiduber.Remote.IFCMService;
import com.example.androiduber.Remote.IGoogleAPI;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverTracking extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;

    Double riderLat, riderLng;
    Double riderDestinationLat, riderDestinationLng;

    String customerId;

    //play services
    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    private static final int PLAY_SERVICE_RES_REQUEST = 7001;

    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;


    private static int UPDATE_INTERVAL = 5000;
    private static int FATEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    private Circle riderMarker;
    private Marker driverMarker;

    private Polyline direction;
    private List<LatLng> polyLineList = new ArrayList<>();
    private Marker carMarker;
    private PolylineOptions polylineOptions, blackPolylineOptions;
    private Polyline blackPolyline;
    private Polyline greyPolyline;
    private Handler handler;
    private int index, next;
    private LatLng startPosition, endPosition;
    private float v;
    private double lat;
    private double lng;


    IGoogleAPI mService;
    IFCMService mFCMService;

    GeoFire geoFire;
    Button btnStartTrip;
    Location pickupLocation;

    Runnable drawPathRunnable = new Runnable() {
        @Override
        public void run() {

            if(index < polyLineList.size() - 1){
                index++;
                next = index + 1;
            }

            if(index < polyLineList.size() - 1){
                startPosition = polyLineList.get(index);
                endPosition = polyLineList.get(next);
            }

            final ValueAnimator valueAnimator = ValueAnimator.ofFloat(0,1);
            valueAnimator.setDuration(3000);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    v = animation.getAnimatedFraction();

                    lng = v*endPosition.longitude + (1-v)*startPosition.longitude;
                    lat = v*endPosition.latitude + (1 -v)*startPosition.latitude;
                    LatLng newPos = new LatLng(lat, lng);
                    carMarker.setPosition(newPos);
                    carMarker.setAnchor(0.5f, 0.5f);
                    carMarker.setRotation(getBearing(startPosition, newPos));
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition
                            .Builder().target(newPos).zoom(15.5f).build()
                    ));
                }
            });

            valueAnimator.start();
            handler.postDelayed(this, 6000);

        }
    };

    private float getBearing(LatLng startPosition, LatLng endPosition) {

        double lat = Math.abs(startPosition.latitude - endPosition.latitude);
        double lng = Math.abs(startPosition.longitude - endPosition.longitude);

        float kq = 0;

        if(startPosition.latitude < endPosition.latitude && startPosition.longitude < endPosition.longitude)
            kq = (float) (Math.toDegrees(Math.atan(lng/lat)));
        else if(startPosition.latitude >= endPosition.latitude && startPosition.longitude < endPosition.longitude)
            kq =  (float) ((90 - Math.toDegrees(Math.atan(lng/lat))) + 90);
        else if(startPosition.latitude >= endPosition.latitude && startPosition.longitude >= endPosition.longitude)
            kq = (float) (Math.toDegrees(Math.atan(lng/lat))) + 180;
        else if(startPosition.latitude < endPosition.latitude && startPosition.longitude >= endPosition.longitude)
            kq = (float) ((90 - Math.toDegrees(Math.atan(lng/lat))) + 270);

        return kq;

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_tracking);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        mService = Common.getGoogleAPI();
        mFCMService = Common.getFCMService();

        if(getIntent() != null){
            riderLat = getIntent().getDoubleExtra("lat", -1.0);
            riderLng = getIntent().getDoubleExtra("lng", -1.0);
            customerId = getIntent().getStringExtra("customerId");
//            riderDestinationLat =  getIntent().getDoubleExtra("lat_des", -1.0);
//            riderDestinationLng =  getIntent().getDoubleExtra("lng_des", -1.0);

        }

        setUpLocation();

        btnStartTrip = findViewById(R.id.btn_start_trip);

        btnStartTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btnStartTrip.getText().equals("Start Trip")){
                    pickupLocation = Common.mLastLocation;
                    btnStartTrip.setText("Trip detail");
                }else if(btnStartTrip.getText().equals("Trip detail")){
                    calculateCashFee();
                }
            }
        });

    }

    private void calculateCashFee() {
//        if(getIntent() != null){
//            riderLat = getIntent().getDoubleExtra("lat", -1.0);
//            riderLng = getIntent().getDoubleExtra("lng", -1.0);
//            customerId = getIntent().getStringExtra("customerId");
//
//        }

        String requestApi = null;

        try{
            requestApi="https://maps.googleapis.com/maps/api/directions/json?mode=driving&" +
                    "transit_routing_preference=less_driving&origin="+ Common.mLastLocation.getLatitude()+","+ Common.mLastLocation.getLongitude() +"&" +
                    "destination="+ riderLat +"," + riderLng
                    +"&key=" + getResources().getString(R.string.google_direction_api);


            Log.d("fee", requestApi);
            mService.getPath(requestApi).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try {
                        // new ParserTask().execute(response.body().toString());
                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        JSONArray routes = jsonObject.getJSONArray("routes");

                        JSONObject object = routes.getJSONObject(0);

                        JSONArray legs = object.getJSONArray("legs");

                        JSONObject legsObject = legs.getJSONObject(0);

                        JSONObject distance = legsObject.getJSONObject("distance");
                        String distance_text = distance.getString("text");
                        Double distance_value = Double.parseDouble(distance_text.replaceAll("[^0-9\\\\.]+", ""));

                        JSONObject time = legsObject.getJSONObject("duration");
                        String time_text = time.getString("text");
                        Integer time_value = Integer.parseInt(time_text.replaceAll("[^0-9\\\\.]+", ""));

                        //create new activity
                        Intent intent = new Intent(DriverTracking.this, TripDetail.class);
                        intent.putExtra("start_address", legsObject.getString("start_address"));
                        intent.putExtra("end_address", legsObject.getString("end_address"));
                        intent.putExtra("time", time_text);
                        intent.putExtra("distance", distance_text);
                        intent.putExtra("total", Common.formulaPrice(distance_value, time_value));

                        sendDropOffNotification(customerId);

                        startActivity(intent);
                        finish();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }


                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(DriverTracking.this, "" + t.getMessage(), Toast.LENGTH_LONG).show();
                    Log.d("error", "" + t.getMessage());
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void setUpLocation() {
            if(checkPlayServices()){
                builGoogleApiClient();
                createLocationRequest();
                displayLocation();
            }

    }

    @SuppressLint("RestrictedApi")
    private void createLocationRequest() {

        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FATEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(DISPLACEMENT);

    }

    private void builGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(LocationServices.API).build();

        googleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if(resultCode != ConnectionResult.SUCCESS){
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICE_RES_REQUEST).show();
            else{
                Toast.makeText(this, "This device is not supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return  false;
        }
        return true;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        //create geofire fencing with radius is 50m
        geoFire = new GeoFire(FirebaseDatabase.getInstance().getReference(Common.driver_tbl).child(Common.currentUser.getCarType()));
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(riderLat,riderLng), 0.05f);
        ((GeoQuery) geoQuery).addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                sendArrivedNotification(customerId);
                btnStartTrip.setEnabled(true);
                btnStartTrip.setText("Drop off here");

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });


    }

    private void sendArrivedNotification(String customerId) {
        Token token = new Token(customerId);
        Notification notification = new Notification("Arrived",
                String.format("The driver has arrived at your location", Common.currentUser.getUsername()));

        Sender sender = new Sender(token.getToken(), notification);

        mFCMService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if(response.body().success != 1)
                    Toast.makeText(DriverTracking.this, "Failed", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {

            }
        });
    }

    private void sendDropOffNotification(String customerId){
        Token token = new Token(customerId);
        Notification notification = new Notification("DropOff", customerId);

        Sender sender = new Sender(token.getToken(), notification);

        mFCMService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if(response.body().success != 1)
                    Toast.makeText(DriverTracking.this, "Failed", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {

            }
        });

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdate();
    }

    private void startLocationUpdate(){

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, (com.google.android.gms.location.LocationListener) this);


    }

    private void displayLocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }

        Common.mLastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if(Common.mLastLocation != null){

                final double latitude = Common.mLastLocation.getLatitude();
                final double longtitude = Common.mLastLocation.getLongitude();

                if(driverMarker != null) driverMarker.remove();

                if(direction != null) direction.remove();
                getDirection(latitude, longtitude);

        }else{
            Log.d("ERROR", "cannot get your location");
        }

    }

    private void getDirection(final double latitude, final double longtitude) {
        LatLng currentPosition = new LatLng(latitude,longtitude);
        String requestApi = null;

        try{
          requestApi="https://maps.googleapis.com/maps/api/directions/json?mode=driving&" +
                        "transit_routing_preference=less_driving&origin="+currentPosition.latitude+","+ currentPosition.longitude+"&" +
                        "destination="+ riderLat+"," + riderLng +"&key=" + getResources().getString(R.string.google_direction_api);


            Log.d("gg api", requestApi);
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
                        double distanceDetail = distance.getDouble("value");

                        JSONObject startLocation = legsObject.getJSONObject("start_location");
                        double lat_start = startLocation.getDouble("lat");
                        double lng_start = startLocation.getDouble("lng");
                        LatLng start = new LatLng(lat_start, lng_start);

                        JSONObject endLocation = legsObject.getJSONObject("end_location");
                        double lat_end = endLocation.getDouble("lat");
                        double lng_end = endLocation.getDouble("lng");
                        LatLng customer = new LatLng(lat_end, lng_end);

                        for(int i = 0; i < routes.length(); i++){
                            JSONObject route =  routes.getJSONObject(i);
                            JSONObject poly = route.getJSONObject("overview_polyline");
                            String polyline = poly.getString("points");
                            polyLineList = decodePoly(polyline);
                        }

                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        for(LatLng latLng:polyLineList)
                            builder.include(latLng);
                        LatLngBounds bounds = builder.build();
                        CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 2);
                        mMap.animateCamera(mCameraUpdate);

                        polylineOptions = new PolylineOptions();
                        polylineOptions.color(Color.GRAY);
                        polylineOptions.width(5);
                        polylineOptions.startCap(new SquareCap());
                        polylineOptions.endCap(new SquareCap());
                        polylineOptions.jointType(JointType.ROUND);
                        polylineOptions.addAll(polyLineList);
                        greyPolyline = mMap.addPolyline(polylineOptions);

                        blackPolylineOptions = new PolylineOptions();
                        blackPolylineOptions.color(Color.BLACK);
                        blackPolylineOptions.width(5);
                        blackPolylineOptions.startCap(new SquareCap());
                        blackPolylineOptions.endCap(new SquareCap());
                        blackPolylineOptions.jointType(JointType.ROUND);
                        blackPolyline = mMap.addPolyline(blackPolylineOptions);

                        mMap.addMarker(new MarkerOptions()
                                .position(polyLineList.get(polyLineList.size()-1))
                                .title("Pick up location"));

//                        riderMarker = mMap.addCircle(new CircleOptions()
//                                .center(new LatLng(riderLat, riderLng))
//                                .radius(50) // radius is 50m
//                                .strokeColor(Color.BLUE)
//                                .fillColor(0x220000FF)
//                                .strokeWidth(5.0f));

                        //animation
                        ValueAnimator polyLineAnimator = ValueAnimator.ofInt(0,100);
                        polyLineAnimator.setDuration(2000);
                        polyLineAnimator.setInterpolator(new LinearInterpolator());
                        polyLineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                List<LatLng> points = greyPolyline.getPoints();
                                int percentValue = (int) animation.getAnimatedValue();
                                int size = points.size();
                                int newPoints = (int) (size* (percentValue/100.0f));
                                List<LatLng> p = points.subList(0, newPoints);
                                blackPolyline.setPoints(p);
                            }
                        });

                        polyLineAnimator.start();

                        if(carMarker != null) carMarker.remove();

                        carMarker= mMap.addMarker(new MarkerOptions().position(currentPosition).flat(true)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));

//
                        handler= new Handler();
                        index = -1;
                        next = 1;
                        handler.postDelayed(drawPathRunnable, 3000);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }


                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(DriverTracking.this, "" + t.getMessage(), Toast.LENGTH_LONG).show();
                    Log.d("error", "" + t.getMessage());
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private List decodePoly(String encoded) {

        List poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            poly.add(new LatLng((double) lat / 1E5, (double) lng / 1E5));
        }

        return poly;
    }

    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Common.mLastLocation = location;
        displayLocation();
    }


}


