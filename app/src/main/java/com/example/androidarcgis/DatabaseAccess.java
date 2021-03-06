package com.example.androidarcgis;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import android.content.ContentValues;

import com.example.androidarcgis.models.LayerSettings;

import org.json.JSONException;
import org.json.JSONObject;


public class DatabaseAccess {
    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase db;
    private String name;
    private String[] coordinates;
    private static DatabaseAccess instance;
    Cursor c = null;
    ContentValues cv;

    private DatabaseAccess(Context context) {
        this.openHelper = new DatabaseOpenHelper(context);
    }

    public static DatabaseAccess getInstance(Context context){
        if(instance == null){
            instance = new DatabaseAccess(context);
        }
        return  instance;
    }

    public void open(){
        this.db = openHelper.getWritableDatabase();
    }

    public void close(){
        if(db!=null)
            this.db.close();
    }
    public String getName(){
        c = db.rawQuery("select pavadinimas from Draudimai", new String []{});
        StringBuffer buffer = new StringBuffer();
        while (c.moveToNext()){
            name = c.getString(0);
            buffer.append(""+name);
        }
        // return buffer.toString();
        return name;
    }
    public String getUredijaName(String mu_kod){
        String pavadinimas = "";
        c = db.rawQuery("select pavadinimas from Uredija where mu_kod = ?", new String[]{mu_kod});
        while (c.moveToNext()){
            pavadinimas = c.getString(0);
        }
        return pavadinimas;
    }
    public String getGirininkijaName(String gir_kod, String mu_kod){
        String pavadinimas = "";
        c = db.rawQuery("select pavadinimas from Girininkija where gir_kod = ? and mu_kod = ?", new String[]{gir_kod, mu_kod});
        while (c.moveToNext()){
            pavadinimas = c.getString(0);
        }
        return pavadinimas;
    }

    public String[] getCoordinates(){
        coordinates = new String [100];
        c = db.rawQuery("select st_astext from uredija where substr(st_astext, 0 ,length(st_astext)-3) not like \"%)%\"", new String []{});
        StringBuffer buffer = new StringBuffer();
        int i = 0;
        while (c.moveToNext()){
            String temp = c.getString(0);
            temp = temp.substring(15,temp.length());
            temp = temp.substring(0,temp.length() - 3);
            coordinates[i] = temp;
            i++;
            //  buffer.append(""+name);
        }
        // return buffer.toString();
        return coordinates;
    }
    public String[] getLawsFromDatabase(String uredija,String girininkija, String kvartalas, String sklypas){
        String [] laws = new String[50];
        c = db.rawQuery("select KirtimoRusis from leidimai where mu_kod = ? and gir_kod= ? and kv_nr = ?", new String[]{uredija,girininkija,kvartalas});
        StringBuffer buffer = new StringBuffer();
        int i = 0;
        while (c.moveToNext()){
            name = c.getString(0);
            if(!checkIfLawInArray(laws,c.getString(0))){
                laws[i] = name;
                i++;
            }
            buffer.append(""+name);
        }
       // laws[0] = name;
        return laws;
    }
    public boolean checkIfLawInArray(String[] array, String law){
        int count = 0;
        for(int i =0;i<array.length;i++){
            if(array[i]!= null && law != null){
                if(array[i].equals(law)){
                    count++;
                }
            }
        }
        if(count ==0){
            return false;
        }
        else return true;
    }
    public String[] getRestrictions(String pgrupe, String prezervatas, String pdraustinis,String pvalstybinis, String pbast, String ppast){
        String nuo;
        String iki;
        String [] restrictions = new String[50];
        c = db.rawQuery("select draudimas, nuo, iki from Draudimai " +
                "where (grupe = ? or grupe is null)" +
                "and (rezervatas is null or rezervatas = ?) " +
                "and (draustinis is null or draustinis = ?) " +
                "and (valstybinis is null or valstybinis = ?) " +
                "and (bast is null or bast = ?) " +
                "and (past is null or past = ? )", new String[]{pgrupe, prezervatas, pdraustinis, pvalstybinis, pbast, ppast});
        int i = 0;
        while (c.moveToNext()){
            name = c.getString(0);
            if(c.getString(1) != null){
                if(checkDate(c.getString(1),c.getString(2))){
                    restrictions[i] = name;
                }
            }
            else{
                restrictions[i] = name;
            }
            Log.d("draudimai", i + ":  " + name);
            i++;
        }
        Log.d("date", "check date nuo 01-01 iki 02-02:  " + checkDate("01-01","02-02"));
        return restrictions;
    }
    public boolean checkDate  (String nuo, String iki){
        Date dateNuo= null;
        Date dateIki= null;
        Date today = Calendar.getInstance().getTime();
        String dateStringNuo = String.valueOf(Calendar.getInstance().get(Calendar.YEAR)) + '-'+nuo;
        String dateStringIki = String.valueOf(Calendar.getInstance().get(Calendar.YEAR)) + '-'+iki;


        try {
            dateNuo = new SimpleDateFormat("yyyy-MM-dd").parse(dateStringNuo);
            dateIki = new SimpleDateFormat("yyyy-MM-dd").parse(dateStringIki);
            Log.d("date nuo", dateNuo.toString());
            Log.d("date iki", dateIki.toString());
            if(today.after(dateNuo) && today.before(dateIki)){
                return true;
            }
            else{
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return true;
    }
    public boolean isLayerActive(String layerName){
        String isActive = null;
        c = db.rawQuery("select Active from Settings where layerName = ?", new String[]{layerName});
        while (c.moveToNext()){
            isActive = c.getString(0);
        }
        if(isActive != null){
            return isActive.equals("1");
        }
        else{
            return false;
        }
    }

    public int[] getLayerScales(String layerName){
        int[] scales = new int[2];
        c = db.rawQuery("select MaxScale, MinScale from Settings where layerName = ?", new String[]{layerName});
        while (c.moveToNext()){
            Log.d("LayerScales: ", "1:   " + c.getString(0) );
            Log.d("LayerScales: ","2:  " +  c.getString(1) );

            if(c.getString(0) != null){
                scales[0] = Integer.parseInt(c.getString(0));
            }
            if(!c.getString(1).equals("")){
                scales[1] = Integer.parseInt(c.getString(1));
            }
        }
        return scales;
    }
    public void toggleLayerActivation(String layerName){
        cv = new ContentValues();
        c = db.rawQuery("select * from Settings where layerName = ?", new String[]{layerName});
        Log.d("toggleLayerActivation", "cursor count: " + c.getCount());
        while (c.moveToNext()){
            cv.put("LayerName",c.getString(1));
            cv.put("MaxScale",c.getString(2));
            cv.put("MinScale",c.getString(3));
            if(c.getString(4).equals("1")){
                cv.put("Active","2");
            }
            else{
                cv.put("Active","1");
            }
            cv.put("Color", c.getString(5));
            db.update("Settings", cv, "layerName = ?", new String[]{layerName});
        }

//        cv = new ContentValues();
//
//        cv.put("Id",100);
//        cv.put("layerName","Sklypas");
//        //cv.put("RecommendedMinScale", 20000);
//        cv.put("MinScale", 20000);
//        cv.put("Active","2");
//        Log.d("ToggleLayerActivationn", "databaseAccess class");
//        //c = db.rawQuery("update Settings set Active = ? where layerName = ?", new String[]{"2",layerName});
//        //db.execSQL("update Settings set Active = ? where layerName = ?", new String[]{"2",layerName});
//        db.update("Settings", cv, "layerName = ?", new String[]{layerName});
    }

    void insertSettings(){
        boolean createSuccessful = false;
        cv = new ContentValues();
        //cv.put("Id",100);
        cv.put("layerName","555555");
        //cv.put("RecommendedMinScale", 5555555);
        cv.put("MinScale", 5555555);
        cv.put("Active","555");
        createSuccessful = db.insert("Settings", null, cv) > 0;
        Log.d("DatabaseSettings", "createSuccessful: "  + createSuccessful);
    }
    void getAllSettings(){
        c = db.rawQuery("select * from Settings", new String[]{});
        while (c.moveToNext()){
            Log.d("DatabaseSettings", "cursor: " + c.getString(0) + " " + c.getString(1) + " " + c.getString(2));
        }
    }

    public void saveLayersToDatabase(JSONObject layer){
        ContentValues cv = new ContentValues();
        try {
            cv.put("layerName", layer.get("layerName").toString());
            cv.put("MaxScale", layer.get("maxScale").toString());
            cv.put("MinScale", layer.get("minScale").toString());
            cv.put("Active","1");
            cv.put("Color",layer.get("color").toString());
//            cv.put("layerName","Sklypas");
//            cv.put("RecommendedMinScale", 20000);
//            cv.put("MinScale", 20000);
//            cv.put("Active","2");
            Log.d("ToggleLayerActivationn", "databaseAccess class");
            //c = db.rawQuery("update Settings set Active = ? where layerName = ?", new String[]{"2",layerName});
            //db.execSQL("update Settings set Active = ? where layerName = ?", new String[]{"2",layerName});
            boolean createSuccessful = db.insert("Settings", null, cv) > 0;
            Log.d("SaveLayerSettings", "createSuccessful: " + createSuccessful);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public LayerSettings[] getLayersSettings(){
        Log.d("getlayersettings", "DatabaseAccess - > getLayersSettings" );
        c = db.rawQuery("select * from Settings", new String[]{});
        Log.d("getlayersettings", "cursor count: " + c.getCount());
        Log.d("getlayersettings", "cursor column count: " + c.getColumnCount());
        LayerSettings [] layers = new LayerSettings[c.getCount()];
        int i = 0;
        while (c.moveToNext()){
            Log.d("getlayersettings", "cursor row: " + c.getInt(2) + " " + c.getInt(3) + " " + c.getInt(4)+ " " + c.getInt(5));
            layers[i] = new LayerSettings(
                    c.getString(1),
                    c.getInt(2),
                    c.getInt(3),
                    c.getInt(4),
                    c.getInt(5)
            );
            i++;
        }
        return layers;
    }

    private int scaleToInt(Cursor cursor, int index){
        int intScale = 0;
        if(cursor.getInt(index) == 0){
            //intScale = Integer.valueOf(scale);
        }
        return intScale;
    }

    public boolean checkIfLayerSettingsExist(){
        c = db.rawQuery("select * from Settings", new String[]{});
        if(c.getCount() > 1){
            return true;
        }
        return false;
    }

    public String getColorId(String layerName){
        c = db.rawQuery("select Color from Settings where layerName = ?", new String[]{layerName});
        while (c.moveToNext()){
            return c.getString(0);
        }
        return "1";

    }

//    private Cursor clearCursorUndefinedValues(Cursor c){
//        for(int i = 0; i< c.getColumnCount(); i++){
//            if(c.getString(i).equals("")){
//            }
//        }
//    }
//
//    private void scaleToInt(){
//        scaleToInt("");
//    }

}
