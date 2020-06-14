package com.example.riderapp.Remote;

import com.example.riderapp.Model.FCMResponse;
import com.example.riderapp.Model.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAC9QiJWo:APA91bEvRXIj8udbHebJIl1tcZJZNazb835kWMT5wODH3MiSXb4ux6aXVuIVZe2WJpwENMbplNbSkMBu2jPCz8MZOHX60ZrjdRZi7ao1WGpt-5wI6E55yf1cTQIFAX8vZY4B8rc9wx1A"
    })


    @POST("fcm/send")
    Call<FCMResponse> sendMessage(@Body Sender body);
}
