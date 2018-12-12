package com.codetmen.app.boxxmedia.fragment;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codetmen.app.boxxmedia.R;
import com.codetmen.app.boxxmedia.activities.RawAudioActivity;
import com.codetmen.app.boxxmedia.activities.UrlAudioActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


/**
 * A simple {@link Fragment} subclass.
 */
public class AudioFragment extends Fragment implements ActivityCompat.OnRequestPermissionsResultCallback {


    @BindView(R.id.btn_audio_raw)
    CardView btnAudioRaw;
    @BindView(R.id.btn_audio_url)
    CardView btnAudioUrl;
    Unbinder unbinder;

    ViewPager mLayout;

    private static final int PERMISSION_REQUEST_READ_STORAGE = 502;

    public AudioFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_audio, container, false);
        unbinder = ButterKnife.bind(this, view);

        // btnAudioRaw click and send to another activity
        btnAudioRaw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // check permission
                permissionStorageCheck();

            }
        });

        // btnAudioUrl click and send to another activity
        btnAudioUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentRaw = new Intent(getContext(), UrlAudioActivity.class);
                startActivity(intentRaw);
            }
        });

        return view;
    }

    // Checking permission
    private void permissionStorageCheck() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            // permissio already available
            Snackbar.make(getView(), "Storage permission available", Snackbar.LENGTH_SHORT).show();
            // if request available go to the another activity
            Intent intentRaw = new Intent(getContext(), RawAudioActivity.class);
            startActivity(intentRaw);
        } else {
            requestReadStoragePermission();
        }
    }

    // Request permission for reading storage
    public void requestReadStoragePermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {

            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with a button to request the missing permission.
            Snackbar.make(getView(), "Storage access is required to retrieve data",
                    Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_READ_STORAGE);
                }
            }).show();

        } else {
            Snackbar.make(getView(),
                    "Permission is not available. Requesting to read storage permission.",
                    Snackbar.LENGTH_SHORT).show();
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_READ_STORAGE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

}
