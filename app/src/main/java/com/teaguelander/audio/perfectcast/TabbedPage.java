package com.teaguelander.audio.perfectcast;

/**
 * Created by Teague-Win10 on 1/7/2017.
 */

public enum TabbedPage {

	FAVOURITES(R.string.tab_favourites, R.layout.view_favourites),
	NOW_PLAYING(R.string.tab_now_playing, R.layout.view_now_playing),
	UP_NEXT(R.string.tab_up_next, R.layout.view_up_next);

	private int titleResId;
	private int layoutResId;

	TabbedPage (int titleResId, int layoutResId) {
		this.titleResId = titleResId;
		this.layoutResId = layoutResId;
	}

	public int getTitleResId() {
		return titleResId;
	}
	public int getLayoutResId() {
		return layoutResId;
	}

}
