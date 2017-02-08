package com.example.android.googleplacesnearbylist.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by victo on 2/7/2017.
 */

public class MyViewport {

    @SerializedName("northeast")
    @Expose
    private MyLatLng northeast;
    @SerializedName("southwest")
    @Expose
    private MyLatLng southwest;

    public MyLatLng getNortheast() {
        return northeast;
    }

    public void setNortheast(MyLatLng northeast) {
        this.northeast = northeast;
    }

    public MyLatLng getSouthwest() {
        return southwest;
    }

    public void setSouthwest(MyLatLng southwest) {
        this.southwest = southwest;
    }
}
