package com.teaguelander.audio.perfectcast.objects;

/**
 * Created by Teague-Win10 on 2/11/2017.
 */

import android.support.v7.widget.RecyclerView;


public interface ItemDragAndDropHelperAdapter {
	void onItemMove(int fromPosition, int toPosition);
//	void onItemDropped();
	void onItemDismiss(int position);
}
