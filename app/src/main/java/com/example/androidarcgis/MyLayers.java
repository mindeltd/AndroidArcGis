package com.example.androidarcgis;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.example.androidarcgis.models.LayerSettings;
import com.example.androidarcgis.services.DatabaseService;
import com.example.androidarcgis.services.LayerStyleService;
import com.google.android.material.bottomnavigation.BottomNavigationView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MyLayers extends AppCompatActivity {
    DatabaseService databaseService;
    LayerSettings [] layers;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_layers);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setSelectedItemId(R.id.layers);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.settings:
                        startActivity(new Intent(getApplicationContext(),SettingsActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.map:
                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.layers:
                        return true;

                }
                return false;
            }
        });


        ListView listView;
        databaseService = new DatabaseService(getApplicationContext());
        listView = findViewById(R.id.listView);
        //layers = new LayerSettings[10];
        layers = databaseService.getLayerSettings();

        MyLayers.MyAdapater adapater = new MyLayers.MyAdapater(this, layers, getLayerNames(layers));
        listView.setAdapter(adapater);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView textView1 = (TextView) view.findViewById(R.id.textView1);
                TextView textView2 = view.findViewById(R.id.textView2);
                databaseService.toggleLayerActivation(textView1.getText().toString());
                if(textView2.getText().toString().equals("Aktyvuotas")){
                    textView2.setText("Deaktyvuotas");
                    layers[i].setActive(2);
                }
                else{
                    textView2.setText("Aktyvuotas");
                    layers[i].setActive(1);
                }

            }
        });
    }


    private String[] getLayerNames(LayerSettings[] layers){
        String [] layerNames = new String[layers.length];
        for(int i = 0; i< layers.length; i++){
            layerNames[i] = layers[i].getLayerName();
        }
        return layerNames;
    }


    class MyAdapater extends ArrayAdapter<String> {

        Context context;
        LayerSettings layerSettings [];
        String[] layerNames;
        MyAdapater(Context c, LayerSettings[] layerSettings, String [] layerNames){
            super(c,R.layout.row, R.id.textView1, layerNames);
            this.context = c;
            this.layerNames = layerNames;
            this.layerSettings = layerSettings;

        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = layoutInflater.inflate(R.layout.row, parent, false);
            ImageView images = row.findViewById(R.id.image);
            TextView myTitle = row.findViewById(R.id.textView1);
            TextView myDescription = row.findViewById(R.id.textView2);
            images.setImageResource(LayerStyleService.getLayerDrawable(String.valueOf(layerSettings[position].getColor())));
            myTitle.setText(layerSettings[position].getLayerName());
            if(layerSettings[position].getActive() == 1){
                myDescription.setText("Aktyvuotas");
            }
            else{
                myDescription.setText("Deaktyvuotas");
            }
            return row;
        }
    }

}
