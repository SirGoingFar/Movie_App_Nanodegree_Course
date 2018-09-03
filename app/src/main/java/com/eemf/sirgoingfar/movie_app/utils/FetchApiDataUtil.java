package com.eemf.sirgoingfar.movie_app.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.eemf.sirgoingfar.movie_app.BuildConfig;
import com.eemf.sirgoingfar.movie_app.data.ApiData;
import com.eemf.sirgoingfar.movie_app.data.MovieContent;
import com.eemf.sirgoingfar.movie_app.data.db.MovieAppRoomDatabase;
import com.eemf.sirgoingfar.movie_app.data.db.MovieEntity;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

public class FetchApiDataUtil {

    private static final String THE_MOVIE_DB_API_KEY = "GET YOURS ON http://www.themoviedb.com TO BE ABLE TO USE THIS APP";
    public static final String URL_POPULAR_MOVIE = "http://api.themoviedb.org/3/movie/popular?api_key=" + THE_MOVIE_DB_API_KEY;
    public static final String URL_TOP_RATED_MOVIE = "http://api.themoviedb.org/3/movie/top_rated?api_key=" + THE_MOVIE_DB_API_KEY;

    private static MovieAppRoomDatabase mDb;


    public static void execute(Context context, String url){

        //check network connectivity
        if(!NetworkStatus.isConnected(context)
                || NetworkStatus.isPoorConectivity(context))
            return;

        //form the URL from the URL string
        URL dataUrl = formUrl(url);

        if(dataUrl == null)
            return;

        //fetch data from the API URL
        String dataJson = fetchDataJson(context, dataUrl);

        if(dataJson == null || dataJson.isEmpty())
            return;

        //serialize the JSON response from the API
        ApiData apiData = serializeJsonToPojo(dataJson);

        //validate that the API data is not NULL/EMPTY
        if(apiData == null || apiData.getResults().isEmpty())
            return;

        //save the serialized data to the Database
        saveToDatabase(context, url, apiData.getResults());
    }


    private static void saveToDatabase(Context context, String url, List<MovieContent> movieList) {

        //get the database instance
        mDb = MovieAppRoomDatabase.getInstance(context);

        //prepare database first
        clearDatabase();

        //populate the Database iteratively
        for(MovieContent movieContent : movieList) {

            MovieEntity movieEntity = new MovieEntity(
                    movieContent.getId(),
                    movieContent.getOriginalTitle(),
                    movieContent.getPosterPath(),
                    movieContent.getOverview(),
                    movieContent.getReleaseDate(),
                    movieContent.getVoteAverage()
            );

            mDb.getMovieDao().insertMovie(movieEntity);
        }

        //set Preference flags
        PreferenceUtil.getsInstance(context).setPrefApiDataPulledSuccessfully(!movieList.isEmpty());
        PreferenceUtil.getsInstance(context)
                .setDatabaseHasPopularMovieData(
                        TextUtils.equals(url, URL_TOP_RATED_MOVIE)
                );
    }

    private static void clearDatabase() {

        List<MovieEntity> allMovie = mDb.getMovieDao().loadAllMovie();

        //clear the DB if the database was earlier populated
        if(!allMovie.isEmpty())
            mDb.getMovieDao().deleteAllMovie();
    }


    private static ApiData serializeJsonToPojo(String dataJson) {
        Gson gson = new Gson();
        return gson.fromJson(dataJson, ApiData.class);
    }

    private static String fetchDataJson(Context context, URL dataUrl) {

        String jsonResponse = null;
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;

        //notify that a network call is on
        PreferenceUtil.getsInstance(context).setIsNetworkCallInProgress(true);

        try {

            urlConnection = (HttpURLConnection) dataUrl.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(15000);
            urlConnection.setReadTimeout(10000);
            urlConnection.setDefaultUseCaches(true);
            urlConnection.connect();


            //Log Network Action
            if(BuildConfig.DEBUG)
                Log.d(FetchApiDataUtil.class.getName(), "Network Request -----> ".concat(dataUrl.toString()));

            if(urlConnection.getResponseCode() == 200){
                inputStream = urlConnection.getInputStream();
                jsonResponse = buildJsonResponse(inputStream);
            }
        } catch (IOException ex){
            ex.printStackTrace();
        } finally {
            if(urlConnection != null)
                urlConnection.disconnect();

            if(inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //Log Network Action
        if(BuildConfig.DEBUG && jsonResponse != null)
            Log.d(FetchApiDataUtil.class.getName(), "Network Response -----> ".concat(jsonResponse));

        //notify that a network call has ended
        PreferenceUtil.getsInstance(context).setIsNetworkCallInProgress(false);

        return jsonResponse;
    }


    private static String buildJsonResponse(InputStream inputStream) {

        StringBuilder builtString = new StringBuilder();

        if(inputStream == null)
            return null;

        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        try {

            String line = bufferedReader.readLine();

            while (line != null){
                builtString.append(line);
                line = bufferedReader.readLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return builtString.toString();
    }

    private static URL formUrl(String url) {

        URL dataUrl = null;

        try{
            dataUrl = new URL(url);
        }catch (MalformedURLException ex){
            ex.printStackTrace();
        }

        return dataUrl;
    }
}
