package com.eemf.sirgoingfar.movie_app.models;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.eemf.sirgoingfar.movie_app.data.db.MovieAppRoomDatabase;


public class CatalogActivityViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private MovieAppRoomDatabase mDb;
    private String movieType;

    public CatalogActivityViewModelFactory(MovieAppRoomDatabase mDb, String movieType) {
        this.mDb = mDb;
        this.movieType = movieType;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new CatalogViewModel(mDb, movieType);
    }
}


