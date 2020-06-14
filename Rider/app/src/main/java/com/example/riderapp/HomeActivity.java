package com.example.riderapp;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.example.riderapp.Common.Common;
import com.example.riderapp.Helper.CustomInfoWindow;

import com.example.riderapp.Model.FCMResponse;
import com.example.riderapp.Model.Notification;
import com.example.riderapp.Model.Sender;
import com.example.riderapp.Model.Token;
import com.example.riderapp.Model.User;
import com.example.riderapp.Remote.IFCMService;
import com.example.riderapp.Remote.IGoogleAPI;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;


import static com.google.android.libraries.places.api.model.Place.Field.LAT_LNG;

import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnInfoWindowClickListener {


    //your location
    SupportMapFragment mapFragment;
    private GoogleMap mMap;

    //play services
    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    private static final int PLAY_SERVICE_RES_REQUEST = 7001;

    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;

    private static int UPDATE_INTERVAL = 5000;
    private static int FATEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    DatabaseReference ref;
    GeoFire geoFire;

    Marker mUserMarker, mMarkerDestination;

    //bottom sheet
    ImageView imgExpandable;
    BottomSheetRiderFragment mBottomSheet;
    Button btnPickupRequest;

    //find driver
//    boolean isDriverFound = false;
//    String driverId = "";
    int radius = 1; // 1 km
    int distance = 1; // 1km
    private static final int LIMIT = 3; //  3km


    //send alert
    IFCMService mService;
    IGoogleAPI mGoogle;
    IGoogleAPI mgg;

    //presense driver
    DatabaseReference driversAvailable;
    //PlaceAutocompleteFragment placeLocation;
    //place location and destination
    AutocompleteSupportFragment placeLocation;
    AutocompleteSupportFragment placeDestination;

    List<com.google.android.libraries.places.api.model.Place.Field> placeFields = Arrays.asList(com.google.android.libraries.places.api.model.Place.Field.ID,
            com.google.android.libraries.places.api.model.Place.Field.NAME,
            com.google.android.libraries.places.api.model.Place.Field.ADDRESS);

    String mPlaceLocation, mPlaceDestination;
    PlacesClient placesClient;

    LatLng locationPickup;
    LatLng destinationPickup;
    //Location mDestination;

    //Vehicle
    ImageView car, motor, bike;
    boolean isCar = true;

    public AlertDialog waitingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mService = Common.getFCMService();
        mGoogle = Common.getGoogleAPI();


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);


        View navigationHeaderView = navigationView.getHeaderView(0);
        TextView tvName = navigationHeaderView.findViewById(R.id.tv_name);
        ImageView imageView = navigationHeaderView.findViewById(R.id.imageView);

        tvName.setText(Common.currentUser.getUsername());

        if (Common.currentUser.getAvatarUrl() != null
                && !TextUtils.isEmpty(Common.currentUser.getAvatarUrl())) {
            Picasso.with(this)
                    .load(Common.currentUser.getAvatarUrl())
                    .into(imageView);
        }

        //vehicle
        car = findViewById(R.id.img_car);
        motor = findViewById(R.id.img_motor);

        car.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCar = true;
                if (isCar) {
                    car.setImageResource(R.drawable.icon_car);
                    motor.setImageResource(R.drawable.dislike);
                    // bike.setImageResource(R.drawable.dislike);
                } else {
                    car.setImageResource(R.drawable.dislike);
                    motor.setImageResource(R.drawable.motor);
                    //bike.setImageResource(R.drawable.dislike);
                }
                mMap.clear();
                loadAllAvaibleDriver(new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()));
            }
        });


        motor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCar = false;
                if (isCar) {
                    car.setImageResource(R.drawable.icon_car);
                    motor.setImageResource(R.drawable.dislike);
                    // bike.setImageResource(R.drawable.dislike);
                } else {
                    car.setImageResource(R.drawable.dislike);
                    motor.setImageResource(R.drawable.motor);
                    //bike.setImageResource(R.drawable.dislike);
                }
                mMap.clear();
                loadAllAvaibleDriver(new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()));
            }
        });

        //map
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Init bottom sheet

        btnPickupRequest = findViewById(R.id.btn_pickup_request);
        btnPickupRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!Common.isDriverFound) {
                    requestPickupHere(FirebaseAuth.getInstance().getCurrentUser().getUid());
                } else {


                    Common.sendRequestToDriver(Common.driverId, mService, getBaseContext(),Common.mLastLocation);

                   // Common.sendRequestToDriverDestination(Common.driverId, mService, Common.mDestination);
                }

            }
        });


        //selection location pick up and destination
        // initPlaces();
        setupPlaceAutoCompleteLocation();
        setupPlaceAutoCompleteDestination();

        setUpLocation();
        updateFirebaseToken();
    }

    private void getDirectionLocationPickup(String destination) {

        String requestApi = null;

        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "&origin=" + Common.mLastLocation.getLatitude() + "," + Common.mLastLocation.getLongitude() + "&" +
                    "destination=" + destination +
                    "&key=" + getResources().getString(R.string.google_direction_api);


            Log.d("location", requestApi);
            mGoogle.getPath(requestApi).enqueue(new Callback<String>() {
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
                        locationPickup = new LatLng(lat_end, lng_end);
                        Common.mLastLocation.setLatitude(lat_end);
                        Common.mLastLocation.setLongitude(lng_end);

                        Log.d("aaa", "" + Common.mLastLocation.getLatitude());
                        mUserMarker = mMap.addMarker(new MarkerOptions().position(locationPickup)
                                .icon(BitmapDescriptorFactory.defaultMarker())
                                .title("Pick up"));

                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationPickup, 15.0f));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }


                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(HomeActivity.this, "" + t.getMessage(), Toast.LENGTH_LONG).show();
                    Log.d("error", "" + t.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void getDirectionDestinationPickup(String destination) {
        Log.d("bbb", "" + Common.mLastLocation.getLatitude());
        String requestApi = null;
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        Common.mDestination = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        try{
            requestApi="https://maps.googleapis.com/maps/api/directions/json?" +
                    "&origin="+  Common.mLastLocation.getLatitude()+"," +  Common.mLastLocation.getLongitude()+"&" +
                    "destination="+ destination +
                    "&key=" + getResources().getString(R.string.google_direction_api);


            Log.d("dess", requestApi);
            mGoogle.getPath(requestApi).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try {


                        JSONObject jsonObject = new JSONObject(response.body().toString());

                        JSONArray routes = jsonObject.getJSONArray("routes");

                        JSONObject object = routes.getJSONObject(0);

                        JSONArray legs = object.getJSONArray("legs");

                        JSONObject legsObject = legs.getJSONObject(0);

                        JSONObject endLocation = legsObject.getJSONObject("end_location");
                        double lat_end = endLocation.getDouble("lat");
                        double lng_end = endLocation.getDouble("lng");
                        destinationPickup = new LatLng(lat_end, lng_end);
                        Common.mDestination.setLatitude(lat_end);
                        Common.mDestination.setLongitude(lng_end);

                        Log.d("lat", "" + destinationPickup.latitude );
                        mMarkerDestination = mMap.addMarker(new MarkerOptions().position(destinationPickup)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_des))
                                .title("Destination"));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destinationPickup, 15.0f));

                        mPlaceLocation = legsObject.getString("start_address");
                        mPlaceDestination = legsObject.getString("end_address");

                        //show info in bottom sheet
                        BottomSheetRiderFragment mBottomSheet =
                                BottomSheetRiderFragment.newInstanse(legsObject.getString("start_address"),
                                        legsObject.getString("end_address"), false);

                        mBottomSheet.show(getSupportFragmentManager(), mBottomSheet.getTag());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }


                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(HomeActivity.this, "" + t.getMessage(), Toast.LENGTH_LONG).show();
                    Log.d("error", "" + t.getMessage());
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void initPlaces() {
        //Places.initialize(this, getString(R.string.places_api_key));
        Places.initialize(getApplicationContext(), getString(R.string.google_place_api));
        placesClient = Places.createClient(this);
    }

    private void setupPlaceAutoCompleteLocation(){
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(),  getString(R.string.google_place_api));
        }
        placeLocation = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.place_location);
        placeLocation.setHint("Location");
       if(placeLocation != null ) {
           placeLocation.setPlaceFields(placeFields);

           placeLocation.setOnPlaceSelectedListener(new PlaceSelectionListener() {
               @Override
               public void onPlaceSelected(@NonNull Place place) {
                   mPlaceLocation = place.getName().toString();
                   mPlaceLocation.replace(" ", "+");
                   Log.d("test", "" + mPlaceLocation);
                   mMap.clear();
                   if(mUserMarker.isVisible())
                       mUserMarker.remove();
                   getDirectionLocationPickup(mPlaceLocation);
                   Toast.makeText(HomeActivity.this, place.getAddress().toString(), Toast.LENGTH_LONG).show();
               }

               @Override
               public void onError(@NonNull Status status) {

               }
           });

       }

    }


    private void setupPlaceAutoCompleteDestination(){
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(),  getString(R.string.place_api));
        }
        placeDestination = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.place_destination);
        placeDestination.setHint("Destination");
        if(placeDestination != null) {
            placeDestination.setPlaceFields(placeFields);
            placeDestination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    mPlaceDestination = place.getName().toString();
                    mPlaceDestination.replace(" ", "+");
                    if(mMarkerDestination != null)
                        mMarkerDestination.remove();
                    getDirectionDestinationPickup(mPlaceDestination);
                    Toast.makeText(HomeActivity.this, place.getAddress().toString(), Toast.LENGTH_LONG).show();

                }

                @Override
                public void onError(@NonNull Status status) {

                }
            });
        }

    }


    private void updateFirebaseToken() {

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference(Common.token_tbl);

        Token token = new Token(FirebaseInstanceId.getInstance().getToken());
        tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(token);

    }


    private void requestPickupHere(String uid) {
        DatabaseReference dbRequest = FirebaseDatabase.getInstance().getReference(Common.pickup_request_tbl);
        GeoFire mGeoFire = new GeoFire(dbRequest);

        mGeoFire.setLocation(uid, new GeoLocation(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude())
                , new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {

                    }
                });

        if(mUserMarker.isVisible())
            mUserMarker.remove();

        //add new marker
        mUserMarker = mMap.addMarker(new MarkerOptions()
                .title("Pick up here")
                .snippet("")
                .position(new LatLng( Common.mLastLocation.getLatitude(),  Common.mLastLocation.getLongitude()))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        mUserMarker.showInfoWindow();

        btnPickupRequest.setText("Getting your driver...");

        findDriver();

    }


    //find driver on 1 km, if not found 1km, auto increase distance
    private void findDriver() {
        DatabaseReference driverLocation;
        if(isCar)
            driverLocation = FirebaseDatabase.getInstance().getReference(Common.driver_tbl).child("Car");
        else
            driverLocation = FirebaseDatabase.getInstance().getReference(Common.driver_tbl).child("Motor bike");

        GeoFire gf = new GeoFire(driverLocation);
        GeoQuery geoQuery = gf.queryAtLocation(new GeoLocation(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()), radius);

        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                //if found
                if(!Common.isDriverFound){
                    Common.isDriverFound = true;
                    Common.driverId = key;
                    btnPickupRequest.setText("Request pickup");
                    Toast.makeText(HomeActivity.this, "" + key, Toast.LENGTH_LONG).show();

                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                //if still not found driver, increase distance
                if(!Common.isDriverFound && radius <LIMIT){
                    radius++;
                    Toast.makeText(HomeActivity.this,"No available any driver near you", Toast.LENGTH_LONG).show();
                    findDriver();
                }else{
                    //Toast.makeText(HomeActivity.this,"No available any driver near you", Toast.LENGTH_LONG).show();
                    btnPickupRequest.setText("Request pickup");
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

//    public  void sendRequestToDriver(String driverId) {
//
//        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token_tbl);
//
//        tokens.orderByKey().equalTo(driverId)
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        for (DataSnapshot postSnapShot:dataSnapshot.getChildren())
//                        {
//                            Token token = postSnapShot.getValue(Token.class);//Get Token object from database with key
//
//                            //Make raw payload - convert LatLng to json
//                            String json_lat_lng = new Gson().toJson(new LatLng(Common.mLastLocation.getLatitude(), Common.getLongitude()));
//                            String riderToken = FirebaseInstanceId.getInstance().getToken();
//                            Notification data = new Notification(riderToken,json_lat_lng); //send it to driver app and we will deserialize it again
//                            Sender content = new Sender(token.getToken(),data); //Send this data to token
//
//                            mService.sendMessage(content).enqueue(new Callback<FCMResponse>() {
//                                @Override
//                                public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
//                                    if (response.body().success == 1)
//                                        Toast.makeText(HomeActivity.this, "Request sent!", Toast.LENGTH_SHORT).show();
//                                    else
//                                        Toast.makeText(HomeActivity.this, "Failed !", Toast.LENGTH_SHORT).show();
//                                }
//
//                                @Override
//                                public void onFailure(Call<FCMResponse> call, Throwable t) {
//                                    Log.e("ERROR",t.getMessage());
//
//                                }
//                            });
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSION_REQUEST_CODE:
                if(grantResults.length >  0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(checkPlayServices()){
                        builGoogleApiClient();
                        createLocationRequest();
                        displayLocation();
                    }
                }
        }
    }

    private void setUpLocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        &&  ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
            //request runtime permission
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.CALL_PHONE

            }, MY_PERMISSION_REQUEST_CODE);
        }else{
            if(checkPlayServices()){

                builGoogleApiClient();
                createLocationRequest();
                displayLocation();

            }
        }

    }

    private void displayLocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }

        Common.mLastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if( Common.mLastLocation != null){
            //presense system driver
            driversAvailable = FirebaseDatabase.getInstance().getReference(Common.driver_tbl).child(isCar?"Car":"Motor bike");
            driversAvailable.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //if have any change from drivers table, we will reload all drivers available
                    loadAllAvaibleDriver(new LatLng( Common.mLastLocation.getLatitude(),  Common.mLastLocation.getLongitude()));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            final double latitude =  Common.mLastLocation.getLatitude();
            final double longtitude =  Common.mLastLocation.getLongitude();

            // add marker
            if(mUserMarker != null){
                mUserMarker.remove();
            }
            mUserMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latitude, longtitude))
                    .title("you"));
            //movew camera to this position
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longtitude), 15.0f));

            //load all drivers online
            loadAllAvaibleDriver(new LatLng( Common.mLastLocation.getLatitude(),  Common.mLastLocation.getLongitude()));


        }else{
            Log.d("ERROR", "cannot get your location");
        }
    }

    //load all avaible drivers in distance 3km
    private void loadAllAvaibleDriver(final LatLng location) {

        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(location)
                        .title("You"));

        DatabaseReference driverLocation;

        if(isCar)
            driverLocation = FirebaseDatabase.getInstance().getReference(Common.driver_tbl).child("Car");
        else
            driverLocation = FirebaseDatabase.getInstance().getReference(Common.driver_tbl).child("Motor bike");

        GeoFire gf = new GeoFire(driverLocation);

        GeoQuery geoQuery = gf.queryAtLocation(new GeoLocation(location.latitude, location.longitude), distance);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl)
                        .child(key)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                //rider
                                User user = dataSnapshot.getValue(User.class);

                                //add driver to map
                                mMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(location.latitude, location.longitude))
                                            .flat(true)
                                            .title("Driver")
                                            .snippet("Phone:" + user.getPhone())
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(distance <= LIMIT){
                    distance++;
                    loadAllAvaibleDriver(location);
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FATEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void builGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) this)
                .addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) this).addApi(LocationServices.API).build();

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
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_update_info) {
            showUpdateInfomation();

        } else if (id == R.id.nav_signout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(HomeActivity.this, LoginUser.class);
            startActivity(intent);
            finish();

        } else if (id == R.id.nav_history) {

        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showUpdateInfomation() {
        AlertDialog.Builder alertdialog = new AlertDialog.Builder(HomeActivity.this);
        alertdialog.setTitle("Update information");

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View update_layout = layoutInflater.inflate(R.layout.activity_update_info_user, null);

        EditText edtName  = update_layout.findViewById(R.id.edt_name);
        EditText edtPhone =  update_layout.findViewById(R.id.edt_phone);
       // ImageView imgAvatar = update_layout.findViewById(R.id.img_avatar);

        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference mDatabaseRef = database.getReference();

        alertdialog.setView(update_layout);
        alertdialog.setPositiveButton("Update infomation", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //dialog.dismiss();

                if (edtName.getText().toString().trim().length() == 0) {
                    edtName.setError("Name is required");
                    return;
                }
                if (edtPhone.getText().toString().trim().length() == 0) {
                    edtPhone.setError("Phone is required");
                    return;
                }

                mDatabaseRef.child(Common.user_rider_btl).
                        child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        dataSnapshot.getRef().child("username").setValue(edtName.getText().toString());
                        dataSnapshot.getRef().child("phone").setValue(edtPhone.getText().toString());
                        Toast.makeText(HomeActivity.this, "Update infor success", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
        alertdialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertdialog.show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.setInfoWindowAdapter(new CustomInfoWindow(this));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //first, check marker destination,
                //if is not null, remove old marker
                if(mMarkerDestination != null)
                    mMarkerDestination.remove();

                double lat = latLng.latitude;
                double lng = latLng.longitude;

//                Common.mDestination.setLatitude(lat);
//                Common.mDestination.setLongitude(lng);

                mMarkerDestination =  mMap.addMarker(new MarkerOptions().position(latLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_des))
                        .title("Destination"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));

                // show bottom sheet
                BottomSheetRiderFragment mBottomSheet =
                        BottomSheetRiderFragment.newInstanse(String.format("%f, %f",  Common.mLastLocation.getLatitude(),  Common.mLastLocation.getLongitude()),
                                String.format("%f, %f", latLng.latitude, latLng.longitude) , true);

                mBottomSheet.show(getSupportFragmentManager(), mBottomSheet.getTag());

            }
        });

        mMap.setOnInfoWindowClickListener(this);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdate();
    }

    private void startLocationUpdate() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, (com.google.android.gms.location.LocationListener) this);

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

    @Override
    public void onInfoWindowClick(Marker marker) {
        if(marker.getTitle().equals("Driver")){
            Log.d("call", "" + Common.mLastLocation.getLatitude());
            Intent intent = new Intent(HomeActivity.this, CallDriver.class);
            intent.putExtra("driverId", marker.getSnippet().replaceAll("\\D+", ""));
            intent.putExtra("lat",  Common.mLastLocation.getLatitude());
            intent.putExtra("lng",  Common.mLastLocation.getLongitude());
            startActivity(intent);
        }
    }
}
