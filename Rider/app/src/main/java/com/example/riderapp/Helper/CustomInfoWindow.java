package com.example.riderapp.Helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.riderapp.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindow implements GoogleMap.InfoWindowAdapter {

    View myView;

    public CustomInfoWindow(Context context){
        myView = LayoutInflater.from(context).inflate(R.layout.custom_rider_info_window, null);
    }

    @Override
    public View getInfoWindow(Marker marker) {

        TextView txtPickupTitle = myView.findViewById(R.id.txt_pickup_info);
        txtPickupTitle.setText(marker.getTitle());

        TextView txtPickupSnippet = myView.findViewById(R.id.txt_pickup_snippet);
        txtPickupSnippet.setText(marker.getSnippet());
        return myView;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
