package com.example.amazoncloneapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.io.IOException;
import java.util.List;

public class PlaceOrderActivity extends AppCompatActivity implements PaymentResultListener {

    EditText shipName, shipPhone, shipAddress, shipCity;
    AppCompatButton confirmOrder;
    FirebaseAuth auth;
    Intent intent;
    String totalAmount;
    TextView cartpricetotal;
    Toolbar cartToolbar;
    int amount;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order);

        shipName = findViewById(R.id.shipName);
        shipPhone = findViewById(R.id.shipPhone);
        shipAddress = findViewById(R.id.shipAddress);
        shipCity = findViewById(R.id.shipCity);
        confirmOrder = findViewById(R.id.confirmOrder);
        cartpricetotal = findViewById(R.id.cartpricetotal);
        cartToolbar = findViewById(R.id.cart_toolbar);

        auth = FirebaseAuth.getInstance();

        cartToolbar.setBackgroundResource(R.drawable.bg_color);
        confirmOrder.setBackgroundResource(R.drawable.bg_color);

        intent = getIntent();
        totalAmount = intent.getStringExtra("totalAmount");

        cartpricetotal.setText(totalAmount);

        String sAmount = "1000";


        amount = Math.round(Float.parseFloat(sAmount) * 100);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                getCurrentLocation();
            }
        } else {
            getCurrentLocation();
        }
        confirmOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if manual address fields are empty
                if (TextUtils.isEmpty(shipAddress.getText().toString()) ||
                        TextUtils.isEmpty(shipCity.getText().toString())) {
                    // If any of the manual address fields is empty, show a toast
                    Toast.makeText(PlaceOrderActivity.this, "Please enter all address fields",
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Proceed with payment or confirmation
                    check();
                }
            }
        });
    }

    private void check() {

        if (TextUtils.isEmpty(shipName.getText().toString())) {
            shipName.setError("Enter name");
            Toast.makeText(this, "Please enter your full name", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(shipPhone.getText().toString())) {
            shipPhone.setError("Enter phone no.");
            Toast.makeText(this, "Please enter your phone no.", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(shipAddress.getText().toString())) {
            shipAddress.setError("Enter address");
            Toast.makeText(this, "Please enter your address", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(shipCity.getText().toString())) {
            shipCity.setError("Enter phone no.");
            Toast.makeText(this, "Please enter your city", Toast.LENGTH_SHORT).show();
        } else {
            paymentFunc();
        }

    }

    private void getCurrentLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    setAddressFromLocation(location);
                }
            }
        });
    }

    private void setAddressFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),
                    location.getLongitude(), 1);
            if (!addresses.isEmpty()) {
                Address address = addresses.get(0);
                // Set the retrieved address in the EditText fields
                shipAddress.setText(address.getAddressLine(0));
                shipCity.setText(address.getLocality());
            } else {
                // Set default location if no address is retrieved
                shipAddress.setText("Default Address");
                shipCity.setText("Default City");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void paymentFunc(){

        Checkout checkout= new Checkout();

        //set key id
        checkout.setKeyID("rzp_test_8Oy1HdT4k97kHV");

        //set image
        checkout.setImage(R.drawable.rzp_logo);

        //initialize JSON object
        JSONObject object= new JSONObject();

        try {
            //put name
            object.put("name","Android User");

            //put description
            object.put("description","Test Payment");

            //put currency unit
            object.put("currency","INR");

            //put amount
            object.put("amount",amount);

            //put mobile number
            object.put("prefill.contact","9876543210");

            //put email
            object.put("prefill.email","androiduser@rzp.com");

            //open razorpay checkout activity
            checkout.open(PlaceOrderActivity.this,object);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void confirmOrderFunc() {
        final String saveCurrentDate, saveCurrentTime;

        Calendar calendar=Calendar.getInstance();
        SimpleDateFormat currentDate= new SimpleDateFormat("dd/MM/yyyy");
        saveCurrentDate=currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime= new SimpleDateFormat("HH:mm:ss a");
        saveCurrentTime=currentTime.format(calendar.getTime());

        final DatabaseReference ordersRef= FirebaseDatabase.getInstance().getReference()
                .child("Orders").child(auth.getCurrentUser().getUid()).child("History")
                .child(saveCurrentDate.replaceAll("/","-")+" "+saveCurrentTime);

        HashMap<String, Object> ordersMap= new HashMap<>();
        ordersMap.put("totalAmount",totalAmount);
        ordersMap.put("name",shipName.getText().toString());
        ordersMap.put("phone",shipPhone.getText().toString());
        ordersMap.put("address",shipAddress.getText().toString());
        ordersMap.put("city",shipCity.getText().toString());
        ordersMap.put("date",saveCurrentDate);
        ordersMap.put("time",saveCurrentTime);

        ordersRef.updateChildren(ordersMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                //empty user's cart after confirming order
                if(task.isSuccessful()) {
                    FirebaseDatabase.getInstance().getReference().child("Cart List")
                            .child("User View").child(auth.getCurrentUser().getUid())
                            .removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(PlaceOrderActivity.this, "Your order has been placed successfully", Toast.LENGTH_SHORT).show();
                                        Intent intentcart= new Intent(PlaceOrderActivity.this, HomeActivity.class);
                                        intentcart.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intentcart);
                                        finish();
                                    }
                                }
                            });
                }
            }
        });

    }


    @Override
    public void onPaymentSuccess(String s) {
        confirmOrderFunc();
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setTitle("Payment ID");
        builder.setMessage(s);
        builder.show();
    }

    @Override
    public void onPaymentError(int i, String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}