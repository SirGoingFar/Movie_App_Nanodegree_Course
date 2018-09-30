package com.eemf.sirgoingfar.movie_app.data.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface AllTableDao {

    //Movie Type
    @Query("SELECT * FROM movieEntity WHERE movie_type =:movieType")
    List<MovieEntity> loadAllMovieTypeUnobserved(String movieType);

    @Query("SELECT * FROM movieEntity WHERE movie_type =:movieType")
    LiveData<List<MovieEntity>> loadAllMovieType(String movieType);

    @Query("SELECT * FROM movieEntity")
    LiveData<List<MovieEntity>> loadAllMovie();

    @Query("SELECT * FROM movieEntity  WHERE id=:id")
    LiveData<MovieEntity> loadMovieById(int id);

    @Query("DELETE FROM movieEntity WHERE movie_type =:movieType")
    void deleteAllMovieType(String movieType);

    @Query("DELETE FROM movieEntity")
    void deleteAllMovie();

    @Insert
    void insertMovie(MovieEntity movieEntity);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateMovie(MovieEntity movieEntity);


    //Trailer
    @Query("SELECT * FROM movieTrailerEntity WHERE movie_id = :movieId")
    List<MovieTrailerEntity> getAllTrailerByMovieId(String movieId);

    @Query("DELETE FROM movieTrailerEntity WHERE movie_id = :movieId")
    void deleteAllTrailerByMovieId(int movieId);

    @Insert
    void insertTrailerObject(MovieTrailerEntity trailerObject);

    //Review
    @Query("SELECT * FROM movieReviewEntity WHERE movie_id = :movieId")
    List<MovieReviewEntity> getAllReviewByMovieId(String movieId);

    @Query("DELETE FROM movieReviewEntity WHERE movie_id = :movieId")
    void deleteAllReviewByMovieId(int movieId);

    @Insert
    void insertReviewObject(MovieReviewEntity reviewEntityObject);


    //Favorite Movie
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
