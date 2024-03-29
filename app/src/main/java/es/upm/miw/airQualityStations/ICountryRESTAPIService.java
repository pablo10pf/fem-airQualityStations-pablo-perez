package es.upm.miw.airQualityStations;

import es.upm.miw.airQualityStations.models.AirData;
import es.upm.miw.airQualityStations.modelsCollection.StationCollection;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;


@SuppressWarnings("Unused")
interface ICountryRESTAPIService {


    // Madrid @GET("map/bounds/?latlng=40.714757,-4.192049,39.992108,-3.029779")

    @GET("map/bounds/?latlng=39.604872,-0.848914,39.336109,-0.158591")
    Call<StationCollection> getStations(@Query("token") String token);


    @GET("feed/{city}/")
    Call<AirData> getAqi(@Path("city") String city, @Query("token") String token);

}
