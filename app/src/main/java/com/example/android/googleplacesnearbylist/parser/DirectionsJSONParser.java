package com.example.android.googleplacesnearbylist.parser;

import com.example.android.googleplacesnearbylist.model.DirectionObject;
import com.google.gson.Gson;

/**
 * Created by victo on 2/8/2017.
 */

public class DirectionsJSONParser {

    public static DirectionObject parseAndGetResult(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, DirectionObject.class);
    }
}
