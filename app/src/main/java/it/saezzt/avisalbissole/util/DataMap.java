package it.saezzt.avisalbissole.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DataMap {
    private ArrayList<String> Date = new ArrayList();

    public DataMap(){
        super();
    }

    public DataMap(HashMap<String, String> map) {
        super();
        for (Map.Entry<String,String> entry: map.entrySet()){
            Date.add(entry.getValue());
        }
    }

    public void DataMap(String date) {
        Date.add(date);
    }

    public ArrayList<String> getDate() {
        return Date;
    }
}
