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
import android.widget.Toast;

import com.teaguelander.audio.perfectcast.database.StaticValues;
import com.teaguelander.audio.perfectcast.MainActivity;
import com.teaguelander.audio.perfectcast.R;
import com.teaguelander.audio.perfectcast.objects.PodcastEpisode;

/**
 * Created by Teague-Win10 on 7/9/2016.
 */
public class AudioService extends Service {

	public static String PLAY_ACTION = "com.teaguelander.audio.perfectcast.PLAY_ACTION";
		public static String EXTRA_FORCE_UPDATE = "forceUpdate";
	public static String PAUSE_ACTION = "com.teaguelander.audio.perfectcast.PAUSE_ACTION";
	public static String REWIND_ACTION = "com.teaguelander.audio.perfectcast.REWIND_ACTION";
	public static String SKIP_ACTION = "com.teaguelander.audio.perfectcast.SKIP_ACTION";
	public static String STOP_ACTION = "com.teaguelander.audio.perfectcast.STOP_ACTION";
	public static String DESTROY_ACTION = "com.teaguelander.audio.perfectcast.DESTROY_ACTION";

	public static String PLAYING_STATUS = "com.teaguelander.audio.perfectcast.PLAYING_STATUS";
	public static String PREPARING_STATUS = "com.teaguelander.audio.perfectcast.PREPARING_STATUS";
	public static String STOPPED_STATUS = "com.teaguelander.audio.perfectcast.STOPPED_STATUS";
	public static String PAUSED_STATUS = "com.teaguelander.audio.perfectcast.PAUSED_STATUS";
	public static String ERROR_STATUS = "com.teaguelander.audio.perfectcast.ERROR_STATUS";
	public static String DESTROYED_STATUS = "com.teaguelander.audio.perfectcast.DESTROYED_STATUS";

	private static int SKIP_LENGTH = 30000;
	private static int RESUME_REWIND_LENGTH = 2000;
	private static int ICON_APP = R.mipmap.ic_launcher;

	TrackQueueService queueService;
	PodcastEpisode currentEpisode;
	int curTrack = R.raw.groove;
	MediaPlayer mp;
	String currentTrackLocation = null;
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

		//TrackQueueService controls the "Up Next" Playlist
		queueService = TrackQueueService.getInstance();
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
//		prefEditor = getSharedPreferences(StaticValues.PREFS_NAME, MODE_PRIVATE).edit();
//		prefEditor.putString("name", "Elena");
//		prefEditor.commit();
		preferences = getSharedPreferences(StaticValues.PREFS_NAME, MODE_PRIVATE);
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

		if (mp != null) {
			resumeTime = mp.getCurrentPosition() - RESUME_REWIND_LENGTH;
			savePreferences();
			mp.release();
		}
		sendBroadcast(new Intent(DESTROYED_STATUS));
		removeNotification();
		unregisterReceiver(receiver);
		Log.d("as", "AudioService destroyed");
	}

	public void playAudio(boolean forceUpdate) {
//OLD	Log.d("as", "Location " + location);
//		Log.d("as", "CurTrack " + currentTrackLocation);
//		if (location == null && currentTrackLocation == null) {
//			location = preferences.getString(StaticValues.PREF_RESUME_URL, null);
//		}
//		Log.d("as", "Location " + location);



		if (forceUpdate || currentEpisode == null) {
			currentEpisode = queueService.getFirstEpisode();
		}

		if (currentEpisode != null) {
			playAudioFromWeb(currentEpisode.mUrl);
		}
	}

	private void playAudioFromWeb(String location) {
		if (location != null) {
			if (mp == null || location.equals(currentTrackLocation) != true) {
				if (mp == null) {
					mp = new MediaPlayer();
				}
				currentTrackLocation = location;
				//Set track to beginning
				resumeTime = 0; //TODO check whether there is a time that needs to be resumed
				savePreferences();

				try {
//					if (currentTrackLocation == null) throw new Exception("No url currently");
					mp.reset();
					mp.setDataSource(currentTrackLocation);
					mp.prepareAsync();
					sendBroadcast(new Intent(PREPARING_STATUS));
				} catch (Exception e) {
					e.printStackTrace();
				}

				//Listener
				mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
					@Override
					public void onPrepared(MediaPlayer mp) {
						resumeAudio();
					}
				});
				//Listener
				mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
					public void onCompletion(MediaPlayer mp) {
//						mp.seekTo(0);
//						pauseAudio();
						stopAudio();
						Log.d("as", "End of Audio reached");
//						sendBroadcast(new Intent(PAUSED_STATUS));
					}
				});
				mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
					@Override
					public boolean onError(MediaPlayer mp, int what, int extra) {
						sendBroadcast(new Intent(ERROR_STATUS));
						return false;
					}
				});
			} else {
				resumeAudio();
			}
		} else if (currentTrackLocation != null) {
			resumeAudio();
		} else {
			Toast.makeText(this, "Nothing to play", Toast.LENGTH_SHORT).show();
		}
	}

	public void resumeAudio() {
		resumeTime = preferences.getInt(StaticValues.PREF_RESUME_TIME, 0);
		mp.seekTo(resumeTime);
		mp.start();
		sendBroadcast(new Intent(PLAYING_STATUS));
		showNotification();
	}

	public void pauseAudio() {
		mp.pause();
		resumeTime = mp.getCurrentPosition() - RESUME_REWIND_LENGTH;
		savePreferences();
		sendBroadcast(new Intent(PAUSED_STATUS));
		showNotification();
	}

	public void stopAudio() {
		mp.stop();
		resumeTime = mp.getCurrentPosition() - RESUME_REWIND_LENGTH;
		savePreferences();
		sendBroadcast(new Intent(STOPPED_STATUS));
		showNotification();
	}

	public void rewindAudio() {
		mp.seekTo(mp.getCurrentPosition() - SKIP_LENGTH);
	}

	public void skipAudio() {
		mp.seekTo(mp.getCurrentPosition() + SKIP_LENGTH);
	}

	private void savePreferences() {
		prefEditor.putString(StaticValues.PREF_RESUME_URL, currentTrackLocation);
		prefEditor.putInt(StaticValues.PREF_RESUME_TIME, resumeTime);
		Log.d("as", "resumeUrl " + currentTrackLocation);
		Log.d("as", "resumeTime " + resumeTime);
		prefEditor.commit();
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
			//playAudio();
		}
		else if (action.equals(PAUSE_ACTION)) {
			pauseAudio();
		}
		else if (action.equals(PLAY_ACTION)) {
//			String url = intent.getStringExtra("url");
			boolean forceUpdate = intent.getBooleanExtra(EXTRA_FORCE_UPDATE, false);
			playAudio(forceUpdate);
		}
		else if (action.equals(SKIP_ACTION)) {
			skipAudio();
		}
		else if (action.equals(REWIND_ACTION)) {
			rewindAudio();
		}
		else if (action.equals(STOP_ACTION)) {
			stopAudio();
		}else if (action.equals(DESTROY_ACTION)) {
//			Toast.makeText(this, "Destroy Action", Toast.LENGTH_LONG).show();
			stopSelf(); //Ends the service
		}
	}

	private void storePreferences() {

	}

}
