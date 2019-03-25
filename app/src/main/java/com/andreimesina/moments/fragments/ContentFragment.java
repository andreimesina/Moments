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
import android.widget.Toast;

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
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ContentFragment extends Fragment {

    private static final String TAG = "ContentFragment";

    private RecyclerView mRecyclerView;
    private MomentAdapter mAdapter;

    private ProgressBar mProgressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private ListenerRegistration mListener;

    private List<Moment> mMoments;

    private static boolean isFirstFetch = true;

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
        listenForMoments(thisActivity);
        setFloatingActionButton(thisActivity);
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
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
    }

    private void listenForMoments(final Activity activity) {
        CollectionReference collectionReference = mFirestore.collection("users")
                .document(mAuth.getUid()).collection("images");

        mListener = collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot values,
                                    @Nullable FirebaseFirestoreException e) {

                    mProgressBar.setVisibility(View.VISIBLE);
                    if (e != null) {
                        Toast.makeText(activity, e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        mProgressBar.setVisibility(View.GONE);
                        return;
                    }

                    for (DocumentChange dc : values.getDocumentChanges()) {
                        switch (dc.getType()) {
                            case ADDED:
                                dc.getDocument().toObject(Moment.class);
                                mMoments.add(0, dc.getDocument().toObject(Moment.class));
                                break;
                            case MODIFIED:
                                break;
                            case REMOVED:
                                removeMoment(dc.getDocument().toObject(Moment.class));
                                break;
                        }
                    }

                    mAdapter = new MomentAdapter(activity, mMoments);
                    mRecyclerView.setAdapter(mAdapter);

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mProgressBar.setVisibility(View.GONE);
                        }
                    }, 500);

                    isFirstFetch = false;
                }
        });
    }

    private void setFloatingActionButton(Activity activity) {
        activity.findViewById(R.id.fab_add_image).setVisibility(View.VISIBLE);
    }

    private boolean momentsContainUrl(String url) {
        for(Moment m : mMoments) {
            if(m.getImageUrl().equalsIgnoreCase(url)) {
                return true;
            }
        }

        return false;
    }

    private void removeMoment(Moment moment) {
        for(int i = 0; i < mMoments.size(); i++) {
            if(mMoments.get(i).getImageUrl().equalsIgnoreCase(moment.getImageUrl())) {
                mMoments.remove(i);
                break;
            }
        }
    }
}