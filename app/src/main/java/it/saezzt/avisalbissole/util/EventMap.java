package it.saezzt.avisalbissole.util;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class EventMap {
    private ArrayList<dbEvent> dbEvents = new ArrayList();
    private final String data = "data";
    private final String dove = "dove";
    private final String info = "info";
    private final String durata = "durata";
    private final String tipo = "tipo";
    private final String titolo = "titolo";
    private  final  String TAG = "EventMap";

    public EventMap(){
        super();
    }

    public EventMap(@NonNull DataSnapshot dataSnapshot){
        if (dataSnapshot.exists()){
            HashMap<String, Object> dataMap = (HashMap<String, Object>) dataSnapshot.getValue();
            DataConvert dataConvert = new DataConvert();
            for (String key : dataMap.keySet()){
                Object dataO = dataMap.get(key);
                try {

                    HashMap<String, Object> eventData = (HashMap<String, Object>) dataO;
                    dbEvents.add(new dbEvent((Double) eventData.get(tipo), (String) eventData.get(titolo), dataConvert.dataConvert((String) eventData.get(data)), (String) eventData.get(dove), (String) eventData.get(info), (Long) eventData.get(durata)));
                    //Log.i(TAG,eventData.get(tipo).toString()+" "+eventData.get(titolo)+" "+eventData.get(data)+" "+eventData.get(dove)+" "+eventData.get(info)+" "+eventData.get(durata));
                    Log.i(TAG,dbEvents.get(dbEvents.size()-1).toString());
                    Log.i(TAG,"giroCompleto");
                }catch (Exception e){
                    Log.e(TAG,e.getMessage());
                }
            }
        }
    }
    public ArrayList<dbEvent> getDbEvents(){
        return dbEvents;
    }
}
