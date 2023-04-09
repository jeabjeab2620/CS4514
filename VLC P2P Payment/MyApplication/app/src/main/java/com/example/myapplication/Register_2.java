package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;


public class Register_2 extends AppCompatActivity  {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_2);

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
        Button confirm_btn = (Button) findViewById(R.id.register_2_confirm_btn);


        confirm_btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Cryptography.generateRSAKeyPair();

                try {
                    PublicKey publicKey = Cryptography.getCertificate("key1").getPublicKey();
                    String publicKeyPEM = Cryptography.publicKeyToPem(publicKey);
                    String email = getIntent().getStringExtra("email");
                    String password = getIntent().getStringExtra("password");
                    String name = getIntent().getStringExtra("name");
                    String hkid = getIntent().getStringExtra("hkid");

                    // Send API Request
                    (new APIRequest()).register(context, email, password, name, hkid, publicKeyPEM, new ServerCallback() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            if (response.has("error")) {
                                try {
                                    Log.e("Register", "Failed:" + response.getString("error").toString());
                                    //end current Activity
                                    setResult(-2, null);
                                    Toast.makeText(getApplicationContext(),  response.getString("error"),Toast.LENGTH_LONG).show();


                                } catch (JSONException e) {
                                    setResult(-2, null);
                                    e.printStackTrace();
                                }
                            } else
                            {
                                try {
                                    Cryptography.storeCertificate("certificate",response.getString("certificate"));
                                    Cryptography.storeCertificate("authority_certificate",response.getString("authority_certificate"));
                                    Cryptography.storeRefreshToken(context, "refresh_token", response.getString("refresh_token"));

                                    PublicKey clientPubKey = Cryptography.getCertificate("certificate").getPublicKey();
                                    PublicKey authorityPubKey = Cryptography.getCertificate("authority_certificate").getPublicKey();

                                    Log.d("RSA", "Public Key: " + publicKeyPEM);
                                    Log.d("Authority", "Client Public Key: " + Cryptography.publicKeyToPem(clientPubKey));
                                    Log.d("Authority", "Authority Public Key: " + Cryptography.publicKeyToPem(authorityPubKey));

                                    if (Cryptography.verify_certificate_with_authority(Cryptography.getCertificate("certificate")))
                                    {
                                        Intent data = new Intent();
                                        data.putExtra("access_token", response.getString("access_token"));

                                        setResult(RESULT_OK, data);
                                    }
                                    else{
                                            setResult(-2, null);
                                    }

                                } catch (JSONException | KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
                                    setResult(-2, null);
                                    e.printStackTrace();
                                }
                            }
                            ((Activity) context).finish();
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),  e.getLocalizedMessage() ,Toast.LENGTH_LONG).show();
                }

                setResult(-2, null);
            }
        });

    }

}