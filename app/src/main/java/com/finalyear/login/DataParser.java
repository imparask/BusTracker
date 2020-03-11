package com.finalyear.login;

import android.util.Log;

import com.google.firebase.firestore.proto.TargetGlobal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

class DataParser {

    public HashMap<String, String> parseDirections(String jsonData) {
        JSONArray jsonArray = null;
        JSONObject jsonObject = null;

        try {
            jsonObject = new JSONObject(jsonData);
            jsonArray = jsonObject.getJSONArray("routes");


            Log.d("json data",jsonData);
            Log.d("json object",jsonObject.toString());
            Log.d("json array1",jsonArray.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getDuration(jsonArray);
    }

    private HashMap<String, String> getDuration(JSONArray googleDirectionsJson) {

        HashMap<String,String> googleDirectionsMap = new HashMap<>();
        String duration = "";
        String distance = "";

        Log.d("json array2",googleDirectionsJson.toString());

        return googleDirectionsMap;
    }
}
