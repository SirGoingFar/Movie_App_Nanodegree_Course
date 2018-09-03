package com.eemf.sirgoingfar.movie_app.data.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface MovieDao {

    @Query("SELECT * FROM movieEntity")
    List<MovieEntity> loadAllMovieUnobserved();

    @Query("SELECT * FROM movieEntity")
    LiveData<List<MovieEntity>> loadAllMovie();

    @Query("SELECT * FROM movieEntity  WHERE id=:id")
    LiveData<MovieEntity> loadMovieById(int id);

    @Query("DELETE FROM movieEntity")
    void deleteAllMovie();

    @Insert
    void insertMovie(MovieEntity movieEntity);
}
