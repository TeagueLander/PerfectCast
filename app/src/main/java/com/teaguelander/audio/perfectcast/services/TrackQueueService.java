package com.teaguelander.audio.perfectcast.services;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.teaguelander.audio.perfectcast.objects.PodcastEpisode;
import com.teaguelander.audio.perfectcast.services.DatabaseService;

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
	private DatabaseService mDatabase;

	private TrackQueueService() {
		mDatabase = DatabaseService.getInstance(null);
		queueItems = mDatabase.getTrackQueue();
	}

	public static synchronized TrackQueueService getInstance() {
		if (instance == null) {
			instance = new TrackQueueService();
		}
		return instance;
	}

// GETTING
	public PodcastEpisode getFirstEpisode() {
		Log.d("tqs", "Getting first episode in queue");
		if (queueItems.size() > 0) {
			return queueItems.get(0);
		}else {
			return null;
		}
	}




//MODIFYING

	public void addEpisode(int position, PodcastEpisode episode) {
		Log.d("tqs", "----------------Queue before insert: ");
		for (PodcastEpisode item : queueItems) {
			Log.d("tqs", "Podcast: " + item.mPodcast.mTitle + " Episode: " + item.mTitle);
		}
		Log.d("tqs", "----------------");

		mDatabase.addEpisode(episode);

//		Remove episode from the position it was in
		for (PodcastEpisode item : queueItems) {
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

		Log.d("tqs", "Queue before database: ");
		for (PodcastEpisode item : queueItems) {
			Log.d("tqs", "Podcast: " + item.mPodcast.mTitle + " Episode: " + item.mTitle + " ID: " + item.mId);
		}

		mDatabase.updateTrackQueue(queueItems);
	}

	public void addEpisodeAtEnd(PodcastEpisode episode) {
		addEpisode(queueItems.size(), episode);
	}

	//TODO may want to remove by PodcastEpisode
	public void removeEpisode(int position) {
		queueItems.remove(position);
		mDatabase.updateTrackQueue(queueItems);
	}

}
