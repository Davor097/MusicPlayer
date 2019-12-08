package hr.tvz.android.grabovacmusicplayer;

import android.content.Context;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class SongItemArrayAdapter extends ArrayAdapter<Song> {

    private Context context;
    private List<Song> songList;
    private final OkHttpClient client = new OkHttpClient();
    private String albumArtUrl;

    public SongItemArrayAdapter(Context context, int resource, List<Song> songList) {
        super(context, resource, songList);
        this.context = context;
        this.songList = songList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View songItem = LayoutInflater.from(getContext()).inflate(R.layout.list_item_layout, parent, false);

        final Song song = songList.get(position);
        ImageView image = songItem.findViewById(R.id.listItemAlbumImage);
        TextView songName = songItem.findViewById(R.id.listItemSongName);
        TextView songArtist = songItem.findViewById(R.id.listItemArtistName);
        TextView songAlbum = songItem.findViewById(R.id.listItemAlbumName);

        Glide.with(context)
                .asBitmap()
                .load(song.getAlbumArtUrl())
                .into(image);
        songName.setText(song.getSongName());
        songAlbum.setText(song.getAlbumName());
        songArtist.setText(song.getArtistName());

        return songItem;
    }
}
