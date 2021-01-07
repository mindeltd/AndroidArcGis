package com.example.androidarcgis.services;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.example.androidarcgis.MainActivity;
import com.example.androidarcgis.MapSelection;
import com.example.androidarcgis.SettingsActivity;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class DownloadManager extends AsyncTask<String, Integer, Long>
{

    private Drawable d;
    private HttpURLConnection conn;
    private InputStream stream; //to read
    private ByteArrayOutputStream out; //to write
    private Context mCtx;
    private String[] mUrls;

    private double fileSize;
    private double downloaded; // number of bytes downloaded
    private int status = DOWNLOADING; //status of current process

    private ProgressDialog progressDialog;

    private static final int MAX_BUFFER_SIZE = 1024; //1kb
    private static final int DOWNLOADING = 0;
    private static final int COMPLETE = 1;
    private int workingStatus;

    public DownloadManager(Context ctx)
    {
        d          = null;
        conn       = null;
        fileSize   = 0;
        downloaded = 0;
        status     = DOWNLOADING;
        mCtx       = ctx;
    }

    public boolean isOnline()
    {
        try
        {
            ConnectivityManager cm = (ConnectivityManager)mCtx.getSystemService(Context.CONNECTIVITY_SERVICE);
            return cm.getActiveNetworkInfo().isConnectedOrConnecting();
        }
        catch (Exception e)
        {
            return false;
        }
    }

    @Override
    protected Long doInBackground(String... urls) {
        mUrls = urls;
        int count;
//        long total = 0;
        try{
            Log.v("downloaddata", "downloading data");
            Log.v("downloaddata", "urls[1]): " + urls[1]);

            URL url  = new URL(urls[0]);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(3000);
            connection.connect();

            int lenghtOfFile = connection.getContentLength();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream(),
                    8192);

            // Output stream
            File testDirectory = new File(Environment.getExternalStorageDirectory()+"/miskoInformacineSistema/");
            if(!testDirectory.exists()){
                testDirectory.mkdir();
            }

            FileOutputStream output = new FileOutputStream(testDirectory+ urls[1]);

            byte data[] = new byte[1024];

            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                // After this onProgressUpdate will be called
               publishProgress((int) ((total * 100) / lenghtOfFile));

                // writing data to file
                output.write(data, 0, count);
            }

            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();
        } catch (Exception e) {
            workingStatus = 2;
            return null;
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... changed)
    {
        progressDialog.setProgress(changed[0]);
    }

    @Override
    protected void onPreExecute()
    {
        progressDialog = new ProgressDialog(mCtx); // your activity
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage("Atsiunčiama ...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    protected void onPostExecute(Long result)
    {
        progressDialog.dismiss();
        if(workingStatus==2){
            Toast.makeText(mCtx,"Nepavyko pasiekti serverio!", Toast.LENGTH_LONG).show();
            //mCtx.startActivity(new Intent(mCtx, SettingsActivity.class));
            return;
        }

        else if(mUrls[1].equals("/layers.gpkg")){
            DatabaseService databaseService = new DatabaseService(mCtx);
            if(!databaseService.checkIfLayerSettingsExist()){
                databaseService.saveLayerSettings();
            }
            triggerRebirth(mCtx,MainActivity.class);

        }

        mCtx.startActivity(new Intent(mCtx, MainActivity.class));

        // do something
    }


    public static void triggerRebirth(Context context, Class myClass) {
        Intent intent = new Intent(context, myClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        Runtime.getRuntime().exit(0);
    }
}