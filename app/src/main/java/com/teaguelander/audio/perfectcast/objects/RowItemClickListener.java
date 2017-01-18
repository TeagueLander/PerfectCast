package com.teaguelander.audio.perfectcast.objects;

/**
 * Created by Teague-Win10 on 1/11/2017.
 */

public interface RowItemClickListener {
	void onRowItemClicked(String feedUrl);
	void onRowItemClicked(PodcastEpisode episode);
}
