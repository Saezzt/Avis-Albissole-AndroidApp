package it.saezzt.avisalbissole.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DataCompare {
    //confronto tra date. Mode serve a decidere se confrontare il giorno completo o il singolo giorno, mese, anno. 0,1,2,3
    //ritorna 0 se uguali. 1 per il primo maggiore, 2 per il secondo.
    public int greaterThan(Date first, Date second, int mode){
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"));
        cal.setTime(first);
        int fYear = cal.get(Calendar.YEAR);
        int fMonth = cal.get(Calendar.MONTH);
        int fDay = cal.get(Calendar.DAY_OF_MONTH);
        cal.setTime(second);
        int sYear = cal.get(Calendar.YEAR);
        int sMonth = cal.get(Calendar.MONTH);
        int sDay = cal.get(Calendar.DAY_OF_MONTH);
        switch (mode){
            case 0:{

                break;
            }
            case 1:{
                return compare(fDay,sDay);
            }
            case 2:{
                return compare(fMonth,sMonth);
            }
            case 3:{
                return compare(fYear,sYear);
            }
        }
        return -1;
    }
    public int greater(Date date, int mode){
        return  greaterThan(date,new Date(System.currentTimeMillis()), mode);
    }
    private int compare(int f, int s){
        if(f==s)return 0;
        else if(f>s) return 1;
        else return  2;
    }
}
