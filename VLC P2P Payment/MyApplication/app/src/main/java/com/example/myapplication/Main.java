package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

public class Main extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        String access_token = getIntent().getStringExtra("access_token");
        Log.d("Access Token", access_token);
        this.addListener();
        this.updateBalance();

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

        Button receive_btn = (Button) findViewById(R.id.main_receive_btn);
        Button send_btn = (Button) findViewById(R.id.main_send_btn);
        TextView transaction_history_text = (TextView) findViewById(R.id.main_view_transaction_btn);

        ActivityResultLauncher<Intent> launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result){
                        if (result.getResultCode() == RESULT_OK){
                            String access_token = "";
                        }
                    }
                }
        );
        receive_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent switchIntent = new Intent(context, Receive_1.class);
                launcher.launch(switchIntent);
            }
        });

        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent switchIntent = new Intent(context, Send_1.class);
                launcher.launch(switchIntent);
            }
        });

        transaction_history_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent switchIntent = new Intent(context, TransactionHistory.class);
                launcher.launch(switchIntent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateBalance();
    }

    private void updateBalance() {

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    (new APIRequest()).getToken(getApplicationContext(), new ServerCallback() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            try {
                                if (response.has("error")) {
                                    Log.e("API Call Error", response.getString("error"));
                                    Toast.makeText(getApplicationContext(),  response.getString("error"),Toast.LENGTH_LONG).show();

                                } else {
                                    String token = response.getString("access_token");

                                    (new APIRequest()).getBalance(getApplicationContext(), token,new ServerCallback() {
                                        @Override
                                        public void onSuccess(JSONObject response) {
                                            try{
                                                if(response.has("error")) {
                                                    Log.e("API Call Error", response.getString("error"));
                                                    //Toast.makeText(getApplicationContext(),  response.getString("error"),Toast.LENGTH_LONG).show();
                                                } else{
                                                    TextView balance = (TextView) findViewById(R.id.main_amount_text);
                                                    balance.setText(Main.formatDecimal(response.getDouble("balance")));

                                                    cancel();

                                                }
                                            } catch (Exception e){
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
        }, 0, 10000);


    }

}