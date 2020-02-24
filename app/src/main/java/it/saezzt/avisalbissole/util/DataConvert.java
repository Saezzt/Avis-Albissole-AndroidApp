package it.saezzt.avisalbissole.util;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DataConvert {

    private SimpleDateFormat formatter = new SimpleDateFormat("dd/MMMMMMMMM/yyyy/HH/mm");
    private static final String TAG = "DataConvert";

    public Date dataConvert(String dateInString) {
        //TODO da gestire in caso di mese errato da DB? Al momento crasha segnalando l'errore come da catch.Il crash avviene nel caso si svolga tutto l'arraylist (l'errore genera un null pointer)
        try {

            Date date = formatter.parse(dateInString);
            return date;

        } catch (ParseException e) {
            Log.e(TAG, String.valueOf(e));
            return null;
        }
    }

    public ArrayList<Date> multiDataConvert(ArrayList<String> arrayDateInString) {
        ArrayList<Date> arrayDate = new ArrayList();
        for (String dateInString : arrayDateInString) {
            Date date = dataConvert(dateInString);
            if (date != null) arrayDate.add(date);
        }
        return arrayDate;
    }
}