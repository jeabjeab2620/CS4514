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

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;


public class Register_1 extends AppCompatActivity  {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_1);

        this.add_listener();

        //add previous bar in mainbox
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        this.finish();
        return true;
    }

    public void add_listener(){
        final Context context = this;
        Button next_btn = (Button) findViewById(R.id.register_1_next_btn);

        ActivityResultLauncher<Intent> launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result){
                        if(result.getResultCode() == -2){
                            ((Activity) context).finish();
                        }
                        else if (result.getResultCode() == RESULT_OK){

                            Intent switchIntent = new Intent(context, Main.class);
                            switchIntent.putExtra("access_token",result.getData().getStringExtra("access_token"));

                            startActivity(switchIntent);

                        }
                    }
                }
        );

        next_btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){

                EditText email_input = (EditText) findViewById(R.id.register_1_email_input);
                EditText password_input = (EditText) findViewById(R.id.register_1_password_input);
                EditText password_confirm_input = (EditText) findViewById(R.id.register_1_password_confirm_input);
                EditText name_input = (EditText) findViewById(R.id.register_1_name_input);
                EditText hkid_input = (EditText) findViewById(R.id.register_1_hkid_input);

                if (email_input.getText().toString().length() == 0 ||
                        password_input.getText().toString().length() == 0 ||
                        password_confirm_input.getText().toString().length() == 0 ||
                        name_input.getText().toString().length() == 0 ||
                        hkid_input.getText().toString().length() == 0
                ){
                    Log.d("Register", "Please fill all information.");
                }
                else if( !password_input.getText().toString().equals(password_confirm_input.getText().toString()) ){
                    Log.d("Register", "Password mismatch");
                }
                else{
                    Intent switchIntent = new Intent(context, Register_2.class);

                    switchIntent.putExtra("email",email_input.getText().toString());
                    switchIntent.putExtra("password",password_input.getText().toString());
                    switchIntent.putExtra("name",name_input.getText().toString());
                    switchIntent.putExtra("hkid",hkid_input.getText().toString());

                    launcher.launch(switchIntent);
                }

            }
        });


    }

}