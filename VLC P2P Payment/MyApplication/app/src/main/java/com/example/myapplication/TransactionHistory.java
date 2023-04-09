package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class TransactionHistory extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transaction_history);
        this.addListener();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        try {
            (new APIRequest()).getToken(getApplicationContext(), new ServerCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        if (response.has("error")) {
                            Log.e("API Call Error", response.getString("error"));
                        } else {
                            String token = response.getString("access_token");
                            (new APIRequest()).getUID(getApplicationContext(), token, new ServerCallback() {
                                @Override
                                public void onSuccess(JSONObject response) {
                                    try {
                                        if (response.has("error")) {
                                            Log.e("API Call Error", response.getString("error"));
                                        } else {
                                            String uid = response.getString("uid");
                                            (new APIRequest()).getPaymentHistory(getApplicationContext(), token, new ServerCallback() {
                                                @Override
                                                public void onSuccess(JSONObject response) {
                                                    try {
                                                        if (response.has("error")) {
                                                            Log.e("API Call Error", response.getString("error"));
                                                        } else {
                                                            JSONArray result = response.getJSONArray("result");
                                                            ArrayList<JSONObject> payment_history = new ArrayList<>();

                                                            for(int i = 0; i < result.length(); i++) {
                                                                payment_history.add(result.getJSONObject(i));
                                                            }

                                                            ListView history_list = (ListView) findViewById(R.id.transaction_history_list);
                                                            CustomAdaptor customAdapter = new CustomAdaptor(getApplicationContext(),uid, payment_history);
                                                            history_list.setAdapter(customAdapter);

                                                        }
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });


                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //function for previous button
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        this.finish();
        return true;
    }

    public static String formatDecimal(double number) {
        double epsilon = 0.004f; // 4 tenths of a cent
        if (Math.abs(Math.round(number) - number) < epsilon) {
            return "$"+String.format("%,.2f", number); // sdb
        } else {
            return "$"+String.format("%,.2f", number); // dj_segfault
        }
    }



    private void addListener() {
        final Context context = this;

    }

    @Override
    protected void onResume() {
        super.onResume();
    }


}