package hr.tvz.android.grabovacmusicplayer;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class SongListFragment extends ListFragment {
    private List<Song> songList = new ArrayList<>();
    private SongItemArrayAdapter la;
    interface Callbacks {
        void onItemSelected(Song song);
        void onItemLongClick(Song song);
    }
    private static Callbacks callbacks = new Callbacks() {
        @Override
        public void onItemSelected(Song song) {

        }

        @Override
        public void onItemLongClick(Song song) {

        }
    };


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                callbacks.onItemLongClick(songList.get(position));
                return true;
            }
        });
    }

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
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setActivateOnItemClick(true);
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
        la.setAllSongList(MainActivity.SONG_LIST);
        la.notifyDataSetChanged();
    }

    public void filterResults(CharSequence constraint) {
        la.getFilter().filter(constraint);
    }
}
