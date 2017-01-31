package com.teaguelander.audio.perfectcast.objects;

import java.util.Date;

/**
 * Created by Teague-Win10 on 1/14/2017.
 */

public class PodcastEpisode {
//TODO get episode from database if its there
	//Database Keys
	public static final String KEY_ID = "id";
	public static final String KEY_PODCAST_ID = "podcast_id";
	public static final String KEY_TITLE = "title";
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_URL = "url";
	public static final String KEY_DURATION = "duration";
	public static final String KEY_PUB_DATE = "date_published";
	public static final String KEY_BYTES = "size_in_bytes";
	public static final String KEY_PROGRESS = "progress";

	public static final String[] COLUMNS = { KEY_ID, KEY_PODCAST_ID, KEY_TITLE, KEY_DESCRIPTION, KEY_URL, KEY_DURATION, KEY_PUB_DATE, KEY_BYTES, KEY_PROGRESS };

	//Object fields
	public long mId = -1;
	public long mPodcastId = -1;
	public String mTitle;
	public String mDescription;
	public String mUrl;
	public String mDuration;
	public Date mPubDate;
	public long mBytes;
	public long mProgress = 0;
	public PodcastDetail mPodcast;

	public PodcastEpisode(){}

	public PodcastEpisode(String mTitle, String mDescription, String mUrl, String mDuration, Date mPubDate, long mBytes, PodcastDetail mPodcast) {
		super();
		this.mTitle = mTitle;
		this.mDescription = mDescription;
		this.mUrl = mUrl;
		this.mDuration = mDuration;
		this.mPubDate = mPubDate;
		this.mBytes = mBytes;
		this.mPodcast = mPodcast;
	}

	public void setIds(long id, long podcastId) {
		if (id != -1) {
			mId = id;
		}
		if (id != -1) {
			mPodcastId = podcastId;
		}
	}

	public void setProgress(long progress) {
		mProgress = progress;
	}

	@Override
	public String toString() {
		return "Title: " + mTitle + " Description: " + mDescription + " URL: " + mUrl;
	}

}
