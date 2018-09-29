package com.eemf.sirgoingfar.movie_app.data.endpoint;

import com.eemf.sirgoingfar.movie_app.data.ApiData;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface MovieClient {

    @GET("3/movie/{movie_type}?api_key={api_key}")
    Call<ApiData> fetchMovieData(@Path("movie_type") String movieType);
}
