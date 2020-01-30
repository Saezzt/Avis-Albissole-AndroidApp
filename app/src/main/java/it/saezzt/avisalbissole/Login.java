package it.saezzt.avisalbissole;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import it.saezzt.avisalbissole.util.DataConvert;
import it.saezzt.avisalbissole.util.DataMap;

public class Login extends AppCompatActivity implements View.OnClickListener {
    GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "SignInActivity";
    private ArrayList<Date> Donazioni = new ArrayList<>();
    //private AmbuViewModel model;
    private DatabaseReference mFirebaseDatabaseReference;
    private static final String DB = "Donazioni";
    private DataMap date;
    Fragment fragment_loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Create a ViewModel the first time the system calls an activity's onCreate() method.
        // Re-created activities receive the same MyViewModel instance created by the first activity.
        //model = new ViewModelProvider(this).get(AmbuViewModel.class);

        // Configure sign-in to request the user's ID, email address, and basic profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken("1071058038218-poemneo9oodqsbo5r1bjugbl114r93uh.apps.googleusercontent.com")
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // creo il listener per il button del login
        findViewById(R.id.sign_in_button).setOnClickListener(this);

        //opero sul DB
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        ValueEventListener dateListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Prendo gli oggetti dal db e li uso per l'aggiornamento della ui
                GenericTypeIndicator<HashMap<String,String>> temp = new GenericTypeIndicator<HashMap<String, String>>() {};
                HashMap<String,String> xtemp = dataSnapshot.getValue(temp);
                date = new DataMap(xtemp);
                for (int i=0;i<date.getDate().size();i++)
                    Log.i("lettura da DB ",date.getDate().get(i));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mFirebaseDatabaseReference.child(DB).addListenerForSingleValueEvent(dateListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        findViewById(R.id.main_fragment).setVisibility(View.GONE);
        // Check for existing Google Sign In account, if the user is already signed in the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        updateUI(account);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
            {
                signIn();
                break;
            }
            case R.id.sign_out_button:
            {
                signOut();
                break;
            }
            case R.id.giorno01:
            {
                setDay(0);
                break;
            }
            case R.id.giorno02:
            {
                setDay(1);
                break;
            }
            case R.id.preferenze:
            {
                preference();
                break;
            }
            // ...
        }
    }

    private void preference() {
        //TODO caricamento preferenze: gestire il nuovo fragment per la lista preferenze
    }

    private int Day = 0;
    private void setDay(int mode) {
        CalendarView calendarView = findViewById(R.id.calendarView);
        if (mode == 0) Day++;
        else {
            if (Day != 0) Day--;
            else Day = Donazioni.size()-1;
        }
        if (Donazioni.size() > Day)calendarView.setDate(Donazioni.get(Day).getTime());
        else { Day = 0; calendarView.setDate(Donazioni.get(Day).getTime());}
    }

    private void signOut() {
        mGoogleSignInClient.signOut();
        findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
        findViewById(R.id.main_fragment).setVisibility(View.GONE);
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    private void updateUI(GoogleSignInAccount account) {
        if (account != null){
            //si disabilità il login e si mostra il mainfragment
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.main_fragment).setVisibility(View.VISIBLE);
            if(date != null){
                //L'Arraylist dei dati scaricati dal DB viene caricato per il calendario
                DataConvert util = new DataConvert();
                for (Date data:util.multiDataConvert(date.getDate())) {
                    Donazioni.add(data);
                }
                //riordino Donazioni
                Collections.sort(Donazioni);
                //Si attivano i Listener e le visibilità per le view
                findViewById(R.id.sign_out_button).setOnClickListener(this);
                CalendarView calendarView = findViewById(R.id.calendarView);
                findViewById(R.id.giorno01).setOnClickListener(this);
                findViewById(R.id.giorno02).setOnClickListener(this);
                findViewById(R.id.preferenze).setOnClickListener(this);
                //data di partenza per il calendario
                if (Donazioni.size() != 0){
                    for (Date date: Donazioni) {
                        if(date.after(new Date(System.currentTimeMillis()))){
                            calendarView.setDate(date.getTime());
                            break;
                        }
                    }
                }
            }else {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragment_loader = new Loader();
                fragmentTransaction.add(R.id.main_fragment, fragment_loader);
                fragmentTransaction.commit();
                new waiting().execute(account);
            }
        }
    }
    private class waiting extends AsyncTask<GoogleSignInAccount, Integer, GoogleSignInAccount>{
        @Override
        protected GoogleSignInAccount doInBackground(GoogleSignInAccount... accounts) {
            Long start = System.currentTimeMillis();
            while (true){
                if(date != null)return accounts[0];
                if(System.currentTimeMillis()-start>5000) Log.i("Attesa del DB","DB non risponde, controllare connessione di rete");
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(GoogleSignInAccount account) {
            super.onPostExecute(account);
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(fragment_loader);
            fragmentTransaction.commit();
            updateUI(account);
        }
    }
}
