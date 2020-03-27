package com.finalyear.login.ui;

import com.finalyear.login.GetDirectionsData;
import com.finalyear.login.R;
import com.finalyear.login.model.ConductorLocationBus;
import com.finalyear.login.model.UserLocation;
import com.finalyear.login.ui.Login;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.utilities.Utilities;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GaeRequestHandler;
import com.google.maps.GeoApiContext;
import com.google.maps.OkHttpRequestHandler;
import com.google.maps.PendingResult;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.Duration;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.finalyear.login.ui.StartActivity.exitConstant;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    private static final int LOCATION_UPDATE_INTERVAL = 1000;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private int count;

    private MapView mMapView;

    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private GoogleMap mGoogleMap;
    private LatLngBounds mMapBoundry;
    private UserLocation mUserPosition;
    private DatabaseReference countdatabase;
    private Handler mHandler = new Handler();
    private Runnable mRunnable;

    private ArrayList<ConductorLocationBus> conductorLocationBus = new ArrayList<>();
    private Map<String,GeoPoint> busLocations = new HashMap<>();
    private Map<String,Integer> busColor = new HashMap<>();
    private Map<String,Integer> busCount = new HashMap<>();
    private Map<String,Marker> markers = new HashMap<>();
    private HashMap<String,String> markerID = new HashMap<>();
    private Map<String,Location> prevLocation = new HashMap<>();
    private Map<String,Location> currLocation = new HashMap<>();
    private Map<String,Float> locationBearing = new HashMap<>();
    private Map<String,Duration> markerDuration = new HashMap<>();
    private ArrayList<String> filterbusNumbers = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        countdatabase = FirebaseDatabase.getInstance().getReference();

        Intent intent = getIntent();
        filterbusNumbers = intent.getStringArrayListExtra("filterBuses");

        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView = (MapView) findViewById(R.id.map);
        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(this);

    }


    private void startUserLocationsRunnable(){
        Log.d(TAG, "startUserLocationsRunnable: starting runnable for retrieving updated locations.");
        mHandler.postDelayed(mRunnable = new Runnable() {
            @Override
            public void run() {
                retrieveUserLocations();
                mHandler.postDelayed(mRunnable, LOCATION_UPDATE_INTERVAL);
            }
        }, LOCATION_UPDATE_INTERVAL);
    }

    private void stopLocationUpdates(){
        mHandler.removeCallbacks(mRunnable);
    }

    private void retrieveUserLocations(){
        Log.d(TAG, "retrieveUserLocations: retrieving location of all buses");
        getConductors();
        setMarkerColor();
        addMarkers();
        removeExtraMarkers();
    }

    private void removeExtraMarkers() {
        try {
            if (markers.size() > conductorLocationBus.size()) {
                for (Map.Entry markerElement : markers.entrySet()) {
                    String key = markerElement.getKey().toString();
                    count = 1;
                    for (ConductorLocationBus locationBus : conductorLocationBus) {
                        String ref_No = locationBus.getConductor().getRef_no();
                        Log.d(TAG, "marker Count : " + count);
                        Log.d(TAG, "marker Key : " + key);
                        Log.d(TAG, "Conductor Ref Number : " + count);
                        Log.d(TAG, "Conductor Bus Location array size : " + conductorLocationBus.size());
                        if (key.equals(ref_No)) {
                            break;
                        }
                        count += 1;
                    }
                    if (count >= conductorLocationBus.size()) {
                        markers.get(key).remove();
                        markers.remove(key);
                        markerID.remove(key);
                        busLocations.remove(key);
                        busColor.remove(key);
                    }
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }


    private void getConductors() {

        try {
            fStore.collection("Conductor Bus Location").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null) {
                            conductorLocationBus.clear();
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                ConductorLocationBus conductor = documentSnapshot.toObject(ConductorLocationBus.class);
                                Log.d(TAG, "Conductor : " + conductor);
                                try {

                                    if(currLocation.get(conductor.getConductor().getRef_no())!=null){
                                        prevLocation.put(conductor.getConductor().getRef_no(),currLocation.get(conductor.getConductor().getRef_no()));
                                    }


                                    if (conductorLocationBus.size() == 0) {
                                        conductorLocationBus.add(conductor);
                                    }
                                    else {
                                        boolean flag = false;
                                        for (ConductorLocationBus busLocation : conductorLocationBus) {
                                            if (busLocation.getConductor().getRef_no().equals(conductor.getConductor().getRef_no())) {
                                                flag = true;
                                            }
                                        }
                                        if (!flag) {
                                            conductorLocationBus.add(conductor);
                                        }
                                    }
                                    Location location = new Location(TAG);
                                    location.setLatitude(conductor.getGeoPoint().getLatitude());
                                    location.setLongitude(conductor.getGeoPoint().getLongitude());

                                    currLocation.put(conductor.getConductor().getRef_no(),location);

                                    if(prevLocation!= null) {
                                        float bearing = prevLocation.get(conductor.getConductor().getRef_no())
                                                .bearingTo(currLocation.get(conductor.getConductor().getRef_no()));
                                        locationBearing.put(conductor.getConductor().getRef_no(),bearing);
                                    }

                                    busLocations.put(conductor.getConductor().getRef_no(), conductor.getGeoPoint());

                                    Log.d(TAG,"Previous Locations : "+prevLocation);
                                    Log.d(TAG,"Current Locations : "+currLocation);
                                }
                                catch (NullPointerException e){
                                    Log.d(TAG,"fetching locations Exception :  "+e.getMessage());
                                }
                            }
                        }
                    } else {
                        Log.d(TAG, "Error..!! " + task.getException());
                    }
                }
            });
        }
        catch (Exception e){
            Log.d(TAG,"getConductorsException : "+e.getMessage());
        }
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context,int vectorResId, int color){

        float[] hsv = new float[3];

        Drawable vectorDrawable = ContextCompat.getDrawable(context,vectorResId);
        vectorDrawable.setBounds(0,0,vectorDrawable.getIntrinsicWidth(),vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),vectorDrawable.getIntrinsicHeight(),Bitmap.Config.ARGB_8888);
        hsv[0]=color;
        hsv[1]=1;
        hsv[2]=1;
        vectorDrawable.setTint(Color.HSVToColor(hsv));
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    private void setMarkerColor() {
        try {
            countdatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        for(ConductorLocationBus conductor : conductorLocationBus){
                            String busCountID = conductor.getBusCount();
                            int color = 120;
                            if(busCountID.equals(data.getKey())){
                                String countCurrent = data.child("Count").getValue().toString();
                                count = Integer.parseInt(countCurrent);
                                busCount.put(conductor.getConductor().getRef_no(),count);
                                if(count<=60 && count >0){
                                    color = color - count*2;
                                }
                                else if(count >60){
                                    color = 0;
                                }
                                busColor.put(conductor.getConductor().getRef_no(),color);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        catch (NullPointerException e){
            Log.d(TAG,"getColor Exception : "+e.getMessage());
        }
    }

    public void moveVechile(final Marker myMarker, final Location finalPosition) {

        final LatLng startPosition = myMarker.getPosition();

        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final float durationInMs = 1000;
        final boolean hideMarker = false;

        handler.post(new Runnable() {
            long elapsed;
            float t;
            float v;

            @Override
            public void run() {
                // Calculate progress using interpolator
                elapsed = SystemClock.uptimeMillis() - start;
                t = elapsed / durationInMs;
                v = interpolator.getInterpolation(t);

                LatLng currentPosition = new LatLng(
                        startPosition.latitude * (1 - t) + (finalPosition.getLatitude()) * t,
                        startPosition.longitude * (1 - t) + (finalPosition.getLongitude()) * t);
                myMarker.setPosition(currentPosition);


                // Repeat till progress is complete
                if (t < 1) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                    // handler.postDelayed(this, 100);
                } else {
                    if (hideMarker) {
                        myMarker.setVisible(false);
                    } else {
                        myMarker.setVisible(true);
                    }
                }
            }
        });
    }


    public void rotateMarker(final Marker marker, final float toRotation) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final float startRotation = marker.getRotation();
        final long duration = 1000;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);

                float rot = t * toRotation + (1 - t) * startRotation;


                marker.setRotation(-rot > 180 ? rot / 2 : rot);
                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
    }


    private void addMarkers(){
        double latitude;
        double longitude;
        int color;
        float bearing;
        Log.d(TAG,"Buses : "+busLocations.size());
        try {
            if (markers.size() == 0) {
                for (ConductorLocationBus locationBus : conductorLocationBus) {
                    if (locationBus != null) {
                        String refNumber = locationBus.getConductor().getRef_no();
                        if(filterbusNumbers.size()!=0){
                            Log.d(TAG,"Filtered Buses : "+filterbusNumbers);
                            if(!filterbusNumbers.contains(locationBus.getBusNumber())){
                                continue;
                            }
                        }
                        latitude = busLocations.get(refNumber).getLatitude();
                        longitude = busLocations.get(refNumber).getLongitude();
                        color= busColor.get(refNumber);
                        bearing = 0;
                        if(locationBearing.get(refNumber)!=null) {
                            bearing = locationBearing.get(refNumber);
                        }

                        MarkerOptions m = new MarkerOptions()
                                .title(locationBus.getBusNumber())
                                .snippet("Destination : "+locationBus.getBusDestination())
                                .icon(bitmapDescriptorFromVector(getApplicationContext(),R.drawable.ic_airport_shuttle_black_24dp,color))
                                .position(new LatLng(latitude, longitude))
                                .rotation(bearing-90)
                                .flat(true);
                        Marker marker = mGoogleMap.addMarker(m);
                        markerID.put(marker.getId(),refNumber);
                        markers.put(refNumber, marker);
                    }
                }
            } else {
                for (ConductorLocationBus locationBus : conductorLocationBus) {
                    if (locationBus != null) {
                        String refNumber = locationBus.getConductor().getRef_no();
                        if(filterbusNumbers.size()!=0){
                            Log.d(TAG,"Filtered Buses : "+filterbusNumbers);
                            if(!filterbusNumbers.contains(locationBus.getBusNumber())){
                                continue;
                            }
                        }
                        latitude = busLocations.get(refNumber).getLatitude();
                        longitude = busLocations.get(refNumber).getLongitude();
                        Log.d(TAG,"bus color : "+busColor);
                        color = busColor.get(refNumber);
                        bearing = 0;
                        if(locationBearing.get(refNumber)!=null) {
                            bearing = locationBearing.get(refNumber);
                        }
                        if(markers.get(refNumber)!=null) {
                            Location location = new Location(TAG);
                            location.setLatitude(latitude);
                            location.setLongitude(longitude);
                            moveVechile(markers.get(refNumber),location);
                            rotateMarker(markers.get(refNumber),bearing-90);
                            markers.get(refNumber).setIcon(bitmapDescriptorFromVector(getApplicationContext(),R.drawable.ic_airport_shuttle_black_24dp,color));
                            markers.get(refNumber).setTitle(locationBus.getBusNumber());
                            markers.get(refNumber).setSnippet("Destination : "+locationBus.getBusDestination());
                        }
                        else{
                            MarkerOptions m = new MarkerOptions()
                                    .title(locationBus.getBusNumber())
                                    .snippet("Destination : "+locationBus.getBusDestination())
                                    .icon(bitmapDescriptorFromVector(getApplicationContext(),R.drawable.ic_airport_shuttle_black_24dp,color))
                                    .position(new LatLng(latitude, longitude))
                                    .rotation(bearing-90)
                                    .flat(true);
                            Marker marker = mGoogleMap.addMarker(m);
                            markerID.put(marker.getId(),refNumber);
                            markers.put(refNumber, marker);
                        }
                    }
                }
            }
        }
        catch(Exception e){
            Log.d(TAG,"add markers exception : "+e.getMessage());
        }

    }

    private void setUserPosition(){

        mUserPosition = new UserLocation();

        DocumentReference userRef = fStore.collection("User Location").document(FirebaseAuth.getInstance().getUid());
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    mUserPosition = task.getResult().toObject(UserLocation.class);
                    Log.d(TAG,"User Position : "+mUserPosition.getGeoPoint().getLatitude()+"," +mUserPosition.getGeoPoint().getLongitude());
                }
            }
        });
    }

    private void setCameraView(){
        try {
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;
            int padding = (int) (width * 0.12); // offset from edges of the map 12% of screen

            double latitude = mUserPosition.getGeoPoint().getLatitude();
            double longitude = mUserPosition.getGeoPoint().getLongitude();

            double bottomBoundaryPoint = latitude - 0.0009;
            double topBoundaryPoint = latitude + 0.0009;
            double leftBoundaryPoint = longitude - 0.0009;
            double rightBoundaryPoint = longitude + 0.0009;

            mMapBoundry = new LatLngBounds(
                    new LatLng(bottomBoundaryPoint, leftBoundaryPoint),
                    new LatLng(topBoundaryPoint, rightBoundaryPoint)
            );

            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundry, width, height, padding));
        }
        catch(Exception e){
            Log.d(TAG,"setCamera Exception : "+e.getMessage());
        }
    }

    private void getDirectionData(Marker marker) {
        Object[] dataTransfer = new Object[2];
        String url = getRequestURL(marker);
        GetDirectionsData getDirectionsData = new GetDirectionsData();
        dataTransfer[0] = mGoogleMap;
        dataTransfer[1] = url;

        getDirectionsData.execute(dataTransfer);
    }

    private String getRequestURL(Marker marker){

        /*
        LatLng origin = new LatLng(marker.getPosition().latitude,marker.getPosition().longitude);
        LatLng destination = new LatLng(mUserPosition.getGeoPoint().getLatitude(),mUserPosition.getGeoPoint().getLongitude());
        String str_org = "origin="+ origin.latitude+","+origin.longitude; //Source
        String str_dest = "destination="+ destination.latitude+","+destination.longitude; //Destination

        String str_org = "origin="+ 19.2198294+","+73.1641636; //Source
        String str_dest = "destination="+ 19.1167426+","+72.9280256; //Destination

        String params = str_org+"&"+str_dest; //All parameters
        String output = "json"; //Output Format

        String url = "https://maps.googleapis.com/maps/api/directions/json?"+params+"&key="+getString(R.string.google_map_api_key);
           */

        StringBuilder googleDirectionsURL = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        googleDirectionsURL.append("origin="+ 19.2198294+","+73.1641636);
        googleDirectionsURL.append("&destination="+ 19.1167426+","+72.9280256);
        googleDirectionsURL.append("&key="+getString(R.string.google_map_api_key));

        return googleDirectionsURL.toString();
    }


    /*
    private String requestDirections(String reqUrl){

        String responseString = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;

        try{
            URL url = new URL(reqUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            //Response Result
            inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuffer stringBuffer = new StringBuffer();
            String line = "";

            while((line = bufferedReader.readLine()) != null){
                stringBuffer.append(line);
            }

            responseString = stringBuffer.toString();
            bufferedReader.close();
            inputStreamReader.close();
        }
        catch (Exception e ){
            e.printStackTrace();
        }
        finally {
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            httpURLConnection.disconnect();
        }

        return responseString;
    }

    private void calculateDuration(Marker marker){
        Log.d(TAG, "calculateDirections: calculating directions.");
        final String ref_no = markerID.get(marker.getId());

        DirectionsApiRequest directions =
                new DirectionsApiRequest(mGeoApiContext);

        directions.origin(
                new com.google.maps.model.LatLng(
                        marker.getPosition().latitude,
                        marker.getPosition().longitude
                )
        );

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                mUserPosition.getGeoPoint().getLatitude(),
                mUserPosition.getGeoPoint().getLongitude()
        );

        Log.d(TAG, "calculateDirections: destination: " + destination.toString());
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                Duration duration = result.routes[0].legs[0].duration;
                Log.d(TAG, "calculateDirections: duration: " + result.routes[0].legs[0].duration);
                Log.d(TAG, "calculateDirections: distance: " + result.routes[0].legs[0].distance);
                markerDuration.put(ref_no,duration);
            }
            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "calculateDirections: Failed to get directions: " + e.getMessage() );
            }
        });

    }
    */


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }



    @Override
    protected void onResume() {
        super.onResume();
        if(exitConstant !=100){
            startActivity(new Intent(getApplicationContext(),StartActivity.class));
        }
        mMapView.onResume();
        startUserLocationsRunnable();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }


    @Override
    public void onMapReady(GoogleMap map) {
        setUserPosition();
        mGoogleMap = map;
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        mGoogleMap.getUiSettings().setZoomGesturesEnabled(true);
        mGoogleMap.getUiSettings().setCompassEnabled(true);
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.setTrafficEnabled(true);

        mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                getDirectionData(marker);
                Intent intent = new Intent(getApplicationContext(),BusTracker.class);
                intent.putExtra("selectedMarkerETA","12mins");//markerDuration.get(markerID.get(marker.getId())).toString());
                intent.putExtra("selectedMarker",markerID.get(marker.getId()));
                intent.putExtra("passengerCount",busCount.get(markerID.get(marker.getId())).toString());
                startActivity(intent);
            }
        });
        mGoogleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                setCameraView();
            }
        });
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
        stopLocationUpdates();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //Toast.makeText(getApplicationContext(),"Logout from application",Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu_maps,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.logout){
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), Login.class));
            finish();
        }
        if(item.getItemId()==R.id.viewallbuses){
            filterbusNumbers.clear();
        }
        return true;
    }

}
