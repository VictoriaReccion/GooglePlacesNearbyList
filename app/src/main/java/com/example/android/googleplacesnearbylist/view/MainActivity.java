package com.example.android.googleplacesnearbylist.view;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.android.googleplacesnearbylist.R;
import com.example.android.googleplacesnearbylist.adapter.PlacesAdapter;
import com.example.android.googleplacesnearbylist.model.MyGeometry;
import com.example.android.googleplacesnearbylist.model.MyLatLng;
import com.example.android.googleplacesnearbylist.model.MyPlace;
import com.example.android.googleplacesnearbylist.model.MyResults;
import com.example.android.googleplacesnearbylist.parser.PlacesJSONParser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import static android.R.id.list;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        PlacesAdapter.ItemClickCallback {

    private static final String TAG = MainActivity.class.getSimpleName();

    // The entry point to Google Play services, used by the Places API and Fused Location Provider.
    private GoogleApiClient mGoogleApiClient;
    // A request object to store parameters for requests to the FusedLocationProviderApi.
    private LocationRequest mLocationRequest;
    // The desired interval for location updates. Inexact. Updates may be more or less frequent.
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    // The fastest rate for active location updates. Exact. Updates will never be more frequent
    // than this value.
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // A default location (Manila, Philippines) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(14.5995, 120.9842);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    // The geographical location where the device is currently located.
    private Location mCurrentLocation;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    private static final int RADIUS = 500;
    public static final String API_KEY_PLACES = "AIzaSyDATMRQZYxWnI4tRkMxcNYgzlYWUgacIgk";

    private boolean hasItemsLeft = true;
    private boolean isInit = false;

    private String next_page_token;

    // View Items
    private RecyclerView recView;
    private PlacesAdapter adapter;
    private List<MyResults> nearbyPlaces;
    private EndlessRecyclerViewScrollListener scrollListener;
    private LinearLayoutManager linearLayoutManager;
    private Handler handler;

    // For intent extra
    public static final String EXTRA_CURR_LAT = "EXTRA_CURR_LAT";
    public static final String EXTRA_CURR_LNG = "EXTRA_CURR_LNG";
    public static final String EXTRA_CURR_TITLE = "EXTRA_CURR_TITLE";
    public static final String EXTRA_DEST_LAT = "EXTRA_DEST_LAT";
    public static final String EXTRA_DEST_LNG = "EXTRA_DEST_LNG";
    public static final String EXTRA_DEST_TITLE = "EXTRA_DEST_TITLE";
    public static final String EXTRA_DISTANCE = "EXTRA_DISTANCE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        linearLayoutManager = new LinearLayoutManager(MainActivity.this);

        // Build the Play services client for use by the Fused Location Provider and the Places API.
        buildGoogleApiClient();
        mGoogleApiClient.connect();
    }

    /**
     * Get the device location and nearby places when the activity is restored after a pause.
     */
    @Override
    protected void onResume() {
        Log.e(TAG, "onResume");
        super.onResume();

        if (mGoogleApiClient.isConnected()) {
            getDeviceLocation();
        }
        // updateMarkers();
    }

    /**
     * Stop location updates when the activity is no longer in focus, to reduce battery consumption.
     */
    @Override
    protected void onPause() {
        Log.e(TAG, "onPause");
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }
    }

    /**
     * Builds a GoogleApiClient.
     * Uses the addApi() method to request the Google Places API and the Fused Location Provider.
     */
    private synchronized void buildGoogleApiClient() {
        Log.e(TAG, "buildGoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        createLocationRequest();

    }

    /**
     * Sets up the location request.
     */
    private void createLocationRequest() {
        Log.e(TAG, "createLocationRequest");
        mLocationRequest = new LocationRequest();

        /*
         * Sets the desired interval for active location updates. This interval is
         * inexact. You may not receive updates at all if no location sources are available, or
         * you may receive them slower than requested. You may also receive updates faster than
         * requested if other applications are requesting location at a faster interval.
         */
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        /*
         * Sets the fastest rate for active location updates. This interval is exact, and your
         * application will never receive updates faster than this value.
         */
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Gets the current location of the device and starts the location update notifications.
     */
    private void getDeviceLocation() {
        Log.e(TAG, "getDeviceLocation");
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         * Also request regular updates about the device location.
         */
        if (mLocationPermissionGranted) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, this);
        } else {
            Log.e(TAG, "Permission: " + mLocationPermissionGranted);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.e(TAG, "onLocationChanged");
        mCurrentLocation = location;

        // updateMarkers();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.e(TAG, "onConnected");
        getDeviceLocation();

        // initialize the data
        if(!isInit) {
            // initData();
            initData2();
            isInit = true;
        }
    }

    /**
     * Handles suspension of the connection to the Google Play services client.
     */
    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "onConnectionSuspended");
        Log.d(TAG, "Play services connection suspended");
    }

    /**
     * Handles failure to connect to the Google Play services client.
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed");
        // Refer to the reference doc for ConnectionResult to see what error codes might
        // be returned in onConnectionFailed.
        Log.d(TAG, "Play services connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }

    private void initData() {
        Log.e(TAG, "initData");

        String GOOGLE_JSON_URL =
                "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                        "location=" + mCurrentLocation.getLatitude() + "," +
                        mCurrentLocation.getLongitude() + "&" +
                        "radius=" + RADIUS + "&" +
                        "key=" + API_KEY_PLACES;

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, GOOGLE_JSON_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        // 1. parse json
                        final MyPlace myPlace = PlacesJSONParser.parseAndGetResult(response);
                        nearbyPlaces = myPlace.getResults();
                        next_page_token = myPlace.getNextPageToken();

                        // 2. set distances from current location
                        for(int i = 0; i < nearbyPlaces.size(); i ++) {
                            nearbyPlaces.get(i).setDistance(
                                getDistanceBetween(mCurrentLocation.getLatitude(),
                                     mCurrentLocation.getLongitude(),
                                     nearbyPlaces.get(i).getGeometry().getLocation().getLat(),
                                     nearbyPlaces.get(i).getGeometry().getLocation().getLng()));
                        }

                        // 3. set up recycler view and adapter
                        recView = (RecyclerView) findViewById(R.id.rec_list_places);
                        recView.setLayoutManager(linearLayoutManager);
                        adapter = new PlacesAdapter(nearbyPlaces, MainActivity.this);
                        recView.setAdapter(adapter);
                        adapter.setItemClickCallback(MainActivity.this);

                        // 4. set up scrolllistener
                        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager)
                        {
                            @Override
                            public void onLoadMore(int page, int totalItemsCount,
                                                   RecyclerView view) {
                                if(hasItemsLeft) {
                                    // to prevent from having many progress bars
                                    if(handler != null)
                                        return;

                                    // 1. set up handler for time delay of progress bar
                                    handler = new Handler();

                                    // 2. add progress bar
                                    nearbyPlaces.add(null);
                                    adapter.notifyItemInserted(nearbyPlaces.size()-1);

                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {

                                            // after 2 seconds

                                            // 3. remove progress bar
                                            nearbyPlaces.remove(nearbyPlaces.size()-1);

                                            // 4. load data
                                            getNextPage(next_page_token);

                                            // 5. set handler to null for next loading
                                            handler = null;
                                        }
                                    },2000);
                                }
                            }
                        };

                        recView.addOnScrollListener(scrollListener);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });

        queue.add(stringRequest);
    }

    private void getNextPage(String next_page_token) {
        Log.e(TAG, "getNextPage");
        String GOOGLE_JSON_URL =
                "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                        "pagetoken=" + next_page_token + "&" +
                        "key=" + API_KEY_PLACES;

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, GOOGLE_JSON_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        // 1. parse json
                        MyPlace myPlace = PlacesJSONParser.parseAndGetResult(response);
                        List<MyResults> nearbyPlaces2 = myPlace.getResults();
                        setNextPageToken(myPlace.getNextPageToken());

                        // 2. set distances from current location
                        for(int i = 0; i < nearbyPlaces2.size(); i ++) {
                            nearbyPlaces2.get(i).setDistance(
                               getDistanceBetween(mCurrentLocation.getLatitude(),
                                    mCurrentLocation.getLongitude(),
                                    nearbyPlaces2.get(i).getGeometry().getLocation().getLat(),
                                    nearbyPlaces2.get(i).getGeometry().getLocation().getLng()));
                        }

                        // 3. add to current list
                        nearbyPlaces.addAll(nearbyPlaces2);

                        // 4. update recycler view
                        adapter.setItems(nearbyPlaces);
                        adapter.notifyDataSetChanged();

                        // 5. check if there are items left in the server
                        if(nearbyPlaces2.size() > 20) {
                            hasItemsLeft = false;
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        queue.add(stringRequest);
    }

    private String getDistanceBetween(Double source_latitude, Double source_longitude,
                                      Double dest_latitude, Double dest_longitude) {
        float[] results = new float[1];
        Location.distanceBetween(source_latitude, source_longitude,
                dest_latitude, dest_longitude, results);

        return String.format(java.util.Locale.US,"%.3f", (results[0]/1000));
    }

    @Override
    public void onItemClick(int p) {
        Intent intent = new Intent(this, MapsActivity.class);

        intent.putExtra(EXTRA_CURR_LAT, mCurrentLocation.getLatitude());
        intent.putExtra(EXTRA_CURR_LNG, mCurrentLocation.getLongitude());
        intent.putExtra(EXTRA_CURR_TITLE, "Your Location");
        intent.putExtra(EXTRA_DEST_LAT, nearbyPlaces.get(p).getGeometry().getLocation().getLat());
        intent.putExtra(EXTRA_DEST_LNG, nearbyPlaces.get(p).getGeometry().getLocation().getLng());
        intent.putExtra(EXTRA_DEST_TITLE, nearbyPlaces.get(p).getName());
        intent.putExtra(EXTRA_DISTANCE, nearbyPlaces.get(p).getDistance());

        startActivity(intent);
    }

    private void setNextPageToken(String next_page_token) {
        this.next_page_token = next_page_token;
    }

    private void initData2() {

        // initialize list
        nearbyPlaces = new ArrayList<>();

        // add places on nearbyPlaces
        MyResults myHome = new MyResults();

        // 1. add latitude and longitude
        MyLatLng myLatLng = new MyLatLng();
        myLatLng.setLat(14.421016);
        myLatLng.setLng(121.003349);
        myHome.setGeometry(new MyGeometry());
        myHome.getGeometry().setLocation(myLatLng);

        // 2. add vicinity
        myHome.setVicinity("Pasonanca Park, Teresa Park Subdivision, Talon Singko, Las Pinas City");

        // 3. add title
        myHome.setName("My Home");

        // 4. set distance
        myHome.setDistance(
                getDistanceBetween(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(),
                        myLatLng.getLat(), myLatLng.getLng())
        );

        // add to list
        nearbyPlaces.add(myHome);

        // set up recycler view
        recView = (RecyclerView) findViewById(R.id.rec_list_places);
        recView.setLayoutManager(linearLayoutManager);
        adapter = new PlacesAdapter(nearbyPlaces, MainActivity.this);
        recView.setAdapter(adapter);
        adapter.setItemClickCallback(MainActivity.this);
    }
}
