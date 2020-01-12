package hr.tvz.android.grabovacmusicplayer;

import java.util.List;

public class Playlist {
    private String id;
    private List<Song> songList;
    private String name;

    public Playlist() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Song> getSongList() {
        return songList;
    }

    public void setSongList(List<Song> songList) {
        this.songList = songList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
