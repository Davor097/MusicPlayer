package hr.tvz.android.grabovacmusicplayer;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class SongListFragment extends ListFragment {
    private List<Song> songList = new ArrayList<>();
    private SongItemArrayAdapter la;
    interface Callbacks {
        void onItemSelected(Song song);
    }
    private static Callbacks callbacks = new Callbacks() {
        @Override
        public void onItemSelected(Song song) {

        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        songList = MainActivity.SONG_LIST;
        la = new SongItemArrayAdapter(getActivity(), R.layout.list_item_layout, songList);
        setListAdapter(la);
    }

    @Override
    public void onStart() {
        super.onStart();

        //refreshSongs();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setActivateOnItemClick(true);
        //refreshSongs();
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);

        callbacks = (Callbacks) activity;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        MainActivity.currentSongIndex = position;
        callbacks.onItemSelected(songList.get(position));
    }

    public void setActivateOnItemClick(boolean activateOnItemClick) {
        getListView().setChoiceMode(activateOnItemClick ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);
    }

    public void refreshSongs() {
        la.notifyDataSetChanged();
    }


}
