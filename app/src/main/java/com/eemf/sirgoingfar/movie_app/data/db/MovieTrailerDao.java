package com.eemf.sirgoingfar.movie_app.data.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface MovieTrailerDao {

    @Query("SELECT * FROM movieTrailerEntity WHERE movie_id = :movieId")
    List<MovieTrailerEntity> getAllTrailerByMovieId(int movieId);

    @Query("DELETE FROM movieTrailerEntity WHERE movie_id = :movieId")
    void deleteAllTrailerByMovieId(int movieId);

    @Insert
    void insertTrailerObject(MovieTrailerEntity trailerObject);

}
