package com.example.riderapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.riderapp.Common.Common;
import com.example.riderapp.Model.Rate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class RateActivity extends AppCompatActivity {

    Button btnSubmit;
    MaterialRatingBar materialRatingBar;
    EditText edtComment;

    FirebaseDatabase database;
    DatabaseReference rateDetail;
    DatabaseReference driverInfoRef;

    double ratingStar= 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);

        database = FirebaseDatabase.getInstance();
        rateDetail = database.getReference(Common.rate_detail_tbl);
        driverInfoRef = database.getReference(Common.user_driver_tbl);

        btnSubmit = findViewById(R.id.btn_submit);
        materialRatingBar = findViewById(R.id.ratingbar);
        edtComment = findViewById(R.id.edt_comment);

        materialRatingBar.setOnRatingChangeListener(new MaterialRatingBar.OnRatingChangeListener() {
            @Override
            public void onRatingChanged(MaterialRatingBar ratingBar, float rating) {
                ratingStar = rating;
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitRateDetail(Common.driverId);
                Intent intent = new Intent(RateActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });


    }

    private void submitRateDetail(String driverId) {
        Rate rate = new Rate();
        rate.setRates(String.valueOf(ratingStar));
        rate.setComments(edtComment.getText().toString());

        rateDetail.child(driverId)
                .push()
                .setValue(rate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        rateDetail.child(driverId)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        double avarageStart = 0.0;
                                        int count = 0;
                                        for(DataSnapshot postSnaphot: dataSnapshot.getChildren()){
                                            Rate ratel = postSnaphot.getValue(Rate.class);
                                            avarageStart+= Double.parseDouble(ratel.getRates());
                                            count++;
                                        }
                                        double finalAverage = avarageStart/count ;
                                        DecimalFormat df = new DecimalFormat("#.#");
                                        String valueUpdate = df.format(finalAverage);

                                        Map<String, Object> driverUpdateRate = new HashMap<>();
                                        driverUpdateRate.put("rates", valueUpdate);

                                        driverInfoRef.child(Common.driverId)
                                                .updateChildren(driverUpdateRate)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        Toast.makeText(RateActivity.this, "Thanks you for submit", Toast.LENGTH_LONG).show();
                                                        finish();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d("rate", "" + e.getMessage());

                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }
}
