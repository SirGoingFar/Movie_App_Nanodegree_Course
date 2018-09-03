package com.eemf.sirgoingfar.movie_app.data.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "movieEntity")
public class MovieEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "movie_id")
    private int movieId;

    @ColumnInfo(name = "original_title")
    private String originalTitle;

    @ColumnInfo(name = "image_path")
    private String imagePath;

    private String overview;

    @ColumnInfo(name = "release_date")
    private String releaseDate;

    @ColumnInfo(name = "user_rating")
    private double userRating;

    @Ignore
    public MovieEntity(int movieId, String originalTitle, String imagePath,
                       String overview, String releaseDate, double userRating) {
        this.movieId = movieId;
        this.originalTitle = originalTitle;
        this.imagePath = imagePath;
        this.overview = overview;
        this.releaseDate = releaseDate;
        this.userRating = userRating;
    }

    public MovieEntity(int id, int movieId, String originalTitle, String imagePath,
                       String overview, String releaseDate, double userRating) {
        this.id = id;
        this.movieId = movieId;
        this.originalTitle = originalTitle;
        this.imagePath = imagePath;
        this.overview = overview;
        this.releaseDate = releaseDate;
        this.userRating = userRating;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public double getUserRating() {
        return userRating;
    }

    public void setUserRating(double userRating) {
        this.userRating = userRating;
    }
}
