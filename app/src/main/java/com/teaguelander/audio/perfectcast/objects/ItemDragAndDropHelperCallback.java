package com.teaguelander.audio.perfectcast.objects;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;

import com.teaguelander.audio.perfectcast.R;

import java.util.List;

import static android.support.v7.widget.helper.ItemTouchHelper.Callback.makeMovementFlags;

/**
 * Created by Teague-Win10 on 2/11/2017.
 */

public class ItemDragAndDropHelperCallback extends ItemTouchHelper.Callback {

	private final ItemDragAndDropHelperAdapter mAdapter;
	private RecyclerView.ViewHolder mCurrentDragItem;
	private Runnable mOnItemMoveEventFinished;
	
	public ItemDragAndDropHelperCallback(ItemDragAndDropHelperAdapter adapter, Runnable onItemMoveEventFinished) {
		mAdapter = adapter;
		mOnItemMoveEventFinished = onItemMoveEventFinished;
	}
	
	@Override
	public boolean isLongPressDragEnabled() {
		return true;
	}
	
	@Override
	public boolean isItemViewSwipeEnabled() {
		return false;
	}
	
	@Override
	public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
		int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
		int swipeFlags = 0; //ItemToItemuchHelper.START | ItemTouchHelper.END;

		if (viewHolder.getAdapterPosition() == 0) { //TODO this means playing track cant move, to do task is to fix it
			dragFlags = 0;
		}
		return makeMovementFlags(dragFlags, swipeFlags);
	}
	
	@Override
	public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
		mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
		return true;
	}
	
	@Override
	public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
		mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
	}

	@Override
	public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
//		Log.d("idahc", "Selected viewholder " + (viewHolder == null) + " actionState " + actionState);
//		if (viewHolder == null) {
//			super.onSelectedChanged(null, actionState);
//		} else {
//			if (viewHolder.getAdapterPosition() != 0) {
//				Log.d("idadhc", "Selected Changed " + actionState);
//				mCurrentDragItem = viewHolder;
//				viewHolder.itemView.setBackgroundResource(R.color.colorBackgroundSelected);
//				super.onSelectedChanged(viewHolder, actionState);
//			}
//		}
		if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
			mCurrentDragItem = viewHolder;
			if (mCurrentDragItem.getAdapterPosition() != 0) {
				mCurrentDragItem.itemView.setBackgroundResource(R.color.colorBackgroundSelected);
				super.onSelectedChanged(viewHolder, actionState);
			}
		} else if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
			//Drop event should save list, we could also put this in void clearView()
			mCurrentDragItem.itemView.setBackgroundResource(R.color.colorBackgroundPrimary);
			mOnItemMoveEventFinished.run();
		}
	}

}
