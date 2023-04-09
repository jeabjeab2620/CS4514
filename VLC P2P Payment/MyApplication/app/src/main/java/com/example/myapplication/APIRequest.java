package com.example.myapplication;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.chromium.net.CronetEngine;
import org.chromium.net.UrlRequest;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class APIRequest {
    private String server = "http://192.168.128.81:8000";
    //private String server = "http://localhost:8000";


    public void register(Context context, String email, String password, String name, String hkid, String public_key, final ServerCallback callback) throws JSONException {
        RequestQueue queue = Volley.newRequestQueue(context);
        JSONObject object = new JSONObject();
        object.put("email", email);
        object.put("password", password);
        object.put("fullname", name);
        object.put("hkid", hkid);
        object.put("public_key",public_key);

        Log.d("API Call", object.toString());


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, this.server + "/user/register", object, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("API CALL", response.toString());
                        callback.onSuccess(response);

                    }

                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.e("API CALL", error.toString());
                    }
                });
        queue.add(jsonObjectRequest);


    }

    public void login(Context context, String email, String password, ServerCallback callback) throws JSONException {
        RequestQueue queue = Volley.newRequestQueue(context);
        JSONObject object = new JSONObject();
        object.put("email", email);
        object.put("password", password);

        Log.d("API Call", object.toString());


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, this.server + "/user/login", object, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("API CALL", response.toString());
                        callback.onSuccess(response);

                    }

                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("API CALL", error.toString());
                    }
                });
        queue.add(jsonObjectRequest);
    }

    public void getToken(Context context, ServerCallback callback) throws JSONException {
        String refresh_token = Cryptography.getRefreshToken(context, "refresh_token");
        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, this.server + "/user/get_token", null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("API CALL", response.toString());
                        callback.onSuccess(response);

                    }


                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("API CALL", error.toString());
                    }

                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer "+refresh_token);
                return headers;
            }
        };
        queue.add(jsonObjectRequest);
    }

    public void getUID(Context context, String access_token, ServerCallback callback) throws JSONException {
        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, this.server + "/user/get_uid", null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("API CALL", response.toString());
                        callback.onSuccess(response);
                    }

                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("API CALL", error.toString());
                    }

                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer "+ access_token);
                return headers;
            }
        };
        queue.add(jsonObjectRequest);
    }

    public void getBalance(Context context, String access_token, ServerCallback callback) throws JSONException {
        RequestQueue queue = Volley.newRequestQueue(context);



        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, this.server + "/wallet/get_balance", null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("API CALL", response.toString());
                        callback.onSuccess(response);

                    }


                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("API CALL", error.toString());
                    }

                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer "+access_token);
                return headers;
            }
        };
        queue.add(jsonObjectRequest);
    }

    public void getTransactionRecord(Context context, String tid, ServerCallback callback) throws JSONException {
        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, this.server + "/payment/get_payment_info/" + tid, null,  new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("API CALL", response.toString());
                        callback.onSuccess(response);

                    }

                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("API CALL", error.toString());
                    }
                });
        queue.add(jsonObjectRequest);
    }

    public void getTransactionStatus(Context context, String access_token, String tid, ServerCallback callback) throws JSONException {
        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, this.server + "/payment/get_status/" + tid, null,  new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("API CALL", response.toString());
                        callback.onSuccess(response);

                    }

                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("API CALL", error.toString());
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer "+access_token);
                return headers;
            }};
        queue.add(jsonObjectRequest);
    }

    public void requestPayment(Context context, String access_token, String senderUID, String receiverUID, String amount, ServerCallback callback) throws JSONException {
        RequestQueue queue = Volley.newRequestQueue(context);
        JSONObject object = new JSONObject();
        object.put("sender", senderUID);
        object.put("receiver", receiverUID);
        object.put("amount", Float.parseFloat(amount));

        Log.d("API Call", object.toString());


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, this.server + "/payment/receive", object, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("API CALL", response.toString());
                        callback.onSuccess(response);

                    }

                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("API CALL", error.toString());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + access_token);
                return headers;
            }
        };

        queue.add(jsonObjectRequest);
    }

    public void cancelPayment(Context context, String tid, ServerCallback callback) throws JSONException {
        RequestQueue queue = Volley.newRequestQueue(context);
        JSONObject object = new JSONObject();
        object.put("tid", tid);

        Log.d("API Call", object.toString());


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, this.server + "/payment/cancel", object, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("API CALL", response.toString());
                        callback.onSuccess(response);

                    }

                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("API CALL", error.toString());
                    }
                });
        queue.add(jsonObjectRequest);
    }

    public void rejectPayment(Context context,String access_token, String tid, ServerCallback callback) throws JSONException {
        RequestQueue queue = Volley.newRequestQueue(context);
        JSONObject object = new JSONObject();
        object.put("tid", tid);

        Log.d("API Call", object.toString());


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, this.server + "/payment/reject_payment", object, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("API CALL", response.toString());
                        callback.onSuccess(response);

                    }

                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("API CALL", error.toString());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer "+ access_token);
                return headers;
            }
        };
        queue.add(jsonObjectRequest);
    }

    public void acceptPayment(Context context,String access_token, String tid, ServerCallback callback) throws JSONException {
        RequestQueue queue = Volley.newRequestQueue(context);
        JSONObject object = new JSONObject();
        object.put("tid", tid);

        Log.d("API Call", object.toString());


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, this.server + "/payment/accept_payment", object, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("API CALL", response.toString());
                        callback.onSuccess(response);

                    }

                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("API CALL", error.toString());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer "+ access_token);
                return headers;
            }
        };
        queue.add(jsonObjectRequest);
    }

    public void getPaymentHistory(Context context, String access_token, ServerCallback callback) throws JSONException {
        RequestQueue queue = Volley.newRequestQueue(context);
        JSONObject object = new JSONObject();

        Log.d("API Call", object.toString());


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, this.server + "/payment/list", object, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("API CALL", response.toString());
                        callback.onSuccess(response);

                    }

                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("API CALL", error.toString());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + access_token);
                return headers;
            }
        };

        queue.add(jsonObjectRequest);
    }
    public void getName(Context context, String access_token, ServerCallback callback) throws JSONException {
        RequestQueue queue = Volley.newRequestQueue(context);
        JSONObject object = new JSONObject();

        Log.d("API Call", object.toString());


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, this.server + "/user/get_name", object, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("API CALL", response.toString());
                        callback.onSuccess(response);

                    }

                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("API CALL", error.toString());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + access_token);
                return headers;
            }
        };

        queue.add(jsonObjectRequest);
    }
}
