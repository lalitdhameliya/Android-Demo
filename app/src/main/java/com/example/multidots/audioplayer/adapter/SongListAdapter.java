package com.example.multidots.audioplayer.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.multidots.audioplayer.R;
import com.example.multidots.audioplayer.utils.AppConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by multidots on 18-May-16.
 */
public class SongListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<HashMap<String, String>> rawItems;
    private LayoutInflater inflater;

    public SongListAdapter(Context context, ArrayList<HashMap<String, String>> rawItems) {
        this.context = context;
        this.rawItems = rawItems;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return rawItems.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = new ViewHolder();
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_play_list, null);
        }
        holder.tvSongName = (TextView) convertView.findViewById(R.id.item_play_list_tv_song_name);
        holder.tvSongAlbum = (TextView) convertView.findViewById(R.id.item_play_list_tv_album);
        holder.tvSongArtist = (TextView) convertView.findViewById(R.id.item_play_list_tv_artist);
        holder.tvDuration = (TextView) convertView.findViewById(R.id.item_play_list_tv_duration);
        holder.ivCover = (ImageView) convertView.findViewById(R.id.item_play_list_img);

        HashMap<String, String> item = rawItems.get(position);

        if (item.get("song_title").toString().trim().length() > 0)
            holder.tvSongName.setText(item.get("song_title"));
        if (item.get("song_album").toString().trim().length() > 0)
            holder.tvSongAlbum.setText(item.get("song_album"));
        if (item.get("song_artist").toString().trim().length() > 0)
            holder.tvSongArtist.setText(item.get("song_artist"));
        if (item.get("song_duration").toString().trim().length() > 0)
            holder.tvDuration.setText(AppConfig.milliSecondsToTimer(Long.parseLong(item.get("song_duration"))));


        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        byte[] rawArt;
        Bitmap art = null;
        BitmapFactory.Options bfo = new BitmapFactory.Options();

        mmr.setDataSource(context, Uri.parse(item.get("song_path")));
        rawArt = mmr.getEmbeddedPicture();

// if rawArt is null then no cover art is embedded in the file or is not
// recognized as such.
        if (null != rawArt) {
            art = BitmapFactory.decodeByteArray(rawArt, 0, rawArt.length, bfo);
            holder.ivCover.setImageBitmap(art);
        }
        return convertView;
    }

    private class ViewHolder {
        TextView tvSongName, tvSongAlbum, tvSongArtist, tvDuration;
        ImageView ivCover;
    }
}
