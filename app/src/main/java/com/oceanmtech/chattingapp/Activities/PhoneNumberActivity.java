package com.oceanmtech.chattingapp.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.oceanmtech.chattingapp.Utils.Internet;
import com.oceanmtech.chattingapp.databinding.ActivityPhoneNumberBinding;

public class PhoneNumberActivity extends AppCompatActivity {

    ActivityPhoneNumberBinding binding;
    PhoneNumberActivity mContext = PhoneNumberActivity.this;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhoneNumberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        if (Internet.isInternetConnected(mContext)) {
            if (auth.getCurrentUser() != null) {
                Intent intent = new Intent(mContext, MainActivity.class);
                startActivity(intent);
                finish();
            }
        } else {
            Toast.makeText(mContext, "No Internet Connection", Toast.LENGTH_LONG).show();
        }
        getSupportActionBar().hide();
        binding.etPhone.requestFocus();

        binding.btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Internet.isInternetConnected(mContext)) {
                    if (binding.etPhone.getText().toString().trim().isEmpty() || binding.etPhone.getText().toString().trim().length() < 13) {
                        Toast.makeText(mContext, "Please enter valid phone number", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(PhoneNumberActivity.this, OTPActivity.class);
                        intent.putExtra("phoneNumber", binding.etPhone.getText().toString());
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(mContext, "No Internet Connection", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}