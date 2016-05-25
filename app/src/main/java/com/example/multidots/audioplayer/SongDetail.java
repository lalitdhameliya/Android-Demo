package com.example.multidots.audioplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.multidots.audioplayer.utils.AppConfig;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by multidots on 18-May-16.
 */
public class SongDetail extends AppCompatActivity implements View.OnClickListener {

    private TextView tvSongTitle;
    private TextView tvAlbumName;
    private TextView tvArtistName;
    private SeekBar seekBar;
    private Context context;
    private ImageView ivPlay;
    private ImageView ivPrevious;
    private ImageView ivNext;
    private TextView tvTotalTime;
    private TextView tvCurrentTime;
    private HashMap<String, String> audioFile;

    private long duration;
    private long currentTime;
    private int counter;
    private AudioManager audioManager;

    private ImageView ivCover;
    private ImageView ivMainCover;
    public static NotificationManager mNotificationManager;
    public static Notification notification;
    public static RemoteViews contentView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_detail);

        initialize();
    }

    private void initialize() {
        context = SongDetail.this;
        audioFile = AppConfig.arrSongList.get(AppConfig.SELECTED_POSITION);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        tvSongTitle = (TextView) findViewById(R.id.activity_detail_tv_song_name);
        tvAlbumName = (TextView) findViewById(R.id.activity_detail_tv_album);
        tvArtistName = (TextView) findViewById(R.id.activity_detail_tv_artist);
        tvCurrentTime = (TextView) findViewById(R.id.activity_detail_tv_current_time);
        tvTotalTime = (TextView) findViewById(R.id.activity_detail_tv_total_time);

        ivMainCover = (ImageView) findViewById(R.id.activity_detail_iv_main_cover);
        ivCover = (ImageView) findViewById(R.id.activity_detail_iv_cover);

        ivPlay = (ImageView) findViewById(R.id.activity_detail_iv_play);
        ivPrevious = (ImageView) findViewById(R.id.activity_detail_iv_previous);
        ivNext = (ImageView) findViewById(R.id.activity_detail_iv_next);
        ivPlay.setOnClickListener(this);
        ivPrevious.setOnClickListener(this);
        ivNext.setOnClickListener(this);

        seekBar = (SeekBar) findViewById(R.id.activity_detail_seek_bar);

        /**
         * setting up data of songs
         */
        try {
            AppConfig.mediaPlayer.reset();
            AppConfig.mediaPlayer.setDataSource(context, Uri.fromFile(new File(audioFile.get("song_path"))));
            AppConfig.mediaPlayer.prepare();
            AppConfig.mediaPlayer.start();
            setDetail();
            ivPlay.setImageResource(R.drawable.ic_pause_white_24dp);
        } catch (IOException e) {
            e.printStackTrace();
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                AppConfig.mediaPlayer.seekTo(seekBar.getProgress());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                AppConfig.mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        AppConfig.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                try {
                    if (AppConfig.SELECTED_POSITION == AppConfig.arrSongList.size() - 1) {
                        AppConfig.SELECTED_POSITION = 0;
                    } else {
                        AppConfig.SELECTED_POSITION += 1;
                    }
                    audioFile = AppConfig.arrSongList.get(AppConfig.SELECTED_POSITION);
                    AppConfig.mediaPlayer.reset();
                    AppConfig.mediaPlayer.setDataSource(context, Uri.fromFile(new File(audioFile.get("song_path"))));
                    AppConfig.mediaPlayer.prepare();
                    AppConfig.mediaPlayer.start();
                    setDetail();
                    sendNotification();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        sendNotification();
    }

    @Override
    public void onClick(View v) {
        if (v == ivPlay) {
            sendNotification();
            if (AppConfig.mediaPlayer.isPlaying()) {
                AppConfig.mediaPlayer.pause();
                ivPlay.setImageResource(R.drawable.ic_play_arrow_white_24dp);
            } else {
                ivPlay.setImageResource(R.drawable.ic_pause_white_24dp);
                tvCurrentTime.post(mUpdateTime);
                AppConfig.mediaPlayer.start();
            }
        } else if (v == ivPrevious) {
            if (AppConfig.SELECTED_POSITION == 0)
                AppConfig.SELECTED_POSITION = 0;
            else
                AppConfig.SELECTED_POSITION -= 1;

            audioFile = AppConfig.arrSongList.get(AppConfig.SELECTED_POSITION);
            try {
                AppConfig.mediaPlayer.reset();
                AppConfig.mediaPlayer.setDataSource(context, Uri.fromFile(new File(audioFile.get("song_path"))));
                AppConfig.mediaPlayer.prepare();
                AppConfig.mediaPlayer.start();
                ivPlay.setImageResource(R.drawable.ic_pause_white_24dp);
                setDetail();
                sendNotification();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (v == ivNext) {
            if (AppConfig.SELECTED_POSITION == AppConfig.arrSongList.size() - 1)
                AppConfig.SELECTED_POSITION = 0;
            else
                AppConfig.SELECTED_POSITION += 1;

            audioFile = AppConfig.arrSongList.get(AppConfig.SELECTED_POSITION);
            try {
                AppConfig.mediaPlayer.reset();
                AppConfig.mediaPlayer.setDataSource(context, Uri.fromFile(new File(audioFile.get("song_path"))));
                AppConfig.mediaPlayer.prepare();
                AppConfig.mediaPlayer.start();
                ivPlay.setImageResource(R.drawable.ic_pause_white_24dp);
                setDetail();
                sendNotification();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setDetail() {
        tvCurrentTime.post(mUpdateTime);
        tvSongTitle.setText(audioFile.get("song_title"));
        tvAlbumName.setText(audioFile.get("song_album"));
        tvArtistName.setText(audioFile.get("song_artist"));

        duration = Integer.parseInt(audioFile.get("song_duration"));
        currentTime = duration;
        seekBar.setMax(Integer.parseInt(String.valueOf(duration)));

        tvTotalTime.setText(String.format(Locale.getDefault(), "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(
                        TimeUnit.MILLISECONDS.toHours(duration)),
                TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(duration))));

        Bitmap coverBitmap = AppConfig.getCoverImage(context, audioFile.get("song_path"));
        if (null != coverBitmap) {
            ivMainCover.setImageBitmap(coverBitmap);
            ivCover.setImageBitmap(coverBitmap);
        }
    }

    private Runnable mUpdateTime = new Runnable() {
        public void run() {
            int currentDuration;
            if (AppConfig.mediaPlayer.isPlaying()) {
                currentDuration = AppConfig.mediaPlayer.getCurrentPosition();
                updatePlayer(currentDuration);
                tvCurrentTime.postDelayed(this, 1000);
            } else {
                tvCurrentTime.removeCallbacks(this);
            }
        }
    };

    private void updatePlayer(int currentDuration) {
        seekBar.setProgress(currentDuration);
        tvCurrentTime.setText("" + AppConfig.milliSecondsToTimer((long) currentDuration));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        sendNotification();
    }

    public void sendNotification() {
        int icon = R.drawable.ic_launcher;
        long when = System.currentTimeMillis();
        notification = new Notification(icon, "", when);

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        contentView = new RemoteViews(getPackageName(), R.layout.player_notification);

        Bitmap coverBitmap = AppConfig.getCoverImage(context, audioFile.get("song_path"));
        if (null != coverBitmap) {
            contentView.setImageViewBitmap(R.id.notification_img, coverBitmap);
        }
        contentView.setImageViewResource(R.id.notification_iv_close,android.R.drawable.ic_menu_close_clear_cancel);
        contentView.setTextViewText(R.id.notification_tv_song_name, audioFile.get("song_title"));
        notification.contentView = contentView;

        Intent notificationIntent = new Intent(this, SongDetail.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.contentIntent = contentIntent;

        notification.flags |= Notification.FLAG_NO_CLEAR; //Do not clear the notification
//        notification.defaults |= Notification.DEFAULT_LIGHTS; // LED
//        notification.defaults |= Notification.DEFAULT_VIBRATE; //Vibration
//        notification.defaults |= Notification.DEFAULT_SOUND; // Sound

        /**
         * for previous button
         */
        Intent previousIntent = new Intent(this, SwitchButtonListener.class);
        previousIntent.setAction("previous");
        PendingIntent previousPendingIntent = PendingIntent.getBroadcast(this, 0, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.contentView.setOnClickPendingIntent(R.id.notification_iv_previous,
                previousPendingIntent);

        /**
         * for next button
         */
        Intent nextIntent = new Intent(this, SwitchButtonListener.class);
        nextIntent.setAction("next");
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.contentView.setOnClickPendingIntent(R.id.notification_iv_next,
                nextPendingIntent);

        /**
         * for next button
         */
        Intent playIntent = new Intent(this, SwitchButtonListener.class);
        playIntent.setAction("play");
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.contentView.setOnClickPendingIntent(R.id.notification_iv_play,
                playPendingIntent);

        /**
         * for closing notification
         */
        Intent closeIntent = new Intent(this, SwitchButtonListener.class);
        closeIntent.setAction("close");
        PendingIntent closePendingIntent = PendingIntent.getBroadcast(this, 0, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.contentView.setOnClickPendingIntent(R.id.notification_iv_close,
                closePendingIntent);

        mNotificationManager.notify(1, notification);
    }

    public static void changeNotificationState(String state) {
    }

    public static class SwitchButtonListener extends BroadcastReceiver {

        private HashMap<String, String> rawData;

        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("RECEIVED ACTION:" + intent.getAction());
            String action = intent.getAction();
            SongDetail songDetail = new SongDetail();
            if (action.equals("play")) {
                if (AppConfig.mediaPlayer.isPlaying()) {
                    AppConfig.mediaPlayer.pause();
                    contentView.setImageViewResource(R.id.notification_iv_play, R.drawable.ic_play_arrow_white_24dp);
                    notification.contentView = contentView;
                    mNotificationManager.notify(1, notification);
                } else {
                    AppConfig.mediaPlayer.start();
                    contentView.setImageViewResource(R.id.notification_iv_play, R.drawable.ic_pause_white_24dp);
                    notification.contentView = contentView;
                    mNotificationManager.notify(1, notification);
                }
//                ((SongDetail)context).setDetail();
            } else if (action.equals("next")) {
                if (AppConfig.SELECTED_POSITION == AppConfig.arrSongList.size() - 1)
                    AppConfig.SELECTED_POSITION = 0;
                else
                    AppConfig.SELECTED_POSITION += 1;

                rawData = AppConfig.arrSongList.get(AppConfig.SELECTED_POSITION);
                try {
                    AppConfig.mediaPlayer.reset();
                    AppConfig.mediaPlayer.setDataSource(context, Uri.fromFile(new File(rawData.get("song_path"))));
                    AppConfig.mediaPlayer.prepare();
                    AppConfig.mediaPlayer.start();
                    contentView.setTextViewText(R.id.notification_tv_song_name, rawData.get("song_title"));
                    notification.contentView = contentView;
                    mNotificationManager.notify(1, notification);
//                    songDetail.ivPlay.setImageResource(R.drawable.ic_pause_white_24dp);
//                    songDetail.setDetail();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (action.equals("previous")) {
                if (AppConfig.SELECTED_POSITION == 0)
                    AppConfig.SELECTED_POSITION = 0;
                else
                    AppConfig.SELECTED_POSITION -= 1;

                rawData = AppConfig.arrSongList.get(AppConfig.SELECTED_POSITION);
                try {
                    AppConfig.mediaPlayer.reset();
                    AppConfig.mediaPlayer.setDataSource(context, Uri.fromFile(new File(rawData.get("song_path"))));
                    AppConfig.mediaPlayer.prepare();
                    AppConfig.mediaPlayer.start();
                    contentView.setTextViewText(R.id.notification_tv_song_name, rawData.get("song_title"));
                    notification.contentView = contentView;
                    mNotificationManager.notify(1, notification);
//                    songDetail.ivPlay.setImageResource(R.drawable.ic_pause_white_24dp);
//                    songDetail.setDetail();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else if (action.equals("close")){
                AppConfig.mediaPlayer.reset();
                mNotificationManager.cancelAll();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNotificationManager.cancel(1);
    }
}
