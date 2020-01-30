package it.saezzt.avisalbissole;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static java.lang.System.currentTimeMillis;

public class Login extends AppCompatActivity implements View.OnClickListener {
    GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "SignInActivity";
    private List<Date> Donazioni = new LinkedList<>();
    private AmbuViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Create a ViewModel the first time the system calls an activity's onCreate() method.
        // Re-created activities receive the same MyViewModel instance created by the first activity.
        model = new ViewModelProvider(this).get(AmbuViewModel.class);

        // Configure sign-in to request the user's ID, email address, and basic profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken("1071058038218-poemneo9oodqsbo5r1bjugbl114r93uh.apps.googleusercontent.com")
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // creo il listener per il button del login
        findViewById(R.id.sign_in_button).setOnClickListener(this);
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
        /*FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = new ItemListDialogFragment();
        fragmentTransaction.add(R.id.main_fragment, fragment);
        fragmentTransaction.commit();*/
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
            Toast toast = Toast.makeText(getApplicationContext(),"EVIL"+ e.getStatusCode(),Toast.LENGTH_SHORT);
            toast. show();
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    private void updateUI(GoogleSignInAccount account) {
        if (account == null){
            Toast toast = Toast.makeText(getApplicationContext(),"nessun account collegato",Toast.LENGTH_SHORT);
            toast. show();
        } else {
            /*
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment fragment = Ambu.newInstance();
            fragmentTransaction.add(R.id.main_fragment, fragment);
            /*
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment fragment = new Fragment();
            fragmentTransaction.add(R.id.main_fragment, fragment);
            fragmentTransaction.commit();
            findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
            */

            Donazioni.addAll((model.getDonazioni()));
            findViewById(R.id.sign_out_button).setOnClickListener(this);
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.main_fragment).setVisibility(View.VISIBLE);
            CalendarView calendarView = findViewById(R.id.calendarView);

            long j = currentTimeMillis()-864000000;
            for (int i=0; i<12; i++){
                long k = 864000000;
                Donazioni.add(new Date(j));
                j = j + k*3;
            }
            if (Donazioni.size() != 0)calendarView.setDate(Donazioni.get(0).getTime());

            findViewById(R.id.preferenze).setOnClickListener(this);
            findViewById(R.id.giorno01).setOnClickListener(this);
            findViewById(R.id.giorno02).setOnClickListener(this);
        }
    }
    private void countdown(int s){
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis()-start<s){

        }
        //TODO eliminare a programma finito se non usato
    }
}
