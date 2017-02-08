package com.example.android.googleplacesnearbylist.model;

import java.util.List;

/**
 * Created by victo on 2/8/2017.
 */

public class LegsObject {

    private List<StepsObject> steps;
    public LegsObject(List<StepsObject> steps) {
        this.steps = steps;
    }
    public List<StepsObject> getSteps() {
        return steps;
    }

}
