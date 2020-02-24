package it.saezzt.avisalbissole;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import it.saezzt.avisalbissole.util.AvisReceiver;
import it.saezzt.avisalbissole.util.EventMap;
import it.saezzt.avisalbissole.util.dbEvent;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;
//TODO metodo per primo avvio con guida
//TODO creazione reminder dell'APP per le donazioni
public class Main extends AppCompatActivity implements View.OnClickListener {
    final int callbackId = 42; //callback per controllo permessi
    Boolean dbLoaded = false; //per il check in updateUI (creo una sola volta le textview)
    GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG00 = "DBloader";
    private static final String TAG01 = "SignInActivity";
    private static final String TAG02 = "OnclickTextView";
    private static final String TAG03 = "ButtonReminder";
    public static final int REQUEST_CODE = 101; // CODE per pending intent
    private static final int idFinalRange = 100;
    private ArrayList<dbEvent> dbEventi = new ArrayList<>();
    private ArrayList<Integer> Donazioni = new ArrayList<>();
    private ArrayList<Integer> Eventi = new ArrayList<>();
    private Boolean vistaDonazioni = true; //per il controllo appSwitch

    private DatabaseReference mFirebaseDatabaseReference;

    Fragment fragment_loader;
    //NOTIFY
    private PendingIntent pendingIntent;
    //SHAREDPREF
    SharedPreferences sharedPref;
    SharedPreferences.OnSharedPreferenceChangeListener alarmListener = (sharedPreferences, key) -> {
        try {

            if (key.equals(getString(R.string.AlarmSetting))) {
                Log.i(TAG01, "Il valore di preferenza è stato aggiornato a: (alarm)" + sharedPreferences.getBoolean(key, false));
                if(sharedPreferences.getBoolean(key, false))avisAlarmManager(dbEventi);
                else avisAlarmManagerDelete();
            }else Log.i(TAG01, "Il valore di preferenza è stato aggiornato a: (else) " + sharedPreferences.getBoolean(key, false));
        }catch (Exception e){
            Log.e(TAG01,e.getMessage());
        }
    };

    //CanaleNOTIFY
    private static final String CHANNEL_ID = "AVIS";

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        createNotificationChannel();
        setContentView(R.layout.activity_main);
        // Create a ViewModel the first time the system calls an activity's onCreate() method.
        // Re-created activities receive the same MyViewModel instance created by the first activity.
        // model = new ViewModelProvider(this).get(AmbuViewModel.class);

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
                EventMap myEvents = new EventMap(dataSnapshot);
                ArrayList<dbEvent> events = myEvents.getDbEvents();
                try{
                    for (dbEvent event:events) {
                        dbEventi.add(event);
                        //aggiunta all'array list ed eventuali altri suddivisioni
                    }
                }catch (NullPointerException e){
                    Log.e(TAG00,e.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mFirebaseDatabaseReference.addListenerForSingleValueEvent(dateListener);


        //inizializzo broadcast per lancio notifiche NOTIFY
        Intent intent = new Intent(this, AvisReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //preferenze
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new AvisSettingsFragment())
                .commit();
        sharedPref = getApplicationContext().getSharedPreferences(getString(R.string.SP_name),Context.MODE_PRIVATE);
        sharedPref.registerOnSharedPreferenceChangeListener(alarmListener);
        Map<String, String> pref = new HashMap<>();
        //pref.putAll(sharedPref.getAll());
        for (Map.Entry<String,?> entry: sharedPref.getAll().entrySet()){
//            pref.put(entry.getKey(),(String)entry.getValue());
            Log.i("SP:"+entry.getKey(),entry.getValue().toString());
        }

        //check permission
        checkPermission(callbackId, Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(getString(R.string.AlarmSetting), Boolean.toString(sharedPref.getBoolean(getString(R.string.AlarmSetting),false)));
        Log.i(getString(R.string.CalendarAlarmSet), Boolean.toString(sharedPref.getBoolean(getString(R.string.CalendarAlarmSet),true)));

        findViewById(R.id.main_fragment).setVisibility(View.GONE);
        findViewById(R.id.settings).setVisibility(View.GONE);
        // Check for existing Google Sign In account, if the user is already signed in the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        updateUI(account);
    }
    @Override
    public void onResume() {
        super.onResume();
        sharedPref.registerOnSharedPreferenceChangeListener(alarmListener);
        comeBack();
    }

    @Override
    public void onPause() {
        super.onPause();
        sharedPref.unregisterOnSharedPreferenceChangeListener(alarmListener);
        comeBack();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
            {
                signIn();
                break;
            }
            //R.id.sign_out_button sotto ridefinito a 0 nell'attesa
            case 0:
            {
                signOut();
                break;
            }
            case R.id.preferenze:
            {
                preference();
                break;
            }
            case R.id.appSwitch:
            {
                comeBack();
                if(vistaDonazioni){
                    for (int i = 0; i < Donazioni.size(); i++) {
                        findViewById(Donazioni.get(i)).setVisibility(View.GONE);
                    }
                    for (int i = 0; i < Eventi.size(); i++) {
                        findViewById(Eventi.get(i)).setVisibility(View.VISIBLE);
                    }
                    FloatingActionButton fab = findViewById(R.id.appSwitch);
                    fab.setImageResource(R.drawable.goccia);
                    Button btn = findViewById(R.id.donationtitle);
                    btn.setText(this.getString(R.string.titolo_giornate_eventi));
                    vistaDonazioni = false;
                }else {
                    for (int i = 0; i < Donazioni.size(); i++) {
                        findViewById(Donazioni.get(i)).setVisibility(View.VISIBLE);
                    }
                    for (int i = 0; i < Eventi.size(); i++) {
                        findViewById(Eventi.get(i)).setVisibility(View.GONE);
                    }
                    FloatingActionButton fab = findViewById(R.id.appSwitch);
                    fab.setImageResource(R.drawable.pork);
                    Button btn = findViewById(R.id.donationtitle);
                    btn.setText(this.getString(R.string.titolo_giornate_donazioni));
                    vistaDonazioni = true;
                }
                /* Funziona ma è più problematico da gestire il cambio immagine del FAB
                LinearLayout toFilterLayout = findViewById(R.id.calendarLayout);
                for (int i = 0; i < toFilterLayout.getChildCount(); i++) {
                    View z = toFilterLayout.getChildAt(i);
                    if (z.getVisibility()==View.GONE)
                        z.setVisibility(View.VISIBLE);
                    else z.setVisibility(View.GONE);
                }*/
                break;
            }
            case R.id.buttonToMap:
            {
                Uri gmmIntentUri = Uri.parse("geo:0,0?q="+findViewById(R.id.buttonToMap).getTag(R.id.where));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                }
                break;
            }
            case R.id.buttonReminder:
            {
                long calID = 0;
                String displayName = null;
                String accountName = null;
                String ownerName = null;
                checkPermission(callbackId, Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR);
                String[] projection =
                        new String[]{
                                CalendarContract.Calendars._ID,
                                CalendarContract.Calendars.NAME,
                                CalendarContract.Calendars.ACCOUNT_NAME,
                                CalendarContract.Calendars.ACCOUNT_TYPE};
                try {
                    Cursor cursor =
                            getContentResolver().
                                    query(CalendarContract.Calendars.CONTENT_URI,
                                            projection,
                                            CalendarContract.Calendars.VISIBLE + " = 1",
                                            null,
                                            null);
                    cursor.moveToFirst();
                    do
                    {
                        calID = cursor.getLong(0);
                        displayName = cursor.getString(1);
                        accountName = cursor.getString(2);
                        ownerName = cursor.getString(3);
                        break;//TODO aggiungere if per poter scegliere quale calendario (al momento prendo il primo)
                    }while (cursor.moveToNext());
                    Log.i(TAG03,"calendario trovato");
                }catch (Exception e){
                    Log.e(TAG03,e.getMessage());
                }
                Date dat = (Date)findViewById(R.id.buttonReminder).getTag(R.id.date);
                Calendar cal = Calendar.getInstance();
                cal.setTime(dat);
                cal.setTimeZone(TimeZone.getDefault());
                long dtstart = cal.getTimeInMillis();
                ContentValues values = new ContentValues();
                String title = (String)findViewById(R.id.buttonReminder).getTag(R.id.title);
                values.put(CalendarContract.Events.DTSTART, dtstart);
                values.put(CalendarContract.Events.DTEND, dtstart+(Long) findViewById(R.id.buttonReminder).getTag(R.id.duration)*60*1000);
                values.put(CalendarContract.Events.TITLE, title);
                values.put(CalendarContract.Events.CALENDAR_ID, calID);
                values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getDisplayName());
                values.put(CalendarContract.Events.DESCRIPTION,(String) findViewById(R.id.buttonReminder).getTag(R.id.info));
                String x = (String) findViewById(R.id.buttonReminder).getTag(R.id.where);
                values.put(CalendarContract.Events.EVENT_LOCATION,x.replace('+',' '));
                Toast toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
                if(sharedPref.getLong(title,0)!=0){
                            String[] proj =
                            new String[]{
                                    CalendarContract.Instances._ID,
                                    CalendarContract.Instances.BEGIN,
                                    CalendarContract.Instances.END,
                                    CalendarContract.Instances.EVENT_ID};
                    Cursor cursor =
                            CalendarContract.Instances.query(getContentResolver(), proj, dtstart, dtstart+(Long)findViewById(R.id.buttonReminder).getTag(R.id.duration)*60*1000, "\""+title+"\"");
                    if (cursor.getCount() > 0) {
                        // l'evento è sul calendario
                        toast.setText("Evento già sul calendario");
                    }else {
                        //l'evento non è sul calendario ma è sulle sharedpref
                        SharedPreferences.Editor edit = sharedPref.edit();
                        edit.putLong(title,0);
                        edit.commit();
                    }
                }
                // CALENDAR REMINDER SETUP
                if(sharedPref.getLong(title,0)==0){
                    try {

                        Uri uri =
                                getContentResolver().
                                        insert(CalendarContract.Events.CONTENT_URI, values);
                        long eventID = Long.parseLong(uri.getLastPathSegment());
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putLong(title,eventID);
                        editor.apply();
                        Log.i(TAG03,Long.toString(sharedPref.getLong(title,0)));
                        Log.i(TAG03,"evento aggiunto");
                        toast.setText("Evento aggiunto al calendario");
                        if(sharedPref.getBoolean(getString(R.string.CalendarAlarmSet),true)){
                            ContentValues valuesR = new ContentValues();
                            valuesR.put(CalendarContract.Reminders.MINUTES, 60*12); //12 ore prima
                            valuesR.put(CalendarContract.Reminders.EVENT_ID, eventID);
                            valuesR.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
                            getContentResolver().insert(CalendarContract.Reminders.CONTENT_URI, valuesR);
                            Log.i(TAG03,"reminder evento aggiunto");
                        }
                    }catch (SecurityException e){
                        Log.e(TAG03,e.getMessage());
                        toast.setText("Non è stato possibile aggiungere l'evento al calendario");
                    }
                }
                toast.show();
                break;
            }
            default:{
                /*try {

                    }
                }catch (Exception e){
                    Log.e(TAG02,e.getMessage());
                }*/
                cardOpen(v.getId());

            }
        }
    }

    private void cardOpen(int id){
        findViewById(R.id.cardOpened).setVisibility(View.VISIBLE);
        findViewById(R.id.calendarLayout).setVisibility(View.GONE);
        Resources r = this.getResources();
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                9,
                r.getDisplayMetrics()
        );
        TextView child;
        TextView tmp = findViewById(id*idFinalRange);
        dbEvent tmpE;
        tmpE = dbEventi.get(id-1);
        child = findViewById(R.id.deData);
        child.setText(tmp.getText());
        child.setPadding(2*px,px,2*px,px);
        child = findViewById(R.id.deTipoEvento);
        child.setText(tmpE.title);
        child = findViewById(R.id.deDescrizione);
        child.setText(tmpE.info);
        findViewById(R.id.buttonReminder).setTag(R.id.where, tmpE.where);
        findViewById(R.id.buttonReminder).setTag(R.id.date, tmpE.date);
        findViewById(R.id.buttonReminder).setTag(R.id.duration, tmpE.duration);
        findViewById(R.id.buttonReminder).setTag(R.id.title, tmpE.title);
        findViewById(R.id.buttonReminder).setTag(R.id.info, tmpE.info);
        findViewById(R.id.buttonToMap).setTag(R.id.where,tmpE.where);
    }

    private void preference() {
        findViewById(R.id.settings).setVisibility(View.VISIBLE);
        findViewById(R.id.main_fragment).setVisibility(View.GONE);
    }
    private void antiPreference(){
        findViewById(R.id.settings).setVisibility(View.GONE);
        findViewById(R.id.main_fragment).setVisibility(View.VISIBLE);
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
            Log.w(TAG01, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    private void updateUI(GoogleSignInAccount account) {
        if (account != null){
            //si disabilità il login e si mostra il mainfragment
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.main_fragment).setVisibility(View.VISIBLE);
            findViewById(R.id.cardOpened).setVisibility(View.GONE);
            if(dbEventi.size()!=0){
                //riordino Donazioni ed Eventi
                Collections.sort(dbEventi);
                //avvio avisAlarmManager se il setup è true
                SharedPreferences sharedPreferences = this.getSharedPreferences(getString(R.string.SP_name), Context.MODE_PRIVATE);
                if(sharedPreferences.getBoolean(getString(R.string.AlarmSetting), false))avisAlarmManager(dbEventi);
                //Si attivano i Listener e le visibilità per le view
                findViewById(R.id.preferenze).setOnClickListener(this);
                findViewById(R.id.appSwitch).setOnClickListener(this);
                findViewById(R.id.buttonToMap).setOnClickListener(this);
                findViewById(R.id.buttonReminder).setOnClickListener(this);

            }else {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragment_loader = new Loader();
                fragmentTransaction.add(R.id.main_fragment, fragment_loader);
                fragmentTransaction.commit();
                new waiting().execute(account);
            }
            //setup calendarLayout
            LinearLayout calendarLayout = (LinearLayout) findViewById(R.id.calendarLayout);
            //aggiunta contenuti
            LinearLayout.LayoutParams tparams = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT );
            LinearLayout.LayoutParams cparams = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT );
            Resources r = this.getResources();
            int px = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    9,
                    r.getDisplayMetrics()
            );
            cparams.setMargins(px,px/2,px,px/2);
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"));
            if ((dbEventi.size() != 0)&dbLoaded==false){
                int i = 1; //parte da uno affinchè il primo elemento sia dispari
                int iD = i-1;
                int iE = i-1;
                for (dbEvent event: dbEventi) {
                    CardView cardChild = new CardView(this);
                    TextView child = new TextView(this);

                    cardChild.setId(i);
                    child.setId(i*idFinalRange);

                    child.setLayoutParams(tparams);
                    cardChild.setLayoutParams(cparams);

                    cal.setTime(event.date);
                    child.setText(cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ITALY)+" "+cal.get(Calendar.DAY_OF_MONTH)+" "+cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ITALY));
                    child.setPadding(2*px,px,2*px,px);
                    child.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    int j;
                    if(event.type.equals(new Long(1))){
                        iD++;
                        j = iD;
                        Donazioni.add(i);
                    }else {
                        iE++;
                        j = iE;
                        Eventi.add(i);
                    }
                    if(j%2 != 0){
                        child.setTextAppearance(this,R.style.rowStyleWhite);
                        cardChild.setBackgroundColor(getResources().getColor(R.color.white));

                    }
                    else {
                        child.setTextAppearance(this,R.style.rowStyleBlue);
                        cardChild.setBackgroundColor(getResources().getColor(R.color.colorTextBar));
                    }
                    cardChild.setCardElevation(9);
                    cardChild.setPadding(0,px,0,px);
                    cardChild.setRadius(9);
                    cardChild.setContentDescription(event.title);
                    cardChild.setTag(R.id.type,event.type);
                    cardChild.addView(child);
                    calendarLayout.addView(cardChild);
                    cardChild.setOnClickListener(this);
                    i++;
                }
                dbLoaded = true;
            }
            if(dbLoaded){
                //filtro eventi
                LinearLayout toFilterLayout = findViewById(R.id.calendarLayout);
                for (int i = 0; i < toFilterLayout.getChildCount(); i++) {
                    View v = toFilterLayout.getChildAt(i);
                    if(v.getTag(R.id.type).equals(new Long(2)))
                        v.setVisibility(View.GONE);
                }
            }
        }
    }
    private class waiting extends AsyncTask<GoogleSignInAccount, Integer, GoogleSignInAccount>{
        @Override
        protected GoogleSignInAccount doInBackground(GoogleSignInAccount... accounts) {
            Long start = System.currentTimeMillis();
            while (true){
                if((dbEventi.size()!=0))return accounts[0];
                //if(System.currentTimeMillis()-start>5000) Log.i("Attesa del DB","DB non risponde, controllare connessione di rete");

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

    public void avisAlarmManager(ArrayList<dbEvent> event){
        int i = 0;
        int timeLeft = 1000*60*60*12;
        for (;i<event.size();i++){
            if (event.get(i).date.getTime()-timeLeft>System.currentTimeMillis())
                break;
        }
        AlarmManager manager = (AlarmManager) getSystemService(this.ALARM_SERVICE);
        Long timeToGo = event.get(i).date.getTime()-timeLeft;
        manager.setExact(AlarmManager.RTC_WAKEUP,timeToGo,pendingIntent);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.AlarmSet),event.get(i).title);
        editor.putLong(event.get(i).title,timeToGo);
        editor.apply();

    }
    private void avisAlarmManagerDelete() {
        /*
        With FLAG_NO_CREATE it will return null if the PendingIntent doesnt already exist. If it already exists it returns
        reference to the existing PendingIntent
        */
        AlarmManager manager = (AlarmManager) getSystemService(this.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(this, AvisReceiver.class), PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null)
            manager.cancel(pendingIntent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            comeBack();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
    @Override
    public void onBackPressed() {
        comeBack();
        return;
    }
    public void comeBack(){
        if(findViewById(R.id.settings).isShown()){
            antiPreference();
        }
        if(findViewById(R.id.cardOpened).isShown()){
            findViewById(R.id.cardOpened).setVisibility(View.GONE);
            findViewById(R.id.calendarLayout).setVisibility(View.VISIBLE);
        }
    }

    private void checkPermission(int callbackId, String... permissionsId) {
        boolean permissions = true;
        for (String p : permissionsId) {
            permissions = permissions && ContextCompat.checkSelfPermission(this, p) == PERMISSION_GRANTED;
        }

        if (!permissions)
            ActivityCompat.requestPermissions(this, permissionsId, callbackId);
    }
}
