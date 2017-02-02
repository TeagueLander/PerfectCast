package com.teaguelander.audio.perfectcast.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.media.MediaPlayer;
//import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

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
	public static String REQUEST_STATUS_ACTION = "com.teaguelander.audio.perfectcast.REQUEST_STATUS_ACTION";

	public static String PLAYING_STATUS = "com.teaguelander.audio.perfectcast.PLAYING_STATUS";
	public static String PREPARING_STATUS = "com.teaguelander.audio.perfectcast.PREPARING_STATUS";
	public static String STOPPED_STATUS = "com.teaguelander.audio.perfectcast.STOPPED_STATUS";
	public static String PAUSED_STATUS = "com.teaguelander.audio.perfectcast.PAUSED_STATUS";
	public static String ERROR_STATUS = "com.teaguelander.audio.perfectcast.ERROR_STATUS";
	public static String DESTROYED_STATUS = "com.teaguelander.audio.perfectcast.DESTROYED_STATUS";
	public static String NEW_TRACK_STATUS = "com.teaguelander.audio.perfectcast.NEW_TRACK_STATUS";
		public static String EXTRA_CURRENT_PROGRESS = "currentProgress";
		public static String EXTRA_MAX_PROGRESS = "maxProgress";

	private static int SKIP_LENGTH = 30000;
	private static int RESUME_REWIND_LENGTH = 2000;

	TrackQueueService queueService;
	PodcastEpisode currentEpisode;
	String currentTrackLocation = null;
	int currentProgress = -1; // Resume time
	int maxProgress = -1;
	Bitmap podcastImage = null;

	MediaPlayer mp;
	NotificationHelper notification;
	String mStatus = DESTROYED_STATUS;
	BroadcastReceiver receiver; //For Intents

	@Override
	public IBinder onBind(Intent arg0) { return null; };

	@Override
	public void onCreate() {
		super.onCreate();

		//Notification Helper
		notification = new NotificationHelper(this);
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

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//Toast.makeText(this, "Audio Service Started", Toast.LENGTH_LONG).show();
		performAction(intent);
		//update();

		return START_NOT_STICKY;
	}

	//Cleanup the service
	public void onDestroy() {
		super.onDestroy();

		if (mp != null) {
			updateEpisode();
			mp.release();
		}
		updateStatus(DESTROYED_STATUS);
		notification.removeNotification();
		unregisterReceiver(receiver);
		Log.d("as", "AudioService destroyed");
	}

	public void updateStatus(String status) {
		mStatus = status;

		Intent intent = new Intent(status);
		intent.putExtra(EXTRA_CURRENT_PROGRESS, currentProgress);
		intent.putExtra(EXTRA_MAX_PROGRESS, maxProgress);
		sendBroadcast(intent);
	}

	public void playAudio(boolean forceUpdate) {

		if (forceUpdate || currentEpisode == null) {
			//Save Position of Episode
			updateEpisode();

			//Get New Episode
			currentEpisode = queueService.getFirstEpisode();
//			podcastImage = storageService.getImageFromStorageOrUrl(currentEpisode.mPodcast.mImageUrl, podcastImage, this);
			if (currentEpisode != null) {
				currentProgress = (int) currentEpisode.mProgress;
				updateStatus(NEW_TRACK_STATUS);
			}
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
//				currentProgress = 0; //TODO check whether there is a time that needs to be resumed
//				savePreferences();

				try {
//					if (currentTrackLocation == null) throw new Exception("No url currently");
					mp.reset();
					mp.setDataSource(currentTrackLocation);
					mp.prepareAsync();
					updateStatus(PREPARING_STATUS);
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
//						updateStatus(PAUSED_STATUS));
					}
				});
				mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
					@Override
					public boolean onError(MediaPlayer mp, int what, int extra) {
						updateStatus(ERROR_STATUS);
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
//		currentProgress = preferences.getInt(StaticValues.PREF_RESUME_TIME, 0);
		maxProgress = mp.getDuration();
		mp.seekTo(currentProgress);
		mp.start();
		updateStatus(PLAYING_STATUS);
		notification.update();
	}

	public void pauseAudio() {
		mp.pause();
		updateEpisode();
		updateStatus(PAUSED_STATUS);
		notification.update();
	}

	public void stopAudio() {
		mp.stop();
		updateEpisode();
		updateStatus(STOPPED_STATUS);
		notification.update();
	}

	public void rewindAudio() {
		mp.seekTo(mp.getCurrentPosition() - SKIP_LENGTH);
	}

	public void skipAudio() {
		mp.seekTo(mp.getCurrentPosition() + SKIP_LENGTH);
	}

//	private void savePreferences() {
//		prefEditor.putString(StaticValues.PREF_RESUME_URL, currentTrackLocation);
//		prefEditor.putInt(StaticValues.PREF_RESUME_TIME, currentProgress);
//		Log.d("as", "resumeUrl " + currentTrackLocation);
//		Log.d("as", "currentProgress " + currentProgress);
//		prefEditor.commit();
//	}
	private void updateEpisode() {
		if (mp != null) {
			currentProgress = mp.getCurrentPosition();
			queueService.updateEpisodeProgress(currentEpisode, currentProgress);
		}
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
		}else if (action.equals(REQUEST_STATUS_ACTION)) {
			updateStatus(mStatus);
		}
	}

	public PodcastEpisode getCurrentEpisode() {
		return currentEpisode;
	}
	public boolean getIsPlaying() {
		if (mp != null) {
			if (mp.isPlaying()) {
				return true;
			}
		}
		return false;
	}

}
