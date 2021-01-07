package com.example.androidarcgis.services;

import android.graphics.Color;
import android.graphics.drawable.Drawable;

import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;
import com.example.androidarcgis.R;

public class LayerStyleService {
    private static String styles [][] = {
            {"2", "#667C5295", "#7C5295", "0"},
            {"3", "#66ff0000", "#BF0000", "1"},
            {"4", "#661261A0", "#1261A0", "2"},
            {"5", "#66FF8C00", "#FF8C00", "3"},
            {"6", "#66555555", "#555555", "4"},
            {"7", "#66009900", "#009900", "5"},
            {"8", "#66BF0000", "#BF0000", "6"}
    };

    private static SimpleFillSymbol.Style enums [] = {
            SimpleFillSymbol.Style.BACKWARD_DIAGONAL,
            SimpleFillSymbol.Style.FORWARD_DIAGONAL,
            SimpleFillSymbol.Style.CROSS,
            SimpleFillSymbol.Style.HORIZONTAL,
            SimpleFillSymbol.Style.VERTICAL,
            SimpleFillSymbol.Style.DIAGONAL_CROSS,
            SimpleFillSymbol.Style.SOLID
    };

    public static SimpleRenderer getRenderer(String id){
        String [] style = getStyle(id);
        SimpleFillSymbol simpleFillSymbol = new SimpleFillSymbol();
        simpleFillSymbol.setColor(Color.parseColor(style[1]));
        simpleFillSymbol.setStyle(enums[Integer.parseInt(style[3])]);
        SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.parseColor(style[2]), 5);
        simpleFillSymbol.setOutline(lineSymbol);
        SimpleRenderer simpleRenderer = new SimpleRenderer();
        simpleRenderer.setSymbol(simpleFillSymbol);
        return simpleRenderer;
    }

    public static int getLayerDrawable(String colorId){
        switch (colorId){
            case "1":
                return R.drawable.one;
            case "2":
                return R.drawable.two;
            case "3":
                return R.drawable.three;
            case "4":
                return R.drawable.four;
            case "5":
                return R.drawable.five;
            case "6":
                return R.drawable.six;
            case "7":
                return R.drawable.seven;
            case "8":
                return R.drawable.eight;
        }
        return R.drawable.one;
    }



    private static String[] getStyle(String id){
        for(int i = 0; i< styles.length; i++){
            if(styles[i][0].equals(id)){
                return styles[i];
            }
        }
        return styles[0];
    }
}
