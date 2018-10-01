package com.eemf.sirgoingfar.movie_app.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.eemf.sirgoingfar.movie_app.R;
import com.eemf.sirgoingfar.movie_app.adapters.MovieRecyclerAdapter;
import com.eemf.sirgoingfar.movie_app.data.db.MovieAppRoomDatabase;
import com.eemf.sirgoingfar.movie_app.data.db.MovieEntity;
import com.eemf.sirgoingfar.movie_app.utils.FetchApiDataUtil;
import com.eemf.sirgoingfar.movie_app.utils.NetworkStatus;
import com.eemf.sirgoingfar.movie_app.utils.PreferenceUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.eemf.sirgoingfar.movie_app.utils.Constants.STATE_EMPTY;
import static com.eemf.sirgoingfar.movie_app.utils.Constants.STATE_FILLED;

public class CatalogActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    //Views
    @BindView(R.id.sr_layout)
    SwipeRefreshLayout swipeRefreshContainer;

    @BindView(R.id.rl_empty_state)
    RelativeLayout emptyStateContainer;

    @BindView(R.id.empty_state_message_holder)
    TextView emptyStateMessageHolder;

    @BindView(R.id.fl_filled_state)
    FrameLayout filledStateContainer;

    @BindView(R.id.rv_movie_list)
    RecyclerView movieTileRecyclerView;

    @BindView(R.id.pb_data_loader)
    ProgressBar dataLoadingProgressBar;

    private MovieAppRoomDatabase mDb;
    private PreferenceUtil prefs;
    private SharedPreferences sharedPreference;
    private String currentSortOrder;
    private ActionBar actionBar;
    private MovieRecyclerAdapter adapter;

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);
        ButterKnife.bind(this);

        //initialize the appropriate variables
        mDb = MovieAppRoomDatabase.getInstance(this);
        prefs = PreferenceUtil.getsInstance(this);
        sharedPreference = PreferenceManager.getDefaultSharedPreferences(this);

        //set up views
        setupView();

        //mine the 'sort_order'
        currentSortOrder = sharedPreference.getString(getString(R.string.pref_sort_order_key), FetchApiDataUtil.TYPE_POPULAR_MOVIE);

        //populate the screen
        if (TextUtils.equals(currentSortOrder, FetchApiDataUtil.TYPE_FAVORITE_MOVIE))
            fetchFavoriteMovies();
        else if (prefs.isApiDataPulledSuccessfully()) {
            if (doesCurrentSortOrderMovieExistInDb())
                fetchDataFromDb(currentSortOrder);
            else
                fetchMovieApiData(currentSortOrder);
        } else
            fetchMovieApiData();

        sharedPreference.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_catalog_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_sort_order) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals(getString(R.string.pref_sort_order_key))) {

            String value = sharedPreferences.getString(key, FetchApiDataUtil.TYPE_POPULAR_MOVIE);

            if (TextUtils.equals(value, FetchApiDataUtil.TYPE_POPULAR_MOVIE)) {

                if (prefs.isPopularMovieApiDataFetchedSuccessfully())
                    fetchDataFromDb(value);
                else
                    fetchMovieApiData(FetchApiDataUtil.TYPE_POPULAR_MOVIE);
            } else if (TextUtils.equals(value, FetchApiDataUtil.TYPE_TOP_RATED_MOVIE)) {

                if (prefs.isTopRatedMovieApiDataFetchedSuccessfully())
                    fetchDataFromDb(value);
                else
                    fetchMovieApiData(FetchApiDataUtil.TYPE_TOP_RATED_MOVIE);

            } else if (TextUtils.equals(value, FetchApiDataUtil.TYPE_FAVORITE_MOVIE))
                fetchFavoriteMovies();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPreference.unregisterOnSharedPreferenceChangeListener(this);
        prefs.setIsNetworkCallInProgress(false);
    }

    @SuppressLint("StaticFieldLeak")
    private void fetchDataFromDb(final String movieType) {

        if (actionBar != null) {
            actionBar.setTitle(TextUtils.equals(movieType, FetchApiDataUtil.TYPE_POPULAR_MOVIE) ?
                    getString(R.string.pref_popular_movie_label) : getString(R.string.pref_top_rated_movie_label));
        }
        /*CatalogActivityViewModelFactory factory = new CatalogActivityViewModelFactory(mDb, movieType);
        final CatalogViewModel model = ViewModelProviders.of(CatalogActivity.this, factory).get(CatalogViewModel.class);
        model.getAllMoviesType().observe(CatalogActivity.this, new Observer<List<MovieEntity>>() {
            @Override
            public void onChanged(@Nullable List<MovieEntity> movieEntities) {
                adapter.setmMovieList((ArrayList<MovieEntity>) movieEntities);
                switchScreen(STATE_FILLED);
            }
        });*/
        new AsyncTask<Void, Void, List<MovieEntity>>() {
            @Override
            protected List<MovieEntity> doInBackground(Void... voids) {
                return mDb.getDao().loadAllMovieTypeUnobserved(movieType);
            }

            @Override
            protected void onPostExecute(List<MovieEntity> movieEntities) {
                adapter.setmMovieList(movieEntities);
                switchScreen(STATE_FILLED);
            }
        }.execute();

    }

    private void fetchMovieApiData() {

        setCurrentSortOrder();

        if (TextUtils.equals(currentSortOrder, FetchApiDataUtil.TYPE_FAVORITE_MOVIE))
            fetchFavoriteMovies();
        else
            fetchMovieApiData(currentSortOrder);
    }

    @SuppressLint("StaticFieldLeak")
    private void fetchMovieApiData(final String movieSortOrder) {

        //check if the data to be fetched is available in the Db
        if (prefs.isApiDataPulledSuccessfully()) {

            if (TextUtils.equals(movieSortOrder, FetchApiDataUtil.TYPE_POPULAR_MOVIE)
                    && prefs.isPopularMovieApiDataFetchedSuccessfully()) {

                fetchDataFromDb(movieSortOrder);

            } else if (TextUtils.equals(movieSortOrder, FetchApiDataUtil.TYPE_TOP_RATED_MOVIE)
                    && prefs.isTopRatedMovieApiDataFetchedSuccessfully()) {

                fetchDataFromDb(movieSortOrder);
            }

            //check if an API call is on
            if (prefs.isNetworkCallInProgress()) {
                showSnackbar(getString(R.string.pls_retry), getString(R.string.keyword_retry), refreshAction());
                switchScreen(STATE_EMPTY);
                emptyStateMessageHolder.setText(getString(R.string.pull_to_refresh_notif));
                dataLoadingProgressBar.setVisibility(View.GONE);
                return;
            }

            //check the network conectivity status
            if (!NetworkStatus.isConnected(this)
                    || NetworkStatus.isPoorConectivity(this)) {
                notifyPoorConnectivity();
                return;
            }

            //switch screen
            switchScreen(STATE_EMPTY);

            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... voids) {
                    FetchApiDataUtil.execute(CatalogActivity.this, FetchApiDataUtil.ACTION_FETCH_MOVIE_DATA, movieSortOrder);
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    fetchDataFromDb(movieSortOrder);
                }
            }.execute();

        }
    }

    private void notifyPoorConnectivity() {
        emptyStateMessageHolder.setText(R.string.poor_connectivity_message);
        dataLoadingProgressBar.setVisibility(View.GONE);
    }

    private void setupView() {

        //Initialize Action Bar
        actionBar = getSupportActionBar();

        //RecyclerView
        adapter = new MovieRecyclerAdapter(this);
        movieTileRecyclerView.setLayoutManager(new GridLayoutManager(this, getMaxSpanCount()));
        movieTileRecyclerView.setAdapter(adapter);

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

                switchScreen(STATE_EMPTY);

                //fetch currentSortOrder API data
                fetchMovieApiData(currentSortOrder);
            }
        });
    }

    private void switchScreen(int screenToShow) {

        if (screenToShow == STATE_EMPTY) {
            emptyStateContainer.setVisibility(View.VISIBLE);
            filledStateContainer.setVisibility(View.GONE);
        } else {
            emptyStateContainer.setVisibility(View.GONE);
            filledStateContainer.setVisibility(View.VISIBLE);
        }

    }

    private boolean doesCurrentSortOrderMovieExistInDb() {

        if (prefs.isApiDataPulledSuccessfully()) {

            if (TextUtils.equals(currentSortOrder, FetchApiDataUtil.TYPE_POPULAR_MOVIE))
                return prefs.isPopularMovieApiDataFetchedSuccessfully();

            else
                return TextUtils.equals(currentSortOrder, FetchApiDataUtil.TYPE_TOP_RATED_MOVIE)
                        && prefs.isTopRatedMovieApiDataFetchedSuccessfully();

        } else
            return false;

    }

    private Runnable refreshAction() {
        return new Runnable() {
            @Override
            public void run() {
                //fetch currentSortOrder API data
                fetchMovieApiData(
                        sharedPreference.getString(getString(R.string.pref_sort_order_key),
                                FetchApiDataUtil.TYPE_POPULAR_MOVIE
                        ));
            }
        };
    }

    private void showSnackbar(String message, String actionLabel, final Runnable actionOperation) {

        Snackbar.make(movieTileRecyclerView, message, Snackbar.LENGTH_LONG)
                .setAction(actionLabel, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        actionOperation.run();
                    }
                })
                .setActionTextColor(ContextCompat.getColor(this, R.color.colorWhite))
                .show();
    }

    private int getMaxSpanCount() {

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        int SCALING_FACTOR = 200;

        int noOfColumns = (int) (dpWidth / SCALING_FACTOR);

        if (noOfColumns < 2)
            noOfColumns = 2;

        return noOfColumns;
    }

    @SuppressLint("StaticFieldLeak")
    private void fetchFavoriteMovies() {

        switchScreen(STATE_EMPTY);

        //set the toolbar title
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.pref_favorite_movie_label));
        }

        new AsyncTask<Void, Void, List<MovieEntity>>() {
            @Override
            protected List<MovieEntity> doInBackground(Void... voids) {
                return mDb.getDao().loadAllFavoriteMovie();
            }

            @Override
            protected void onPostExecute(List<MovieEntity> movieEntities) {

                if (movieEntities.isEmpty()) {
                    emptyStateMessageHolder.setText(getString(R.string.msg_no_favorite_movie));
                    dataLoadingProgressBar.setVisibility(View.GONE);
                } else {
                    adapter.setmMovieList(movieEntities);
                    switchScreen(STATE_FILLED);
                }
            }
        }.execute();
    }

    private void setCurrentSortOrder() {
        currentSortOrder = sharedPreference.getString(getString(R.string.pref_sort_order_key), FetchApiDataUtil.TYPE_POPULAR_MOVIE);
    }
}
