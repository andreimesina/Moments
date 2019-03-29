package com.andreimesina.moments.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andreimesina.moments.R;

public class ContactFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.contact, container, false);
        container.findViewById(R.id.fab_add_image).setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setMailListener(view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(R.string.contact_title);
    }

    private void setMailListener(@Nullable View container) {
        TextView emailView = container.findViewById(R.id.text_developer_email);

        emailView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto","andreimesina98@gmail.com", null));
                mailIntent.putExtra(Intent.EXTRA_SUBJECT, "Mail from Moments");

                getContext().startActivity(Intent.createChooser(mailIntent, "Send mail"));
            }
        });
    }
}
