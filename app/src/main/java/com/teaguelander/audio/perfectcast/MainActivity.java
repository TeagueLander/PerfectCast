package com.teaguelander.audio.perfectcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
//import android.support.design.widget.FloatingActionButton;
//import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.widget.Button;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    boolean isAudioPlaying = false;
    BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        //The bottom toolbar which has audio controls
        Toolbar controlToolbar = (Toolbar) findViewById(R.id.control_toolbar);
        ImageButton playPauseButton = (ImageButton) findViewById(R.id.playPauseButton);

        //BroadcastReceiver and filter - recieves actions like play and pause from the notification tray
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                ImageButton playPauseButton = (ImageButton) findViewById(R.id.playPauseButton);
                if (action.equals(AudioService.PAUSE_ACTION)) {
                    playPauseButton.setBackgroundResource(android.R.drawable.ic_media_play);
                    isAudioPlaying = false;
                }
                else if (action.equals(AudioService.PLAY_ACTION)) {
                    playPauseButton.setBackgroundResource(android.R.drawable.ic_media_pause);
                    isAudioPlaying = true;
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(AudioService.PLAY_ACTION);
        filter.addAction(AudioService.PAUSE_ACTION);
        registerReceiver(receiver, filter);

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAudioService();
            }
        });
        Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopAudioService();
            }
        });
    }

    @Override()
    public void onDestroy() {
        //Cleanup goes here
        super.onDestroy();
        stopAudioService();
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.search) {
            return true;
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    public void startAudioService() {
        startService(new Intent(getBaseContext(), AudioService.class));
        isAudioPlaying = true;
        ImageButton playPauseButton = (ImageButton) findViewById(R.id.playPauseButton);
        playPauseButton.setBackgroundResource(android.R.drawable.ic_media_pause);
    }
    public void stopAudioService() {
        stopService(new Intent(getBaseContext(), AudioService.class));
        isAudioPlaying = false;
        ImageButton playPauseButton = (ImageButton) findViewById(R.id.playPauseButton);
        playPauseButton.setBackgroundResource(android.R.drawable.ic_media_play);
    }

    //Ideally the button would have a pending intent on it instead.  That way an intent is sent to the service and the service returns an intent which changes the icon
    public void playButtonPressed(View view) {
        if(isAudioPlaying){
            startService(new Intent(AudioService.PAUSE_ACTION, null, getBaseContext(), AudioService.class));
            ImageButton playPauseButton = (ImageButton) findViewById(R.id.playPauseButton);
            playPauseButton.setBackgroundResource(android.R.drawable.ic_media_play);
            isAudioPlaying = false;
        }else {
            startService(new Intent(AudioService.PLAY_ACTION, null, getBaseContext(), AudioService.class));
            ImageButton playPauseButton = (ImageButton) findViewById(R.id.playPauseButton);
            playPauseButton.setBackgroundResource(android.R.drawable.ic_media_pause);
            isAudioPlaying = true;
        }
    }
}
