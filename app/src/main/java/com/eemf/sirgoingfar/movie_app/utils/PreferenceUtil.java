package com.eemf.sirgoingfar.movie_app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferenceUtil {

    //Constants
    public static final String PREF_API_DATA_PULLED_SUCCESSFULLY = "pref_api_data_pulled_successfully";
    public static final String PREF_DATABASE_HAS_TOP_RATED_MOVIE_DATA = "pref_database_has_top_rated_movie_data";
    private static final String PREF_NETWORK_CALL_IS_ON = "pref_network_call_is_on";
    private static final String PREF_POPULAR_MOVIE_API_DATA_FETCHED_SUCCESSFULLY = "pref_popular_movie_api_data_fetched_successfuly";
    private static final String PREF_TOP_RATED_MOVIE_API_DATA_FETCHED_SUCCESSFULLY = "pref_top_rated_movie_api_data_fetched_successfuly";

    private SharedPreferences mPref;
    private static PreferenceUtil sInstance;

    public static PreferenceUtil getsInstance(Context context){
        if(sInstance == null)
            sInstance = new PreferenceUtil(context);

        return sInstance;
    }

    private PreferenceUtil(Context context) {
        mPref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void setPrefApiDataPulledSuccessfully(Boolean isApiDataPulledAtAppFirstLaunch){
        getEditor().putBoolean(PREF_API_DATA_PULLED_SUCCESSFULLY, isApiDataPulledAtAppFirstLaunch)
                .apply();
    }

    public boolean isApiDataPulledSuccessfully() {
        return mPref.getBoolean(PREF_API_DATA_PULLED_SUCCESSFULLY, false);
    }

    public void setDatabaseHasTopRatedMovieData(boolean databaseHasTopRatedMovieData) {
        getEditor().putBoolean(PREF_DATABASE_HAS_TOP_RATED_MOVIE_DATA, databaseHasTopRatedMovieData)
                .apply();
    }

    public boolean doesDatabaseHaveTopRatedMovieData() {
        return mPref.getBoolean(PREF_DATABASE_HAS_TOP_RATED_MOVIE_DATA, false);
    }

    public void setIsNetworkCallInProgress(Boolean isNetworkCallOn){
        getEditor().putBoolean(PREF_NETWORK_CALL_IS_ON, isNetworkCallOn)
                .apply();
    }

    public boolean isNetworkCallInProgress(){
        return mPref.getBoolean(PREF_NETWORK_CALL_IS_ON, false);
    }

    public void setIsPopularMovieApiDataFetchedSuccessfully(boolean isPopularMovieApiDataFetchedSuccessfully) {
        getEditor().putBoolean(PREF_POPULAR_MOVIE_API_DATA_FETCHED_SUCCESSFULLY, isPopularMovieApiDataFetchedSuccessfully)
                .apply();
    }

    public boolean isPopularMovieApiDataFetchedSuccessfully() {
        return mPref.getBoolean(PREF_POPULAR_MOVIE_API_DATA_FETCHED_SUCCESSFULLY, false);
    }

    public void setIsTopRatedMovieApiDataFetchedSuccessfully(boolean isTopRatedMovieApiDataFetchedSuccessfully) {
        getEditor().putBoolean(PREF_TOP_RATED_MOVIE_API_DATA_FETCHED_SUCCESSFULLY, isTopRatedMovieApiDataFetchedSuccessfully)
                .apply();
    }

    public boolean isTopRatedMovieApiDataFetchedSuccessfully() {
        return mPref.getBoolean(PREF_TOP_RATED_MOVIE_API_DATA_FETCHED_SUCCESSFULLY, false);
    }

    private SharedPreferences.Editor getEditor(){
        return mPref.edit();
    }
}
