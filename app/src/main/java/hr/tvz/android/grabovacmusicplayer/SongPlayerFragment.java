package hr.tvz.android.grabovacmusicplayer;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;

public class SongPlayerFragment extends Fragment {
    private Song song;
    private Handler handler = new Handler();
    private int duration;


    public SongPlayerFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        song = new Song();
        song.setSongName(getArguments().getString("songName"));
        song.setArtistName(getArguments().getString("songArtistName"));
        song.setAlbumName(getArguments().getString("songAlbumName"));
        song.setPathToFile(getArguments().getString("songPathToFile"));
        song.setAlbumArtUrl(getArguments().getString("albumArtUrl"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.player_layout, container, false);
        final Button playButton = view.findViewById(R.id.buttonPlaySong);
        final Button nextButton = view.findViewById(R.id.buttonNextSong);
        Button previousButton = view.findViewById(R.id.buttonPreviousSong);
        final SeekBar seekBar = view.findViewById(R.id.seekBar);
        final TextView timePlaying = view.findViewById(R.id.indicatorTimePlaying);
        final TextView timeRemaining = view.findViewById(R.id.indicatorTimeRemaining);
        TextView songName = view.findViewById(R.id.songNameTextView);
        TextView artistName = view.findViewById(R.id.artistNameTextView);
        CircleImageView albumArt = view.findViewById(R.id.imageViewAlbumPlaying);

        Glide.with(this)
                .asBitmap()
                .load(song.getAlbumArtUrl())
                .into(albumArt);
        songName.setText(song.getSongName());
        songName.setSelected(true);
        artistName.setText(song.getArtistName());

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.mediaPlayer.isPlaying()) {
                    MainActivity.pause();
                    playButton.setBackgroundResource(R.drawable.play);
                } else {
                    MainActivity.play();
                    playButton.setBackgroundResource(R.drawable.pause);
                }
            }
        });

        albumArt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                song = MainActivity.getNextRandom();

                Bundle arguments = new Bundle();
                arguments.putString("songName", song.getSongName());
                arguments.putString("songArtistName", song.getArtistName());
                arguments.putString("songAlbumName", song.getAlbumName());
                arguments.putString("songPathToFile", song.getPathToFile());
                arguments.putString("albumArtUrl", song.getAlbumArtUrl());
                SongPlayerFragment fragment = new SongPlayerFragment();
                fragment.setArguments(arguments);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

                transaction.replace(R.id.playerFrame, fragment).commit();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                song = MainActivity.getNext();

                Bundle arguments = new Bundle();
                arguments.putString("songName", song.getSongName());
                arguments.putString("songArtistName", song.getArtistName());
                arguments.putString("songAlbumName", song.getAlbumName());
                arguments.putString("songPathToFile", song.getPathToFile());
                arguments.putString("albumArtUrl", song.getAlbumArtUrl());
                SongPlayerFragment fragment = new SongPlayerFragment();
                fragment.setArguments(arguments);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

                transaction.replace(R.id.playerFrame, fragment).commit();
            }
        });
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                song = MainActivity.getPrevious();

                Bundle arguments = new Bundle();
                arguments.putString("songName", song.getSongName());
                arguments.putString("songArtistName", song.getArtistName());
                arguments.putString("songAlbumName", song.getAlbumName());
                arguments.putString("songPathToFile", song.getPathToFile());
                arguments.putString("albumArtUrl", song.getAlbumArtUrl());
                SongPlayerFragment fragment = new SongPlayerFragment();
                fragment.setArguments(arguments);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

                transaction.replace(R.id.playerFrame, fragment).commit();
            }
        });
        MainActivity.startPlaying(song);
        if (!MainActivity.IS_STREAM) {
            duration = MainActivity.mediaPlayer.getDuration();
        } else {

        }
        seekBar.setMax(duration);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    seekBar.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                MainActivity.mediaPlayer.seekTo(seekBar.getProgress());
                timePlaying.setText(createTimeLabel(seekBar.getProgress()));
                timeRemaining.setText(createTimeLabel(duration - seekBar.getProgress()));
            }



        });

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (MainActivity.mediaPlayer != null && MainActivity.IS_PREPARED) {
                    int currentPosition = MainActivity.mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(currentPosition);

                    timePlaying.setText(createTimeLabel(seekBar.getProgress()));
                    timeRemaining.setText(createTimeLabel(duration - seekBar.getProgress()));

                    if (duration - currentPosition <= 0) {
                        try {
                            //nextButton.performClick();
                        } catch (Exception e) {
                            seekBar.setProgress(currentPosition);
                        }
                    }
                }
                handler.postDelayed(this, 1000);
            }
        });
        playButton.setBackgroundResource(R.drawable.pause);
        return view;
    }

    public String createTimeLabel(int time) {
        String timeLabel = "";
        int min = time / 1000 / 60;
        int sec = time / 1000 % 60;

        timeLabel = min + ":";
        if (sec < 10) timeLabel += "0";
        timeLabel += sec;


        return timeLabel;
    }


}
