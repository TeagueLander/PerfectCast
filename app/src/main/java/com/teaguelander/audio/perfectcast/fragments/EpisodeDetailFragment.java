package com.teaguelander.audio.perfectcast.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.teaguelander.audio.perfectcast.MainActivity;
import com.teaguelander.audio.perfectcast.R;
import com.teaguelander.audio.perfectcast.objects.PodcastEpisode;
import com.teaguelander.audio.perfectcast.services.AudioService;
import com.teaguelander.audio.perfectcast.services.TrackQueueService;

/**
 * Created by Teague-Win10 on 1/17/2017.
 */

public class EpisodeDetailFragment extends Fragment {

	public static String QUEUE_REMOVE_ITEM = "com.teaguelander.audio.perfectcast.QUEUE_REMOVE_ITEM";
	public static String QUEUE_ADD_ITEM = "com.teaguelander.audio.perfectcast.QUEUE_ADD_ITEM";
		public static String ITEM_POSITION = "itemPosition";
		public static String OLD_POSITION = "oldPosition";

	TrackQueueService mTrackQueueService;
	BroadcastReceiver receiver;
	PodcastEpisode mEpisode;
	int mPositionInQueue = -1;

	View mView;
	TextView titleTextView;
	Button playEpisodeButton;
	Button toggleQueueButton;
	TextView descriptionTextView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mTrackQueueService = TrackQueueService.getInstance();

		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if (action.equals(AudioService.COMPLETED_STATUS)) {
					if (mPositionInQueue == 0) {
						mPositionInQueue = -1;
						setToggleQueueButtonImage();
					}
				}
				if (action.equals(AudioService.NEW_TRACK_STATUS)) { //TODO at to queue button should change here instead of when button pressed?
					getEpisodeInQueueStatus();
				}
				//TODO added PAUSE PLAY
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction(AudioService.NEW_TRACK_STATUS);
		filter.addAction(AudioService.COMPLETED_STATUS);
//		filter.addAction(EpisodeDetailFragment.QUEUE_REMOVE_ITEM);
		getActivity().registerReceiver(receiver, filter);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		getActivity().unregisterReceiver(receiver);
	}

	public void setEpisode(PodcastEpisode episode) {
		mEpisode = episode;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.view_episode_detail, container, false);

		if (mEpisode != null) {
			titleTextView = (TextView) mView.findViewById(R.id.episode_title);
			playEpisodeButton = (Button) mView.findViewById(R.id.playEpisodeButton);
			toggleQueueButton = (Button) mView.findViewById(R.id.addtoQueueButton);
			descriptionTextView = (TextView) mView.findViewById(R.id.episode_description);

			if (mEpisode.mTitle != null) {
				titleTextView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
				titleTextView.setSingleLine(true);
				titleTextView.setMarqueeRepeatLimit(5);
				titleTextView.setSelected(true);
				titleTextView.setText(mEpisode.mTitle);
			}
			if (mEpisode.mDescription != null) {
				descriptionTextView.setText(Html.fromHtml(mEpisode.mDescription).toString());
			}

			getEpisodeInQueueStatus();
			setToggleQueueButtonImage();

			playEpisodeButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					int oldPosition = mPositionInQueue;
					mPositionInQueue = TrackQueueService.getInstance().addEpisode(0, mEpisode); //should always return 0
					((MainActivity) getActivity()).playEpisodeInQueue();

					//Add to front, not in queue
					Intent intent = new Intent(QUEUE_ADD_ITEM);
					intent.putExtra(ITEM_POSITION, mPositionInQueue);
					if (oldPosition != -1) {
						intent.putExtra(OLD_POSITION, oldPosition);
					}
					getActivity().sendBroadcast(intent);
				}
			});
			toggleQueueButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mPositionInQueue > -1) {
						if (mPositionInQueue != 0) {
							mTrackQueueService.removeEpisode(mPositionInQueue);
							Intent intent = new Intent(QUEUE_REMOVE_ITEM);
							intent.putExtra(ITEM_POSITION, mPositionInQueue);
							getActivity().sendBroadcast(intent);

							mPositionInQueue = -1;
							setToggleQueueButtonImage();
						} else {
							Log.d("edf", "CANT REMOVE EPISODE AT FRONT OF QUEUE"); //TODO remove episode at front of queue
							Toast.makeText(getContext(), "Currently cant remove item at front of queue", Toast.LENGTH_LONG).show();
						}
					} else {
						mPositionInQueue = TrackQueueService.getInstance().addEpisodeAtEnd(mEpisode); //TODO maybe the trackqueue should make broadcasts
						Intent intent = new Intent(QUEUE_ADD_ITEM);
						intent.putExtra(ITEM_POSITION, mPositionInQueue);
						getActivity().sendBroadcast(intent);

						setToggleQueueButtonImage();
					}
				}
			});

		}

		return mView;
	}

	private void getEpisodeInQueueStatus() {
		mPositionInQueue = mTrackQueueService.checkForEpisode(mEpisode);
	}

	private void setToggleQueueButtonImage() {
		int drawableResource;
		String text;

		if (mPositionInQueue == -1) {
			drawableResource = android.R.drawable.ic_menu_add;
			text = "Add to Queue";
		}else {
			drawableResource = android.R.drawable.ic_menu_delete;
			text = "Remove from Queue";
//			toggleQueueButton.setTextSize(10);
		}
		Drawable drawable = getContext().getResources().getDrawable(drawableResource, null);

		toggleQueueButton.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
		toggleQueueButton.setText(text);
	}

}
