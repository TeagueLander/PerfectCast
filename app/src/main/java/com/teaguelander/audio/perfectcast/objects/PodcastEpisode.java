package com.teaguelander.audio.perfectcast.objects;

import java.util.Date;

/**
 * Created by Teague-Win10 on 1/14/2017.
 */

public class PodcastEpisode {

	//Database Keys
	public static final String KEY_ID = "id";
	public static final String KEY_PODCAST_ID = "podcast_id";
	public static final String KEY_TITLE = "title";
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_URL = "url";

	public static final String[] COLUMNS = { KEY_ID, KEY_PODCAST_ID, KEY_TITLE, KEY_DESCRIPTION, KEY_URL };

	//Object fields
	public long mId = -1;
	public long mPodcastId = -1;
	public String mTitle;
	public String mDescription;
	public String mUrl;
	public String mDuration;
	public Date mPubDate;
	public String mBytes;
	public PodcastDetail mPodcast;

	public PodcastEpisode(){};

	public PodcastEpisode(String mTitle, String mDescription, String mUrl, String mDuration, Date mPubDate, String mBytes, PodcastDetail mPodcast) {
		super();
//		this.mId = mId;
		this.mTitle = mTitle;
		this.mDescription = mDescription;
		this.mUrl = mUrl;
		this.mDuration = mDuration;
		this.mPubDate = mPubDate;
		this.mBytes = mBytes;
		this.mPodcast = mPodcast;
	}

	public void setIds(long id, long podcastId) {
		mId = id;
		mPodcastId = podcastId;
	}

	@Override
	public String toString() {
		return "Title: " + mTitle + " Description: " + mDescription + " URL: " + mUrl;
	}

}
