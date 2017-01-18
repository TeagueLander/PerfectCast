package com.teaguelander.audio.perfectcast.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.media.MediaPlayer;
import android.support.v7.app.NotificationCompat;
import android.app.NotificationManager;
//import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.teaguelander.audio.perfectcast.database.DataManager;
import com.teaguelander.audio.perfectcast.MainActivity;
import com.teaguelander.audio.perfectcast.R;

import java.io.IOException;

/**
 * Created by Teague-Win10 on 7/9/2016.
 */
public class AudioService extends Service {

	public static String PLAY_ACTION = "com.teaguelander.audio.perfectcast.PLAY_ACTION";
	public static String PAUSE_ACTION = "com.teaguelander.audio.perfectcast.PAUSE_ACTION";
	public static String REWIND_ACTION = "com.teaguelander.audio.perfectcast.REWIND_ACTION";
	public static String SKIP_ACTION = "com.teaguelander.audio.perfectcast.SKIP_ACTION";
	public static String STOP_ACTION = "com.teaguelander.audio.perfectcast.STOP_ACTION";
	public static String DESTROY_ACTION = "com.teaguelander.audio.perfectcast.DESTROY_ACTION";

	private static int SKIP_LENGTH = 30000;
	private static int RESUME_REWIND_LENGTH = 2000;
	private static int ICON_APP = R.mipmap.ic_launcher;

	//Uri curTrack = R.raw.groove;//Uri.parse("https://api.spreaker.com/download/episode/8950331/episode_28_mixdown.mp3"); //
	int curTrack = R.raw.groove;
	String curWebTrack = "http://www.archive.org/download/ZeldaDungeonChristmasSpecialPodcast/Z-talkChristmas.mp3";
	MediaPlayer mp;
	BroadcastReceiver receiver; //For Intents
	SharedPreferences preferences;
	SharedPreferences.Editor prefEditor;
	int notificationId = -1; //notificationID allows you to update the notification later on.

	int resumeTime = 0; // Resume time

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
				performAction(intent);
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction(PLAY_ACTION);
		filter.addAction(PAUSE_ACTION);
		filter.addAction(SKIP_ACTION);
		filter.addAction(REWIND_ACTION);
		filter.addAction(DESTROY_ACTION);
		registerReceiver(receiver, filter);

		//SharedPreferences
//		prefEditor = getSharedPreferences(DataManager.PREFS_NAME, MODE_PRIVATE).edit();
//		prefEditor.putString("name", "Elena");
//		prefEditor.commit();
		preferences = getSharedPreferences(DataManager.PREFS_NAME, MODE_PRIVATE);
		prefEditor = preferences.edit();

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//Toast.makeText(this, "Audio Service Started", Toast.LENGTH_LONG).show();
		performAction(intent);
		//showNotification();

		return START_NOT_STICKY;
	}

	//Cleanup the service
	public void onDestroy() {
		super.onDestroy();

		resumeTime = mp.getCurrentPosition() - RESUME_REWIND_LENGTH;
		prefEditor.putInt(DataManager.PREF_RESUME_TIME, resumeTime);
		prefEditor.commit();

		mp.release();
//		sendBroadcast(new Intent(STOP_ACTION));
		removeNotification();
		unregisterReceiver(receiver);
		Log.d("as", "AudioService destroyed");
	}

	public void playAudio() {
		if (mp == null) {
			//mp = MediaPlayer.create(this, curTrack); //From Local
			mp = new MediaPlayer();
			try {
				mp.setDataSource(curWebTrack);
				mp.prepare();
			} catch (IOException e) {
				e.printStackTrace();
			}
			resumeTime = preferences.getInt(DataManager.PREF_RESUME_TIME, 0);

			//Listeners
			mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
				public void onCompletion(MediaPlayer mp) {
//					stopAudio(); //Use broadcast STOP_ACTION here instead
					mp.seekTo(0);
					pauseAudio();
					Log.d("as", "End of Audio reached");
					sendBroadcast(new Intent(STOP_ACTION));
//					Toast.makeText(getApplicationContext(), "Hello World", Toast.LENGTH_SHORT).show();
				}
			});
		}
		mp.seekTo(resumeTime);
		mp.start();
		showNotification();
	}

//	private void playAudioFromWeb() {
//		if (mp == null) {
//			mp = new MediaPlayer();
//		}
//		try
//	}

	public void pauseAudio() {
		mp.pause();
		resumeTime = mp.getCurrentPosition() - RESUME_REWIND_LENGTH;
		prefEditor.putInt(DataManager.PREF_RESUME_TIME, resumeTime);
		prefEditor.commit();
		showNotification();
	}

	public void stopAudio() {
		mp.stop();
		resumeTime = mp.getCurrentPosition() - RESUME_REWIND_LENGTH;
		prefEditor.putInt(DataManager.PREF_RESUME_TIME, resumeTime);
		prefEditor.commit();
		showNotification();
	}

	public void rewindAudio() {
		mp.seekTo(mp.getCurrentPosition() - SKIP_LENGTH);
	}

	public void skipAudio() {
		mp.seekTo(mp.getCurrentPosition() + SKIP_LENGTH);
	}

	//Creates and sends the notification that controls our audio, returns notification id
	private int showNotification() {

		//Notification Title, Icon, and message
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
		mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC); //This means it can be seen on the lock screen
		mBuilder.setSmallIcon(ICON_APP);
		mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), ICON_APP));
		mBuilder.setContentTitle("PerfectCast");
		mBuilder.setContentText("Now Playing");
		mBuilder.setShowWhen(false); //Tells us whether to display the time
		mBuilder.setStyle(new NotificationCompat.MediaStyle() //We should add to this to give it more info
				.setShowActionsInCompactView(new int[] {0,1,2})
				.setMediaSession(null)
		);
		mBuilder.setDeleteIntent(PendingIntent.getBroadcast(this, 100, new Intent(DESTROY_ACTION), 0));

		//Add Buttons
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
						PendingIntent.FLAG_UPDATE_CURRENT
				);
		mBuilder.setContentIntent(pendingOpenMainActivity);

		//Flags
		if (!mp.isPlaying()) {
			mBuilder.setOngoing(false);
		}else {
			mBuilder.setOngoing(true);
		}

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

	private void performAction(Intent intent) {
		String action = intent.getAction();
		Log.d("as", "AudioService received Intent: " + action);
		if (action == null) {
			playAudio();
		}
		else if (action.equals(PAUSE_ACTION)) {
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
		else if (action.equals(STOP_ACTION)) {
//			stopAudio();
		}else if (action.equals(DESTROY_ACTION)) {
//			Toast.makeText(this, "Destroy Action", Toast.LENGTH_LONG).show();
			stopSelf(); //Ends the service
		}
	}

	private void storePreferences() {

	}

	//Listeners
	/*mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
		public void onCompletion(MediaPlayer mp) {
			Toast.makeText(getApplicationContext(), "Hello World", Toast.LENGTH_SHORT).show();
		}
	});*/

}
