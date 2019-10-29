package es.upm.miw.demoretrofit;

import java.util.List;

import es.upm.miw.demoretrofit.models.AirData;
import es.upm.miw.demoretrofit.modelsCollection.Datum;
import es.upm.miw.demoretrofit.modelsCollection.StationCollection;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;


@SuppressWarnings("Unused")
interface ICountryRESTAPIService {


    @GET("map/bounds/?latlng=40.714757,-4.192049,39.992108,-3.029779")
    Call<StationCollection> getStations(@Query("token") String token);


    @GET("feed/{city}/")
    Call<AirData> getAqi(@Path("city") String city, @Query("token") String token);

}
