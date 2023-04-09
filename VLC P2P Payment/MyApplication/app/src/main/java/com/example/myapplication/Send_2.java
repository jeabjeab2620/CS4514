package com.example.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;


public class Send_2 extends AppCompatActivity {
    private String mTID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.send_2);

        mTID = getIntent().getStringExtra("tid");
        try{
            (new APIRequest()).getTransactionRecord(getApplicationContext(), mTID, new ServerCallback(){

                @Override
                public void onSuccess(JSONObject response) {
                    try{
                        if (response.has("error")){
                            Toast.makeText(getApplicationContext(), response.getString("error"), Toast.LENGTH_LONG).show();
                            cancelTransaction();
                            finish();
                        }
                        else{


                            TextView amountTextView = findViewById(R.id.send_2_amount_text);
                            TextView receiverNameTextView = findViewById(R.id.send_2_name_text);

                            receiverNameTextView.setText(response.getString("receiver_name"));
                            amountTextView.setText(response.getString("amount"));

                        }

                    } catch(Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }catch(Exception e){
            e.printStackTrace();
        }



        this.addListener();


    }

    private void cancelTransaction() {
            try{
                (new APIRequest()).cancelPayment(getApplicationContext(), mTID, new ServerCallback(){

                    @Override
                    public void onSuccess(JSONObject response) {
                        try{
                            if (response.has("error")){
                                Toast.makeText(getApplicationContext(), response.getString("error"),Toast.LENGTH_LONG).show();
                            }
                            else{
                                Toast.makeText(getApplicationContext(), "Cancel Success", Toast.LENGTH_SHORT).show();
                            }

                        } catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                });
            }catch(Exception e){
                e.printStackTrace();
            }

        }

    public void addListener() {
        final Context context = this;
        Button rejectButton = (Button) findViewById(R.id.send_2_reject_btn);
        Button confirmButton = (Button) findViewById(R.id.send_2_confirm_btn);

        rejectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                try {
                    (new APIRequest()).getToken(getApplicationContext(), new ServerCallback() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            try {
                                if (response.has("error")) {
                                    Log.e("API Call Error", response.getString("error"));
                                } else {
                                    String token = response.getString("access_token");

                                    (new APIRequest()).rejectPayment(getApplicationContext(), token, mTID, new ServerCallback() {
                                        @Override
                                        public void onSuccess(JSONObject response) {
                                            try{
                                                if(response.has("error")) {
                                                    Toast.makeText(getApplicationContext(), response.getString("error"), Toast.LENGTH_LONG).show();

                                                    Send_2.this.finish();
                                                } else{

                                                    Toast.makeText(getApplicationContext(), response.getString("result"), Toast.LENGTH_LONG).show();

                                                    Send_2.this.finish();
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
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                try {
                    (new APIRequest()).getToken(getApplicationContext(), new ServerCallback() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            try {
                                if (response.has("error")) {
                                    Log.e("API Call Error", response.getString("error"));
                                } else {
                                    String token = response.getString("access_token");

                                    (new APIRequest()).acceptPayment(getApplicationContext(), token, mTID, new ServerCallback() {
                                        @Override
                                        public void onSuccess(JSONObject response) {
                                            try{
                                                if(response.has("error")) {
                                                    Toast.makeText(getApplicationContext(), response.getString("error"), Toast.LENGTH_LONG).show();

                                                    Send_2.this.finish();
                                                } else{

                                                    Toast.makeText(getApplicationContext(), response.getString("result"), Toast.LENGTH_LONG).show();

                                                    Send_2.this.finish();
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
        });
    }

}




















