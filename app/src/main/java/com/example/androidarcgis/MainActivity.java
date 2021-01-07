package com.example.androidarcgis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.esri.arcgisruntime.data.GeoPackage;

import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.LayerList;
import com.esri.arcgisruntime.mapping.MobileMapPackage;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;

import java.io.FileNotFoundException;
import java.util.Arrays;
import android.view.MotionEvent;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.example.androidarcgis.services.DatabaseService;
import com.example.androidarcgis.services.LayerStyleService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
//adb push ltmap.mmpk /sdcard/ArcGIS/samples/MapPackage/ltmap.mmpk

//adb push riboje.gpkg /sdcard/ArcGIS/Samples/GeoPackage/riboje.gpkg

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "teeest";
    private static final String FILE_EXTENSION = ".mmpk";
    private static File extStorDir;
    private static String extSDCardDirName;
    private static String filename;
    private static String mmpkFilePath;

    private DatabaseAccess databaseAccess;

    String[] reqPermission = new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE };
    private MapView mMapView;
    private MobileMapPackage mapPackage;
    private int requestCode = 2;

    private LocationDisplay mLocationDisplay;

    private static LayerList mOperationalLayers;


    static ArcGISMap mMap;

    private void setupLocationDisplay() {
        mLocationDisplay = mMapView.getLocationDisplay();
        mLocationDisplay.addDataSourceStatusChangedListener(dataSourceStatusChangedEvent -> {
            if (dataSourceStatusChangedEvent.isStarted() || dataSourceStatusChangedEvent.getError() == null) {
                return;
            }

            int requestPermissionsCode = 2;
            String[] requestPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

            if (!(ContextCompat.checkSelfPermission(MainActivity.this, requestPermissions[0]) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(MainActivity.this, requestPermissions[1]) == PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(MainActivity.this, requestPermissions, requestPermissionsCode);
                Toast.makeText(MainActivity.this, "Norėdami matyti lokaciją, pridėkite lokacijos teises telefono nustatymuose", Toast.LENGTH_LONG).show();
                Log.d("teeest", "Location permissions granted");
            } else {
                String message = String.format("Error in DataSourceStatusChangedListener: %s",
                        dataSourceStatusChangedEvent.getSource().getLocationDataSource().getError().getMessage());
                Log.d("teeest", "Location error :" + message);
            }
        });
        mLocationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.RECENTER);
        mLocationDisplay.startAsync();
    }



    private static String createMobileMapPackageFilePath() {
        return extStorDir.getAbsolutePath() + File.separator + extSDCardDirName + File.separator + filename
                + FILE_EXTENSION;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] reqPermission2 = new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(MainActivity.this, reqPermission2, requestCode);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setSelectedItemId(R.id.map);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.settings:
                        startActivity(new Intent(getApplicationContext(),SettingsActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.map:
                        return true;
                    case R.id.layers:
                        startActivity(new Intent(getApplicationContext(),MyLayers.class));
                        overridePendingTransition(0,0);
                        return true;

                }
                return false;
            }
        });


        databaseAccess = DatabaseAccess.getInstance(getApplicationContext());
        databaseAccess.open();


        mMap = new ArcGISMap();
        mMapView = new MapView(this);

        extStorDir = Environment.getExternalStorageDirectory();

        extSDCardDirName = this.getResources().getString(R.string.config_data_sdcard_offline_dir);

        filename = this.getResources().getString(R.string.map_mmpk);

        mmpkFilePath = createMobileMapPackageFilePath();

        mMapView = findViewById(R.id.mapView);


        File file = new File(mmpkFilePath);
        if(file.exists()){
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                Log.d("loadmobilemappackage", "READ_EXTERNAL_STORAGE  Permission granted");
            }
            else{
                Log.d("loadmobilemappackage", "READ_EXTERNAL_STORAGE  Permission was not granted");
            }
            if (ContextCompat.checkSelfPermission(MainActivity.this, reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
                Log.d("loadmobilemappackage", "11granted permission: " + reqPermission[0]);
                loadMobileMapPackage(mmpkFilePath);
            } else {
                Log.d("loadmobilemappackage", "11not granted permission: " + reqPermission[0]);
                ActivityCompat.requestPermissions(MainActivity.this, reqPermission, requestCode);

            }
            setupLocationDisplay();
           // requestWritePermission(mMap);

        }
        else{
            Log.d("loadmobilemappackage", "file doesn't exist");
            startActivity(new Intent(getApplicationContext(),DownloadMap.class));
        }

    }

    public String[] getLaws(String uredija,String girininkija, String kvartalas, String sklypas){
        String [] laws = new String[50];
        laws = databaseAccess.getLawsFromDatabase(uredija, girininkija,kvartalas, sklypas);
        return laws;
    }

    public String getUredijaName( String mu_kod){
        String uredijosPavadinimas;
        uredijosPavadinimas = databaseAccess.getUredijaName(mu_kod);
        return  uredijosPavadinimas;
    }

    public String getGirininkijaName( String gir_kod, String mu_kod){
        String girininkijosPavadinimas;
        girininkijosPavadinimas = databaseAccess.getGirininkijaName(gir_kod, mu_kod);
        Log.d("girininkija", gir_kod+"   " +girininkijosPavadinimas );
        return  girininkijosPavadinimas;
    }

    public String[] getRestrictions(String grupe, String rezervatas, String draustinis, String valstybinis, String bast, String past){
        String [] restrictions = new String[50];
        restrictions = databaseAccess.getRestrictions(grupe, rezervatas, draustinis, valstybinis, bast, past);
        return restrictions;
    }

    public boolean isLayerActive(String layerName, Context context){
        return (databaseAccess.isLayerActive(layerName));
    }

    public int[] getScales(String layerName){
        return(databaseAccess.getLayerScales(layerName));
    }

    private void loadMobileMapPackage(String mmpkFile) {


        File file = new File(mmpkFile);
        if(file.exists()){
            Log.d("loadmobilemappackage", "file exists");
        }
        else{
            Log.d("loadmobilemappackage", "file doesn't exist");
        }

        Scanner input = null;


        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            Log.d("loadmobilemappackage", "READ_EXTERNAL_STORAGE  Permission granted");
        }
        else{
            Log.d("loadmobilemappackage", "READ_EXTERNAL_STORAGE  Permission was not granted");
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            Log.d("loadmobilemappackage", "WRITE_EXTERNAL_STORAGE  Permission granted");
        }
        else{
            Log.d("loadmobilemappackage", "WRITE_EXTERNAL_STORAGE  Permission was not granted");
        }





        try {
            input = new Scanner(file);
            Log.d("loadmobilemappackage", "read");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d("loadmobilemappackage", "reading exception " + e.toString());
        }



        mapPackage = new MobileMapPackage(mmpkFile);


        // load the mobile map package asynchronously

        new android.os.Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        mapPackage.loadAsync();
                    }
                },1000
        );




        Log.d("loadmobilemappackage", "line before public void run() {");

        // add done listener which will invoke when mobile map package has loaded
        mapPackage.addDoneLoadingListener(new Runnable() {

            @Override
            public void run() {
                Log.d("loadmobilemappackage", "run");
                Log.d("loadmobilemappackage", "mmpk file path: " + mmpkFile);
                // check load status and that the mobile map package has maps
                Log.d("loadmobilemappackage", "mapPackage.getLoadStatus(): " + mapPackage.getLoadStatus());
                Log.d("loadmobilemappackage", "mapPackage.getLoadError(): " + mapPackage.getLoadError());
//                if(mapPackage.getLoadStatus() == LoadStatus.FAILED_TO_LOAD){
//                    loadMobileMapPackage(mmpkFile);
//                }
                if (mapPackage.getLoadStatus() == LoadStatus.LOADED && !mapPackage.getMaps().isEmpty()) {
                    Log.d("teeest"," map package loaded");
                    // add the map from the mobile map package to the MapView
                    mMap = mapPackage.getMaps().get(0);


                    requestWritePermission(mMap);

                    Point point = new Point(25.279652,54.687157);
                    Point wgs84Point = (Point) GeometryEngine.project(point, SpatialReferences.getWgs84());
                    SimpleMarkerSymbol markerSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, 0xffff0000, 10);

                    // add the point with a symbol to graphics overlay and add overlay to map view
                    GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
                   // mMapView.getGraphicsOverlays().add(graphicsOverlay);
                    graphicsOverlay.getGraphics().add(new Graphic(wgs84Point, markerSymbol));


                    String sr = mMap.getSpatialReference().toString();
                    Log.d("teeest","spatial ref: " + sr);
                    mMapView.setMap(mMap);
                } else {
                    // log an issue if the mobile map package fails to load
                    Log.d(TAG, "String mmpkFile" + mmpkFile);
                    Log.e(TAG, "getLoadError().getMessage()" + mapPackage.getLoadError().getMessage());
                }
            }
        });
    }


    @SuppressLint("ClickableViewAccessibility")
    private void openGeoPackage(ArcGISMap mMap) {

        mOperationalLayers = mMap.getOperationalLayers();
        String geoPackagePath =
                Environment.getExternalStorageDirectory() + getString(R.string.geopackage_folder) + "layers.gpkg";

        File file = new File(geoPackagePath);
        if(!file.exists()){
            Intent intent = new Intent(getBaseContext(), DownloadMap.class);
            intent.putExtra("layers", "true");
            startActivity(intent);
        }


        GeoPackage geoPackage = new GeoPackage(geoPackagePath);
        geoPackage.loadAsync();
        geoPackage.addDoneLoadingListener(() -> {
            if (geoPackage.getLoadStatus() == LoadStatus.LOADED) {
                int numberOfLayers = geoPackage.getGeoPackageFeatureTables().size();
                FeatureLayer[] featureLayers = new FeatureLayer[numberOfLayers];
                Log.d("FeatureTables" , "before if");
                Log.d("FeatureTables" , "size: " + String.valueOf(geoPackage.getGeoPackageFeatureTables().size()));
                int activeLayerIndex = 0;
                for(int i = 0; i<= geoPackage.getGeoPackageFeatureTables().size()-1; i++){
                    String tableName = geoPackage.getGeoPackageFeatureTables().get(i).getTableName();
                    Log.d("FeatureTables" , String.valueOf(i));
                    Log.d("FeatureTables" , geoPackage.getGeoPackageFeatureTables().get(i).getTableName());
                    Log.d("isLayerActive", "tableName: " + tableName + "   is Active: " +  isLayerActive(tableName, this));
                    if(isLayerActive(tableName, this)){

                        featureLayers[activeLayerIndex] = new FeatureLayer(geoPackage.getGeoPackageFeatureTables().get(i));
                        Log.d("tablename" , tableName);
                        int [] scales = getScales(tableName);

                        DatabaseService databaseService = new DatabaseService(getApplicationContext());
                        String colorId = databaseService.getColorId(tableName);
                        Log.d("getlayerstyle", "table name: " + tableName + "   " + "colorId : " + colorId);
                        if(!colorId.equals("1")){
                            featureLayers[activeLayerIndex].setRenderer(LayerStyleService.getRenderer(colorId));
                        }



                        featureLayers[activeLayerIndex].setMaxScale(scales[0]);
                        featureLayers[activeLayerIndex].setMinScale(scales[1]);
                        mOperationalLayers.add(featureLayers[activeLayerIndex]);
                        activeLayerIndex++;
                    }

                }
                for(int i = 0; i < activeLayerIndex; i++){
                    Log.d("featureTablesDebug", "featureLayers[i].getFeatureTable().getTableName()" + featureLayers[i].getFeatureTable().getTableName());
                }

                Log.d("FeatureTables" , "After If");


                mMapView.setMap(mMap);


                mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
                    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {

                        float centreX=mMapView.getX() + mMapView.getWidth()  / 2;
                        float centreY=mMapView.getY() + mMapView.getHeight() / 2;

                        android.graphics.Point screenPointt = new android.graphics.Point((int) motionEvent.getX(), (int) motionEvent.getY());
                        android.graphics.Point screenPoint = new android.graphics.Point(Math.round(centreX), Math.round(centreY));
                        Point mapPoint = mMapView.screenToLocation(screenPoint);



                        Point clickPoint = mMapView.screenToLocation(new android.graphics.Point(Math.round(motionEvent.getX()), Math.round(motionEvent.getY())));
                        int tolerance = 2;
                        double mapTolerance = tolerance * mMapView.getUnitsPerDensityIndependentPixel();

                        Envelope envelope = new Envelope(clickPoint.getX() - mapTolerance, clickPoint.getY() - mapTolerance,
                                clickPoint.getX() + mapTolerance, clickPoint.getY() + mapTolerance, mMap.getSpatialReference());
                        QueryParameters query = new QueryParameters();
                        query.setGeometry(envelope);




                        final ListenableFuture<List<IdentifyLayerResult>> identifyFuture =
                                mMapView.identifyLayersAsync(screenPointt, 2, false, 5);
                        identifyFuture.addDoneListener(() -> {
                            try {
                                List<IdentifyLayerResult> identifyLayersResults = identifyFuture.get();

                                String []  kvartalas = new String[10];
                                String []  bast = new String[10];
                                String []  past = new String[10];
                                String [] leidimai = new String[10];
                                String [] restrictionsRequest = new String [10];
                                Arrays.fill(restrictionsRequest, "");
                                boolean valstybinis = false;
                                String draustinis = "";
                                String rezervatas = "";
                                String grupe = "";
                                String [] restrictions = new String[50];
                                if (identifyFuture.get().size() == 0){
                                    Toast.makeText(MainActivity.this, "Pasirinkite sklypą", Toast.LENGTH_SHORT).show();
                                }
                                else if(identifyLayersResults.get(0).getLayerContent().getName().equals("uredijaRR") && identifyFuture.get().size() == 1){
                                    String uredijaName = identifyLayersResults.get(0).getElements().get(0).getAttributes().get("pavadinimas").toString();
                                    Toast.makeText(getApplicationContext(), uredijaName + " urėdija", Toast.LENGTH_SHORT).show();
                                }
                                else if(identifyLayersResults.get(0).getLayerContent().getName().equals("girininkija") && identifyFuture.get().size() == 1){
                                    String girininkijaName = identifyLayersResults.get(0).getElements().get(0).getAttributes().get("pavadinimas").toString();
                                    Toast.makeText(getApplicationContext(), girininkijaName + " girininkija", Toast.LENGTH_SHORT).show();
                                }
                                else if(identifyLayersResults.get(0).getLayerContent().getName().equals("kvartalas") && identifyFuture.get().size() == 1 ){
                                    Log.d("kvartalas","1");
                                    kvartalas[0] = identifyLayersResults.get(0).getElements().get(0).getAttributes().get("mu_kod").toString();
                                    kvartalas[1] = identifyLayersResults.get(0).getElements().get(0).getAttributes().get("gir_kod").toString();
                                    kvartalas[2] = identifyLayersResults.get(0).getElements().get(0).getAttributes().get("kv_nr").toString();
                                    kvartalas[3] = identifyLayersResults.get(0).getElements().get(0).getAttributes().get("Shape_Area").toString();
                                }
                                boolean lawsTriggered = false;
                                for(int i = 1; i<= identifyFuture.get().size();i++){
                                    if(identifyFuture.get().size() >= i){
                                        if(identifyLayersResults.get(i-1).getLayerContent().getName().equals("leidimai")){
                                            lawsTriggered = true;
                                        }
                                    }
                                }


                                for(int i = 1; i<= identifyFuture.get().size();i++){
                                    if(identifyFuture.get().size() >= i){
                                        Log.d("index", String.valueOf(i));
                                        if(identifyLayersResults.get(i-1).getLayerContent().getName().equals("sklypas")){
                                            Log.d("kvartalas","2");
                                            kvartalas[0] = identifyLayersResults.get(i-1).getElements().get(0).getAttributes().get("mu_kod").toString();
                                            kvartalas[1] = identifyLayersResults.get(i-1).getElements().get(0).getAttributes().get("gir_kod").toString();
                                            kvartalas[2] = identifyLayersResults.get(i-1).getElements().get(0).getAttributes().get("kv_nr").toString();
                                            kvartalas[3] = identifyLayersResults.get(i-1).getElements().get(0).getAttributes().get("skl_nr").toString();
                                            if(lawsTriggered){
                                                leidimai = getLaws(kvartalas[0],kvartalas[1],kvartalas[2],kvartalas[3]);
                                            }
                                            kvartalas[5] = getUredijaName( identifyLayersResults.get(i-1).getElements().get(0).getAttributes().get("mu_kod").toString());
                                            kvartalas[6] = getGirininkijaName(identifyLayersResults.get(i-1).getElements().get(0).getAttributes().get("gir_kod").toString(), identifyLayersResults.get(i-1).getElements().get(0).getAttributes().get("mu_kod").toString());
                                        }
                                        if(identifyLayersResults.get(i-1).getLayerContent().getName().equals("bast")){
                                            //Log.d("attributes bast:", identifyLayersResults.get(i-1).getElements().get(0).getAttributes().toString());
                                            bast[0] = "Buveinių apsaugai svarbi teritorija";
                                            bast[1] = identifyLayersResults.get(i-1).getElements().get(0).getAttributes().get("PAVADINIMAS").toString();
                                            Log.d("attributes", bast[1]);
                                            bast[2] = identifyLayersResults.get(i-1).getElements().get(0).getAttributes().get("BENDR_REGLAM").toString();
                                            Log.d("attributes", bast[2]);
                                            restrictionsRequest[4] = "Taip";
                                        }
                                        if(identifyLayersResults.get(i-1).getLayerContent().getName().equals("past")){
                                            past[0] = "Paukščių apsaugai svarbi teritorija";
                                            past[1] = identifyLayersResults.get(i-1).getElements().get(0).getAttributes().get("PAVADINIMAS").toString();
                                            Log.d("attributes", past[1]);
                                            past[2] = identifyLayersResults.get(i-1).getElements().get(0).getAttributes().get("BENDR_REGLAM").toString();
                                            restrictionsRequest[5] = "Taip";
                                        }
                                        if(identifyLayersResults.get(i-1).getLayerContent().getName().equals("valstybiniai")){
                                            valstybinis = true;
                                            restrictionsRequest[3] = "Taip";
                                        }
                                        else{
                                            restrictionsRequest[3] = "Ne";
                                        }
                                        if(identifyLayersResults.get(i-1).getLayerContent().getName().equals("miskupogrupiai")){
                                            grupe = identifyLayersResults.get(i-1).getElements().get(0).getAttributes().get("grupe").toString();
                                            restrictionsRequest[0] = grupe;
                                            Log.d("mmm", "grupe: " + grupe);
                                        }
                                        if(identifyLayersResults.get(i-1).getLayerContent().getName().equals("sklypas")){
                                            kvartalas[4] = identifyLayersResults.get(i-1).getElements().get(0).getAttributes().get("skl_nr").toString();
                                        }
                                        if(identifyLayersResults.get(i-1).getLayerContent().getName().equals("draustinis")){
                                            Log.d("attributes draustinis: " , identifyLayersResults.get(i-1).getElements().get(0).getAttributes().toString());
                                            draustinis = identifyLayersResults.get(i-1).getElements().get(0).getAttributes().get("PAVADINIMAS").toString();
                                            restrictionsRequest[2] = "Taip";
                                        }
                                        if(identifyLayersResults.get(i-1).getLayerContent().getName().equals("rezervatas")){
                                            Log.d("attributes draustinis: " , identifyLayersResults.get(i-1).getElements().get(0).getAttributes().toString());
                                            rezervatas = identifyLayersResults.get(i-1).getElements().get(0).getAttributes().get("PAVADINIMAS").toString();
                                            restrictionsRequest[1] = "Taip";
                                        }


                                        }
                                }

                                restrictions = getRestrictions(restrictionsRequest[0],restrictionsRequest[1],restrictionsRequest[2],restrictionsRequest[3],restrictionsRequest[4],restrictionsRequest[5]);


                                Bundle bundle = new Bundle();
                                BottomSheetDialog bottomSheet = new BottomSheetDialog();
                                bundle.putStringArray("sklypas", kvartalas);
                                bundle.putStringArray("bast", bast);
                                bundle.putStringArray("past", past);
                                bundle.putBoolean("valstybinis", valstybinis);
                                bundle.putString("grupe",grupe);
                                bundle.putStringArray("leidimai",leidimai);
                                bundle.putString("draustinis", draustinis);
                                bundle.putString("rezervatas", rezervatas);
                                bundle.putStringArray("draudimai", restrictions);
                                bottomSheet.setArguments(bundle);
                                if(kvartalas[0] == null){
                                    Log.d("kvartalasError", "kvartalas[0]: " + kvartalas[0]);
                                    Toast.makeText(MainActivity.this, "Pasirinkite sklypą", Toast.LENGTH_SHORT).show();
                                }
                                else if(kvartalas[4] == null ){
                                    Log.d("kvartalasError", "kvartalas[0]: " + kvartalas[0]);
                                    Toast.makeText(MainActivity.this, "Pasirinkite sklypą", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    bottomSheet.show(getSupportFragmentManager(), "bottomSheet");
                                }
                            } catch (InterruptedException | ExecutionException ex) {
                            }
                        });




                        return super.onSingleTapConfirmed(motionEvent);
                    }
                });
            } else {
                Toast.makeText(MainActivity.this, "Klaida! Sluoksnių failas nerastas", Toast.LENGTH_LONG).show();
                Log.e(TAG, "GeoPackage failed to load!" + geoPackage.getLoadError());
                Log.d(TAG, "GeoPackage  path: " + geoPackagePath);
            }
        });

    }



    /**
     * Request write permission on the device.
     */
    private void requestWritePermission(ArcGISMap mMap) {
        // define permission to request
        String[] reqPermission = new String[] { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        int requestCode = 2;
        // For API level 23+ request permission at runtime
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
            openGeoPackage(mMap);
        } else {
            // request permission
            ActivityCompat.requestPermissions(MainActivity.this, reqPermission, requestCode);
        }
    }

    /**
     * Handle the permissions request response.
     */


//    public void onRequestPermissionsResult(int requestCode,
//                                           @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//           // openGeoPackage(mMap);
//        } else {
//            // report to user that permission was denied
//            Toast.makeText(MainActivity.this,
//                    getResources().getString(R.string.geopackage_read_permission_denied), Toast.LENGTH_SHORT).show();
//        }
//    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.dispose();
    }
}