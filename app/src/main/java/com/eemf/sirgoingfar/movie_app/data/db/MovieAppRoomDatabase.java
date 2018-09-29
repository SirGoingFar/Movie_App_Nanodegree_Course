package com.eemf.sirgoingfar.movie_app.data.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {MovieEntity.class, MovieReviewEntity.class, MovieTrailerEntity.class, PopularMovieEntity.class},
        version = 1, exportSchema = false)
public abstract class MovieAppRoomDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "movie_app_db";
    private static final Object LOCK = new Object();
    private static MovieAppRoomDatabase sInstance;

    public static MovieAppRoomDatabase getInstance (Context context){

        if (sInstance == null){
            synchronized (LOCK){
                sInstance = Room.databaseBuilder(context, MovieAppRoomDatabase.class, DATABASE_NAME)
                        .build();
            }
        }

        return sInstance;
    }

    public abstract AllDao getDao();
}
