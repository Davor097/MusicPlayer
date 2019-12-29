package hr.tvz.android.grabovacmusicplayer;


import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class Song {
    public static final String API_KEY = "81bc9b1e9674844a4ae0bb22fd77380e";
    private String songName;
    private String albumName;
    private String artistName;
    private String pathToFile;
    private String durationInSeconds;
    private final OkHttpClient client = new OkHttpClient();
    private String albumArtUrl = null;

    public String getDurationInSeconds() {
        return durationInSeconds;
    }

    public void setDurationInSeconds(String durationInSeconds) {
        this.durationInSeconds = durationInSeconds;
    }

    public String getAlbumArtUrl() {
        return albumArtUrl;
    }

    public void setAlbumArtUrl(String albumArtUrl) {
        this.albumArtUrl = albumArtUrl;
    }

    public Song() {

    }

    public Song(String songName, String albumName, String artistName, String pathToFile) {
        this.songName = songName;
        this.albumName = albumName;
        this.artistName = artistName;
        this.pathToFile = pathToFile;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        if (albumName != null || !albumName.equals("")) {
            this.albumName = albumName;
        } else {
            this.albumName = "Unknown Album";
        }
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        if (artistName != null || artistName.equals("")) {
            this.artistName = artistName;
        } else {
            this.artistName = "Unknown Artist";
        }
    }

    public String getPathToFile() {
        return pathToFile;
    }

    public void setPathToFile(String pathToFile) {
        this.pathToFile = pathToFile;
    }

    public void initAlbumArt() {
        if (this.albumName == null || this.albumName.equals("")
            || this.artistName == null || this.artistName.equals("")) {
            return;
        }
        String url = "http://ws.audioscrobbler.com/2.0/?method=album.getinfo&" +
                "api_key=" + Song.API_KEY +
                "&artist=" + this.artistName +
                "&album=" + this.albumName +
                "&format=json";
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                ResponseBody responseBody = response.body();
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(responseBody.string());
                    String albumArtUrl = jsonObject.getJSONObject("album").getJSONArray("image").getJSONObject(2).getString("#text");
                    setAlbumArtUrl(albumArtUrl);
                    Log.d("RESPONSE:", albumArtUrl);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }
}
