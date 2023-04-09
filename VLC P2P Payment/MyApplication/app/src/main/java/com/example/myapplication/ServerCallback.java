package com.example.myapplication;

import org.json.JSONException;
import org.json.JSONObject;

public interface ServerCallback{
    void onSuccess(JSONObject response);
}

