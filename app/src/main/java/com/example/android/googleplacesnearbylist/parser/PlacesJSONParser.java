package com.example.android.googleplacesnearbylist.parser;

import com.example.android.googleplacesnearbylist.model.MyPlace;
import com.google.gson.Gson;

/**
 * Created by victo on 2/7/2017.
 */

public class PlacesJSONParser {

    public static MyPlace parseAndGetResult(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, MyPlace.class);
    }
}
