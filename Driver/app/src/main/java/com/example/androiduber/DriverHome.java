package com.example.androiduber;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import com.example.androiduber.Common.Common;
import com.example.androiduber.Model.Token;
import com.example.androiduber.Model.User;
import com.example.androiduber.Remote.IGoogleAPI;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.os.Handler;
import android.provider.MediaStore;
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

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverHome extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener
        {


    private GoogleMap mMap;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;

    //play services
    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    private static final int PLAY_SERVICE_RES_REQUEST = 7001;

    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;


    private static int UPDATE_INTERVAL = 5000;
    private static int FATEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    DatabaseReference databaseDriver;
    GeoFire geoFire;

    Marker mCurrent;

    MaterialAnimatedSwitch location_switch;
    SupportMapFragment mapFragment;

    //car animation
    private List<LatLng> polyLineList;
    private Marker carMarker;
    private float v;
    private double lat;
    private double lng;
    private Handler handler;
    private LatLng startPosition, endPosition, currentPosition;
    private int index, next;
    private Button btnGo;
    private PlaceAutocompleteFragment places;
    private EditText edtPlace;
    private String destination;
    private PolylineOptions polylineOptions, blackPolylineOptions;
    private Polyline blackPolyline;
    private Polyline greyPolyline;

    PlacesClient placesClient;
    List<com.google.android.libraries.places.api.model.Place.Field> placeFields = Arrays.asList(com.google.android.libraries.places.api.model.Place.Field.ID,
            com.google.android.libraries.places.api.model.Place.Field.NAME,
            com.google.android.libraries.places.api.model.Place.Field.ADDRESS);

    AutocompleteSupportFragment place_fragment;

    private IGoogleAPI mService;

    //presense system;
    DatabaseReference onlineRef, currentUserRef;
    int AUTOCOMPLETE_REQUEST_CODE = 1;
    private int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

//save image avatar
    FirebaseStorage firebaseStorage ;
    StorageReference storageReference ;
    private static int RESULT_LOAD_IMAGE = 1;

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
        setContentView(R.layout.activity_driver_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);


        View navigationHeaderView =  navigationView.getHeaderView(0);
        TextView tvName = navigationHeaderView.findViewById(R.id.tvName);
        TextView tvStars = navigationHeaderView.findViewById(R.id.tv_stars);
        ImageView imageView = navigationHeaderView.findViewById(R.id.img_avatar);
        tvStars.setText(Common.currentUser.getRates());
        tvName.setText(Common.currentUser.getUsername());

        if(Common.currentUser.getAvatarUrl() != null
            && !TextUtils.isEmpty(Common.currentUser.getAvatarUrl())){
            Picasso.with(this)
                    .load(Common.currentUser.getAvatarUrl())
                    .into(imageView);

        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //presense system
        onlineRef = FirebaseDatabase.getInstance().getReference().child(".info/connected");
        currentUserRef = FirebaseDatabase.getInstance().getReference(Common.driver_tbl)
                .child(Common.currentUser.getCarType())
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        onlineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // remove value from driver table db when driver offline

                currentUserRef.onDisconnect().removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //init view
        location_switch = findViewById(R.id.location_switch);
        location_switch.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(boolean b) {
                if(b){

                    FirebaseDatabase.getInstance().goOnline(); // set connected when switchto on
                    //startLocationUpdate();
                    databaseDriver = FirebaseDatabase.getInstance().getReference(Common.driver_tbl).child(Common.currentUser.getCarType());
                    geoFire = new GeoFire(databaseDriver);
                    displayLocation();
                    Toast.makeText(DriverHome.this, "You are online", Toast.LENGTH_SHORT).show();

                }else{

                    FirebaseDatabase.getInstance().goOffline();
                    stopLocationUpdates();
                    mCurrent.remove();
                    mMap.clear();
                    if(handler != null) handler.removeCallbacks(drawPathRunnable);
                    Toast.makeText(DriverHome.this, "You offline", Toast.LENGTH_SHORT).show();
                }
            }
        });

        polyLineList = new ArrayList<>();
        // initPlaces();
        setupPlaceAutocomplete();
//
        //Geo fire

        setUpLocation();
        mService = Common.getGoogleAPI();
        updateFirebaseToken();

    }

    @Override
    protected void onStop() {
        FirebaseDatabase.getInstance().goOffline();
        stopLocationUpdates();
        mCurrent.remove();
        mMap.clear();
        if(handler != null) handler.removeCallbacks(drawPathRunnable);

        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.d("place", "Place: " + place.getName());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.


            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }
    private void updateFirebaseToken() {

        FirebaseDatabase db=FirebaseDatabase.getInstance();
        DatabaseReference tokens=db.getReference(Common.token_tbl);

        Token token=new Token(FirebaseInstanceId.getInstance().getToken());
        tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);

    }

    private void initPlaces() {
        Places.initialize(this, getString(R.string.places_api_key));
        placesClient = Places.createClient(this);
    }

    //search location
    private void setupPlaceAutocomplete() {
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(),  getString(R.string.places_api_key));
        }

        place_fragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        if(place_fragment != null) {
            place_fragment.setPlaceFields(placeFields);

            place_fragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull com.google.android.libraries.places.api.model.Place place) {
                    destination = place.getName().toString();
                    destination = destination.replace(" ", "+");
                    getDirection();
                    Toast.makeText(DriverHome.this, "" + place.getName(), Toast.LENGTH_LONG).show();

                    Log.d("DDD", "" + place.getName());
                }

                @Override
                public void onError(@NonNull Status status) {

                }
            });
        }
    }

    private void getDirection() {

        currentPosition = new LatLng(Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude());
        String requestApi = null;

        try{

            requestApi="https://maps.googleapis.com/maps/api/directions/json?mode=driving&" +
                    "transit_routing_preference=less_driving&origin="+currentPosition.latitude+","+currentPosition.longitude+"&" +
                    "destination="+ destination +"&key=" + getResources().getString(R.string.google_direction_api);


            Log.d("gg api", requestApi);
            mService.getPath(requestApi).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        JSONArray jsonArray = jsonObject.getJSONArray("routes");

                        for(int i = 0; i < jsonArray.length(); i++){
                            JSONObject route =  jsonArray.getJSONObject(i);
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

                        carMarker= mMap.addMarker(new MarkerOptions().position(currentPosition).flat(true)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));

                        handler= new Handler();
                        index = -1;
                        next = 1;
                        handler.postDelayed(drawPathRunnable, 3000);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }


                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(DriverHome.this, "" + t.getMessage(), Toast.LENGTH_LONG).show();
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case MY_PERMISSION_REQUEST_CODE:
                if(grantResults.length >  0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    if(checkPlayServices()){
//
//                        builGoogleApiClient();
//                        createLocationRequest();
                    if (location_switch.isChecked()) {
                        displayLocation();
                    }

                //}
                }
        }
    }

    private void stopLocationUpdates() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }

        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, (com.google.android.gms.location.LocationListener) this);

    }


    private void displayLocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }

        Common.mLastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

                if(Common.mLastLocation != null){

                    if(location_switch.isChecked()){
                        final double latitude = Common.mLastLocation.getLatitude();
                        final double longtitude = Common.mLastLocation.getLongitude();

                        //update firebase
                        geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                new GeoLocation(latitude, longtitude), new GeoFire.CompletionListener() {
                                    @Override
                                    public void onComplete(String key, DatabaseError error) {
                                        // add marker
                                        if(mCurrent != null){
                                            mCurrent.remove();
                                        }
                                        mCurrent = mMap.addMarker(new MarkerOptions()

                                                .position(new LatLng(latitude, longtitude))
                                                .title("you"));

                                        //movew camera to this position

                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longtitude), 15.0f));



                                    }
                                });
                    }
                }else{
                    Log.d("ERROR", "cannot get your location");
                }

    }

    private void setUpLocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            //request runtime permission
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);
        }else{
            if(checkPlayServices()){

                builGoogleApiClient();
                createLocationRequest();
                if(location_switch.isChecked()){
                    databaseDriver = FirebaseDatabase.getInstance().getReference(Common.driver_tbl).child(Common.currentUser.getCarType());
                    geoFire = new GeoFire(databaseDriver);
                    displayLocation();
                }

            }

//            buildLocationRequest();
//            buildLocationCallback();
//            if(location_switch.isChecked())
//                displayLocation();

        }

    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {

                for(Location location: locationResult.getLocations()) {
                    Common.mLastLocation = location;
                }
                displayLocation();
            }
        };

    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FATEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(DISPLACEMENT);

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

    private void startLocationUpdate(){

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, (com.google.android.gms.location.LocationListener) this);


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
        getMenuInflater().inflate(R.menu.driver_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_change_password) {
            // Handle the camera action
//            Intent intent = new Intent(DriverHome.this, ChangePassword.class);
//            startActivity(intent);

            changePassword();
        } else if (id == R.id.nav_signout) {
            signOut();
        } else if (id == R.id.nav_update_info) {
//            Intent intent = new Intent(DriverHome.this, EditInforUser.class);
//            startActivity(intent);

            showUpdateInfo();

        } else if (id == R.id.nav_change_vehicle) {

            showDialogUpdateCarType();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void changePassword() {
        AlertDialog.Builder alertdialog = new AlertDialog.Builder(DriverHome.this);
        alertdialog.setTitle("Change password");

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View update_layout = layoutInflater.inflate(R.layout.layout_change_password, null);
        EditText edtPassword = update_layout.findViewById(R.id.edt_password);
        EditText edtNewPassword = update_layout.findViewById(R.id.edt_new_password);
        EditText edtRepeatPassword = update_layout.findViewById(R.id.edt_repeat_password);

        alertdialog.setView(update_layout);

        alertdialog.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(edtNewPassword.getText().toString().equals(edtRepeatPassword.getText().toString())){
                    String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

                    AuthCredential credential = EmailAuthProvider.getCredential(email, edtPassword.getText().toString());
                    FirebaseAuth.getInstance().getCurrentUser()
                            .reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        FirebaseAuth.getInstance().getCurrentUser()
                                                .updatePassword(edtRepeatPassword.getText().toString())
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            Map<String, Object> password =  new HashMap<>();

                                                            password.put("password", edtRepeatPassword.getText().toString());

                                                            DatabaseReference driverInfomation = FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl);

                                                            driverInfomation.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                                    .updateChildren(password)
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if(task.isSuccessful()){
                                                                                Toast.makeText(DriverHome.this, "Password was change", Toast.LENGTH_SHORT).show();

                                                                            }else{
                                                                                Toast.makeText(DriverHome.this, "Password was change but not update to Driver Infomation", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                });
                                    }
                                }
                            });

                }else{
                    Toast.makeText(DriverHome.this, "password does not match", Toast.LENGTH_LONG).show();
                }

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


    private void showUpdateInfo() {
        AlertDialog.Builder alertdialog = new AlertDialog.Builder(DriverHome.this);
        alertdialog.setTitle("Update your information");

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View update_layout = layoutInflater.inflate(R.layout.layout_update_info, null);
        EditText edtName = update_layout.findViewById(R.id.edt_name);
        EditText edtPhone = update_layout.findViewById(R.id.edt_phone);
        ImageView imgAvatar = update_layout.findViewById(R.id.img_avatar);

        FirebaseDatabase  database = FirebaseDatabase.getInstance();
        DatabaseReference mDatabaseRef = database.getReference();

        alertdialog.setView(update_layout);
        imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });
        alertdialog.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                mDatabaseRef.child(Common.user_driver_tbl).
                        child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        dataSnapshot.getRef().child("username").setValue(edtName.getText().toString());
                        dataSnapshot.getRef().child("phone").setValue(edtPhone.getText().toString());

                        Toast.makeText(DriverHome.this, "Update infor success", Toast.LENGTH_SHORT).show();

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

    private void chooseImage() {
//        Intent intent = new Intent();
//        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(Intent.createChooser(intent, "Select picture"), Common.PICK_IMAGE_REQUEST);

        Intent i = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//                super.onActivityResult(requestCode, resultCode, data);
//                if(requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK
//                        && data != null && data.getData() != null){
//                    Uri uri = data.getData();
//
//                    if(uri != null)
//                    {
//                        ProgressDialog mDialog = new ProgressDialog(this);
//                        mDialog.setMessage("Uploading...");
//                        mDialog.show();
//
//                        String imageName = UUID.randomUUID().toString();
//                        StorageReference imageFolder = storageReference.child("images/"+imageName);
//
//
//                        imageFolder.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                            @Override
//                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                                mDialog.dismiss();
//                                imageFolder.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<Uri> task) {
//
//                                        Map<String, Object> avatarUpdate = new HashMap<>();
//                                        avatarUpdate.put("avatarUrl", uri.toString());
//
//                                        DatabaseReference driverInfo = FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl);
//                                        driverInfo.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                                                .updateChildren(avatarUpdate)
//                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                    @Override
//                                                    public void onComplete(@NonNull Task<Void> task) {
//                                                        if(task.isSuccessful()){
//                                                            Toast.makeText(DriverHome.this, "Uploaded", Toast.LENGTH_LONG).show();
//                                                        }
//                                                        else
//                                                            Toast.makeText(DriverHome.this, "Uploaded fail", Toast.LENGTH_LONG).show();
//                                                    }
//                                                });
//
//
//                                    }
//                                });
//
//                            }
//                        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
//                            @Override
//                            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
//                                double progeress = (100.0* taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
//                                mDialog.setMessage("Upload" + progeress + "%");
//                            }
//                        });
//                    }
//                }
//    }


    private void showDialogUpdateCarType() {
        AlertDialog.Builder alertdialog = new AlertDialog.Builder(DriverHome.this);
        alertdialog.setTitle("Choose vehicle type");

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View update_layout = layoutInflater.inflate(R.layout.layout_vehicle, null);

        RadioButton car = update_layout.findViewById(R.id.rbtn_car);
        RadioButton motor = update_layout.findViewById(R.id.rbtn_motor);
        RadioButton bike = update_layout.findViewById(R.id.rbtn_bike);
//
//
        if(Common.currentUser.getCarType().equals("Car"))
            car.setChecked(true);
        else if(Common.currentUser.getCarType().equals("Motor bike"))
            motor.setChecked(true);
        else if(Common.currentUser.getCarType().equals("Bike"))
            bike.setChecked(true);

        alertdialog.setView(update_layout);
        alertdialog.setPositiveButton("Choose", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //dialog.dismiss();

                Map<String, Object> update = new HashMap<>();
                if(car.isChecked()) update.put("carType", car.getText().toString());
                else if(motor.isChecked()) update.put("carType", motor.getText().toString());
                else if(bike.isChecked()) update.put("carType", bike.getText().toString());

                DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl);
                mDatabaseRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .updateChildren(update)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful())
                                    Toast.makeText(DriverHome.this, "Vehicle type updated", Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(DriverHome.this, "Vehicle type updated fail", Toast.LENGTH_SHORT).show();
                            }
                        });


                mDatabaseRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Common.currentUser = dataSnapshot.getValue(User.class);
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

    //logout
    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(DriverHome.this, LoginUser.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
       // startLocationUpdate();
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
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        //update location

    }


}
