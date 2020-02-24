package it.saezzt.avisalbissole.util;

import com.google.firebase.database.Exclude;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class dbEvent implements Comparable {
    public Long type;
    public String title;
    public Date date;
    public String where;
    public String info;
    public Long duration;

    public dbEvent(){
        // Default constructor required for calls to DataSnapshot.getValue(dbEvent.class)
        this.type = null;
        this.title = null;
        this.date = null;
        this.where = null;
        this.info = null;
        this.duration = null;
    }

    public dbEvent(Long type, String title, Date date, String where, String info, Long duration){
        this.type = type;
        this.title = title;
        this.date = date;
        this.where = where;
        this.info = info;
        this.duration = duration;
    }

    public dbEvent(dbEvent e){
        this.type = e.type;
        this.title = e.title;
        this.date = e.date;
        this.where = e.where;
        this.info = e.info;
        this.duration = e.duration;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("tipo", type);
        result.put("titolo", title);
        result.put("data", date);
        result.put("dove", where);
        result.put("info", info);
        result.put("durata", duration);

        return result;
    }

    @Override
    public int compareTo(Object o) {
        Long comparedDate =((dbEvent)o).date.getTime();

        if(this.date.getTime()>comparedDate) return 1;
        else if(this.date.getTime()<comparedDate) return -1;
        return 0;
    }
}
