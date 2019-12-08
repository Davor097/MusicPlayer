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
    public Equalizer equalizer = new Equalizer(0, MainActivity.mediaPlayer.getAudioSessionId());

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

        equalizer.setEnabled(true);

        int numOfBands = equalizer.getNumberOfBands();
        Log.d("EQ:", "number of bands: " + numOfBands);
        int[] band0 = equalizer.getBandFreqRange((short)0);
        int[] band1 = equalizer.getBandFreqRange((short)1);
        int[] band2 = equalizer.getBandFreqRange((short)2);
        int[] band3 = equalizer.getBandFreqRange((short)3);
        int[] band4 = equalizer.getBandFreqRange((short)4);
        Log.d("EQ", "band 0: " + (short) band0[0] + " - " +(short) band0[1]);
        Log.d("EQ", "band 1: " +(short) band1[0] + " - " + (short)band1[1]);
        Log.d("EQ", "band 2: " +(short) band2[0] + " - " +(short) band2[1]);
        Log.d("EQ", "band 3: " +(short) band3[0] + " - " +(short) band3[1]);
        Log.d("EQ", "band 4: " + (short)band4[0] + " - " + (short)band4[1]);

        short[] bandLevelRange = equalizer.getBandLevelRange();
        low.setMin(bandLevelRange[0]);
        low.setMax(bandLevelRange[1]);
        low.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    seekBar.setProgress(progress);
                    equalizer.setBandLevel(LOW_BAND, (short) (progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d("EQ LOW SEEK PROGRESS",  " " + seekBar.getProgress());
                Log.d("EQ LOW LEVEL", " " + equalizer.getBandLevel(LOW_BAND));
            }
        });

        lowMid.setMin(bandLevelRange[0]);
        lowMid.setMax(bandLevelRange[1]);
        lowMid.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    seekBar.setProgress(progress);
                    equalizer.setBandLevel(LOW_MID_BAND, (short) (progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d("EQ LOW MID SEEK PROGRESS",  " " + seekBar.getProgress());
                Log.d("EQ LOW MID LEVEL", " " + equalizer.getBandLevel(LOW_MID_BAND));
            }
        });

        mid.setMin(bandLevelRange[0]);
        mid.setMax(bandLevelRange[1]);
        mid.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    seekBar.setProgress(progress);
                    equalizer.setBandLevel(MID_BAND, (short) (progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d("EQ MID SEEK PROGRESS",  " " + seekBar.getProgress());
                Log.d("EQ MID LEVEL", " " + equalizer.getBandLevel(MID_BAND));
            }
        });

        highMid.setMin(bandLevelRange[0]);
        highMid.setMax(bandLevelRange[1]);
        highMid.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    seekBar.setProgress(progress);
                    equalizer.setBandLevel(HIGH_MID_BAND, (short) (progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d("EQ HIGH MID SEEK PROGRESS",  " " + seekBar.getProgress());
                Log.d("EQ HIGH MID LEVEL", " " + equalizer.getBandLevel(HIGH_MID_BAND));
            }
        });

        high.setMin(bandLevelRange[0]);
        high.setMax(bandLevelRange[1]);
        high.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    seekBar.setProgress(progress);
                    equalizer.setBandLevel(HIGH_BAND, (short) (progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d("EQ HIGH SEEK PROGRESS",  " " + seekBar.getProgress());
                Log.d("EQ HIGH LEVEL", " " + equalizer.getBandLevel(HIGH_BAND));

            }
        });
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
