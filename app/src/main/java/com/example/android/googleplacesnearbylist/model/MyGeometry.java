package com.example.android.googleplacesnearbylist.model;

/**
 * Created by victo on 2/7/2017.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MyGeometry {

    @SerializedName("location")
    @Expose
    private MyLatLng location;
    @SerializedName("viewport")
    @Expose
    private MyViewport viewport;

    public MyLatLng getLocation() {
        return location;
    }

    public void setLocation(MyLatLng location) {
        this.location = location;
    }

    public MyViewport getViewport() {
        return viewport;
    }

    public void setViewport(MyViewport viewport) {
        this.viewport = viewport;
    }
}
