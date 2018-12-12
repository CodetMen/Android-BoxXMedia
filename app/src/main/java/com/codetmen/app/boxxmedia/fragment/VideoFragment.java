package com.codetmen.app.boxxmedia.fragment;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codetmen.app.boxxmedia.R;
import com.codetmen.app.boxxmedia.activities.RawVideoActivity;
import com.codetmen.app.boxxmedia.activities.UrlVideoActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


/**
 * A simple {@link Fragment} subclass.
 */
public class VideoFragment extends Fragment implements ActivityCompat.OnRequestPermissionsResultCallback {


    @BindView(R.id.btn_video_raw)
    CardView btnVideoRaw;
    @BindView(R.id.btn_video_url)
    CardView btnVideoUrl;
    Unbinder unbinder;

    ViewPager mLayout;

    private static final int PERMISSION_REQUEST_READ_STORAGE = 502;
    private static final int PERMISSION_REQUEST_INTERNET = 501;

    public VideoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_video, container, false);
        unbinder = ButterKnife.bind(this, view);

        // btnVideoRaw click and send to another activity
        btnVideoRaw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // check permission
                permissionStorageCheck();

            }
        });

        // btnVideoUrl click and send to another activity
        btnVideoUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // permission internet check
                permissionInternetCheck();

            }
        });

        return view;
    }

    // checking permission for storage
    private void permissionStorageCheck() {
        // Check storage permission
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // Permission is already available
            Snackbar.make(getView(), "Storage permission is available.", Snackbar.LENGTH_SHORT).show();
            // if request available go to the another activity
            Intent intentVideo = new Intent(getContext(), RawVideoActivity.class);
            startActivity(intentVideo);
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

    private void permissionInternetCheck() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED){
            // permission already set
            Snackbar.make(getView(), "Internet permission is available", Snackbar.LENGTH_SHORT).show();
            // if request is available go to another activity
            Intent intentVideo = new Intent(getContext(), UrlVideoActivity.class);
            startActivity(intentVideo);
        } else {
            requestInternetPermission();
        }
    }

    private void requestInternetPermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.INTERNET)) {

            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with a button to request the missing permission.
            Snackbar.make(getView(), "Internet access is required to retrieve data",
                    Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.INTERNET}, PERMISSION_REQUEST_INTERNET);
                }
            }).show();

        } else {
            Snackbar.make(getView(),
                    "Permission is not available. Requesting to connect internet.",
                    Snackbar.LENGTH_SHORT).show();
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.INTERNET}, PERMISSION_REQUEST_INTERNET);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // BEGIN_INCLUDE(onRequestPermissionsResult)
        if (requestCode == PERMISSION_REQUEST_READ_STORAGE) {
            // request for internet permission
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // Permission has been granted. Start camera preview Activity.
                Snackbar.make(mLayout, "Storage permission was granted. Starting preview.",
                        Snackbar.LENGTH_SHORT)
                        .show();
            } else {
                // Permission request was denied.
                Snackbar.make(mLayout, "Storage permission request was denied.",
                        Snackbar.LENGTH_SHORT)
                        .show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

}
