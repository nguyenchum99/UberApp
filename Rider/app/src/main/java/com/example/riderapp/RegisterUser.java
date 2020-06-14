package com.example.riderapp;

import android.content.Intent;
import android.os.Bundle;

import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.riderapp.Common.Common;
import com.example.riderapp.Model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterUser extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference users;

    private EditText edtEmail;
    private EditText edtUsername;
    private EditText edtPassword;
    private EditText edtPhone;
    private Button btnRegister;
    private TextView tvLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        users = database.getReference(Common.user_rider_btl);

        mapping();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isEmpty(edtEmail)) {
                    edtEmail.setError("Email is required");
                    return;
                }
                //check
                if(isEmail(edtEmail) == false) {
                    edtEmail.setError("Enter valid email");
                    return;
                }

                if(isEmpty(edtUsername)) {
                    edtUsername.setError("Name is required");
                    return;
                }

                if(isEmpty(edtPassword)) {
                    edtPassword.setError("Password is required");
                    return;
                }

                if(edtPassword.getText().length() < 6){
                    edtPassword.setError("Password must have 6 characters or more");
                    return;
                }

                if(isEmpty(edtPhone)) {
                    edtPhone.setError("Phone is required");
                    return;

                }

                //register new user
                auth.createUserWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                //save user to database
                                User user = new User();
                                user.setEmail(edtEmail.getText().toString());
                                user.setUsername(edtUsername.getText().toString());
                                user.setPassword(edtPassword.getText().toString());
                                user.setPhone(edtPhone.getText().toString());


                                //use email to key
                                users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Intent intent = new Intent(RegisterUser.this, LoginUser.class);
                                                startActivity(intent);
                                                Toast.makeText(RegisterUser.this,"Register Success", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(RegisterUser.this,"Register fail", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegisterUser.this,"Register fail", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterUser.this, LoginUser.class);
                startActivity(intent);
            }
        });
    }

    private  void mapping(){
        edtEmail = (EditText) findViewById(R.id.edt_email);
        edtUsername = (EditText) findViewById(R.id.edt_username);
        edtPassword = (EditText) findViewById(R.id.edt_password);
        edtPhone = (EditText) findViewById(R.id.edt_phone);

        btnRegister = (Button) findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);
    }

    private boolean isEmail(EditText text){
        CharSequence email = text.getText().toString();
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isEmpty(EditText text){
        CharSequence charSequence = text.getText().toString();
        return TextUtils.isEmpty(charSequence);
    }

}