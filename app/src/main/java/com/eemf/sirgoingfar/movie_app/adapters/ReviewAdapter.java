package com.eemf.sirgoingfar.movie_app.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eemf.sirgoingfar.movie_app.R;
import com.eemf.sirgoingfar.movie_app.data.db.MovieReviewEntity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.Holder> {

    private Context mContext;
    private List<MovieReviewEntity> mList;

    public ReviewAdapter(Context mContext) {
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ReviewAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(mContext).inflate(R.layout.item_movie_review, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewAdapter.Holder holder, int position) {
        holder.reviewer.setText(holder.getCurrentItem().getReviewer().isEmpty() ? mContext.getString(R.string.keyword_anonymous) : holder.getCurrentItem().getReviewer());
        holder.review.setText(holder.getCurrentItem().getReview().isEmpty() ? mContext.getString(R.string.keyphrase_no_review) : holder.getCurrentItem().getReview());
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void setmList(List<MovieReviewEntity> list) {
        if (list != null)
            mList = list;
    }

    public class Holder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_reviewer)
        TextView reviewer;

        @BindView(R.id.tv_review)
        TextView review;

        Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        MovieReviewEntity getCurrentItem() {
            return mList.get(getAdapterPosition());
        }
    }
}
