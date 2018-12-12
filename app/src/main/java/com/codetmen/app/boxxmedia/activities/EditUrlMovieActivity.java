package com.codetmen.app.boxxmedia.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.codetmen.app.boxxmedia.R;
import com.codetmen.app.boxxmedia.db_app.VideoUrlHelper;
import com.codetmen.app.boxxmedia.video_package.MovieUrl;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditUrlMovieActivity extends AppCompatActivity {

    @BindView(R.id.et_title_movie)
    EditText etTitleMovie;
    @BindView(R.id.et_url_movie)
    EditText etUrlMovie;
    @BindView(R.id.btn_save_movie)
    CardView btnSaveMovie;
    @BindView(R.id.btn_cancel_movie)
    CardView btnCancelMovie;
    @BindView(R.id.tv_edit_or_save_movie)
    TextView tvEdtOrSvMovie;

    public static String EXTRA_VIDEO = "Extra_Video";
    public static String EXTRA_POSITION = "Extra_Position";
    public static int REQUEST_ADD = 100;
    public static int RESULT_ADD = 101;
    public static int REQUEST_UPDATE = 200;
    public static int RESULT_UPDATE = 201;
    public static int RESULT_DELETE = 301;

    final int ALERT_DIALOG_CLOSE = 10;
    final int ALERT_DIALOG_DELETE = 20;

    private boolean isEdit = false;
    private boolean isEmpty = false;

    private MovieUrl movieUrl;
    private int position;
    private VideoUrlHelper videoUrlHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_url_movie);
        ButterKnife.bind(this);

        // open database
        videoUrlHelper = new VideoUrlHelper(this);
        videoUrlHelper.open();

        movieUrl = getIntent().getParcelableExtra(EXTRA_VIDEO);

        if (movieUrl != null){
            position = getIntent().getIntExtra(EXTRA_POSITION, 0);
            isEdit = true;
        }

        String actionBarTitle = null;
        String btnSaveTitle = null;

        if (isEdit){
            actionBarTitle = "Edit Video";
            btnSaveTitle = "Edit";
            etTitleMovie.setText(movieUrl.getTitleMovie());
            etUrlMovie.setText(movieUrl.getUrlMovie());
        } else {
            actionBarTitle = "Add Video";
            btnSaveTitle = "Save";
        }

        getSupportActionBar().setTitle(actionBarTitle);
        tvEdtOrSvMovie.setText(btnSaveTitle);

        // set click action save video
        btnSaveMovie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action_save_video();
            }
        });

        // set click action cancel video
        btnCancelMovie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog(ALERT_DIALOG_CLOSE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoUrlHelper != null){
            videoUrlHelper.close();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        showAlertDialog(ALERT_DIALOG_CLOSE);
    }

    private void action_save_video() {

        // get data from edittext
        String titleVideo = etTitleMovie.getText().toString().trim();
        String urlVideo = etUrlMovie.getText().toString().trim();

        // if field is empty so the error will display
        if (TextUtils.isEmpty(titleVideo)){
            isEmpty =  true;
            etTitleMovie.setError(getResources().getString(R.string.warn_empty_field));
        } else if (TextUtils.isEmpty(urlVideo)){
            isEmpty = true;
            etUrlMovie.setError(getResources().getString(R.string.warn_empty_field));
        }

        // data will insert it if not empty
        if (!isEmpty){
            MovieUrl newMovie = new MovieUrl();
            newMovie.setTitleMovie(titleVideo);
            newMovie.setUrlMovie(urlVideo);

            Intent intent = new Intent();

            // if edit so setresult is update otherwise setresult is add
            if (isEdit){
                newMovie.setId(movieUrl.getId());
                videoUrlHelper.update(newMovie);
                intent.putExtra(EXTRA_POSITION, position);
                setResult(RESULT_UPDATE, intent);
                finish();
            } else {
                videoUrlHelper.insert(newMovie);
                setResult(RESULT_ADD);
                finish();
            }
        }
    }

    // Confirmatiopn dialog before exit or cancel
    private void showAlertDialog(int type){
        final boolean isDialogClose = type == ALERT_DIALOG_CLOSE;
        String dialogTitle = null, dialogMessage = null;

        if (isDialogClose){
            dialogTitle = "Batal";
            dialogMessage = "Apakah anda ingin membatalkan perubahan pada form?";
        }else{
            dialogTitle = "Hapus Note";
            dialogMessage = "Apakah anda yakin ingin menghapus item ini?";
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle(dialogTitle);
        alertDialogBuilder
                .setMessage(dialogMessage)
                .setCancelable(false)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (isDialogClose){
                            finish();
                        }else{
                            videoUrlHelper.delete(movieUrl.getId());
                            Intent intent = new Intent();
                            intent.putExtra(EXTRA_POSITION, position);
                            setResult(RESULT_DELETE, intent);
                            finish();
                        }
                    }
                })
                .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
