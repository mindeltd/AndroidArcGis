package com.example.androidarcgis;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.zip.Inflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BottomSheetDialog extends BottomSheetDialogFragment {

    String[] sklypas = new String[10];
    String[] bast = new String[10];
    String[] past = new String[10];
    String draustinioPavadinimas;
    String rezervatoPavadinimas;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_sheet_layout, container, false);
        sklypas = getArguments().getStringArray("sklypas");
        bast = getArguments().getStringArray("bast");
        past = getArguments().getStringArray("past");
        draustinioPavadinimas = getArguments().getString("draustinis");
        rezervatoPavadinimas = getArguments().getString("rezervatas");
        String [] draudimai = new String[50];
        draudimai = getArguments().getStringArray("draudimai");
        Log.d("testas","sklypas[0]: " + sklypas[0] + "sklypas[1]: " + sklypas[1] + "sklypas[2]: " + sklypas[2]);
        TextView uredija = (TextView) v.findViewById(R.id.uredija);
        //uredija.append(sklypas[0]);
        TextView kvartalas = (TextView) v.findViewById(R.id.kvartalas);
        TextView miskoTipas = (TextView) v.findViewById(R.id.miskoTipas);
        TextView girininkija = (TextView) v.findViewById(R.id.girininkija);
        TextView sklypoNr = (TextView) v.findViewById(R.id.sklypoNr);



        if(sklypas != null){
            appendColoredText(uredija, sklypas[5],Color.parseColor("#FFFFFF"));
            //girininkija.append(sklypas[1]);
            appendColoredText(girininkija, sklypas[6],Color.parseColor("#FFFFFF"));
            appendColoredText(kvartalas, sklypas[2],Color.parseColor("#FFFFFF"));
            //kvartalas.append(sklypas[2]);
            //sklypoNr.append(sklypas[4]);
            appendColoredText(sklypoNr, sklypas[4],Color.parseColor("#FFFFFF"));
        }


        if(getArguments().getBoolean("valstybinis") == true){
           // miskoTipas.append("Valstybinis");
            appendColoredText(miskoTipas, "Valstybinis",Color.parseColor("#FFFFFF"));
        }
        else{
            appendColoredText(miskoTipas, "Privatus",Color.parseColor("#FFFFFF"));
        }
        TextView miskoGrupe = (TextView) v.findViewById(R.id.miskoGrupe);
        if(!getArguments().getString("grupe").equals("")){
            Log.d("testas", "grupe.length: " + getArguments().getString("grupe").length() );
           // miskoGrupe.append(getArguments().getString("grupe"));
            appendColoredText(miskoGrupe, getArguments().getString("grupe") ,Color.parseColor("#FFFFFF"));
        }
        TextView leidimai = (TextView) v.findViewById(R.id.kirtimai);
        if(getArguments().getStringArray("leidimai")[0] != null ){
            for(int i = 0; i < getArguments().getStringArray("leidimai").length;i++){
                if(getArguments().getStringArray("leidimai")[i] != null){
                    //leidimai.append("\n" + getArguments().getStringArray("leidimai")[i]);
                    appendColoredText(leidimai,"\n" + getArguments().getStringArray("leidimai")[i] ,Color.parseColor("#FFFFFF"));
                }
            }
        }
        else{
            leidimai.setVisibility(View.GONE);
        }

        TextView bastTipas = (TextView) v.findViewById(R.id.bastTipas);
        if(getArguments().getStringArray("bast")[0] != null){
            //saugomaTeritorija.append(getArguments().getStringArray("bast")[1]);
            appendColoredText(bastTipas, getArguments().getStringArray("bast")[1] ,Color.parseColor("#FFFFFF"));
            // appendColoredText(bastReglamentas, getArguments().getStringArray("bast")[2] ,Color.parseColor("#FFFFFF"));
        }
        else{
            bastTipas.setVisibility(View.GONE);
        }
        TextView pastTipas = (TextView) v.findViewById(R.id.pastTipas);
        if(getArguments().getStringArray("past")[0] != null){
            appendColoredText(pastTipas, getArguments().getStringArray("past")[1] ,Color.parseColor("#FFFFFF"));
        }
        else{
            pastTipas.setVisibility(View.GONE);
        }
        TextView draustinioTipas = (TextView) v.findViewById(R.id.draustinioTipas);
        if(!draustinioPavadinimas.equals("")){
            appendColoredText(draustinioTipas,draustinioPavadinimas,Color.parseColor("#FFFFFF"));
        }
        else{
            draustinioTipas.setVisibility(View.GONE);
        }
        TextView rezervatoTipas = (TextView) v.findViewById(R.id.rezervatoTipas);
        if(!rezervatoPavadinimas.equals("")){
            appendColoredText(rezervatoTipas,rezervatoPavadinimas,Color.parseColor("#FFFFFF"));
        }
        else{
            rezervatoTipas.setVisibility(View.GONE);
        }

        if(rezervatoPavadinimas.equals("")
                && draustinioPavadinimas.equals("")
                && getArguments().getStringArray("past")[0] == null
                && getArguments().getStringArray("bast")[0] == null
        ){
            TextView saugomosTeritorijos = (TextView) v.findViewById(R.id.saugomosTeritorijos);
            saugomosTeritorijos.setVisibility(View.GONE);
        }
        TextView draudimaiTextView = (TextView) v.findViewById(R.id.draudimai);
        if(draudimai[0] != null){
            for(int i = 0; i < draudimai.length; i++){
                if(draudimai[i] != null){
                    appendColoredText(draudimaiTextView,"\n" + draudimai[i] + "\n" ,Color.parseColor("#FFFFFF"));
                }
                else{
                    break;
                }
            }
        }
        else{
            draudimaiTextView.setVisibility(View.GONE);
        }
        TextView leidimaiTextView = (TextView) v.findViewById(R.id.leidimai);
        if(draudimai[0] == null && getArguments().getStringArray("leidimai")[0] == null){
            leidimaiTextView.setVisibility(View.GONE);
        }






        return v;
    }
    public static void appendColoredText(TextView tv, String text, int color) {
        int start = tv.getText().length();
        tv.append(text);
        int end = tv.getText().length();

        Spannable spannableText = (Spannable) tv.getText();
        spannableText.setSpan(new ForegroundColorSpan(color), start, end, 0);
    }
}
