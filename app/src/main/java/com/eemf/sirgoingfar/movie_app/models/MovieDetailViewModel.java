package com.eemf.sirgoingfar.movie_app.models;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.eemf.sirgoingfar.movie_app.data.db.MovieAppRoomDatabase;
import com.eemf.sirgoingfar.movie_app.data.db.MovieEntity;

public class MovieDetailViewModel extends ViewModel {

    private LiveData<MovieEntity> movieObject;

    public MovieDetailViewModel(MovieAppRoomDatabase mDb, int movieId) {
        movieObject = mDb.getDao().loadMovieById(movieId);
    }

    public LiveData<MovieEntity> getMovieObject() {
        return movieObject;
    }
}
