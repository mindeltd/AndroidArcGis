package com.example.androidarcgis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DownloadMap extends AppCompatActivity {
    TextView header;
    TextView column;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_map);


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setSelectedItemId(R.id.map);

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

        Button downloadButton = (Button) findViewById(R.id.downloadButton);

        String layers = getIntent().getStringExtra("layers");

        header = findViewById(R.id.header);
        column = findViewById(R.id.column);

        if(layers!=null){
            if(layers.equals("true")){
                header.setText("Neturite sluoksnių!");
                column.setText("Atsisiųsti sluoksnius galite paspaudę vieną iš žemiau esančių mygtukų");
                downloadButton.setText("Atsisiųsti visos teritorijos sluoksnius");
                downloadButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        download(getResources().getString(R.string.http) + getResources().getString(R.string.ip) + "/getAllLayers/", "/layers.gpkg");
                    }
                });
                Button selectLayersButton = findViewById(R.id.selectLayersButton);
                selectLayersButton.setVisibility(View.VISIBLE);
                selectLayersButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(getApplicationContext(),MapSelection.class));
                    }
                });
            }
        }
        else{
            downloadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    download(getResources().getString(R.string.http) + getResources().getString(R.string.ip) + "/getMap/", "/map.mmpk");
                }
            });
        }


    }


    private void download(String url, String fileName){
        String [] parameters = new String[3];
        parameters[0] = url;
        parameters[1] = fileName;
        new com.example.androidarcgis.services.DownloadManager(DownloadMap.this).execute(parameters);
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
}
