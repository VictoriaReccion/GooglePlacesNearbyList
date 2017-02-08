package com.example.android.googleplacesnearbylist.model;

/**
 * Created by victo on 2/8/2017.
 */

public class StepsObject {

    private PolylineObject polyline;
    public StepsObject(PolylineObject polyline) {
        this.polyline = polyline;
    }
    public PolylineObject getPolyline() {
        return polyline;
    }

}
