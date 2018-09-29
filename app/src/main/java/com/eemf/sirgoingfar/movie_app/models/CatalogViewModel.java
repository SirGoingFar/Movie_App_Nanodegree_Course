package com.eemf.sirgoingfar.movie_app.models;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.eemf.sirgoingfar.movie_app.data.db.MovieAppRoomDatabase;
import com.eemf.sirgoingfar.movie_app.data.db.MovieEntity;

import java.util.List;


public class CatalogViewModel extends ViewModel {

    private LiveData<List<MovieEntity>> allMoviesType;

    public CatalogViewModel(MovieAppRoomDatabase mDb, String movieType) {
        allMoviesType = mDb.getDao().loadAllMovieType(movieType);
    }

    public LiveData<List<MovieEntity>> getAllMoviesType() {
        return allMoviesType;
    }
}
