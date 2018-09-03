package com.eemf.sirgoingfar.movie_app.models;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.eemf.sirgoingfar.movie_app.data.db.MovieAppRoomDatabase;
import com.eemf.sirgoingfar.movie_app.data.db.MovieEntity;

import java.util.List;


public class CatalogViewModel extends AndroidViewModel {

    private LiveData<List<MovieEntity>> allMovies;

    public CatalogViewModel(@NonNull Application application) {
        super(application);
        MovieAppRoomDatabase mDb = MovieAppRoomDatabase.getInstance(this.getApplication());
        allMovies = mDb.getMovieDao().loadAllMovie();
    }

    public LiveData<List<MovieEntity>> getAllMovies() {
        return allMovies;
    }
}
