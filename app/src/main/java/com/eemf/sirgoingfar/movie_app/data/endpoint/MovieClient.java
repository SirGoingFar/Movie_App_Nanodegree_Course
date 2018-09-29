package com.eemf.sirgoingfar.movie_app.data.endpoint;

import com.eemf.sirgoingfar.movie_app.data.MovieApiData;
import com.eemf.sirgoingfar.movie_app.data.VideoContent;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface MovieClient {

    @GET("3/movie/{movie_type}?api_key={api_key}")
    Call<MovieApiData> fetchMovieData(@Path("movie_type") String movieType);

    @GET("3/movie/{movie_id}/videos?api_key={api_key}")
    Call<VideoContent> fetchMovieTrailerData(@Path("movie_id") String movieId);
}
