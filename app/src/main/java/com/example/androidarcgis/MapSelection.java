package com.example.androidarcgis;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.esri.arcgisruntime.data.GeoPackage;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Multipoint;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.MobileMapPackage;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.example.androidarcgis.services.CheckIfServerRunning;
import com.example.androidarcgis.services.DatabaseService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutionException;

import android.app.ProgressDialog;

public class MapSelection extends AppCompatActivity {
    static ArcGISMap mMap;
    MapView mMapView;
    PointCollection pointCollection;
    Graphic polygonGraphic;
    Graphic multiPointGraphic;
    Button confirmButton;
    Button declineButton;
    Polygon polygon;
    GraphicsOverlay overlay;
    DatabaseService databaseService;

    private ProgressDialog progressDialog;



    private Envelope createEnvelope() {
        Envelope envelope = new Envelope(-123.0, 33.5, -101.0, 48.0, SpatialReferences.getWgs84());
        return envelope;
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_selection);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setSelectedItemId(R.id.settings);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.map:
                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.settings:
                        startActivity(new Intent(getApplicationContext(),SettingsActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.layers:
                        startActivity(new Intent(getApplicationContext(),MyLayers.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });


//        try {
//            URL url  = new URL("http://192.168.0.107:9595/getMap/");
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }



//        try {
//            URL url  = new URL("http://192.168.0.107:9595/getMap/");
//            new DownloadFilesTask().execute(url);
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }


        //downloadData();

        loadMobileMapPackage();

        removeFailedDownloads();
        confirmButton = (Button) findViewById(R.id.confirmBtn);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("httpRequest" , "Confirm button clicked");

                String coordinates = "";
                for(int i = 0; i < pointCollection.size(); i++){
                    Point projectedPoint = (Point) GeometryEngine.project(pointCollection.get(i),
                            SpatialReference.create(3346));
                    coordinates = coordinates + projectedPoint.getX() + ',' + projectedPoint.getY()+'|';
                }
                coordinates = coordinates.substring(0, coordinates.length()-1);

                //getLayers(coordinates);
                String[] parameters = new String[3];
                parameters[0]= getResources().getString(R.string.http) + getResources().getString(R.string.ip) + "/getLayers?polygon=" + coordinates;
                Log.d("urlgeneration","R.string.http + R.string.ip :" + parameters[0]);
                parameters[1]= "/layers.gpkg";
                new com.example.androidarcgis.services.DownloadManager(MapSelection.this).execute(parameters);
//                File file = new File("/storage/emulated/0/miskoInformacineSistema/layers.gpkg");
//                boolean deleted = file.delete();
                Log.d("httpRequest" , "coordinates: " + coordinates);
                Log.d("httpRequest" , "server is working ");

            }
        });
        declineButton = (Button) findViewById(R.id.declineBtn);
        declineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pointCollection.clear();
                overlay.getGraphics().remove(polygonGraphic);
                overlay.getGraphics().remove(multiPointGraphic);
                setButtonsVisibility();
            }
        });
    }


    private void loadMobileMapPackage(){
        mMap = new ArcGISMap();
        mMapView = new MapView(this);
        mMapView = (MapView) findViewById(R.id.mapView);

        if (ContextCompat.checkSelfPermission(MapSelection.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            loadMobileMapPackage("/storage/emulated/0/miskoInformacineSistema/map.mmpk");
        } else {
            // request permission
            String permissions[] = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(MapSelection.this, permissions, 2);
        }
    }

    private void loadMobileMapPackage(String path){

        File file = new File(path);
        if(file.exists()){
            Log.d("loadmobilemappackage", "file exists");
        }
        else{
            Log.d("loadmobilemappackage", "file doesn't exist");
            return;
        }

        MobileMapPackage mapPackage = new MobileMapPackage(path);


        // load the mobile map package asynchronously
        mapPackage.loadAsync();

        Log.d("loadmobilemappackage", "line before public void run() {");

        // add done listener which will invoke when mobile map package has loaded
        mapPackage.addDoneLoadingListener(new Runnable() {

            @Override
            public void run() {
                if (mapPackage.getLoadStatus() == LoadStatus.LOADED && !mapPackage.getMaps().isEmpty()) {
                    Log.d("teeest"," map package loaded");
                    mMap = mapPackage.getMaps().get(0);


//                    requestWritePermission(mMap);
                    mMapView.setMap(mMap);
                    SimpleMarkerSymbol markerSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.parseColor("#379683"), 14);
        SimpleFillSymbol fillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.parseColor("#66379683"), null);
        overlay = new GraphicsOverlay();
        mMapView.getGraphicsOverlays().add(overlay);

        polygonGraphic = new Graphic();
        pointCollection = new PointCollection(SpatialReferences.getWgs84());
        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(MapSelection.this, mMapView) {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {

                android.graphics.Point location = new android.graphics.Point(Math.round(e.getX()), Math.round(e.getY()));
                Point originalPoint = mMapView.screenToLocation(location);
                Point projectedPoint = (Point) GeometryEngine.project(originalPoint,
                        SpatialReference.create(4326));


                if(pointCollection.size() <= 3){
                    pointCollection.add(projectedPoint);
                }
                else{
                    return super.onSingleTapConfirmed(e);
                }

                setButtonsVisibility();

                overlay.getGraphics().remove(polygonGraphic);
                overlay.getGraphics().remove(multiPointGraphic);
                //3857

                Multipoint multipoint = new Multipoint(pointCollection);
                multiPointGraphic = new Graphic(multipoint,markerSymbol);
                overlay.getGraphics().add(multiPointGraphic);

                polygon = new Polygon(pointCollection);
                polygonGraphic = new Graphic(polygon,fillSymbol);
                overlay.getGraphics().add(polygonGraphic);

//                overlay.getGraphics().add(new Graphic(projectedPoint, markerSymbol));
                Log.d("ClickCoordinates x : ", String.valueOf(projectedPoint.getX()));
                Log.d("ClickCoordinates y : ", String.valueOf(projectedPoint.getY()));
                Log.d("ClickCoordinates x : ", String.valueOf(location.x));
                Log.d("ClickCoordinates y : ", String.valueOf(location.y));

                return super.onSingleTapConfirmed(e);
            }
        });
                } else {
                    // log an issue if the mobile map package fails to load
                    Log.d("mapselection", "String mmpkFile" + path);
                    Log.e("mapselection", "getLoadError().getMessage()" + mapPackage.getLoadError().getMessage());
                }
            }
        });

    }


    private void setButtonsVisibility(){
        if(pointCollection.size() >= 1){
            declineButton.setVisibility(View.VISIBLE);
        }
        else{
            declineButton.setVisibility(View.GONE);
        }

        if(pointCollection.size() >= 3){
            confirmButton.setVisibility(View.VISIBLE);
        }
        else{
            confirmButton.setVisibility(View.GONE);
        }
    }

    public void removeFailedDownloads(){
        Log.d("DownloadFile", "removeFailedDownloads");
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterByStatus (DownloadManager.STATUS_FAILED|DownloadManager.STATUS_PENDING|DownloadManager.STATUS_RUNNING|DownloadManager.STATUS_PAUSED);
        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        Cursor c = dm.query(query);
        Log.d("DownloadFile", "cursor length: " + c.getCount());
        while(c.moveToNext()) {
            dm.remove(c.getLong(c.getColumnIndex(DownloadManager.COLUMN_ID)));
        }
    }



    @Override
    protected void onPause(){
        mMapView.pause();
        super.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        mMapView.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.dispose();
    }

}
