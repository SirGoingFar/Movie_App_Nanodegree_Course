package com.eemf.sirgoingfar.movie_app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.eemf.sirgoingfar.movie_app.R;

public class PreferenceUtil {

    //Constants
    public static final String PREF_API_DATA_PULLED_SUCCESSFULLY = "pref_api_data_pulled_successfully";
    public static final String PREF_DATABASE_HAS_TOP_RATED_MOVIE_DATA = "pref_database_has_popular_movie_data";
    private static final String PREF_NETWORK_CALL_IS_ON = "pref_network_call_is_on";

    //Other Variables
    private Context mContext;
    private SharedPreferences mPref;
    private static PreferenceUtil sInstance;

    public static PreferenceUtil getsInstance(Context context){
        if(sInstance == null)
            sInstance = new PreferenceUtil(context);

        return sInstance;
    }

    private PreferenceUtil(Context context) {
        mPref = PreferenceManager.getDefaultSharedPreferences(context);
        mContext = context;
    }

    public void setPrefApiDataPulledSuccessfully(Boolean isApiDataPulledAtAppFirstLaunch){
        getEditor().putBoolean(PREF_API_DATA_PULLED_SUCCESSFULLY, isApiDataPulledAtAppFirstLaunch)
                .apply();
    }

    public boolean getPrefApiDataPulledSuccessfully(){
        return mPref.getBoolean(PREF_API_DATA_PULLED_SUCCESSFULLY, false);
    }

    public void setDatabaseHasPopularMovieData(boolean databaseHasTopRatedMovieData){
        getEditor().putBoolean(PREF_DATABASE_HAS_TOP_RATED_MOVIE_DATA, databaseHasTopRatedMovieData)
                .apply();
    }

    public boolean getDatabaseHasTopRatedMovieData(){
        return mPref.getBoolean(PREF_DATABASE_HAS_TOP_RATED_MOVIE_DATA, false);
    }

    public void setIsNetworkCallInProgress(Boolean isNetworkCallOn){
        getEditor().putBoolean(PREF_NETWORK_CALL_IS_ON, isNetworkCallOn)
                .apply();
    }

    public boolean isNetworkCallInProgress(){
        return mPref.getBoolean(PREF_NETWORK_CALL_IS_ON, false);
    }

    private SharedPreferences.Editor getEditor(){
        return mPref.edit();
    }
}
