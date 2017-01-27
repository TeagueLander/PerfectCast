package com.teaguelander.audio.perfectcast.services;

import com.teaguelander.audio.perfectcast.objects.PodcastDetail;
import com.teaguelander.audio.perfectcast.objects.PodcastEpisode;

/**
 * Created by Teague-Win10 on 1/26/2017.
 */

public class TrackManager {

	DatabaseService mDatabaseService;
	AudioService mParentService;

	public TrackManager(AudioService parentService) {
		mDatabaseService = DatabaseService.getInstance(null);
		mParentService = parentService;
	}

	public PodcastEpisode getCurrentTrack() {
		return mDatabaseService.getNextEpisode();
	}

	public PodcastDetail getPodcastDetail(long id) {
//		return mDatabaseService.get
		return null;
	}

}
