package com.eemf.sirgoingfar.movie_app.models;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.eemf.sirgoingfar.movie_app.data.db.MovieAppRoomDatabase;


public class MovieDetailViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private MovieAppRoomDatabase mDb;
    private int movieId;

    public MovieDetailViewModelFactory(MovieAppRoomDatabase mDb, int movieId) {
        this.mDb = mDb;
        this.movieId = movieId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new MovieDetailViewModel(mDb, movieId);
    }
}
