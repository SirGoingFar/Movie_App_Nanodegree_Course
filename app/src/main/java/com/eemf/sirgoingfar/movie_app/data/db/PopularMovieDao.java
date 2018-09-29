package com.eemf.sirgoingfar.movie_app.data.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface PopularMovieDao {

    @Query("SELECT * FROM popularMovieEntity")
    List<PopularMovieEntity> loadAllPopularMovieUnobserved();

    @Query("SELECT * FROM popularMovieEntity")
    LiveData<List<PopularMovieEntity>> loadAllPopularMovie();

    @Query("SELECT * FROM popularMovieEntity  WHERE id=:id")
    LiveData<PopularMovieEntity> loadPopularMovieById(int id);

    @Query("DELETE FROM popularMovieEntity")
    void deleteAllPopularMovie();

    @Insert
    void insertMovie(PopularMovieEntity popularMovieEntity);

    @Update
    void updateMovie(PopularMovieEntity movieEntity);
}
