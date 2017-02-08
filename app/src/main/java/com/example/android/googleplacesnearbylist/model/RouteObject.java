package com.example.android.googleplacesnearbylist.model;

import java.util.List;

/**
 * Created by victo on 2/8/2017.
 */

public class RouteObject {

    private List<LegsObject> legs;
    private MyViewport bounds;

    public RouteObject(List<LegsObject> legs, MyViewport bounds) {
        this.bounds = bounds;
        this.legs = legs;
    }

    public List<LegsObject> getLegs() {
        return legs;
    }

    public MyViewport getBounds(){return bounds; }

}
