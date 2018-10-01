package com.eemf.sirgoingfar.movie_app.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.eemf.sirgoingfar.movie_app.BuildConfig;
import com.eemf.sirgoingfar.movie_app.data.MovieApiData;
import com.eemf.sirgoingfar.movie_app.data.MovieContent;
import com.eemf.sirgoingfar.movie_app.data.ReviewContent;
import com.eemf.sirgoingfar.movie_app.data.VideoContent;
import com.eemf.sirgoingfar.movie_app.data.db.MovieAppRoomDatabase;
import com.eemf.sirgoingfar.movie_app.data.db.MovieEntity;
import com.eemf.sirgoingfar.movie_app.data.db.MovieReviewEntity;
import com.eemf.sirgoingfar.movie_app.data.db.MovieTrailerEntity;
import com.eemf.sirgoingfar.movie_app.data.endpoint.MovieClient;

import java.io.IOException;
import java.util.List;

import retrofit2.Response;
import retrofit2.Retrofit;

public class FetchApiDataUtil {

    //Constants
    private static final String BASE_URL = "http://api.themoviedb.org/";
    public static final String TYPE_POPULAR_MOVIE = "popular";
    public static final String TYPE_TOP_RATED_MOVIE = "top_rated";
    public static final String TYPE_FAVORITE_MOVIE = "favorite";

    //ACTIONS
    public static final String ACTION_FETCH_MOVIE_DATA = "fetch_movie_data";
    public static final String ACTION_FETCH_MOVIE_TRAILER = "fetch_movie_trailer";
    public static final String ACTION_FETCH_MOVIE_REVIEW = "fetch_movie_review";

    private static MovieAppRoomDatabase mDb;
    private static PreferenceUtil prefs;


    public static void execute(Context context, @NonNull String action, @NonNull String queryParams) {

        //instantiate needed resources
        prefs = PreferenceUtil.getsInstance(context);
        mDb = MovieAppRoomDatabase.getInstance(context);


        //check network connectivity
        if (!NetworkStatus.isConnected(context)
                || NetworkStatus.isPoorConectivity(context))
            return;

        switch (action) {

            case ACTION_FETCH_MOVIE_DATA:
                fetchMovieTypeApiData(context, queryParams);
                break;

            case ACTION_FETCH_MOVIE_TRAILER:
                fetchMovieTrailerApiData(context, queryParams);
                break;

            case ACTION_FETCH_MOVIE_REVIEW:
                fetchMovieReview(context, queryParams);
                break;
        }
    }

    private static void fetchMovieTrailerApiData(Context context, String queryParams) {

        int movieId;

        try {
            movieId = Integer.parseInt(queryParams);
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            movieId = -1;
        }

        if (movieId < 0)
            return;

        //fetch Trailer data
        VideoContent content = fetchTrailerApiData(queryParams);

        if (content != null && !content.getResults().isEmpty())
            saveToDatabase(context, content, movieId);
    }

    private static void saveToDatabase(Context context, VideoContent content, int movieId) {

        //clear the Trailer Db for the movieId
        clearTrailerDatabase(movieId);

        //save the TrailerEntity object(s) into the Db
        for (VideoContent.ResultsItem item : content.getResults()) {
            MovieTrailerEntity trailerObject = new MovieTrailerEntity(
                    content.getId(),
                    item.getKey(),
                    item.getType(),
                    item.getSite()
            );

            mDb.getDao().insertTrailerObject(trailerObject);
        }
    }

    private static void clearTrailerDatabase(int movieId) {

        List<MovieTrailerEntity> allMovieIdTrailer = mDb.getDao().getAllTrailerByMovieId(String.valueOf(movieId));

        if (!allMovieIdTrailer.isEmpty())
            mDb.getDao().deleteAllTrailerByMovieId(movieId);
    }

    private static VideoContent fetchTrailerApiData(String movieId) {

        Retrofit retrofit = NetworkIOHelper.getRetrofitInstance(BASE_URL, false);

        try {
            return (retrofit.create(MovieClient.class)
                    .fetchMovieTrailerData(movieId, BuildConfig.API_KEY)
                    .execute()).body();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void fetchMovieReview(Context context, String queryParams) {

        int movieId;

        try {
            movieId = Integer.parseInt(queryParams);
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            movieId = -1;
        }

        if (movieId < 0)
            return;

        //fetch Review data
        ReviewContent content = fetchReviewApiData(queryParams);

        if (content != null && !content.getResults().isEmpty())
            saveToDatabase(context, content, movieId);

    }

    private static void saveToDatabase(Context context, ReviewContent content, int movieId) {

        //clear the Trailer Db for the movieId
        clearReviewDatabase(movieId);

        //save the ReviewEntity object(s) into the Db
        for (ReviewContent.ResultsItem item : content.getResults()) {
            MovieReviewEntity reviewObject = new MovieReviewEntity(
                    content.getId(),
                    item.getAuthor(),
                    item.getContent()
            );

            mDb.getDao().insertReviewObject(reviewObject);
        }

    }

    private static void clearReviewDatabase(int movieId) {

        List<MovieReviewEntity> allMovieIdReview = mDb.getDao().getAllReviewByMovieId(String.valueOf(movieId));

        if (!allMovieIdReview.isEmpty())
            mDb.getDao().deleteAllReviewByMovieId(movieId);

    }

    private static ReviewContent fetchReviewApiData(String movieId) {
        Retrofit retrofit = NetworkIOHelper.getRetrofitInstance(BASE_URL, false);

        try {
            return (retrofit.create(MovieClient.class)
                    .fetchMovieReviewData(movieId, BuildConfig.API_KEY)
                    .execute()).body();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void fetchMovieTypeApiData(Context context, String movieType) {

        //Fetch API data
        MovieApiData movieApiData = fetchMovieTypeData(context, movieType);

        //validate that the API data is not NULL
        if (movieApiData == null)
            return;

        //save the serialized data to the Database
        saveToDatabase(context, movieType, movieApiData.getResults());
    }


    private static void saveToDatabase(Context context, String movieType, List<MovieContent> movieList) {

        //prepare database first
        clearMovieDatabase(movieType);

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

            mDb.getDao().insertMovie(movieEntity);
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

    private static void clearMovieDatabase(String movieType) {

        List<MovieEntity> allMovieType = mDb.getDao().loadAllMovieTypeUnobserved(movieType);

        //clear the DB if the database was earlier populated with the movie type
        if (!allMovieType.isEmpty())
            mDb.getDao().deleteAllMovieType(movieType);
    }

    private static MovieApiData fetchMovieTypeData(Context context, @NonNull String movieType) {

        //notify that a network call is on
        prefs.setIsNetworkCallInProgress(true);

        Retrofit retrofit = NetworkIOHelper.getRetrofitInstance(BASE_URL, false);

        try {

            Response<MovieApiData> result = retrofit.create(MovieClient.class)
                    .fetchMovieData(movieType, BuildConfig.API_KEY).execute();

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
