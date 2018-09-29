package com.eemf.sirgoingfar.movie_app.data.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "movieTrailerEntity")
public class MovieTrailerEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "movie_id")
    private int movieId;

    @ColumnInfo(name = "trailer_key")
    private String trailerKey;

    @ColumnInfo(name = "trailer_type")
    private String trailerType;

    @ColumnInfo(name = "trailer_site")
    private String trailerSite;

    public MovieTrailerEntity(int id, int movieId, String trailerKey, String trailerType, String trailerSite) {
        this.id = id;
        this.movieId = movieId;
        this.trailerKey = trailerKey;
        this.trailerType = trailerType;
        this.trailerSite = trailerSite;
    }

    @Ignore
    public MovieTrailerEntity(int movieId, String trailerKey, String trailerType, String trailerSite) {
        this.movieId = movieId;
        this.trailerKey = trailerKey;
        this.trailerType = trailerType;
        this.trailerSite = trailerSite;
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

    public String getTrailerKey() {
        return trailerKey;
    }

    public void setTrailerKey(String trailerKey) {
        this.trailerKey = trailerKey;
    }

    public String getTrailerType() {
        return trailerType;
    }

    public void setTrailerType(String trailerType) {
        this.trailerType = trailerType;
    }

    public String getTrailerSite() {
        return trailerSite;
    }

    public void setTrailerSite(String trailerSite) {
        this.trailerSite = trailerSite;
    }
}
