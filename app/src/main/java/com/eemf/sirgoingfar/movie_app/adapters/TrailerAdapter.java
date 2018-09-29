package com.eemf.sirgoingfar.movie_app.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eemf.sirgoingfar.movie_app.R;
import com.eemf.sirgoingfar.movie_app.data.db.MovieTrailerEntity;
import com.eemf.sirgoingfar.movie_app.utils.Constants;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.Holder> {

    private Context mContext;
    private List<MovieTrailerEntity> mList;

    public TrailerAdapter(Context mContext) {
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public TrailerAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(mContext).inflate(R.layout.item_movie_trailer, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TrailerAdapter.Holder holder, int position) {
        holder.videoTypeHolder.setText(holder.getCurrentItem().getTrailerType());
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void setmList(List<MovieTrailerEntity> list) {
        if (list != null)
            mList = list;
    }

    public class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.tv_video_type)
        TextView videoTypeHolder;

        @BindView(R.id.container)
        ConstraintLayout container;

        public Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            container.setOnClickListener(this);
        }

        MovieTrailerEntity getCurrentItem() {
            return mList.get(getAdapterPosition());
        }

        @Override
        public void onClick(View clickedView) {

            switch (clickedView.getId()) {

                case R.id.container:
                    //Launch Browser
                    String trailerUrl = Constants.YOUTUBE_BASE_URL.concat(getCurrentItem().getTrailerKey());
                    Intent openTrailerIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(trailerUrl));
                    openTrailerIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    if (openTrailerIntent.resolveActivity(mContext.getPackageManager()) != null)
                        mContext.startActivity(openTrailerIntent);

                    break;

            }
        }
    }
}
