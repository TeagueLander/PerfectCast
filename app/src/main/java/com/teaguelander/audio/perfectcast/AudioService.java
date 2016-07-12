package com.teaguelander.audio.perfectcast;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.IBinder;
import android.media.MediaPlayer;
import android.support.v7.app.NotificationCompat;
import android.app.NotificationManager;
//import android.support.v4.app.TaskStackBuilder;
//import android.widget.Toast;

/**
 * Created by Teague-Win10 on 7/9/2016.
 */
public class AudioService extends Service {

    //Uri curTrack = R.raw.groove;//Uri.parse("https://api.spreaker.com/download/episode/8950331/episode_28_mixdown.mp3"); //
    int curTrack = R.raw.groove;
    MediaPlayer mp;
    BroadcastReceiver receiver;
    int notificationId = -1; //notificationID allows you to update the notification later on.

    public static String PLAY_ACTION = "com.teaguelander.audio.perfectcast.PLAY_ACTION";
    public static String PAUSE_ACTION = "com.teaguelander.audio.perfectcast.PAUSE_ACTION";
    public static String REWIND_ACTION = "com.teaguelander.audio.perfectcast.REWIND_ACTION";
    public static String SKIP_ACTION = "com.teaguelander.audio.perfectcast.SKIP_ACTION";

    @Override
    public IBinder onBind(Intent arg0) { return null; };

    @Override
    public void onCreate() {
        super.onCreate();

        //BroadcastReceiver and filter - recieves actions like play and pause from the notification tray
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(PAUSE_ACTION)) {
                    pauseAudio();
                }
                else if (action.equals(PLAY_ACTION)) {
                    playAudio();
                }
                else if (action.equals(SKIP_ACTION)) {
                    skipAudio();
                }
                else if (action.equals(REWIND_ACTION)) {
                    rewindAudio();
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(PLAY_ACTION);
        filter.addAction(PAUSE_ACTION);
        filter.addAction(SKIP_ACTION);
        filter.addAction(REWIND_ACTION);
        registerReceiver(receiver, filter);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Continues running until its stopped
        //Toast.makeText(this, "Audio Service Started", Toast.LENGTH_LONG).show();

        mp = MediaPlayer.create(this, curTrack);
        playAudio();
        //showNotification();

        return START_NOT_STICKY;
    }

    //Cleanup the service
    public void onDestroy() {
        super.onDestroy();
        //Toast.makeText(this, "Audio Service Destroyed", Toast.LENGTH_LONG).show();
        mp.stop();
        removeNotification();
        unregisterReceiver(receiver);
    }

    public void playAudio() {
        mp = MediaPlayer.create(this, curTrack);
        mp.start();
        showNotification();
    }

    public void pauseAudio() {
        mp.pause();
        showNotification();
    }

    public void rewindAudio() {
        mp.seekTo(mp.getCurrentPosition() - 30000);
    }

    public void skipAudio() {
        mp.seekTo(mp.getCurrentPosition() + 30000);
    }

    //Creates and sends the notification that controls our audio, returns notification id
    private int showNotification() {

        //Notification Title, Icon, and message
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        mBuilder.setContentTitle("PerfectCast");
        mBuilder.setContentText("Now Playing");
        mBuilder.setShowWhen(false); //Tells us whether to display the time
        mBuilder.setStyle(new NotificationCompat.MediaStyle() //We should add to this to give it more info
                .setShowActionsInCompactView(new int[] {0,1,2})
                .setMediaSession(null)
        );

        //Add pause button
        PendingIntent pendingRewindIntent = PendingIntent.getBroadcast(this, 100, new Intent(REWIND_ACTION), 0);
        mBuilder.addAction(android.R.drawable.ic_media_rew, "Rewind", pendingRewindIntent);
        if (!mp.isPlaying()) {
            PendingIntent pendingPlayIntent = PendingIntent.getBroadcast(this, 100, new Intent(PLAY_ACTION), 0);
            mBuilder.addAction(android.R.drawable.ic_media_play, "Play", pendingPlayIntent);
        }else {
            PendingIntent pendingPauseIntent = PendingIntent.getBroadcast(this, 100, new Intent(PAUSE_ACTION), 0);
            mBuilder.addAction(android.R.drawable.ic_media_pause, "Pause", pendingPauseIntent);
        }
        PendingIntent pendingSkipIntent = PendingIntent.getBroadcast(this, 100, new Intent(SKIP_ACTION), 0);
        mBuilder.addAction(android.R.drawable.ic_media_ff, "Skip", pendingSkipIntent);

        //Notification Pressed set (may need to ahve a stackbuilder here)
        Intent openMainActivity = new Intent(this,MainActivity.class);
        openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent pendingOpenMainActivity =
                PendingIntent.getActivity(
                        this,
                        0,
                        openMainActivity,
                        PendingIntent.FLAG_IMMUTABLE //Notification wont be removed when clicked
                );
        mBuilder.setContentIntent(pendingOpenMainActivity);

        //Flags
        mBuilder.setOngoing(true); //Notification cant be removed by swiping it

        //Gets the id of the notification manager and then sends the notification we built
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(notificationId, mBuilder.build());
        return notificationId;
    }


    private int removeNotification() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(notificationId);

        return 0;
    }

}
