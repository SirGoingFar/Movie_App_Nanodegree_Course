package com.eemf.sirgoingfar.movie_app.activities;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.eemf.sirgoingfar.movie_app.R;
import com.eemf.sirgoingfar.movie_app.adapters.ReviewAdapter;
import com.eemf.sirgoingfar.movie_app.adapters.TrailerAdapter;
import com.eemf.sirgoingfar.movie_app.data.db.MovieAppRoomDatabase;
import com.eemf.sirgoingfar.movie_app.data.db.MovieEntity;
import com.eemf.sirgoingfar.movie_app.data.db.MovieReviewEntity;
import com.eemf.sirgoingfar.movie_app.data.db.MovieTrailerEntity;
import com.eemf.sirgoingfar.movie_app.models.MovieDetailViewModel;
import com.eemf.sirgoingfar.movie_app.models.MovieDetailViewModelFactory;
import com.eemf.sirgoingfar.movie_app.utils.Constants;
import com.eemf.sirgoingfar.movie_app.utils.FetchApiDataUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.eemf.sirgoingfar.movie_app.utils.Constants.STATE_EMPTY;
import static com.eemf.sirgoingfar.movie_app.utils.Constants.STATE_FILLED;
import static com.eemf.sirgoingfar.movie_app.utils.Constants.STATE_NO_REVIEW;
import static com.eemf.sirgoingfar.movie_app.utils.Constants.STATE_NO_TRAILER;

public class MovieDetailActivity extends AppCompatActivity implements View.OnClickListener {

    //constant
    private final int TOTAL_RATING = 10;
    private static final String ARG_TRAILER_RECYCLERVIEW_POSITION = "arg_trailer_recyclerview_position";
    private static final String ARG_REVIEW_RECYCLERVIEW_POSITION = "arg_review_recyclerview_position";

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

    @BindView(R.id.rv_review)
    RecyclerView reviewRecyclerView;

    @BindView(R.id.pb_review_layout_loader)
    ProgressBar reviewLoader;

    @BindView(R.id.empty_review_holder)
    TextView noReviewHolder;

    @BindView(R.id.ll_favorite)
    LinearLayout favoritePickerContainer;

    @BindView(R.id.tv_favorite)
    TextView favoriteTextView;

    @BindView(R.id.ic_favorite)
    ImageView favoriteIcon;

    @BindView(R.id.sr_movie_detail)
    SwipeRefreshLayout swipeRefreshContainer;

    //Other variables
    private MovieEntity movieObject;
    private TrailerAdapter trailerAdapter;
    private ReviewAdapter reviewAdapter;
    private MovieAppRoomDatabase mDb;
    private SharedPreferences sharedPreference;
    private String currentSortOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        ButterKnife.bind(this);

        //Instantiate the needed instances
        mDb = MovieAppRoomDatabase.getInstance(this);
        sharedPreference = PreferenceManager.getDefaultSharedPreferences(this);

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

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

        if (trailerRecyclerView.isShown())
            outState.putInt(ARG_TRAILER_RECYCLERVIEW_POSITION,
                    ((LinearLayoutManager) trailerRecyclerView.getLayoutManager()).findFirstVisibleItemPosition());

        if (reviewRecyclerView.isShown())
            outState.putInt(ARG_REVIEW_RECYCLERVIEW_POSITION,
                    ((LinearLayoutManager) reviewRecyclerView.getLayoutManager()).findFirstVisibleItemPosition());

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        int trailerRvPosition;
        int reviewRvPosition;

        LinearLayoutManager trailerRvLayoutManager = (LinearLayoutManager) trailerRecyclerView.getLayoutManager();
        LinearLayoutManager reviewRvLayoutManager = (LinearLayoutManager) reviewRecyclerView.getLayoutManager();

        if (trailerRecyclerView.isShown()) {
            trailerRvPosition = savedInstanceState.getInt(ARG_TRAILER_RECYCLERVIEW_POSITION);

            if (trailerRvPosition > RecyclerView.NO_POSITION && trailerRvLayoutManager != null) {
                trailerRvLayoutManager.scrollToPosition(trailerRvPosition);
            }
        }

        if (reviewRecyclerView.isShown()) {
            reviewRvPosition = savedInstanceState.getInt(ARG_REVIEW_RECYCLERVIEW_POSITION);

            if (reviewRvPosition > RecyclerView.NO_POSITION && reviewRvLayoutManager != null) {
                reviewRvLayoutManager.scrollToPosition(reviewRvPosition);
            }
        }
    }

    private void fetchNeededData() {

        //get the List of Trailers
        getTrailerFromDb(false);

        //get the List of Reviews
        getReviewFromDb(false);

    }

    @SuppressLint("StaticFieldLeak")
    private void getReviewFromDb(final boolean isCallFromPostExecute) {

        new AsyncTask<Void, Void, List<MovieReviewEntity>>() {

            @Override
            protected List<MovieReviewEntity> doInBackground(Void... voids) {
                return mDb.getDao().getAllReviewByMovieId(String.valueOf(movieObject.getMovieId()));
            }

            @Override
            protected void onPostExecute(List<MovieReviewEntity> movieReviewEntities) {

                //check if the object is present - fetch Review from either DB or API
                if (movieReviewEntities.isEmpty()) {
                    if (!isCallFromPostExecute)
                        fetchApiReviewData();
                    else
                        switchReviewLayoutState(STATE_NO_REVIEW);
                } else {
                    //set the adapter list
                    reviewAdapter.setmList(movieReviewEntities);

                    //switch to the filled state
                    switchReviewLayoutState(STATE_FILLED);
                }
            }

        }.execute();

    }

    @SuppressLint("StaticFieldLeak")
    private void fetchApiReviewData() {

        //change to the empty state view
        switchReviewLayoutState(STATE_EMPTY);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                FetchApiDataUtil.execute(MovieDetailActivity.this,
                        FetchApiDataUtil.ACTION_FETCH_MOVIE_REVIEW,
                        String.valueOf(movieObject.getMovieId()));
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                getReviewFromDb(true);
            }
        }.execute();

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

        reviewAdapter = new ReviewAdapter(this);
        reviewRecyclerView.setHasFixedSize(true);
        reviewRecyclerView.setNestedScrollingEnabled(false);
        reviewRecyclerView.setAdapter(reviewAdapter);

        favoriteIcon.setOnClickListener(this);
        favoriteTextView.setOnClickListener(this);

        //toggle Favorite picker as needed
        toggleFavoriteOption(movieObject.isFavorite());

        //Swipe Refresh Layout
        swipeRefreshContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                swipeRefreshContainer.setRefreshing(false);

                //set current sort order
                setCurrentSortOrder();
                //if the sort order is 'favorite', return
                if (TextUtils.equals(currentSortOrder, FetchApiDataUtil.TYPE_FAVORITE_MOVIE))
                    return;

                //fetch trailer and review data
                switchReviewLayoutState(STATE_EMPTY);
                fetchApiReviewData();

                switchTrailerLayoutState(STATE_EMPTY);
                fetchApiTrailerData();

            }
        });
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

    private void switchReviewLayoutState(int state) {

        switch (state) {

            case STATE_EMPTY:
                reviewLoader.setVisibility(View.VISIBLE);
                reviewRecyclerView.setVisibility(View.GONE);
                noReviewHolder.setVisibility(View.GONE);
                break;

            case STATE_FILLED:
                reviewLoader.setVisibility(View.GONE);
                reviewRecyclerView.setVisibility(View.VISIBLE);
                noReviewHolder.setVisibility(View.GONE);
                break;

            case STATE_NO_REVIEW:
                reviewLoader.setVisibility(View.GONE);
                reviewRecyclerView.setVisibility(View.GONE);
                noReviewHolder.setVisibility(View.VISIBLE);
                break;

        }
    }

    @Override
    public void onClick(View clickedView) {

        switch (clickedView.getId()) {

            case R.id.tv_favorite:
            case R.id.ic_favorite:
                toggleFavoriteOption(!movieObject.isFavorite());
                updateDatabase();
                break;
        }
    }

    private void toggleFavoriteOption(boolean isSelected) {
        favoriteTextView.setSelected(isSelected);
        favoriteIcon.setSelected(isSelected);
    }

    @SuppressLint("StaticFieldLeak")
    private void updateDatabase() {

        //toggle object itself
        movieObject.setFavorite(!movieObject.isFavorite());

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                mDb.getDao().updateMovie(movieObject);
                return null;
            }
        }.execute();

    }

    private void setCurrentSortOrder() {
        currentSortOrder = sharedPreference.getString(
                getString(R.string.pref_sort_order_key),
                FetchApiDataUtil.TYPE_POPULAR_MOVIE);
    }

}
