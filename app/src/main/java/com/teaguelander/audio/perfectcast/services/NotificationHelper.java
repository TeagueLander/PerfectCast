package com.teaguelander.audio.perfectcast.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import com.teaguelander.audio.perfectcast.MainActivity;
import com.teaguelander.audio.perfectcast.R;
import com.teaguelander.audio.perfectcast.objects.PodcastEpisode;

/**
 * Created by Teague-Win10 on 1/31/2017.
 */

public class NotificationHelper {

	private static int ICON_APP = R.mipmap.ic_launcher;

	AudioService as;
	int notificationId = -1; //notificationID allows you to update the notification later on.

	public NotificationHelper(AudioService as) {
		this.as = as;
	}

	public int removeNotification() {
		NotificationManager mNotificationManager = (NotificationManager) as.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(notificationId);

		return -1;
	}

	public synchronized int showNotification() {

		PodcastEpisode currentEpisode = as.getCurrentEpisode();
		boolean isPlaying = as.getIsPlaying();

		//Notification Title, Icon, and message
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(as);
		mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC); //This means it can be seen on the lock screen
		mBuilder.setSmallIcon(ICON_APP);
//		mBuilder.setLargeIcon(podcastImage);
//		mBuilder.setLargeIcon(PicassoService.getInstance(null).getBitmap(currentEpisode.mPodcast.mImageUrl));
		mBuilder.setContentTitle(currentEpisode.mPodcast.mTitle);
		mBuilder.setContentText(currentEpisode.mTitle);
		mBuilder.setShowWhen(false); //Tells us whether to display the time
		mBuilder.setStyle(new NotificationCompat.MediaStyle() //We should add to this to give it more info
							  .setShowActionsInCompactView(new int[] {0,1,2})
							  .setMediaSession(null)
		);
		mBuilder.setDeleteIntent(PendingIntent.getBroadcast(as, 100, new Intent(AudioService.DESTROY_ACTION), 0));

		//Add Buttons
		PendingIntent pendingRewindIntent = PendingIntent.getBroadcast(as, 100, new Intent(AudioService.REWIND_ACTION), 0);
		mBuilder.addAction(android.R.drawable.ic_media_rew, "Rewind", pendingRewindIntent);
		if (!isPlaying) {
			PendingIntent pendingPlayIntent = PendingIntent.getBroadcast(as, 100, new Intent(AudioService.PLAY_ACTION), 0);
			mBuilder.addAction(android.R.drawable.ic_media_play, "Play", pendingPlayIntent);
		}else {
			PendingIntent pendingPauseIntent = PendingIntent.getBroadcast(as, 100, new Intent(AudioService.PAUSE_ACTION), 0);
			mBuilder.addAction(android.R.drawable.ic_media_pause, "Pause", pendingPauseIntent);
		}
		PendingIntent pendingSkipIntent = PendingIntent.getBroadcast(as, 100, new Intent(AudioService.SKIP_ACTION), 0);
		mBuilder.addAction(android.R.drawable.ic_media_ff, "Skip", pendingSkipIntent);

		//Notification Pressed set (may need to ahve a stackbuilder here)
		Intent openMainActivity = new Intent(as, MainActivity.class);
		openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		PendingIntent pendingOpenMainActivity =
			PendingIntent.getActivity(
				as,
				0,
				openMainActivity,
				PendingIntent.FLAG_UPDATE_CURRENT
			);
		mBuilder.setContentIntent(pendingOpenMainActivity);

		//Flags
		if (!isPlaying) {
			mBuilder.setOngoing(false);
		}else {
			mBuilder.setOngoing(true);
		}

		//Gets the id of the notification manager and then sends the notification we built
		NotificationManager mNotificationManager = (NotificationManager) as.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(notificationId, mBuilder.build());
		return notificationId;
	}


	/*showcustomnotification() {
		//		Notification.Builder builder= new Notification.Builder(getApplicationContext());
//		builder.setSmallIcon(ICON_APP);
//
//		RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification);
//		remoteViews.setImageViewResource(R.id.image, ICON_APP);
//		remoteViews.setTextViewText(R.id.play, "HELLOSOOO");
//
//		builder.setContent(remoteViews);
//
//		Notification notification = builder.build();
//		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//		notificationManager.notify(notificationId, notification);
//
//		return notificationId;
	}*/

}
