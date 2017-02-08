package com.example.android.googleplacesnearbylist.model;

import java.util.List;

/**
 * Created by victo on 2/7/2017.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MyPlace {

    @SerializedName("html_attributions")
    @Expose
    private List<String> htmlAttributions = null;
    @SerializedName("next_page_token")
    @Expose
    private String nextPageToken;
    @SerializedName("results")
    @Expose
    private List<MyResults> results = null;

    public List<String> getHtmlAttributions() {
        return htmlAttributions;
    }

    public void setHtmlAttributions(List<String> htmlAttributions) {
        this.htmlAttributions = htmlAttributions;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }

    public List<MyResults> getResults() {
        return results;
    }

    public void setResults(List<MyResults> results) {
        this.results = results;
    }
}
