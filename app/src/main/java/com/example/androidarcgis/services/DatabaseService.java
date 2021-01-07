package com.example.androidarcgis.services;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.androidarcgis.DatabaseAccess;
import com.example.androidarcgis.R;
import com.example.androidarcgis.models.LayerSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DatabaseService {
    private DatabaseAccess databaseAccess;
    private Context mContext;

    public DatabaseService(Context context){
        this.mContext = context;
        databaseAccess = DatabaseAccess.getInstance(context);
        databaseAccess.open();
    }

    public LayerSettings[] getLayerSettings(){
        Log.d("getlayersettings", "DatabaseService.java -> getLayerSettings" );
        LayerSettings[] layers = databaseAccess.getLayersSettings();
        return layers;
    }

    public String getColorId(String layerName){
        return databaseAccess.getColorId(layerName);
    }

    public void toggleLayerActivation(String layerName){
        databaseAccess.toggleLayerActivation(layerName);
    }

    public void saveLayerSettings(){
//         Request a string response from the provided URL.
        Log.d("saveLayerSettings", "saveLayerSettings()");
        String url = String.format(mContext.getResources().getString(R.string.http) + mContext.getResources().getString(R.string.ip) + "/getLayersSettings");
        RequestQueue queue = Volley.newRequestQueue(mContext);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("saveLayerSettings", "onResponse");
                        JSONArray jsonArray;
                        try {
                            jsonArray = new JSONArray(response);
                            for(int i = 0; i<jsonArray.length(); i++){
                                Log.d("saveLayerSettings" ,"jsonArray.get(i): " + jsonArray.get(i));
                                JSONObject settingLayer = new JSONObject(jsonArray.get(i).toString());
                                databaseAccess.saveLayersToDatabase(settingLayer);
                            }

                        } catch (JSONException e) {
                            Log.d("saveLayerSettings", "JSONException: " + e.toString());
                            Log.d("httpRequest" ,"JSONException: " + e.toString());
                            e.printStackTrace();
                        }
                        Log.d("httpRequest" , "working, response: " + response.substring(0,response.length()-1) );
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("httpRequest" , "not working");
                Log.d("saveLayerSettings", "onErrorResponse: " + error.toString());
            }
        });

// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public boolean checkIfLayerSettingsExist(){
        return databaseAccess.checkIfLayerSettingsExist();
    }

    static public boolean isServerReachable(Context context) {
        ConnectivityManager connMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connMan.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            try {
                URL urlServer = new URL(context.getResources().getString(R.string.http) + context.getString(R.string.ip) + "/getAllLayers/");
                HttpURLConnection urlConn = (HttpURLConnection) urlServer.openConnection();
                urlConn.setConnectTimeout(3000); //<- 3Seconds Timeout
                urlConn.connect();
                if (urlConn.getResponseCode() == 200) {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e1) {
                Log.d("httpRequest", e1.toString());
                return false;
            }
//            catch (IOException e) {
//                return false;
//            }
        }
        return false;
    }


    public static boolean exists(Context context) {

//        try {
//            HttpURLConnection.setFollowRedirects(false);
//            // note : you may also need
//            // HttpURLConnection.setInstanceFollowRedirects(false)
//            HttpURLConnection con = (HttpURLConnection) new URL(context.getResources().getString(R.string.http) + context.getString(R.string.ip) + "/getAllLayers/")
//                    .openConnection();
//            con.setRequestMethod("HEAD");
//            return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }


        URL url = null;
        try {
            url = new URL(context.getResources().getString(R.string.http) + context.getString(R.string.ip) + "/getAllLayers/");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            int code = connection.getResponseCode();
            if(code == 200) {
               return true;
            }
        } catch (MalformedURLException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
        return true;

    }

}
