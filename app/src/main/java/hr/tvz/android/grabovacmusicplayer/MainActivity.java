package hr.tvz.android.grabovacmusicplayer;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.TimeUtils;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

@EActivity
public class MainActivity extends FragmentActivity implements SongListFragment.Callbacks, NavigationView.OnNavigationItemSelectedListener {
    public static List<Song> SONG_LIST = new ArrayList<>();
    public static boolean IS_PLAYING = false;
    public static boolean IS_STREAM = false;
    public static boolean IS_PREPARED = false;
    public static MediaPlayer mediaPlayer = new MediaPlayer();
    public static Equalizer mainEQ = new Equalizer(0, mediaPlayer.getAudioSessionId());
    public static int currentSongIndex = 0;
    public static int duration = 0;

    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IS_STREAM = false;
        SONG_LIST.addAll(getAllSongs(getApplicationContext()));
        initAlbumArts();
        setContentView(R.layout.activity_main);

        TextInputEditText songFilterText = findViewById(R.id.songTextInputFilter);

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.seekTo(0);
                duration = mp.getDuration();
                mp.start();
                Log.d("MEDIA PLAYER DEBUG", "Prepared, started playing.");
                MainActivity.IS_PLAYING = true;
            }
        });

        mainEQ.setEnabled(true);


        drawer = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navView);
        navigationView.setNavigationItemSelectedListener(this);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        SongListFragment listFragment = new SongListFragment();
        transaction.replace(R.id.songListFrame, listFragment).commit();

        songFilterText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                SongListFragment fragment = (SongListFragment) getSupportFragmentManager().findFragmentById(R.id.songListFrame);
                fragment.filterResults(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.playerMenuSelection:
                SONG_LIST.clear();
                IS_STREAM = false;
                SONG_LIST.addAll(getAllSongs(getApplicationContext()));
                ((SongListFragment) getSupportFragmentManager().findFragmentById(R.id.songListFrame)).refreshSongs();
                break;
            case R.id.equalizerMenuSelection:
                Intent eqIntent = new Intent(getApplicationContext(), EQActivity.class);
                startActivity(eqIntent);
                break;
            case R.id.streamMenuSelection:
                SONG_LIST.clear();
                IS_STREAM = true;
                getSongListFromServer();
                break;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    SONG_LIST = getAllSongs(getApplicationContext());
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onItemSelected(Song song) {
        Bundle arguments = new Bundle();
        arguments.putString("songName", song.getSongName());
        arguments.putString("songArtistName", song.getArtistName());
        arguments.putString("songAlbumName", song.getAlbumName());
        arguments.putString("songPathToFile", song.getPathToFile());
        arguments.putString("albumArtUrl", song.getAlbumArtUrl());
        arguments.putString("songDuration", song.getDurationInSeconds());
        SongPlayerFragment fragment = new SongPlayerFragment();
        fragment.setArguments(arguments);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        FrameLayout frameLayout = findViewById(R.id.playerFrame);
        frameLayout.setLayoutParams(new LinearLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 0, 3));
        transaction.replace(R.id.playerFrame, fragment).commit();
    }

    public static void initAlbumArts() {
        for (Song song: SONG_LIST) {
            song.initAlbumArt();
        }
    }

    public static List<Song> getAllSongs(Context context) {
        List<Song> allSongs;
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.AudioColumns.DATA,
                MediaStore.Audio.AudioColumns.TITLE,
                MediaStore.Audio.AudioColumns.ALBUM,
                MediaStore.Audio.ArtistColumns.ARTIST
        };
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sort = MediaStore.Audio.Media.TITLE + " ASC";

        allSongs = getSongListFromQuery(uri, projection, selection, sort, context);

        return allSongs;
    }

    public static List<Song> getSongListFromQuery(Uri uri, String[] projection, String selection, String sort, Context context) {
        List<Song> songList = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(uri, projection, selection, null, sort);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String path = cursor.getString(0);
                String name = cursor.getString(1);
                String album = cursor.getString(2);
                String artist = cursor.getString(3);

                Song song = new Song(name, album, artist, path);
                songList.add(song);
            }
        }
        return songList;
    }

    Handler mainThreadHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            initAlbumArts();
            ((SongListFragment) getSupportFragmentManager().findFragmentById(R.id.songListFrame)).refreshSongs();
        }
    };

    @Background
    public void getSongListFromServer() {
        String url = "http://grabovac.subsonic.org/rest/getRandomSongs?u=admin&p=admin&v=1.16.1&c=myapp";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        try {
            Response response = client.newCall(request).execute();
            ResponseBody responseBody = response.body();

            DocumentBuilderFactory dbc = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = dbc.newDocumentBuilder();
            Document document = documentBuilder.parse(responseBody.byteStream());
            NodeList nodeList = document.getElementsByTagName("song");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Song song = new Song();

                String streamPath = "http://grabovac.subsonic.org/rest/stream?u=admin&p=admin&v=1.16.1&c=androidapp&id=";
                Node songInfo = nodeList.item(i);
                streamPath += songInfo.getAttributes().getNamedItem("id").getNodeValue();
                song.setSongName(songInfo.getAttributes().getNamedItem("title").getNodeValue());
                song.setArtistName(songInfo.getAttributes().getNamedItem("artist").getNodeValue());
                song.setAlbumName(songInfo.getAttributes().getNamedItem("album").getNodeValue());
                String durationOfSong = songInfo.getAttributes().getNamedItem("duration").getNodeValue();
                Log.d("DURATION_OF_SONG", "length: " + durationOfSong);
                song.setDurationInSeconds(durationOfSong);
                song.setPathToFile(streamPath);
                Log.d("SUBSONIC SONG DATA", song.getSongName() + ";" + song.getPathToFile());
                SONG_LIST.add(song);
                Log.d("SONG_LIST AFTER ADDING", SONG_LIST.get(i).getSongName());
            }

            Message msg = Message.obtain();
            msg.obj = "Handle new song list";
            mainThreadHandler.sendMessage(msg);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void releasePlayer() {
        mediaPlayer.release();
        mediaPlayer = new MediaPlayer();
        Equalizer.Settings prevSettings = mainEQ.getProperties();
        mainEQ.release();
        mainEQ = new Equalizer(0, mediaPlayer.getAudioSessionId());
        Log.d("SESSION ID", "audio session id: " + mediaPlayer.getAudioSessionId());
        mainEQ.setProperties(prevSettings);
        mainEQ.setEnabled(true);
        MainActivity.IS_PREPARED = false;
        MainActivity.IS_PLAYING = false;
    }

    public static void startPlaying(Song song) {
        try {
            releasePlayer();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                    MainActivity.IS_PREPARED = true;
                }
            });
            if (IS_STREAM) {
                mediaPlayer.setDataSource(song.getPathToFile());
                mediaPlayer.prepareAsync();
            } else {
                mediaPlayer.setDataSource(song.getPathToFile());
                mediaPlayer.prepare();
            }

        } catch (Exception e) {
            Log.e("ERROR", "error during loading of the file check if path is correct");
        }
    }

    public static void play() {
        mediaPlayer.start();
        MainActivity.IS_PLAYING = true;

    }

    public static void pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            MainActivity.IS_PLAYING = false;
        }
    }

    public static Song getNextRandom() {
        ThreadLocalRandom randomGenerator = ThreadLocalRandom.current();
        int random = randomGenerator.nextInt(0, SONG_LIST.size() + 1);
        currentSongIndex = random;
        return SONG_LIST.get(random);
    }

    public static Song getPreviousRandom() {
        return getNextRandom();
    }

    public static Song getNext() {
        if (currentSongIndex == SONG_LIST.size()) {
            currentSongIndex = 0;
            return SONG_LIST.get(currentSongIndex);
        }

        return SONG_LIST.get(++currentSongIndex);
    }

    public static Song getPrevious() {
        if (currentSongIndex != 0) {
            return SONG_LIST.get(--currentSongIndex);
        } else {
            currentSongIndex = SONG_LIST.size();
            return SONG_LIST.get(currentSongIndex);
        }
    }
}
