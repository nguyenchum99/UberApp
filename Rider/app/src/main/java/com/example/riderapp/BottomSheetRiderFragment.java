package com.example.riderapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.riderapp.Common.Common;
import com.example.riderapp.Remote.IGoogleAPI;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONArray;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BottomSheetRiderFragment extends BottomSheetDialogFragment {

    String mLocation, mDestination;
    IGoogleAPI mService;
    TextView tvMoney;
    TextView tvLocation;
    TextView tvDestination;

    boolean isTapOnMap;
    
    public static BottomSheetRiderFragment newInstanse(String  location, String destination, boolean isTapOnMap){
        BottomSheetRiderFragment f = new BottomSheetRiderFragment();
        Bundle args = new Bundle();
        args.putString("location", location);
        args.putString("destination", destination);
        args.putBoolean("isTapOnMap", isTapOnMap);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocation = getArguments().getString("location");
        mDestination = getArguments().getString("destination");
        isTapOnMap = getArguments().getBoolean("isTapOnMap");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.bottom_sheet_rider, container, false);
        tvLocation = view.findViewById(R.id.tv_location);
        tvDestination = view.findViewById(R.id.tv_destination);
        tvMoney = view.findViewById(R.id.tv_money);

        mService = Common.getGoogleAPI();
        getPrice(mLocation, mDestination);
        //set data
        if(!isTapOnMap){
            tvLocation.setText(mLocation);
            tvDestination.setText(mDestination);
        }

        return view;
    }

    private void getPrice(String mLocation, String mDestination) {
        String requestApi = null;

        try{
            requestApi="https://maps.googleapis.com/maps/api/directions/json?" +
                    "&origin="+ mLocation +"&" +
                    "destination="+ mDestination +
                    "&key=" + getResources().getString(R.string.google_direction_api);

            Log.d("sheet", requestApi);

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
                        String distanceDetail = distance.getString("text");
                        Double distanceValue = Double.parseDouble(distanceDetail.replaceAll("[^0-9\\\\.]+", ""));

                        JSONObject time = legsObject.getJSONObject("duration");
                        String time_text = time.getString("text");
                        Double time_value = Double.parseDouble(time_text.replaceAll("[^0-9\\\\.]+", ""));

//                        String calculator = String.format("%s + %s = $%.2s", distanceDetail, time_text,
//                                Common.getPrice(distanceValue, time_value));

//                        String total = String.format("%s + %s = %.2f", distanceDetail, time_text,
//                                Common.getPrice(distanceValue, time_value));

                        tvMoney.setText("" + Common.getPrice(distanceValue, time_value) + "$");

                        if(isTapOnMap){
                            String start_address = legsObject.getString("start_address");
                            String end_address = legsObject.getString("end_address");

                            tvLocation.setText(start_address);
                            tvDestination.setText(end_address);
                        }else{

                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }


                @Override
                public void onFailure(Call<String> call, Throwable t) {

                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}