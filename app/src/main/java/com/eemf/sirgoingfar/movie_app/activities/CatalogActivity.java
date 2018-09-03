package com.eemf.sirgoingfar.movie_app.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.Toast;

import com.eemf.sirgoingfar.movie_app.R;
import com.eemf.sirgoingfar.movie_app.adapters.MovieRecyclerAdapter;
import com.eemf.sirgoingfar.movie_app.data.db.MovieAppRoomDatabase;
import com.eemf.sirgoingfar.movie_app.data.db.MovieEntity;
import com.eemf.sirgoingfar.movie_app.utils.FetchApiDataUtil;
import com.eemf.sirgoingfar.movie_app.utils.NetworkStatus;
import com.eemf.sirgoingfar.movie_app.utils.PreferenceUtil;

import java.util.ArrayList;

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

    //Other Variables
    private MovieRecyclerAdapter adapter;
    private ArrayList<MovieEntity> mMovieList = new ArrayList<>();
    private PreferenceUtil prefs;
    private SharedPreferences sharedPreference;
    private String currentSortOrder;
    private MovieAppRoomDatabase mDb;

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

        //mine the 'sort_order'
        currentSortOrder = sharedPreference.getString(getString(R.string.pref_sort_order_key), "");

        //populate the screen
        if (PreferenceUtil.getsInstance(this).getPrefApiDataPulledSuccessfully()) {
            if (doesCurrentDataMatchUserSortOrder())
                fetchDataFromDb();
            else
                fetchMovieApiData(TextUtils.equals(currentSortOrder, getString(R.string.pref_popular_movie_value)) ?
            FetchApiDataUtil.URL_POPULAR_MOVIE : FetchApiDataUtil.URL_TOP_RATED_MOVIE);
        } else
            fetchMovieApiData(
                    TextUtils.equals(currentSortOrder, getString(R.string.pref_top_rated_movie_value)) ?
                            FetchApiDataUtil.URL_TOP_RATED_MOVIE : FetchApiDataUtil.URL_POPULAR_MOVIE
            );


        sharedPreference.registerOnSharedPreferenceChangeListener(this);
    }

    @SuppressLint("StaticFieldLeak")
    private void fetchDataFromDb() {
        new AsyncTask<Void, Void, ArrayList<MovieEntity>>() {
            @Override
            protected ArrayList<MovieEntity> doInBackground(Void... voids) {
                return (ArrayList<MovieEntity>) mDb.getMovieDao().loadAllMovie();
            }

            @Override
            protected void onPostExecute(ArrayList<MovieEntity> movieList) {
                mMovieList = movieList;
                setupView();
            }
        }.execute();
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

            String value = sharedPreferences.getString(key, "");

            if (TextUtils.equals(value, getString(R.string.pref_popular_movie_value)))
                fetchMovieApiData(FetchApiDataUtil.URL_POPULAR_MOVIE);

            else if (TextUtils.equals(value, getString(R.string.pref_top_rated_movie_value)))
                fetchMovieApiData(FetchApiDataUtil.URL_TOP_RATED_MOVIE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPreference.unregisterOnSharedPreferenceChangeListener(this);
    }

    @SuppressLint("StaticFieldLeak")
    private void fetchMovieApiData(final String sortOrderOptionUrl) {

        //check if the data to be fetched is the current one in the Db
        if(prefs.getPrefApiDataPulledSuccessfully()){
            String currentDataUrl;
            if(prefs.getDatabaseHasTopRatedMovieData())
                currentDataUrl = FetchApiDataUtil.URL_TOP_RATED_MOVIE;
            else
                currentDataUrl = FetchApiDataUtil.URL_POPULAR_MOVIE;

            if(TextUtils.equals(currentDataUrl, sortOrderOptionUrl))
                return;
        }

        if(PreferenceUtil.getsInstance(this).isNetworkCallInProgress()) {
            Toast.makeText(this, getString(R.string.pls_retry), Toast.LENGTH_SHORT).show();
            emptyStateMessageHolder.setText(getString(R.string.pull_to_refresh_notif));
            switchScreen(EMPTY_STATE);
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
                FetchApiDataUtil.execute(CatalogActivity.this, sortOrderOptionUrl);
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

        //RecyclerView
        adapter = new MovieRecyclerAdapter(this, mMovieList);
        movieTileRecyclerView.setHasFixedSize(true);
        movieTileRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        movieTileRecyclerView.setAdapter(adapter);

        //Swipe Refresh Layout
        swipeRefreshContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshContainer.setRefreshing(false);

                if (TextUtils.isEmpty(currentSortOrder))
                    return;

                fetchMovieApiData(
                        TextUtils.equals(currentSortOrder, getString(R.string.pref_popular_movie_value)) ?
                                FetchApiDataUtil.URL_POPULAR_MOVIE : FetchApiDataUtil.URL_TOP_RATED_MOVIE
                );
            }
        });

        //switch screen
        switchScreen(FILLED_STATE);
    }

    private void switchScreen(int screenToShow) {

        if (screenToShow == EMPTY_STATE) {
            emptyStateContainer.setVisibility(View.VISIBLE);
            filledStateContainer.setVisibility(View.GONE);
        } else {
            emptyStateContainer.setVisibility(View.GONE);
            filledStateContainer.setVisibility(View.VISIBLE);
        }

    }

    private boolean doesCurrentDataMatchUserSortOrder(){
        if(prefs.getPrefApiDataPulledSuccessfully()){

            String currentDataUrl;
            String currentSortOrderUrl;

            //get the URL of the data in the database
            if(prefs.getDatabaseHasTopRatedMovieData())
                currentDataUrl = FetchApiDataUtil.URL_TOP_RATED_MOVIE;
            else
                currentDataUrl = FetchApiDataUtil.URL_POPULAR_MOVIE;

            //get the URL of the current sort order
            currentSortOrderUrl = TextUtils.equals(sharedPreference.getString(getString(R.string.pref_sort_order_key), ""),getString(R.string.pref_popular_movie_value)) ?
                    FetchApiDataUtil.URL_POPULAR_MOVIE : FetchApiDataUtil.URL_TOP_RATED_MOVIE;

            return TextUtils.equals(currentDataUrl, currentSortOrderUrl);
        }
        else
            return false;
    }
}
