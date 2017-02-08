package com.example.android.googleplacesnearbylist.view;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.android.googleplacesnearbylist.R;
import com.example.android.googleplacesnearbylist.model.DirectionObject;
import com.example.android.googleplacesnearbylist.model.LegsObject;
import com.example.android.googleplacesnearbylist.model.PolylineObject;
import com.example.android.googleplacesnearbylist.model.RouteObject;
import com.example.android.googleplacesnearbylist.model.StepsObject;
import com.example.android.googleplacesnearbylist.parser.DirectionsJSONParser;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private double curr_lat;
    private double curr_lng;
    private double dest_lat;
    private double dest_lng;
    private String curr_title;
    private String dest_title;

    private String GOOGLE_JSON_URL;
    private static final String MODE_DRIVING = "driving";
    private static final String MODE_WALKING = "walking";

    private static final int DEFAULT_ZOOM = 17;

    private ArrayList<Marker> markers;
    private DirectionObject directionObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // get data from previous active
        Intent intent = getIntent();
        curr_lat = intent.getDoubleExtra(MainActivity.EXTRA_CURR_LAT, -1);
        curr_lng = intent.getDoubleExtra(MainActivity.EXTRA_CURR_LNG, -1);
        dest_lat = intent.getDoubleExtra(MainActivity.EXTRA_DEST_LAT, -1);
        dest_lng = intent.getDoubleExtra(MainActivity.EXTRA_DEST_LNG, -1);
        curr_title = intent.getStringExtra(MainActivity.EXTRA_CURR_TITLE);
        dest_title = intent.getStringExtra(MainActivity.EXTRA_DEST_TITLE);

        GOOGLE_JSON_URL =
                "https://maps.googleapis.com/maps/api/directions/json?" +
                        "origin=" + curr_lat + "," + curr_lng + "&" +
                        "destination=" + dest_lat + "," + dest_lng + "&" +
                        "mode=" + MODE_DRIVING + "&" +
                        "key=" + MainActivity.API_KEY_PLACES;

        markers = new ArrayList<>();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add source marker
        markers.add(mMap.addMarker(new MarkerOptions()
                .position(new LatLng(curr_lat, curr_lng))
                .title(curr_title)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        ));

        // Add destination marker
        markers.add(mMap.addMarker(new MarkerOptions()
                .position(new LatLng(dest_lat, dest_lng))
                .title(dest_title)
        ));

        // Get direction from Direction API Server
        getDirectionFromDirectionApiServer();
    }

    private void getDirectionFromDirectionApiServer() {
        RequestQueue queue = Volley.newRequestQueue(this);

        Log.e("GOOGLE_JSON_URL", GOOGLE_JSON_URL);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, GOOGLE_JSON_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.e("RESPONSE", response);

                        // parse json
                        directionObject =
                                DirectionsJSONParser.parseAndGetResult(response);

                        // get directions
                        List<LatLng> mDirections =
                                getDirectionPolylines(directionObject.getRoutes());

                        Log.e("MapsActivity", "mDirections size: " + mDirections.size());

                        // draw on map
                        drawRouteOnMap(mMap, mDirections);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MapsActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        queue.add(stringRequest);
    }

    private List<LatLng> getDirectionPolylines(List<RouteObject> routes){
        List<LatLng> directionList = new ArrayList<>();
        for(RouteObject route : routes){
            List<LegsObject> legs = route.getLegs();
            for(LegsObject leg : legs){
                List<StepsObject> steps = leg.getSteps();
                for(StepsObject step : steps){
                    PolylineObject polyline = step.getPolyline();
                    String points = polyline.getPoints();
                    List<LatLng> singlePolyline = decodePoly(points);
                    for (LatLng direction : singlePolyline){
                        directionList.add(direction);
                    }
                }
            }
        }
        return directionList;
    }

    private void drawRouteOnMap(GoogleMap map, List<LatLng> positions){
        PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
        options.addAll(positions);
        Polyline polyline = map.addPolyline(options);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : markers) {
            builder.include(marker.getPosition());
        }

        LatLng boundSouthWest = new LatLng(
                directionObject.getRoutes().get(0).getBounds().getSouthwest().getLat(),
                directionObject.getRoutes().get(0).getBounds().getSouthwest().getLng());
        LatLng boundNorthEast = new LatLng(
                directionObject.getRoutes().get(0).getBounds().getNortheast().getLat(),
                directionObject.getRoutes().get(0).getBounds().getNortheast().getLng());

        builder.include(boundNorthEast);
        builder.include(boundSouthWest);

        LatLngBounds bounds = builder.build();

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, DEFAULT_ZOOM));
    }
    /**
     * Method to decode polyline points
     * Courtesy : http://jeffreysambells.com/2010/05/27/decoding-polylines-from-
     * google-maps-direction-api-with-java
     * */
    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }



}
