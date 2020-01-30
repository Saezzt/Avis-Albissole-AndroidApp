package it.saezzt.avisalbissole;

import androidx.lifecycle.ViewModel;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static java.lang.System.currentTimeMillis;

public class AmbuViewModel extends ViewModel {
    //private MutableLiveData<Date> donazioni;
    private List<Date> Donazioni = new LinkedList<>();

    /*public LiveData<Date> getDonazioni(){
        if (donazioni == null) {
            donazioni = new MutableLiveData<Date>();
            loadDonazioni();
        }
        return  donazioni;
    }*/

    private void loadDonazioni() {
        // Do an asynchronous operation to fetch dates.
        for (int k=0; k<12; k++){
            Date date = new Date(currentTimeMillis()+86400000*k);
            Donazioni.add(date);
        }
    }

    public List<Date> getDonazioni(){
        if (Donazioni == null) {
            loadDonazioni();
        }
        return Donazioni;
    }

}
