package com.andreimesina.moments;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.andreimesina.moments.model.Moment;
import com.andreimesina.moments.utils.ActivityUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

public class MomentAdapter extends RecyclerView.Adapter<MomentAdapter.MomentHolder> {

    private static final String TAG = "MomentAdapter";

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

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
    public void onBindViewHolder(@NonNull MomentHolder momentHolder, int position) {
        Moment currentMoment = mMoments.get(position);
        String story = currentMoment.getStory();
        String location = currentMoment.getLocation();

        if(story.length() > 0) {
            momentHolder.textViewStory.setText("Story: " + currentMoment.getStory());
        } else {
            momentHolder.textViewStory.setText("No story");
        }

        if(location.length() > 0) {
            momentHolder.textViewLocation.setVisibility(View.VISIBLE);
            momentHolder.textViewLocation.setText("Location: " + currentMoment.getLocation());
        } else {
            momentHolder.textViewLocation.setVisibility(View.GONE);
        }

        TextView moreView = momentHolder.itemView.findViewById(R.id.text_more);

        if(momentHolder.textViewStory.getLineCount() > 1) {
            moreView.setVisibility(View.VISIBLE);
        }

        loadImage(momentHolder, currentMoment);
        setLastItemMargin(momentHolder, position);
        listenForMenuOptionsClick(momentHolder, currentMoment);
    }

    @Override
    public int getItemCount() {
        return mMoments.size();
    }

    private void loadImage(@NonNull MomentHolder momentHolder, Moment currentMoment) {
        Glide.with(mContext)
                .load(currentMoment.getImageUrl())
                .transition(DrawableTransitionOptions.withCrossFade())
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(momentHolder.imageView);
    }

    private void setLastItemMargin(@NonNull MomentHolder momentHolder, int position) {
        RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) momentHolder.itemView.getLayoutParams();

        if(position == mMoments.size() - 1) {
            lp.setMargins((int) ActivityUtils.dpToPx(mContext, 8),
                    (int) ActivityUtils.dpToPx(mContext, 8),
                    (int) ActivityUtils.dpToPx(mContext, 8),
                    (int) ActivityUtils.dpToPx(mContext, 60));
        } else {
            lp.setMargins((int) ActivityUtils.dpToPx(mContext, 8),
                    (int) ActivityUtils.dpToPx(mContext, 8),
                    (int) ActivityUtils.dpToPx(mContext, 8),
                    0);
        }

        momentHolder.itemView.setLayoutParams(lp);
    }

    private void listenForMenuOptionsClick(@NonNull final MomentHolder momentHolder,
                                           final Moment currentMoment) {
        final ImageButton btnOptions = momentHolder.itemView.findViewById(R.id.btn_overflow_card);

        btnOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptionsMenu(btnOptions, currentMoment);
            }
        });
    }

    private void showOptionsMenu(View view, final Moment currentMoment) {
        PopupMenu popupMenu = new PopupMenu(mContext, view);
        MenuInflater menuInflater = popupMenu.getMenuInflater();
        menuInflater.inflate(R.menu.card_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.item_card_edit) {
                    Toast.makeText(mContext, "Edit", Toast.LENGTH_SHORT).show();
                    return true;
                } else if(item.getItemId() == R.id.item_card_delete) {
                    deleteImageFromFirebase(currentMoment);
                    return true;
                }
                return false;
            }
        });

        popupMenu.show();
    }

    private void deleteImageFromFirebase(final Moment moment) {
        storage.getReference("users/" + auth.getUid() + "/" +
                auth.getCurrentUser().getDisplayName() + "/images/" + moment.getFilename())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        firestore.collection("users").document(auth.getUid())
                                .collection("images").document(moment.getFilename())
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error deleting moment", e);
                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(mContext, "Error deleting moment" + e, Toast.LENGTH_SHORT)
                                .show();
                    }
        });
    }
    
    public class MomentHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView textViewStory;
        public TextView textViewLocation;

        public MomentHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.image_preview_card);
            textViewStory = itemView.findViewById(R.id.text_story_card);
            textViewLocation = itemView.findViewById(R.id.text_location_card);
        }
    }
}
