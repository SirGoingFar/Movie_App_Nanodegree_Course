package com.eemf.sirgoingfar.movie_app.activities;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.eemf.sirgoingfar.movie_app.R;
import com.eemf.sirgoingfar.movie_app.data.db.MovieAppRoomDatabase;
import com.eemf.sirgoingfar.movie_app.data.db.MovieEntity;
import com.eemf.sirgoingfar.movie_app.models.MovieDetailViewModel;
import com.eemf.sirgoingfar.movie_app.models.MovieDetailViewModelFactory;
import com.eemf.sirgoingfar.movie_app.utils.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;

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

    private MovieEntity movieObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();

        if(actionBar != null)
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
        MovieDetailViewModel model = ViewModelProviders.of(this, factory).get(MovieDetailViewModel.class);
        model.getMovieObject().observe(this, new Observer<MovieEntity>() {
            @Override
            public void onChanged(@Nullable MovieEntity movieEntity) {
                movieObject = movieEntity;
                setupView();
            }
        });
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        else
            return super.onOptionsItemSelected(item);
    }
}
