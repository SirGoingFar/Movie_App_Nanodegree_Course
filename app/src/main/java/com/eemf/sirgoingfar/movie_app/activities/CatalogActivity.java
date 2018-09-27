package com.eemf.sirgoingfar.movie_app.activities;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.eemf.sirgoingfar.movie_app.R;
import com.eemf.sirgoingfar.movie_app.adapters.MovieRecyclerAdapter;
import com.eemf.sirgoingfar.movie_app.data.db.MovieEntity;
import com.eemf.sirgoingfar.movie_app.models.CatalogViewModel;
import com.eemf.sirgoingfar.movie_app.utils.FetchApiDataUtil;
import com.eemf.sirgoingfar.movie_app.utils.NetworkStatus;
import com.eemf.sirgoingfar.movie_app.utils.PreferenceUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CatalogActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    //Constants
    private final int EMPTY_STATE = 0;
    private final int FILLED_STATE = 1;

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
        prefs = PreferenceUtil.getsInstance(this);
        sharedPreference = PreferenceManager.getDefaultSharedPreferences(this);

        //set up views
        setupView();

        //mine the 'sort_order'
        currentSortOrder = sharedPreference.getString(getString(R.string.pref_sort_order_key), FetchApiDataUtil.TYPE_POPULAR_MOVIE);

        //populate the screen
        if (prefs.isApiDataPulledSuccessfully()) {
            if (doesCurrentMovieDataMatchUserSortOrder())
                fetchDataFromDb();
            else
                fetchMovieApiData();
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

            if (TextUtils.equals(value, FetchApiDataUtil.TYPE_POPULAR_MOVIE))
                fetchMovieApiData(FetchApiDataUtil.TYPE_POPULAR_MOVIE);

            else if (TextUtils.equals(value, FetchApiDataUtil.TYPE_TOP_RATED_MOVIE))
                fetchMovieApiData(FetchApiDataUtil.TYPE_TOP_RATED_MOVIE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPreference.unregisterOnSharedPreferenceChangeListener(this);
        prefs.setIsNetworkCallInProgress(false);
    }

    @SuppressLint("StaticFieldLeak")
    private void fetchDataFromDb() {

        CatalogViewModel model = ViewModelProviders.of(CatalogActivity.this).get(CatalogViewModel.class);
        model.getAllMovies().observe(CatalogActivity.this, new Observer<List<MovieEntity>>() {
            @Override
            public void onChanged(@Nullable List<MovieEntity> movieEntities) {
                adapter.setmMovieList((ArrayList<MovieEntity>) movieEntities);
                switchScreen(FILLED_STATE);
            }
        });

    }

    private void fetchMovieApiData() {
        fetchMovieApiData(TextUtils.equals(currentSortOrder, getString(R.string.pref_popular_movie_value)) ?
                FetchApiDataUtil.TYPE_POPULAR_MOVIE : FetchApiDataUtil.TYPE_TOP_RATED_MOVIE);
    }

    @SuppressLint("StaticFieldLeak")
    private void fetchMovieApiData(final String movieSortOrder) {

        //check if the data to be fetched is the current one in the Db
        if (prefs.isApiDataPulledSuccessfully()) {

            if (prefs.doesDatabaseHaveTopRatedMovieData())
                currentSortOrder = FetchApiDataUtil.TYPE_TOP_RATED_MOVIE;
            else
                currentSortOrder = FetchApiDataUtil.TYPE_POPULAR_MOVIE;

            if (TextUtils.equals(currentSortOrder, movieSortOrder)) {
                fetchDataFromDb();
                return;
            }
        }

        if (prefs.isNetworkCallInProgress()) {
            showSnackbar(getString(R.string.pls_retry), getString(R.string.keyword_retry), refreshAction());
            switchScreen(EMPTY_STATE);
            emptyStateMessageHolder.setText(getString(R.string.pull_to_refresh_notif));
            dataLoadingProgressBar.setVisibility(View.GONE);
            return;
        }

        //start fetching data
        if (!NetworkStatus.isConnected(this)
                || NetworkStatus.isPoorConectivity(this)) {
            notifyPoorConnectivity();
            return;
        }

        //switch screen
        switchScreen(EMPTY_STATE);

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                FetchApiDataUtil.execute(CatalogActivity.this, movieSortOrder);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                fetchDataFromDb();
            }
        }.execute();

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
        movieTileRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        movieTileRecyclerView.setAdapter(adapter);

        //Swipe Refresh Layout
        swipeRefreshContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                switchScreen(EMPTY_STATE);

                swipeRefreshContainer.setRefreshing(false);

                //fetch currentSortOrder API data
                fetchMovieApiData(
                        sharedPreference.getString(getString(R.string.pref_sort_order_key),
                                FetchApiDataUtil.TYPE_POPULAR_MOVIE
                        ));
            }
        });
    }

    private void switchScreen(int screenToShow) {

        String actionBarTitle;

        if (actionBar != null) {

            if (prefs.doesDatabaseHaveTopRatedMovieData())
                actionBarTitle = getString(R.string.pref_top_rated_movie_label);
            else
                actionBarTitle = getString(R.string.pref_popular_movie_label);

            actionBar.setTitle(actionBarTitle);
        }

        if (screenToShow == EMPTY_STATE) {
            emptyStateContainer.setVisibility(View.VISIBLE);
            filledStateContainer.setVisibility(View.GONE);
        } else {
            emptyStateContainer.setVisibility(View.GONE);
            filledStateContainer.setVisibility(View.VISIBLE);
        }

    }

    private boolean doesCurrentMovieDataMatchUserSortOrder() {

        if (prefs.isApiDataPulledSuccessfully()) {

            String currentMovieType;

            //get the label of the data in the database
            if (prefs.doesDatabaseHaveTopRatedMovieData())
                currentMovieType = FetchApiDataUtil.TYPE_TOP_RATED_MOVIE;
            else
                currentMovieType = FetchApiDataUtil.TYPE_POPULAR_MOVIE;

            //get the value of the current sort order
            currentSortOrder = TextUtils.equals(sharedPreference.getString(getString(R.string.pref_sort_order_key),
                    FetchApiDataUtil.TYPE_POPULAR_MOVIE), getString(R.string.pref_popular_movie_value)) ?
                    FetchApiDataUtil.TYPE_POPULAR_MOVIE : FetchApiDataUtil.TYPE_TOP_RATED_MOVIE;

            return TextUtils.equals(currentMovieType, currentSortOrder);
        }
        else
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

}
