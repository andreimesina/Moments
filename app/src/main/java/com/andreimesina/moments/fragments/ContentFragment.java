package com.andreimesina.moments.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.andreimesina.moments.MomentAdapter;
import com.andreimesina.moments.R;
import com.andreimesina.moments.model.Moment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ContentFragment extends Fragment {

    private static final String TAG = "ContentFragment";

    private RecyclerView mRecyclerView;
    private MomentAdapter mAdapter;

    private ProgressBar mProgressBar;
    private View mGroupWelcome;

    private boolean isWelcomeVisible = false;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private ListenerRegistration mListener;

    private List<Moment> mMoments;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content, container, false);

        mMoments = new ArrayList<>();
        mProgressBar = getActivity().findViewById(R.id.progress_home);

        initFirebase();

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity thisActivity = getActivity();

        thisActivity.setTitle(R.string.home_title);

        initRecyclerView(thisActivity);
        listenForMoments();
        setFloatingActionButton(thisActivity);

        mGroupWelcome = getActivity().findViewById(R.id.group_welcome);
        if(mAdapter.getItemCount() == 0) {
            showWelcome();
        } else {
            hideWelcome();
        }
    }

    @Override
    public void onDestroy() {
        mListener.remove();
        super.onDestroy();
    }

    private void initRecyclerView(Activity activity) {
        mRecyclerView = activity.findViewById(R.id.content_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));

        mAdapter = new MomentAdapter(activity, mMoments);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
    }

    private void listenForMoments() {
        CollectionReference collectionReference = mFirestore.collection("users")
                .document(mAuth.getUid()).collection("images");

        mListener = collectionReference.addSnapshotListener(MetadataChanges.INCLUDE,
                    new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot values,
                                    @Nullable FirebaseFirestoreException e) {
                    mProgressBar.setVisibility(View.VISIBLE);

                    if (e != null) {
                        mProgressBar.setVisibility(View.GONE);
                        return;
                    }

                    for (DocumentChange dc : values.getDocumentChanges()) {
                        switch (dc.getType()) {
                            case ADDED:
                                dc.getDocument().toObject(Moment.class);
                                mMoments.add(0, dc.getDocument().toObject(Moment.class));

                                if(isWelcomeVisible) {
                                    hideWelcome();
                                }
                                break;
                            case MODIFIED:
                                updateMoment(dc.getDocument().toObject(Moment.class));
                                break;
                            case REMOVED:
                                removeMoment(dc.getDocument().toObject(Moment.class));
                                break;
                        }
                    }

                    mAdapter.notifyDataSetChanged();

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mProgressBar.setVisibility(View.GONE);
                        }
                    }, 500);
                }
        });
    }

    private void setFloatingActionButton(Activity activity) {
        activity.findViewById(R.id.fab_add_image).setVisibility(View.VISIBLE);
    }

    private void showWelcome() {
        mGroupWelcome.setVisibility(View.VISIBLE);
        isWelcomeVisible = true;
    }

    private void hideWelcome() {
        mGroupWelcome.setVisibility(View.GONE);
        isWelcomeVisible = false;
    }

    private void updateMoment(Moment moment) {
        for(int i = 0; i < mMoments.size(); i++) {
            if(mMoments.get(i).getImageUrl().equals(moment.getImageUrl())) {
                mMoments.get(i).setStory(moment.getStory());
                mMoments.get(i).setLocation(moment.getLocation());
                break;
            }
        }
    }

    private void removeMoment(Moment moment) {
        for(int i = 0; i < mMoments.size(); i++) {
            if(mMoments.get(i).getImageUrl().equals(moment.getImageUrl())) {
                mMoments.remove(i);
                break;
            }
        }
    }
}
