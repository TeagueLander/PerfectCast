package com.teaguelander.audio.perfectcast.services;

import android.util.Log;

import com.teaguelander.audio.perfectcast.objects.PodcastEpisode;

import java.util.ArrayList;

/**
 * Created by Teague-Win10 on 1/28/2017.
 */
//TODO maybe more methods should be synchronized in this and/or other places such as Database service so the functions can't interfere with each other
public class TrackQueueService {

	//Database  Columns
	public static final String KEY_EPISODE_ID = "episode_id";
	public static final String KEY_ORDER_NUMBER = "order_number";
	public static final String[] COLUMNS = { KEY_EPISODE_ID, KEY_ORDER_NUMBER };

	private static TrackQueueService instance;
	private ArrayList<PodcastEpisode> queueItems;
	private DatabaseService mDatabaseService;

	private TrackQueueService() {
		mDatabaseService = DatabaseService.getInstance(null);
		queueItems = mDatabaseService.getTrackQueue();
	}

	public static synchronized TrackQueueService getInstance() {
		if (instance == null) {
			instance = new TrackQueueService();
		}
		return instance;
	}

// GETTING
	public PodcastEpisode getFirstEpisode() {
//		Log.d("tqs", "Getting first episode in queue");
		if (queueItems.size() > 0) {
			return queueItems.get(0);
		}else {
			return null;
		}
	}

	public int checkForEpisode(PodcastEpisode episode) {
		int position = -1;
		for (PodcastEpisode queueEpisode: queueItems) {
			position++;
//			Log.d("tqs","Episode " + episode.mId + "  against " + queueEpisode.mId);
			if (episode.mUrl.equals(queueEpisode.mUrl)) {
				return position;
			}
		}
		return -1;
	}

//MODIFYING
	public int addEpisode(int position, PodcastEpisode episode) {
		/*Log.d("tqs", "----------------Queue before insert: ");
		for (PodcastEpisode item : queueItems) {
			Log.d("tqs", "Podcast: " + item.mPodcast.mTitle + " Episode: " + item.mTitle);
		}
		Log.d("tqs", "----------------");*/

		episode.setIds(mDatabaseService.addEpisode(episode),-1);

//		Remove episode from the position it was in
		for (PodcastEpisode item : queueItems) {
//			Log.d("tqs", "maybe remove? " + episode.mId + " " + item.mId);
			if (episode.mId == item.mId) {
				queueItems.remove(item);
				break;
			}
		}

		//Check if position is too large
		if (position > queueItems.size()) {
			position = queueItems.size();
		}
		queueItems.add(position, episode);

		/*Log.d("tqs", "Queue before database: ");
		for (PodcastEpisode item : queueItems) {
			Log.d("tqs", "Podcast: " + item.mPodcast.mTitle + " Episode: " + item.mTitle + " ID: " + item.mId);
		}*/

		mDatabaseService.updateTrackQueue(queueItems);
		return position;
	}

	public int addEpisodeAtEnd(PodcastEpisode episode) {
		return addEpisode(queueItems.size(), episode);
	}

	//TODO may want to remove by PodcastEpisode
	public void removeEpisode(int position) {
		queueItems.remove(position);
		mDatabaseService.updateTrackQueue(queueItems);
	}

	public void updateEpisodeProgress(PodcastEpisode episode, long progress) {
		episode.setProgress(progress);
		mDatabaseService.updateEpisodeProgress(episode);
	}

	public ArrayList<PodcastEpisode> getQueueItems() {
		return queueItems;
	}

	public void onEpisodeFinished() {
		PodcastEpisode episode = queueItems.get(0);

		episode.setProgress(episode.mMaxProgress);
//		mDatabaseService.updateEpisodeProgress(episode); Done in playAudio

		removeEpisode(0);
	}

	public void updateDatabase() {
		printQueue();
		mDatabaseService.updateTrackQueue(queueItems);
	}

	private void printQueue() {
		Log.d("tqs", "SAVING DATABASE >>>>>>>>>>>>>>>>" + " REF ID: " + Integer.toHexString(System.identityHashCode(queueItems)) );
		for (PodcastEpisode episode : queueItems) {
			Log.d("tqs", "Title: " + episode.mTitle);
		}
		Log.d("tqs", "END SAVING DATABASE >>>>>>>>>>>>>>>>");
	}
}
