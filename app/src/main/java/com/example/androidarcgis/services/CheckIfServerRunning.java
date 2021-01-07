package com.example.androidarcgis.services;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.androidarcgis.MainActivity;
import com.example.androidarcgis.MapSelection;
import com.example.androidarcgis.R;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class CheckIfServerRunning extends AsyncTask<String, Void, String> {

    private Context context;
    private int status = 0;

    public CheckIfServerRunning(Context mContext) {
        context = mContext;
    }

    protected String doInBackground(String... urls) {
        ConnectivityManager connMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connMan.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            try {
                URL urlServer = new URL(context.getResources().getString(R.string.http) + context.getString(R.string.ip) + "/getAllLayers/");
                HttpURLConnection urlConn = (HttpURLConnection) urlServer.openConnection();
                urlConn.setConnectTimeout(3000); //<- 3Seconds Timeout
                urlConn.connect();
                if (urlConn.getResponseCode() == 200) {
                    status = 1;
                } else {
                    return "error";
                }
            } catch (Exception e1) {
                status = 2;
                return "error";
            }
        }
        return null;
    }

    protected void onPostExecute(String file_url) {
        if(status == 2 ){
            Toast.makeText(context,"Nepavyko pasiekti serverio!", Toast.LENGTH_LONG).show();
        }
    }
}