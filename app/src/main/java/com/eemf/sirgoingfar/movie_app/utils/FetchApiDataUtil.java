package com.eemf.sirgoingfar.movie_app.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.eemf.sirgoingfar.movie_app.data.ApiData;
import com.eemf.sirgoingfar.movie_app.data.MovieContent;
import com.eemf.sirgoingfar.movie_app.data.db.MovieAppRoomDatabase;
import com.eemf.sirgoingfar.movie_app.data.db.MovieEntity;
import com.eemf.sirgoingfar.movie_app.data.endpoint.MovieClient;

import java.io.IOException;
import java.util.List;

import retrofit2.Response;
import retrofit2.Retrofit;

public class FetchApiDataUtil {

    //Todo: Put the API_KEY received from http://www.themoviedb.org in place of {API_KEY} in the two constants below
    //in order to use this App
    private static final String BASE_URL = "http://api.themoviedb.org/";
    public static final String TYPE_POPULAR_MOVIE = "popular";
    public static final String TYPE_TOP_RATED_MOVIE = "top_rated";

    private static MovieAppRoomDatabase mDb;
    private static PreferenceUtil prefs;


    public static void execute(Context context, String movieType) {

        prefs = PreferenceUtil.getsInstance(context);

        //check network connectivity
        if (!NetworkStatus.isConnected(context)
                || NetworkStatus.isPoorConectivity(context))
            return;

        //Fetch API data
        ApiData apiData = fetchApiData(context, movieType);

        //validate that the API data is not NULL
        if (apiData == null)
            return;

        //save the serialized data to the Database
        saveToDatabase(context, movieType, apiData.getResults());
    }


    private static void saveToDatabase(Context context, String movieType, List<MovieContent> movieList) {

        //get the database instance
        mDb = MovieAppRoomDatabase.getInstance(context);

        //prepare database first
        clearDatabase(movieType);

        //populate the Database iteratively
        for (MovieContent movieContent : movieList) {

            MovieEntity movieEntity = new MovieEntity(
                    movieContent.getId(),
                    movieContent.getOriginalTitle(),
                    movieContent.getPosterPath(),
                    movieContent.getOverview(),
                    movieContent.getReleaseDate(),
                    movieContent.getVoteAverage(),
                    movieType);

            mDb.getMovieDao().insertMovie(movieEntity);
        }

        //set Preference flags
        prefs.setPrefApiDataPulledSuccessfully(!movieList.isEmpty());
        prefs.setDatabaseHasTopRatedMovieData(TextUtils.equals(movieType, TYPE_TOP_RATED_MOVIE));

        //Set individual Movie type flag
        if (TextUtils.equals(movieType, TYPE_TOP_RATED_MOVIE))
            prefs.setIsTopRatedMovieApiDataFetchedSuccessfully(true);

        if (TextUtils.equals(movieType, TYPE_POPULAR_MOVIE))
            prefs.setIsPopularMovieApiDataFetchedSuccessfully(true);
    }

    private static void clearDatabase(String movieType) {

        List<MovieEntity> allMovieType = mDb.getMovieDao().loadAllMovieTypeUnobserved(movieType);

        //clear the DB if the database was earlier populated with the movie type
        if (!allMovieType.isEmpty())
            mDb.getMovieDao().deleteAllMovieType(movieType);
    }

    private static ApiData fetchApiData(Context context, @NonNull String movieType) {

        //notify that a network call is on
        prefs.setIsNetworkCallInProgress(true);

        Retrofit retrofit = NetworkIOHelper.getRetrofitInstance(BASE_URL, false);

        try {

            Response<ApiData> result = retrofit.create(MovieClient.class)
                    .fetchMovieData(movieType).execute();

            //notify that a network call has ended
            prefs.setIsNetworkCallInProgress(false);

            return result.body();

        } catch (IOException e) {
            e.printStackTrace();
            //notify that a network call has ended
            prefs.setIsNetworkCallInProgress(false);
            return null;
        }
    }
}
