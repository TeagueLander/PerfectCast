package com.teaguelander.audio.perfectcast.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.teaguelander.audio.perfectcast.MainActivity;
import com.teaguelander.audio.perfectcast.R;
import com.teaguelander.audio.perfectcast.objects.PodcastEpisode;

/**
 * Created by Teague-Win10 on 1/31/2017.
 */

public class NotificationHelper {

	private static int ICON_APP = R.mipmap.ic_launcher;

	AudioService as;
	int mNotificationId = -1; //notificationID allows you to update the notification later on.
	Bitmap mBitmap;
	PodcastEpisode currentEpisode;
	boolean isPlaying;
	Target mCurrentTargetFunction;


	public NotificationHelper(AudioService as) {
		this.as = as;
	}

	private void getNotificationImage() {
		mCurrentTargetFunction = new Target() {
			@Override
			public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
				Log.d("nh", "Loaded BITMAP");
				setLargeIcon(bitmap);
				showNotification();
			}

			@Override
			public void onBitmapFailed(Drawable errorDrawable) {
				Log.d("nh", "Failed BITMAP");
				showNotification();
			}

			@Override
			public void onPrepareLoad(Drawable placeHolderDrawable) {
				Log.d("nh", "Prepared BITMAP");
			}
		};
		Log.d("nh", "PicassoService is trying to load " + currentEpisode.mPodcast.mImageUrl);
		PicassoService.loadIntoTarget(currentEpisode.mPodcast.mImageUrl, mCurrentTargetFunction);
	}

	public void setLargeIcon(Bitmap bitmap) {
		mBitmap = bitmap;
	}

	public int removeNotification() {
		NotificationManager mNotificationManager = (NotificationManager) as.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(mNotificationId);

		return -1;
	}

	public void update() {
		Log.d("nh", "Notification Update called");
		PodcastEpisode currentEpisode = as.getCurrentEpisode();
		isPlaying = as.getIsPlaying();

		Log.d("nh", "NOTIFICATION HELPER " + currentEpisode.toString());
		if (currentEpisode != this.currentEpisode) {
			Log.d("nh", "NEED A BLOODY NEW IMAGE FUCK!");
			this.currentEpisode = currentEpisode;
			getNotificationImage();
		} else {
			Log.d("nh", "NO NEED IMAGE NEEDED!");
			showNotification();
		}
	}

	public synchronized int showNotification() {

		Log.d("nh", "SHOW NOTICIATION");

		//Notification Title, Icon, and message
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(as);
		mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC); //This means it can be seen on the lock screen
		mBuilder.setSmallIcon(ICON_APP);
		if (mBitmap != null) {
			mBuilder.setLargeIcon(mBitmap);
		}
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
		mNotificationManager.notify(mNotificationId, mBuilder.build());
		return mNotificationId;
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
//		notificationManager.notify(mNotificationId, notification);
//
//		return mNotificationId;
	}*/

}
