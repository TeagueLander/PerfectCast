package com.teaguelander.audio.perfectcast.objects;

import android.provider.Settings;

import com.teaguelander.audio.perfectcast.services.DatabaseService;

import java.util.ArrayList;

/**
 * Created by Teague-Win10 on 1/28/2017.
 */

public class TrackQueue {

	//Database  Columns
	public static final String KEY_EPISODE_ID = "episode_id";
	public static final String KEY_ORDER_NUMBER = "order_number";
	public static final String[] COLUMNS = { KEY_EPISODE_ID, KEY_ORDER_NUMBER };

//	private static String[] viewColumns = null;
	private ArrayList<PodcastEpisode> queueItems;

	public TrackQueue() {
//		queueItems = new ArrayList<PodcastEpisode>();
		queueItems = DatabaseService.getInstance(null).getTrackQueue();
	}

//	public void addEpisode(PodcastEpisode episode) {
//
//	}



	//Setup VIEW_COLUMNS
//	public static String[] getViewColumns() {
//		if (viewColumns == null) {
//			viewColumns = new String[PodcastEpisode.COLUMNS.length + 1];
//			viewColumns[0] = KEY_ORDER_NUMBER;
//		}
//		return viewColumns;
//	}


//	public class QueueItem {
//		public long mEpisodeId;
////		public long mOrderNumber;
//		private PodcastEpisode mEpisode;
//
////		public QueueItem(long episodeId, long orderNumber) {
//		public QueueItem(long episodeId) {
//			mEpisodeId = episodeId;
////			mOrderNumber = orderNumber;
//		}
//	}



}
