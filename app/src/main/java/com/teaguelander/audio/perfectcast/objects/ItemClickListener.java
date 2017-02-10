package com.teaguelander.audio.perfectcast.objects;

/**
 * Created by Teague-Win10 on 1/11/2017.
 */

public interface ItemClickListener {
	void onItemClicked(String feedUrl);
	void onItemClicked(PodcastEpisode episode);
	void onItemClicked(PodcastDetail podcast);
}
