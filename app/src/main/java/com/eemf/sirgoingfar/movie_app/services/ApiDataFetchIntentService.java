package com.eemf.sirgoingfar.movie_app.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.eemf.sirgoingfar.movie_app.utils.FetchApiDataUtil;
import com.eemf.sirgoingfar.movie_app.utils.PreferenceUtil;


public class ApiDataFetchIntentService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public ApiDataFetchIntentService() {
        super(ApiDataFetchIntentService.class.getName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        Context context = this.getApplicationContext();

        String sortOrderUrl =
                PreferenceUtil.getsInstance(context).getDatabaseHasTopRatedMovieData() ?
                        FetchApiDataUtil.URL_TOP_RATED_MOVIE : FetchApiDataUtil.URL_POPULAR_MOVIE;

        FetchApiDataUtil.execute(context, sortOrderUrl);
    }
}
