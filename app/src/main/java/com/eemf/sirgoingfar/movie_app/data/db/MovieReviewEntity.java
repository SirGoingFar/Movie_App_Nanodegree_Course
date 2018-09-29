package com.eemf.sirgoingfar.movie_app.data.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "movieReviewEntity")
public class MovieReviewEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "movie_id")
    private int movieId;

    @ColumnInfo(name = "reviewer")
    private String reviewer;

    @ColumnInfo(name = "review")
    private String review;

    public MovieReviewEntity(int id, int movieId, String reviewer, String review) {
        this.id = id;
        this.movieId = movieId;
        this.reviewer = reviewer;
        this.review = review;
    }

    @Ignore
    public MovieReviewEntity(int movieId, String reviewer, String review) {
        this.movieId = movieId;
        this.reviewer = reviewer;
        this.review = review;
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

    public String getReviewer() {
        return reviewer;
    }

    public void setReviewer(String reviewer) {
        this.reviewer = reviewer;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }
}
