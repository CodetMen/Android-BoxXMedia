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
import com.codetmen.app.boxxmedia.audio_package.SongUrl;
import com.codetmen.app.boxxmedia.db_app.SongUrlHelper;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditUrlSongActivity extends AppCompatActivity {

    @BindView(R.id.et_title_song)
    EditText etTitleSong;
    @BindView(R.id.et_url_song)
    EditText etUrlSong;
    @BindView(R.id.btn_save_song)
    CardView btnSaveSong;
    @BindView(R.id.btn_cancel_song)
    CardView btnCancelSong;
    @BindView(R.id.tv_edit_or_save_song)
    TextView tvEdtOrSvSong;

    public static String EXTRA_SONG = "Extra_Song";
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

    private SongUrl songUrl;
    private int position;
    private SongUrlHelper songUrlHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_url_song);
        ButterKnife.bind(this);

        // open database
        songUrlHelper = new SongUrlHelper(this);
        songUrlHelper.open();

        songUrl = getIntent().getParcelableExtra(EXTRA_SONG);

        if (songUrl != null){
            position = getIntent().getIntExtra(EXTRA_POSITION, 0);
            isEdit = true;
        }

        String actionBarTitle = null;
        String btnSaveTitle = null;

        if (isEdit){
            actionBarTitle = "Edit Song";
            btnSaveTitle = "Edit";
            etTitleSong.setText(songUrl.getTitleSong());
            etUrlSong.setText(songUrl.getUrlSong());
        } else {
            actionBarTitle = "Add Song";
            btnSaveTitle = "Save";
        }

        getSupportActionBar().setTitle(actionBarTitle);
        tvEdtOrSvSong.setText(btnSaveTitle);

        // set click action save song
        btnSaveSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action_save_song();
            }
        });

        // set click action cancel song
        btnCancelSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog(ALERT_DIALOG_CLOSE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (songUrlHelper != null){
            songUrlHelper.close();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        showAlertDialog(ALERT_DIALOG_CLOSE);
    }

    private void action_save_song() {

        // get data from edittext
        String titleSong = etTitleSong.getText().toString().trim();
        String urlSong = etUrlSong.getText().toString().trim();

        // if field is empty so the error will display
        if (TextUtils.isEmpty(titleSong)){
            isEmpty = true;
            etTitleSong.setError(getResources().getString(R.string.warn_empty_field));
        } else if (TextUtils.isEmpty(urlSong)){
            isEmpty = true;
            etUrlSong.setError(getResources().getString(R.string.warn_empty_field));
        }

        // data will insert it if not empty
        if (!isEmpty){
            SongUrl newSong = new SongUrl();
            newSong.setTitleSong(titleSong);
            newSong.setUrlSong(urlSong);

            Intent intent = new Intent();

            // if edit so setresult is update otherwise setresult is add
            if (isEdit){
                newSong.setId(songUrl.getId());
                songUrlHelper.update(newSong);
                intent.putExtra(EXTRA_POSITION, position);
                setResult(RESULT_UPDATE, intent);
                finish();
            } else {
                songUrlHelper.insert(newSong);
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
                    public void onClick(DialogInterface dialog,int id) {
                        if (isDialogClose){
                            finish();
                        }else{
                            songUrlHelper.delete(songUrl.getId());
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