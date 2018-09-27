package com.eemf.sirgoingfar.movie_app.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.eemf.sirgoingfar.movie_app.R;
import com.eemf.sirgoingfar.movie_app.activities.MovieDetailActivity;
import com.eemf.sirgoingfar.movie_app.data.db.MovieEntity;
import com.eemf.sirgoingfar.movie_app.utils.Constants;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MovieRecyclerAdapter extends RecyclerView.Adapter<MovieRecyclerAdapter.Holder> {

    private Context mContext;
    private ArrayList<MovieEntity> mMovieList;

    public MovieRecyclerAdapter(Context mContext) {
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_movie_list, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, int position) {

        Glide.with(mContext)
                .load(Constants.IMAGE_PREFIX.concat(holder.getCurrentItem().getImagePath()))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.moviePoster);

        holder.moviePoster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MovieDetailActivity.class);
                intent.putExtra(Constants.EXTRA_CLICKED_MOVIE_ID, (int) holder.itemView.getTag());
                mContext.startActivity(intent);
            }
        });

        holder.itemView.setTag(holder.getCurrentItem().getId());
    }

    @Override
    public int getItemCount() {
        return mMovieList.size();
    }

    class Holder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_movie_poster)
        ImageView moviePoster;

        Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        MovieEntity getCurrentItem() {
            return mMovieList.get(getAdapterPosition());
        }
    }

    public void setmMovieList(ArrayList<MovieEntity> mMovieList){
        this.mMovieList = mMovieList;
        notifyDataSetChanged();
    }
}
