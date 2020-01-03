package hr.tvz.android.grabovacmusicplayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.audiofx.Equalizer;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;

import com.sdsmdg.harjot.crollerTest.Croller;
import com.sdsmdg.harjot.crollerTest.OnCrollerChangeListener;

public class EQActivity extends Activity {
    //public static Equalizer equalizer = MainActivity.mainEQ;

    private SeekBar low;
    private SeekBar lowMid;
    private SeekBar mid;
    private SeekBar highMid;
    private SeekBar high;
    private Croller volumeKnob;
    private AudioManager audioManager;

    private final short LOW_BAND = 0;
    private final short LOW_MID_BAND = 1;
    private final short MID_BAND = 2;
    private final short HIGH_MID_BAND = 3;
    private final short HIGH_BAND = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_equalizer);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        low = findViewById(R.id.lowEQ);
        lowMid = findViewById(R.id.lowMidEQ);
        mid = findViewById(R.id.midEQ);
        highMid = findViewById(R.id.highMidEQ);
        high = findViewById(R.id.highEQ);
        volumeKnob = findViewById(R.id.volumeKnob);

        short[] bandLevelRange = MainActivity.mainEQ.getBandLevelRange();
        low.setMin(bandLevelRange[0]);
        low.setMax(bandLevelRange[1]);
        low.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    seekBar.setProgress(progress);
                    MainActivity.mainEQ.setBandLevel(LOW_BAND, (short) (progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        lowMid.setMin(bandLevelRange[0]);
        lowMid.setMax(bandLevelRange[1]);
        lowMid.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    seekBar.setProgress(progress);
                    MainActivity.mainEQ.setBandLevel(LOW_MID_BAND, (short) (progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mid.setMin(bandLevelRange[0]);
        mid.setMax(bandLevelRange[1]);
        mid.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    seekBar.setProgress(progress);
                    MainActivity.mainEQ.setBandLevel(MID_BAND, (short) (progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        highMid.setMin(bandLevelRange[0]);
        highMid.setMax(bandLevelRange[1]);
        highMid.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    seekBar.setProgress(progress);
                    MainActivity.mainEQ.setBandLevel(HIGH_MID_BAND, (short) (progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        high.setMin(bandLevelRange[0]);
        high.setMax(bandLevelRange[1]);
        high.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    seekBar.setProgress(progress);
                    MainActivity.mainEQ.setBandLevel(HIGH_BAND, (short) (progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        low.setProgress(MainActivity.mainEQ.getBandLevel(LOW_BAND));
        lowMid.setProgress(MainActivity.mainEQ.getBandLevel(LOW_MID_BAND));
        mid.setProgress(MainActivity.mainEQ.getBandLevel(MID_BAND));
        highMid.setProgress(MainActivity.mainEQ.getBandLevel(HIGH_MID_BAND));
        high.setProgress(MainActivity.mainEQ.getBandLevel(HIGH_BAND));

        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        volumeKnob.setMax(maxVolume);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        volumeKnob.setProgress(currentVolume);
        volumeKnob.setLabel("" + currentVolume);
        volumeKnob.setOnCrollerChangeListener(new OnCrollerChangeListener() {
            @Override
            public void onProgressChanged(Croller croller, int progress) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                croller.setLabel("" + progress);
            }

            @Override
            public void onStartTrackingTouch(Croller croller) {

            }

            @Override
            public void onStopTrackingTouch(Croller croller) {

            }
        });
    }
}
