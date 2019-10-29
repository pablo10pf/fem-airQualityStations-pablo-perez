package es.upm.miw.airQualityStations;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import es.upm.miw.airQualityStations.modelsCollection.Datum;
import es.upm.miw.airQualityStations.modelsCollection.StationCollection;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;



public class ActividadPrincipal extends Activity {

    private static final String API_BASE_URL = "https://api.waqi.info/";
    //private static final String API_BASE_URL_BOUNDS ="https://api.waqi.info/";
    private static final String TOKEN = "c180c35f2d4ea911aba2f6d75815b2945ba441c8";

    private static final String LOG_TAG = "MiW";

    private TextView tvRespuesta;
    private EditText etCountryName;

    private ICountryRESTAPIService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actividad_principal);
        tvRespuesta = (TextView) findViewById(R.id.tvRespuesta);
        etCountryName = (EditText) findViewById(R.id.countryName);

        // btb added for retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ICountryRESTAPIService.class);
    }


    //
    // A este método se llama cada vez que se hace click sobre la lupa
    // Ver  activity_actividad_principal.xml
    //
    public void obtenerInfoPais(View v) {
        String countryName = etCountryName.getText().toString();
        Log.i(LOG_TAG, "obtenerInfoPais => país=" + countryName);
        tvRespuesta.setText("");

        // Realiza la llamada por nombre
        //Call<List<Country>> call_async = apiService.getCountryByName(countryName);
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
}