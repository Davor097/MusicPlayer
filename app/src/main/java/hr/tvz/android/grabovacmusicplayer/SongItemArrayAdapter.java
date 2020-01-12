package hr.tvz.android.grabovacmusicplayer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class SongItemArrayAdapter extends ArrayAdapter<Song> {

    private Context context;
    private List<Song> songList;
    private List<Song> allSongList;

    public SongItemArrayAdapter(Context context, int resource, List<Song> songList) {
        super(context, resource, songList);
        this.context = context;
        this.songList = songList;
        this.allSongList = new ArrayList<>(songList);
    }

    public void setAllSongList(List<Song> allSongList) {
        this.allSongList = new ArrayList<>(allSongList);
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

    @NonNull
    @Override
    public Filter getFilter() {
        return songFilter;
    }

    private Filter songFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            Log.d("FILTER START", "all list: " + allSongList.size());
            FilterResults results = new FilterResults();
            List<Song> suggestions = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                suggestions.addAll(allSongList);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Song song : allSongList) {
                    if (song.getSongName().toLowerCase().contains(filterPattern)) {
                        suggestions.add(song);
                    }
                }
            }
            results.values = suggestions;
            results.count = suggestions.size();
            Log.d("SONG FILTER", "list size: " + suggestions.size());
            Log.d("SONG FILTER", "all list: " + allSongList.size());

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            addAll((List)results.values);
            notifyDataSetChanged();
        }
    };
}
