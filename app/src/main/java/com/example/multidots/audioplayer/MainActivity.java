package com.example.multidots.audioplayer;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.multidots.audioplayer.adapter.SongListAdapter;
import com.example.multidots.audioplayer.utils.AppConfig;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity{

    private Cursor cursor;
    private ListView lvSongs;
    private SongListAdapter adapter;
    private Context context;
    private MediaPlayer mediaPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();
    }

    private void initialize() {
        context = MainActivity.this;
        lvSongs = (ListView) findViewById(R.id.activity_main_lv);

        //This will get the list of songs available in the external storage.
        getSongList();
        lvSongs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    AppConfig.SELECTED_POSITION = position;
                    startActivity(new Intent(MainActivity.this,SongDetail.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void getSongList() {
        AppConfig.arrSongList.clear();
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION
        };

        cursor = this.managedQuery(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null);

        while (cursor.moveToNext()) {
            HashMap<String, String> tmpSong = new HashMap<String, String>();
            tmpSong.put("song_id", cursor.getString(0));
            tmpSong.put("song_artist", cursor.getString(1));
            tmpSong.put("song_title", cursor.getString(2));
            tmpSong.put("song_path", cursor.getString(3));
            tmpSong.put("song_album", cursor.getString(4));
            tmpSong.put("song_duration", cursor.getString(5));
            AppConfig.arrSongList.add(tmpSong);
        }
        if (AppConfig.arrSongList!=null && AppConfig.arrSongList.size()>0) {
            adapter = new SongListAdapter(context, AppConfig.arrSongList);
            lvSongs.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }
}
