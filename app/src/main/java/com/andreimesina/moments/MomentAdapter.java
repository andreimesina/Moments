package com.andreimesina.moments;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.andreimesina.moments.model.Moment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.List;

public class MomentAdapter extends RecyclerView.Adapter<MomentAdapter.MomentHolder> {
    private Context mContext;
    private List<Moment> mMoments;

    public MomentAdapter(Context context, List<Moment> moments) {
        mContext = context;
        mMoments = moments;
    }

    @NonNull
    @Override
    public MomentHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_moment,
                parent, false);

        Moment currentMoment = mMoments.get(i);
        view.setTag(currentMoment.getImageUrl());

        return new MomentHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MomentHolder momentHolder, int i) {
        Moment currentMoment = mMoments.get(i);
        momentHolder.textViewStory.setText(currentMoment.getStory());

        Glide.with(mContext)
                .load(currentMoment.getImageUrl())
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(momentHolder.imageView);
    }

    @Override
    public int getItemCount() {
        return mMoments.size();
    }

    public class MomentHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView textViewStory;

        public MomentHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.image_preview_card);
            textViewStory = itemView.findViewById(R.id.text_story_card);
        }
    }
}
