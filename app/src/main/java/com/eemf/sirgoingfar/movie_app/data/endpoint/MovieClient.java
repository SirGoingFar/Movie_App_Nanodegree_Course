package com.eemf.sirgoingfar.movie_app.data.endpoint;

import com.eemf.sirgoingfar.movie_app.data.MovieApiData;
import com.eemf.sirgoingfar.movie_app.data.ReviewContent;
import com.eemf.sirgoingfar.movie_app.data.VideoContent;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MovieClient {

    @GET("3/movie/{movie_type}")
    Call<MovieApiData> fetchMovieData(
            @Path("movie_type") String movieType,
            @Query("api_key") String apiKey);

    @GET("3/movie/{movie_id}/videos")
    Call<VideoContent> fetchMovieTrailerData(
            @Path("movie_id") String movieId,
            @Query("api_key") String apiKey);

    @GET("3/movie/{movie_id}/reviews")
    Call<ReviewContent> fetchMovieReviewData(
            @Path("movie_id") String movieId,
            @Query("api_key") String apiKey);
}
