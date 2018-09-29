package com.eemf.sirgoingfar.movie_app.data.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface MovieReviewDao {

    @Query("SELECT * FROM movieReviewEntity WHERE movie_id = :movieId")
    List<MovieReviewEntity> getAllReviewByMovieId(int movieId);

    @Query("DELETE FROM movieReviewEntity WHERE movie_id = :movieId")
    void deleteAllReviewByMovieId(int movieId);

}
