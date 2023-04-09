package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class Login extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        this.addListener();

        //add previous bar in mainbox
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    //function for previous button
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        this.finish();
        return true;
    }

    public void addListener() {
        final Context context = this;

        Button login_btn = (Button) findViewById(R.id.login_login_btn);

        login_btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                EditText email_input = (EditText) findViewById(R.id.login_email_input);
                EditText password_input = (EditText) findViewById(R.id.login_password_input);
                if (email_input.getText().toString().length() == 0 || password_input.getText().toString().length() == 0)
                {
                    Log.d("Login", "Please input email and password");
                }
                else{
                    try {
                        (new APIRequest()).login(context, email_input.getText().toString(), password_input.getText().toString(), new ServerCallback() {
                            @Override
                            public void onSuccess(JSONObject response){
                                if (response.has("error")) {

                                    setResult(-2, null);

                                    try {
                                        Log.e("Login", "Failed: " + response.getString("error").toString());
                                        Toast.makeText(getApplicationContext(),  response.getString("error"),Toast.LENGTH_LONG).show();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                } else{
                                    try{
                                        Intent data = new Intent();
                                        data.putExtra("access_token", response.getString("access_token"));
                                        setResult(RESULT_OK, data);

                                        ((Activity) context).finish();
                                    }
                                    catch(Exception e){
                                        e.printStackTrace();
                                        setResult(-2,null);
                                    }
                                }
                            }
                        });



                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            }
        });

    }
}