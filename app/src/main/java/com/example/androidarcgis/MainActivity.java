package com.example.androidarcgis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.data.GeoPackage;
import com.esri.arcgisruntime.data.GeoPackageFeatureTable;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.GeoElement;
import com.esri.arcgisruntime.mapping.LayerList;
import com.esri.arcgisruntime.mapping.MobileMapPackage;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.FillSymbol;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;


import java.io.File;
import java.util.List;
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
    // define permission to request
    String[] reqPermission = new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE };
    private MapView mMapView;
    private MobileMapPackage mapPackage;
    private int requestCode = 2;

    DatabaseAccess databaseAccess;

    private LocationDisplay mLocationDisplay;

    private static LayerList mOperationalLayers;


    static ArcGISMap mMap;


    /**
     * Create the mobile map package file location and name structure
     */
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
                Log.d("teeest", "Location permissions granted");
            } else {
                String message = String.format("Error in DataSourceStatusChangedListener: %s",
                        dataSourceStatusChangedEvent.getSource().getLocationDataSource().getError().getMessage());
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
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
        //mMap = new ArcGISMap();
        mMapView = new MapView(this);
       // mMap = new ArcGISMap(Basemap.Type.TOPOGRAPHIC, 54.68715, 25.279652, 12);

        // get sdcard resource name
        extStorDir = Environment.getExternalStorageDirectory();
        // get the directory
        extSDCardDirName = this.getResources().getString(R.string.config_data_sdcard_offline_dir);
        // get mobile map package filename
        filename = this.getResources().getString(R.string.ltmap_mmpk);
        // create the full path to the mobile map package file
        mmpkFilePath = createMobileMapPackageFilePath();
       // mmpkFilePath = "ltmap.mmpk";
        //Log.d("path", getApplicationContext().getFilesDir().getAbsolutePath() + "/assets/databases/ltmap.mmpk");



        // inflate MapView from layout
        mMapView = findViewById(R.id.mapView);

        // create a map with the BasemapType topographic
          // mMap = new ArcGISMap(Basemap.Type.TOPOGRAPHIC, 54.68715, 25.279652, 12);
        // set the map to be displayed in this view
        // For API level 23+ request permission at runtime
        if (ContextCompat.checkSelfPermission(MainActivity.this, reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
            loadMobileMapPackage(mmpkFilePath);
        } else {
            // request permission
            ActivityCompat.requestPermissions(MainActivity.this, reqPermission, requestCode);
        }
        /* ** ADD ** */
        setupLocationDisplay();
        //requestWritePermission(mMap);



    }

    public String[] getLaws(String uredija,String girininkija, String kvartalas, String sklypas){
        databaseAccess = DatabaseAccess.getInstance(getApplicationContext());
        databaseAccess.open();
        String [] laws = new String[50];
        laws = databaseAccess.getLawsFromDatabase(uredija, girininkija,kvartalas, sklypas);
        return laws;
    }

    public String getUredijaName( String mu_kod){
        databaseAccess = DatabaseAccess.getInstance(getApplicationContext());
        databaseAccess.open();
        String uredijosPavadinimas;
        uredijosPavadinimas = databaseAccess.getUredijaName(mu_kod);
        return  uredijosPavadinimas;
    }

    public String getGirininkijaName( String gir_kod, String mu_kod){
        databaseAccess = DatabaseAccess.getInstance(getApplicationContext());
        databaseAccess.open();
        String girininkijosPavadinimas;
        girininkijosPavadinimas = databaseAccess.getGirininkijaName(gir_kod, mu_kod);
        Log.d("girininkija", gir_kod+"   " +girininkijosPavadinimas );
        return  girininkijosPavadinimas;
    }

    public String[] getRestrictions(String grupe, String rezervatas, String draustinis, String valstybinis, String bast, String past){
        databaseAccess = DatabaseAccess.getInstance(getApplicationContext());
        databaseAccess.open();
        String [] restrictions = new String[50];
        restrictions = databaseAccess.getRestrictions(grupe, rezervatas, draustinis, valstybinis, bast, past);
        return restrictions;
    }

    private void loadMobileMapPackage(String mmpkFile) {
        //[DocRef: Name=Open Mobile Map Package-android, Category=Work with maps, Topic=Create an offline map]
        // create the mobile map package
        mapPackage = new MobileMapPackage(mmpkFile);
        // load the mobile map package asynchronously
        mapPackage.loadAsync();

        // add done listener which will invoke when mobile map package has loaded
        mapPackage.addDoneLoadingListener(new Runnable() {

            @Override
            public void run() {
                // check load status and that the mobile map package has maps
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

        String geoPackagePath =
                Environment.getExternalStorageDirectory() + getString(R.string.geopackage_folder) + "db.gpkg";
        GeoPackage geoPackage = new GeoPackage(geoPackagePath);
        geoPackage.loadAsync();
        geoPackage.addDoneLoadingListener(() -> {
            if (geoPackage.getLoadStatus() == LoadStatus.LOADED) {

                FeatureTable uredijaTable = geoPackage.getGeoPackageFeatureTables().get(0);
                FeatureTable girininkijaTable = geoPackage.getGeoPackageFeatureTables().get(1);
                FeatureTable kvartalasTable = geoPackage.getGeoPackageFeatureTables().get(2);
                FeatureTable bastTable = geoPackage.getGeoPackageFeatureTables().get(3);
                FeatureTable pastTable = geoPackage.getGeoPackageFeatureTables().get(4);
                FeatureTable draustinisTable = geoPackage.getGeoPackageFeatureTables().get(5);
                FeatureTable rezervatasTable = geoPackage.getGeoPackageFeatureTables().get(6);
                FeatureTable sklypasTable = geoPackage.getGeoPackageFeatureTables().get(7);
                FeatureTable miskuPogrupiaiTable = geoPackage.getGeoPackageFeatureTables().get(8);
                FeatureTable valstybiniaiMiskaiTable = geoPackage.getGeoPackageFeatureTables().get(9);
                FeatureTable leidimaiTable = geoPackage.getGeoPackageFeatureTables().get(12);



                if (uredijaTable == null) {
                    Toast.makeText(MainActivity.this, "No feature table found in the package!", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "No feature table found in this package!");
                    return;
                }

                // Create a layer to show the feature table
                FeatureLayer uredijaFeatureLayer = new FeatureLayer(uredijaTable);
                FeatureLayer girininkijaFeatureLayer = new FeatureLayer(girininkijaTable);
                FeatureLayer kvartalasFeatureLayer = new FeatureLayer(kvartalasTable);
                FeatureLayer bastFeatureLayer = new FeatureLayer(bastTable);
                FeatureLayer pastFeatureLayer = new FeatureLayer(pastTable);
                FeatureLayer draustinisFeatureLayer = new FeatureLayer(draustinisTable);
                FeatureLayer rezervatasFeatureLayer = new FeatureLayer(rezervatasTable);
                FeatureLayer sklypasFeatureLayer = new FeatureLayer(sklypasTable);
                FeatureLayer miskuPogrupiaiFeatureLayer = new FeatureLayer(miskuPogrupiaiTable);
                FeatureLayer valstybiniaiMiskaiFeatureLayer = new FeatureLayer(valstybiniaiMiskaiTable);
                FeatureLayer leidimaiFeatureLayer = new FeatureLayer(leidimaiTable);


                SimpleLineSymbol symbol = new SimpleLineSymbol();


                SimpleRenderer renderer = new SimpleRenderer(symbol);
                SimpleFillSymbol fillSymbol = new SimpleFillSymbol();
                fillSymbol.setColor(Color.TRANSPARENT);
                renderer.setSymbol(fillSymbol);
                pastFeatureLayer.setRenderer(renderer);

                SimpleRenderer renderValstybiniai = new SimpleRenderer(symbol);
                renderValstybiniai.setSymbol(fillSymbol);
                valstybiniaiMiskaiFeatureLayer.setRenderer(renderValstybiniai);

                SimpleRenderer renderMiskuPogrupiai = new SimpleRenderer(symbol);
                renderMiskuPogrupiai.setSymbol(fillSymbol);
                miskuPogrupiaiFeatureLayer.setRenderer(renderMiskuPogrupiai);


                SimpleLineSymbol symbolred = new SimpleLineSymbol();
                SimpleRenderer rendererRed = new SimpleRenderer(symbolred);
                SimpleFillSymbol fillsymbolRed = new SimpleFillSymbol();
                fillsymbolRed.setColor(Color.parseColor("#66ff0000"));
                //fillsymbolRed.setColor(Color.RED);
                rendererRed.setSymbol(fillsymbolRed);
                leidimaiFeatureLayer.setRenderer(rendererRed);

                SimpleFillSymbol fillsymbolBlue = new SimpleFillSymbol();
                //fillsymbolBlue.setColor(Color.parseColor("#661261A0"));
                fillsymbolBlue.setColor(Color.parseColor("#FFFFFF"));
                fillsymbolBlue.setStyle(SimpleFillSymbol.Style.BACKWARD_DIAGONAL);
                //fillsymbolBlue.setColor(Color.TRANSPARENT);
                SimpleRenderer rendererBlue = new SimpleRenderer();
                rendererBlue.setSymbol(fillsymbolBlue);
                pastFeatureLayer.setRenderer(rendererBlue);

                SimpleFillSymbol fillsymbolYellow = new SimpleFillSymbol();
                //fillsymbolYellow.setColor(Color.parseColor("#66FFF200"));
                fillsymbolYellow.setColor(Color.parseColor("#ff0000"));
                fillsymbolYellow.setStyle(SimpleFillSymbol.Style.FORWARD_DIAGONAL);
                //fillsymbolYellow.setColor(Color.TRANSPARENT);
                SimpleRenderer rendererYellow = new SimpleRenderer();
                rendererYellow.setSymbol(fillsymbolYellow);
                bastFeatureLayer.setRenderer(rendererYellow);

                SimpleFillSymbol fillsymbolViolet = new SimpleFillSymbol();
                fillsymbolViolet.setColor(Color.parseColor("#667C5295"));
                SimpleRenderer rendererViolet = new SimpleRenderer();
                rendererViolet.setSymbol(fillsymbolViolet);
                rezervatasFeatureLayer.setRenderer(rendererViolet);

                SimpleFillSymbol fillsymbolOrange = new SimpleFillSymbol();
                fillsymbolOrange.setColor(Color.parseColor("#661261A0"));
               // fillsymbolOrange.setColor(Color.parseColor("#66FB8B23"));
                SimpleRenderer rendererOrange = new SimpleRenderer();
                rendererOrange.setSymbol(fillsymbolOrange);
                draustinisFeatureLayer.setRenderer(rendererOrange);

                String table = uredijaFeatureLayer.getFeatureTable().getLayer().getAttribution();
                Log.d(TAG, "Feature table: " + table);
                girininkijaFeatureLayer.setMinScale(1000000);
                girininkijaFeatureLayer.setMaxScale(60000);
                uredijaFeatureLayer.setMaxScale(1000000);
                kvartalasFeatureLayer.setMinScale(60000);
                kvartalasFeatureLayer.setMaxScale(20000);
                bastFeatureLayer.setMinScale(60000);
                pastFeatureLayer.setMinScale(60000);
                draustinisFeatureLayer.setMinScale(60000);
               // rezervatasFeatureLayer.setMinScale(1000000);
                sklypasFeatureLayer.setMinScale(20000);
                miskuPogrupiaiFeatureLayer.setMinScale(20000);
                valstybiniaiMiskaiFeatureLayer.setMinScale(20000);
                leidimaiFeatureLayer.setMinScale(60000);
                //bastFeatureLayer.setVisible(false);
                rezervatasFeatureLayer.setMinScale(60000);


                mOperationalLayers = mMap.getOperationalLayers();
                mOperationalLayers.add(uredijaFeatureLayer);
                mOperationalLayers.add(girininkijaFeatureLayer);
                mOperationalLayers.add(kvartalasFeatureLayer);
                mOperationalLayers.add(bastFeatureLayer);
                mOperationalLayers.add(pastFeatureLayer);
                mOperationalLayers.add(sklypasFeatureLayer);
                mOperationalLayers.add(miskuPogrupiaiFeatureLayer);
                mOperationalLayers.add(valstybiniaiMiskaiFeatureLayer);
                mOperationalLayers.add(leidimaiFeatureLayer);
                mOperationalLayers.add(draustinisFeatureLayer);
                mOperationalLayers.add(rezervatasFeatureLayer);


                mMapView.setMap(mMap);

                String name = mMap.getOperationalLayers().get(0).getName();


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
                                // get the identify results from the future - returns when the operation is complete
                                List<IdentifyLayerResult> identifyLayersResults = identifyFuture.get();
                                //Log.d("mmm", "identifyLayersResults.size(): " + identifyFuture.get().size() );
                               // Log.d("mmm", "identifyLayersResults.size(): " + identifyLayersResults.get(1).getLayerContent().getName() );
                                //Log.d("mmm", "attributes " + identifyLayersResults.get(1).getElements().get(0).getAttributes().toString());


                            //    String muKod = identifyLayersResults.get(0).getElements().get(0).getAttributes().toString();
                                //Log.d("attributes", identifyLayersResults.get(0).getLayerContent().getName());
                                //Log.d("attributes: ", muKod );

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
                                    Toast.makeText(MainActivity.this, "Pasirinkite sklypą", Toast.LENGTH_LONG).show();
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

                                for(int i = 1; i<= identifyFuture.get().size();i++){
                                    if(identifyFuture.get().size() >= i){
                                        Log.d("index", String.valueOf(i));
                                        if(identifyLayersResults.get(i-1).getLayerContent().getName().equals("sklypas")){
                                            Log.d("kvartalas","2");
                                            kvartalas[0] = identifyLayersResults.get(i-1).getElements().get(0).getAttributes().get("mu_kod").toString();
                                            kvartalas[1] = identifyLayersResults.get(i-1).getElements().get(0).getAttributes().get("gir_kod").toString();
                                            kvartalas[2] = identifyLayersResults.get(i-1).getElements().get(0).getAttributes().get("kv_nr").toString();
                                            kvartalas[3] = identifyLayersResults.get(i-1).getElements().get(0).getAttributes().get("skl_nr").toString();
                                            leidimai = getLaws(kvartalas[0],kvartalas[1],kvartalas[2],kvartalas[3]);
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
                                    Toast.makeText(MainActivity.this, "Pasirinkite sklypą", Toast.LENGTH_LONG).show();
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
                Toast.makeText(MainActivity.this, "GeoPackage failed to load! " + geoPackage.getLoadError(), Toast.LENGTH_LONG).show();
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
        String[] reqPermission = new String[] { Manifest.permission.READ_EXTERNAL_STORAGE };
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


    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGeoPackage(mMap);
        } else {
            // report to user that permission was denied
            Toast.makeText(MainActivity.this,
                    getResources().getString(R.string.geopackage_read_permission_denied), Toast.LENGTH_SHORT).show();
        }
    }

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