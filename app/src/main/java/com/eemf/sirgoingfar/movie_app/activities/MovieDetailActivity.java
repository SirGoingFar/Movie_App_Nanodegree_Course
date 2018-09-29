package com.eemf.sirgoingfar.movie_app.activities;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.eemf.sirgoingfar.movie_app.R;
import com.eemf.sirgoingfar.movie_app.adapters.TrailerAdapter;
import com.eemf.sirgoingfar.movie_app.data.db.MovieAppRoomDatabase;
import com.eemf.sirgoingfar.movie_app.data.db.MovieEntity;
import com.eemf.sirgoingfar.movie_app.data.db.MovieTrailerEntity;
import com.eemf.sirgoingfar.movie_app.models.MovieDetailViewModel;
import com.eemf.sirgoingfar.movie_app.models.MovieDetailViewModelFactory;
import com.eemf.sirgoingfar.movie_app.utils.Constants;
import com.eemf.sirgoingfar.movie_app.utils.FetchApiDataUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.eemf.sirgoingfar.movie_app.utils.Constants.STATE_EMPTY;
import static com.eemf.sirgoingfar.movie_app.utils.Constants.STATE_FILLED;
import static com.eemf.sirgoingfar.movie_app.utils.Constants.STATE_NO_TRAILER;

public class MovieDetailActivity extends AppCompatActivity {

    //constant
    private final int TOTAL_RATING = 10;

    //views
    @BindView(R.id.iv_movie_poster)
    ImageView moviePoster;

    @BindView(R.id.tv_movie_title)
    TextView movieTitle;

    @BindView(R.id.tv_release_date)
    TextView releaseDate;

    @BindView(R.id.tv_synopsis)
    TextView movieOverview;

    @BindView(R.id.pb_user_rating)
    ProgressBar userRatingProgressBar;

    @BindView(R.id.rv_trailer)
    RecyclerView trailerRecyclerView;

    @BindView(R.id.pb_trailer_layout_loader)
    ProgressBar trailerLoader;

    @BindView(R.id.empty_trailer_holder)
    TextView noTrailerHolder;

    //Lists
    List<MovieTrailerEntity> trailerList = new ArrayList<>();

    //Other variables
    private MovieEntity movieObject;
    private TrailerAdapter trailerAdapter;
    private MovieAppRoomDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        ButterKnife.bind(this);

        //Instantiate the Db instance
        mDb = MovieAppRoomDatabase.getInstance(this);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        //check intent for the needed detail
        Intent receivedIntent = getIntent();

        if (!receivedIntent.hasExtra(Constants.EXTRA_CLICKED_MOVIE_ID))
            //close this Activity if no Movie Id is sent
            finish();

        //Instantiate the View Models and Movie Object
        MovieDetailViewModelFactory factory = new MovieDetailViewModelFactory(
                MovieAppRoomDatabase.getInstance(this),
                receivedIntent.getIntExtra(Constants.EXTRA_CLICKED_MOVIE_ID, 0));
        final MovieDetailViewModel model = ViewModelProviders.of(this, factory).get(MovieDetailViewModel.class);
        model.getMovieObject().observe(this, new Observer<MovieEntity>() {
            @Override
            public void onChanged(@Nullable MovieEntity movieEntity) {
                model.getMovieObject().removeObserver(this);
                movieObject = movieEntity;
                setupView();

                //fetch the screen data
                fetchNeededData();
            }
        });
    }

    private void fetchNeededData() {

        //get the List of Trailers
        getTrailerFromDb(false);

    }

    @SuppressLint("StaticFieldLeak")
    private void fetchApiTrailerData() {

        //change to the empty state view
        switchTrailerLayoutState(STATE_EMPTY);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                FetchApiDataUtil.execute(MovieDetailActivity.this,
                        FetchApiDataUtil.ACTION_FETCH_MOVIE_TRAILER,
                        String.valueOf(movieObject.getMovieId()));
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                getTrailerFromDb(true);
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private void getTrailerFromDb(final boolean isCallFromPostExecute) {

        new AsyncTask<Void, Void, List<MovieTrailerEntity>>() {

            @Override
            protected List<MovieTrailerEntity> doInBackground(Void... voids) {
                return mDb.getDao().getAllTrailerByMovieId(String.valueOf(movieObject.getMovieId()));
            }

            @Override
            protected void onPostExecute(List<MovieTrailerEntity> movieTrailerEntities) {

                //check if the object is present - fetch Trailer from either DB or API
                if (movieTrailerEntities.isEmpty()) {
                    if (!isCallFromPostExecute)
                        fetchApiTrailerData();
                    else
                        switchTrailerLayoutState(STATE_NO_TRAILER);
                } else {
                    //set the adapter list
                    trailerAdapter.setmList(movieTrailerEntities);

                    //switch to the filled state
                    switchTrailerLayoutState(STATE_FILLED);
                }
            }

        }.execute();
    }

    private void setupView() {

        movieTitle.setText(movieObject.getOriginalTitle());
        releaseDate.setText(getString(R.string.release_date, movieObject.getReleaseDate()));
        movieOverview.setText(movieObject.getOverview());

        if (movieObject.getUserRating() == 0.0)
            userRatingProgressBar.setProgress(0);
        else {
            int progress = (int) ((movieObject.getUserRating() / TOTAL_RATING) * 100);
            userRatingProgressBar.setProgress(progress);
        }

        Glide.with(this)
                .load(Constants.IMAGE_PREFIX.concat(movieObject.getImagePath()))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(moviePoster);

        trailerAdapter = new TrailerAdapter(this);
        trailerRecyclerView.setHasFixedSize(true);
        trailerRecyclerView.setAdapter(trailerAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }

    private void switchTrailerLayoutState(int state) {

        switch (state) {

            case STATE_EMPTY:
                trailerLoader.setVisibility(View.VISIBLE);
                trailerRecyclerView.setVisibility(View.GONE);
                noTrailerHolder.setVisibility(View.GONE);
                break;

            case STATE_FILLED:
                trailerLoader.setVisibility(View.GONE);
                trailerRecyclerView.setVisibility(View.VISIBLE);
                noTrailerHolder.setVisibility(View.GONE);
                break;

            case STATE_NO_TRAILER:
                trailerLoader.setVisibility(View.GONE);
                trailerRecyclerView.setVisibility(View.GONE);
                noTrailerHolder.setVisibility(View.VISIBLE);
                break;

        }
    }
}
