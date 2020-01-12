package hr.tvz.android.grabovacmusicplayer;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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
    public static String STREAM_INFO_URL = "http://grabovac.subsonic.org/rest/getRandomSongs?u=admin&p=admin&v=1.16.1&c=myapp";
    public static String STREAM_PLAY_URL = "http://grabovac.subsonic.org/rest/stream?u=admin&p=admin&v=1.16.1&c=androidapp&id=";

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
                initAlbumArts();
                ((SongListFragment) getSupportFragmentManager().findFragmentById(R.id.songListFrame)).refreshSongs();
                drawer.closeDrawers();
                break;
            case R.id.equalizerMenuSelection:
                Intent eqIntent = new Intent(getApplicationContext(), EQActivity.class);
                startActivity(eqIntent);
                drawer.closeDrawers();
                break;
            case R.id.streamMenuSelection:
                SONG_LIST.clear();
                IS_STREAM = true;
                getSongListFromServer();
                drawer.closeDrawers();
                break;
            case R.id.createPlaylistSelection:
                openCreatePlaylistDialog();
                drawer.closeDrawers();
                break;
            case R.id.loadPlaylistSelection:
                openLoadPlaylistDialog();
                drawer.closeDrawers();
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

    @Override
    public void onItemLongClick(final Song song) {
        final List<Playlist> playlists = getPlaylists();
        if (playlists.isEmpty()) {
            Toast.makeText(this, "Create a playlist before adding songs.", Toast.LENGTH_SHORT);
        } else {
            final List<String> playlistsNames = new ArrayList<>();
            for (Playlist playlist : playlists) {
                playlistsNames.add(playlist.getName());
            }
            String[] options = playlistsNames.toArray(new String[0]);

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setTitle("Add to playlist");
            dialogBuilder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Playlist playlist = playlists.get(which);
                    addSongToPlaylist(playlist, song);
                    Log.d("PLAYLIST DIALOG", "Adding song to playlist");
                }
            });
            dialogBuilder.show();
        }
    }

    public void openCreatePlaylistDialog() {
        final EditText input = new EditText(this);

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Create a playlist");
        dialog.setView(input);
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                createPlaylist(input.getText().toString());
                Toast.makeText(getApplicationContext(), "Long click on a song to add it to a playlist", Toast.LENGTH_LONG);

            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        dialog.show();
    }

    public void openLoadPlaylistDialog() {
        final List<Playlist> playlists = getPlaylists();
        if (playlists.isEmpty()) {
            Toast.makeText(this, "Create a playlist first.", Toast.LENGTH_SHORT);
        } else {
            final List<String> playlistsNames = new ArrayList<>();
            for (Playlist playlist : playlists) {
                playlistsNames.add(playlist.getName());
            }
            String[] options = playlistsNames.toArray(new String[0]);

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setTitle("Select a playlist");
            dialogBuilder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Playlist playlist = playlists.get(which);
                    loadFromPlaylist(playlist);
                    Log.d("PLAYLIST LOADED", "Playlist: " + playlist.getName());
                }
            });
            dialogBuilder.show();
        }

    }

    public void loadFromPlaylist(Playlist playlist) {
        List<Song> songs;
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", Long.parseLong(playlist.getId()));
        String[] projection = {
                MediaStore.Audio.AudioColumns.DATA,
                MediaStore.Audio.AudioColumns.TITLE,
                MediaStore.Audio.AudioColumns.ALBUM,
                MediaStore.Audio.ArtistColumns.ARTIST,
                MediaStore.Audio.AudioColumns._ID
        };
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sort = MediaStore.Audio.Media.TITLE + " ASC";

        songs = getSongListFromQuery(uri, projection, selection, sort, getApplicationContext());

        SONG_LIST.clear();
        SONG_LIST.addAll(songs);
        initAlbumArts();

        ((SongListFragment)getSupportFragmentManager().findFragmentById(R.id.songListFrame)).refreshSongs();
    }

    public void addSongToPlaylist(Playlist playlist, Song song) {
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", Long.parseLong(playlist.getId()));

        ContentResolver contentResolver = getContentResolver();
        ContentValues contentValues = new ContentValues();

        Cursor c = contentResolver.query(uri, new String[] {"*"}, null, null, null);
        int songOrderNumber = c.getCount() + 1;
        c.close();

        contentValues.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, songOrderNumber);
        contentValues.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, song.getId());

        contentResolver.insert(uri, contentValues);
        contentResolver.notifyChange(Uri.parse("content://media"), null);
        Toast.makeText(this, "Song added to " + playlist.getName(), Toast.LENGTH_SHORT);
    }

    public void createPlaylist(String name){
        Uri playlists = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        Cursor c = getContentResolver().query(playlists, new String[] {"*"}, null, null, null);

        if (c.moveToFirst()) {
            do {
                String plName = c.getString(c.getColumnIndex(MediaStore.Audio.Playlists.NAME));
                if (plName.equalsIgnoreCase(name)) {
                    Toast.makeText(this, "Playlist with that name already exists.", Toast.LENGTH_SHORT);
                    return;
                }
            } while (c.moveToNext());
        }
        c.close();

        ContentValues newPlaylistValues = new ContentValues();
        newPlaylistValues.put(MediaStore.Audio.Playlists.NAME, name);
        newPlaylistValues.put(MediaStore.Audio.Playlists.DATE_MODIFIED, System.currentTimeMillis());
        Uri newPlaylist = getContentResolver().insert(playlists, newPlaylistValues);
        Log.d("PLAYLIST OPERATION", "Playlist added: " + newPlaylist);
    }

    public List<Playlist> getPlaylists() {
        List<Playlist> listOfPlaylists = new ArrayList<>();
        Uri playlists = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        Cursor c = getContentResolver().query(playlists, new String[] {"*"}, null, null, null);


        while(c.moveToNext()) {
            String plName = c.getString(c.getColumnIndex(MediaStore.Audio.Playlists.NAME));
            String id = c.getString(c.getColumnIndex(MediaStore.Audio.Playlists._ID));
            Playlist temp = new Playlist();
            temp.setName(plName);
            temp.setId(id);
            listOfPlaylists.add(temp);
        }

        return listOfPlaylists;
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
                MediaStore.Audio.ArtistColumns.ARTIST,
                MediaStore.Audio.AudioColumns._ID
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
                String id = cursor.getString(4);

                Song song = new Song(name, album, artist, path);
                song.setId(id);
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
        String url = STREAM_INFO_URL;

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

                String streamPath = STREAM_PLAY_URL;
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
