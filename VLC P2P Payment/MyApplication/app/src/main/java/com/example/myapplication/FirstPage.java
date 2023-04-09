package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class FirstPage extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first_page);

        this.addListener();

    }
    public void addListener(){
        final Context context = this;

        Button login_btn = (Button) findViewById(R.id.first_page_login_btn);
        Button register_btn = (Button) findViewById(R.id.first_page_register_btn);


        ActivityResultLauncher<Intent> launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result){
                        if (result.getResultCode() == RESULT_OK){
                            Intent switchIntent = new Intent(context, Main.class);
                            switchIntent.putExtra("access_token",result.getData().getStringExtra("access_token"));
                            startActivity(switchIntent);
                            ((Activity)context).finish();
                        }
                    }
                }
        );
        login_btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent switchIntent = new Intent(context, Login.class);
                launcher.launch(switchIntent);
            }
        });

        register_btn.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v){
                Intent switchIntent = new Intent(context, Register_1.class);
                startActivity(switchIntent);
            }
        });
    }
}