package com.example.android.googleplacesnearbylist.model;

import java.util.List;

/**
 * Created by victo on 2/8/2017.
 */

public class LegsObject {

    private List<StepsObject> steps;
    private String end_address;
    private String start_address;

    public LegsObject(List<StepsObject> steps, String end_address, String start_address) {
        this.steps = steps;
        this.end_address = end_address;
        this.start_address = start_address;
    }

    public List<StepsObject> getSteps() {
        return steps;
    }

    public String getEndAddress() {
        return end_address;
    }

    public String getStartAddress() {
        return start_address;
    }

}
