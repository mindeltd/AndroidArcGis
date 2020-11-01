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


public class DatabaseAccess {
    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase db;
    private String name;
    private String[] coordinates;
    private static DatabaseAccess instance;
    Cursor c = null;

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
        c = db.rawQuery("select KirtimoRusis from leidimai where mu_kod = ? and gir_kod= ? and kv_nr = ? and skl_nr = ?", new String[]{uredija,girininkija,kvartalas,sklypas});
        StringBuffer buffer = new StringBuffer();
        int i = 0;
        while (c.moveToNext()){
            name = c.getString(0);
            laws[i] = name;
            i++;
            buffer.append(""+name);
        }
       // laws[0] = name;
        return laws;
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
}
