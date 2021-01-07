package com.example.androidarcgis;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class SettingsActivity extends AppCompatActivity {

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setUpListView();

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
                        return true;
                    case R.id.layers:
                        startActivity(new Intent(getApplicationContext(),MyLayers.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });
    }

    private void setUpListView(){
        listView = (ListView) findViewById(R.id.listView);

        String [] settings = {"Mano sluoksniai","Išsaugoti pasirinktą teritoriją", "Išsaugoti visą teritoriją"};
        int[] images = { R.drawable.layers, R.drawable.selectmap, R.drawable.globe};

        MyAdapater myAdapater = new MyAdapater(getApplicationContext(), settings,images);
        listView.setAdapter(myAdapater);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i){
                    case 0:
                        startActivity(new Intent(getApplicationContext(),MyLayers.class));
                        break;
                    case 1:
                        startActivity(new Intent(getApplicationContext(),MapSelection.class));
                        break;
                    case 2:
                        String[] parameters = new String[3];
                        parameters[0]= getResources().getString(R.string.http) + getResources().getString(R.string.ip) + "/getAllLayers/";
                        parameters[1]= "/layers.gpkg";
                        new com.example.androidarcgis.services.DownloadManager(SettingsActivity.this).execute(parameters);
                        break;
                }
            }
        });

    }

    class MyAdapater extends ArrayAdapter<String> {

        Context context;
        String[] settings;
        int[] images;
        MyAdapater(Context c, String[] settings, int[] images){
            super(c,R.layout.settings_line, R.id.lineText, settings);
            this.context = c;
            this.settings = settings;
            this.images = images;

        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = layoutInflater.inflate(R.layout.settings_line, parent, false);
            ImageView image = row.findViewById(R.id.lineImage);
            TextView text = row.findViewById(R.id.lineText);
            image.setImageResource(images[position]);
            text.setText(settings[position]);
            return row;
        }
    }
}
