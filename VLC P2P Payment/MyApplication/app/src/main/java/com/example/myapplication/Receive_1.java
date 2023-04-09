package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class Receive_1 extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.receive_1);

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

        Button confirm_btn = (Button) findViewById(R.id.receive_1_confirm_btn);
        EditText amount_input = (EditText) findViewById(R.id.receive_1_amount_input);

        confirm_btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent switchIntent = new Intent(context, Receive_2.class);
                switchIntent.putExtra("amount", amount_input.getText().toString());
                startActivity(switchIntent);
                finish();
            }
        });


    }
}