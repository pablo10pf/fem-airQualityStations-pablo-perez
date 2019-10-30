package es.upm.miw.airQualityStations;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import es.upm.miw.airQualityStations.fcube.commands.FCColor;
import es.upm.miw.airQualityStations.fcube.config.FeedbackCubeManager;
import es.upm.miw.airQualityStations.models.AirData;
import es.upm.miw.airQualityStations.modelsCollection.Datum;
import es.upm.miw.airQualityStations.modelsCollection.StationCollection;
import es.upm.miw.airQualityStations.views.DataStationAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// Firebase
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;

public class ActividadPrincipal extends Activity {

    private static final String API_BASE_URL = "https://api.waqi.info/";
    //private static final String API_BASE_URL_BOUNDS ="https://api.waqi.info/";
    private static final String TOKEN = "c180c35f2d4ea911aba2f6d75815b2945ba441c8";
    private static final String LOG_TAG = "MiW";

    // btb Firebase database variables
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessagesDatabaseReference;
    private ChildEventListener mChildEventListener;

    // btb Firebase authentication variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private static final int RC_SIGN_IN = 2018;

    private TextView tvRespuesta;
    private EditText etCountryName;

    private ICountryRESTAPIService apiService;

    private ListView lvStations;
    private DataStationAdapter stationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actividad_principal);

        // btb Get instance of Firebase database
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();

        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child("stations");


        //tvRespuesta = (TextView) findViewById(R.id.tvRespuesta);
        etCountryName = (EditText) findViewById(R.id.countryName);
        lvStations = (ListView) findViewById(R.id.lvStations);
        // btb added for retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ICountryRESTAPIService.class);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // user is signed in
                    CharSequence username = user.getDisplayName();
                    Toast.makeText(ActividadPrincipal.this, getString(R.string.firebase_user_fmt, username), Toast.LENGTH_LONG).show();
                    Log.i(LOG_TAG, "onAuthStateChanged() " + getString(R.string.firebase_user_fmt, username));
                   // ((TextView) findViewById(R.id.textView)).setText(getString(R.string.firebase_user_fmt, username));
                } else {
                    // user is signed out
                    startActivityForResult(
                            // Get an instance of AuthUI based on the default app
                            AuthUI.getInstance().
                                    createSignInIntentBuilder().
                                    setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.GoogleBuilder().build(),
                                            new AuthUI.IdpConfig.EmailBuilder().build()
                                    )).
                                    setIsSmartLockEnabled(!BuildConfig.DEBUG /* credentials */, true /* hints */).
                                    build(),
                            RC_SIGN_IN
                    );
                }
            }
        };
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, R.string.signed_in, Toast.LENGTH_SHORT).show();
                Log.i(LOG_TAG, "onActivityResult " + getString(R.string.signed_in));
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, R.string.signed_cancelled, Toast.LENGTH_SHORT).show();
                Log.i(LOG_TAG, "onActivityResult " + getString(R.string.signed_cancelled));
                finish();
            }
        }
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.opciones_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.opcLogout:
                //mFirebaseAuth.signOut();
                //Log.i(LOG_TAG, getString(R.string.signed_out));

                FCColor fcc = new FCColor("192.168.0.100", "" + "0", ""
                        + "255", "" + "0");
                new FeedbackCubeManager().execute(fcc);
                return true;

        }
        return true;
    }

    public void getStations(View v) {
        String countryName = etCountryName.getText().toString();
        Log.i(LOG_TAG, "obtenerInfoPais => país=" + countryName);
        tvRespuesta.setText("");


        Call<StationCollection> call_async = apiService.getStations(TOKEN);
        // Asíncrona
        call_async.enqueue(new Callback<StationCollection>() {


            @Override
            public void onResponse(Call<StationCollection> call, Response<StationCollection> response) {
                StationCollection stationCollection = response.body();
                if (null != stationCollection) {
                    /*tvRespuesta.append( airData.getData().getCity().getName() + " - " + String.valueOf(airData.getData().getAqi()) + "\n\n");
                    if(airData.getData().getAqi()<=50){
                        tvRespuesta.setBackgroundColor(Color.GREEN);
                    }else if (airData.getData().getAqi() >=50 && airData.getData().getAqi() <=100 ){
                        tvRespuesta.setBackgroundColor(Color.YELLOW);
                    }else{
                        tvRespuesta.setBackgroundColor(Color.RED);
                    }*/

                    for(Datum datum : stationCollection.getData()){
                        tvRespuesta.append(datum.getStation().getName()+datum.getAqi()+"\n");
                        mMessagesDatabaseReference.push().setValue(datum);
                    }
                    //Log.i(LOG_TAG, "obtenerInfoPais => respuesta=" + airData.getData().getCity().getName() + " - " + airData.getData().getAqi());
                } else {
                    tvRespuesta.setText(getString(R.string.strError));
                    Log.i(LOG_TAG, getString(R.string.strError));
                }
            }


            @Override
            public void onFailure(Call<StationCollection> call, Throwable t) {
                Toast.makeText(
                        getApplicationContext(),
                        "ERROR: "+t.getMessage() ,
                        Toast.LENGTH_LONG
                ).show();
                Log.e(LOG_TAG, t.getMessage());
            }
        });


        // Síncrona... no aquí => NetworkOnMainThreadException
//        Call<Country> call_sync = apiService.getCountryByName("spain");
//        try {
//            Country country = call_sync.execute().body();
//            Log.i(LOG_TAG, "SYNC => " + country.toString());
//        } catch (IOException e) {
//            Log.e(LOG_TAG, e.getMessage());
//        }
    }

    public void getInfoStation(View v) {
        String countryName = etCountryName.getText().toString();
        Log.i(LOG_TAG, "obtenerInfoPais => país=" + countryName);
        tvRespuesta.setText("");

        // Realiza la llamada por nombre
        //Call<List<Country>> call_async = apiService.getCountryByName(countryName);
        Call<AirData> call_async = apiService.getAqi(countryName,TOKEN);
        // Asíncrona
        call_async.enqueue(new Callback<AirData>() {


            @Override
            public void onResponse(Call<AirData> call, Response<AirData> response) {
                AirData airData= response.body();
                if (null != airData) {
                        tvRespuesta.setText(airData.getData().getCity().getName() +" - "+airData.getData().getAqi()+"\n");

                    //Log.i(LOG_TAG, "obtenerInfoPais => respuesta=" + airData.getData().getCity().getName() + " - " + airData.getData().getAqi());
                } else {
                    tvRespuesta.setText(getString(R.string.strError));
                    Log.i(LOG_TAG, getString(R.string.strError));
                }
            }


            @Override
            public void onFailure(Call<AirData> call, Throwable t) {
                Toast.makeText(
                        getApplicationContext(),
                        "ERROR: "+t.getMessage() ,
                        Toast.LENGTH_LONG
                ).show();
                Log.e(LOG_TAG, t.getMessage());
            }
        });

    }
}