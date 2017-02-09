package com.teaguelander.audio.perfectcast.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.teaguelander.audio.perfectcast.R;
import com.teaguelander.audio.perfectcast.objects.PodcastEpisode;
import com.teaguelander.audio.perfectcast.services.AudioService;
import com.teaguelander.audio.perfectcast.services.PicassoService;
import com.teaguelander.audio.perfectcast.services.TrackQueueService;

import org.w3c.dom.Text;

/**
 * Created by Teague-Win10 on 1/8/2017.
 */

public class NowPlayingFragment extends Fragment {

	TrackQueueService trackQueueService;
	BroadcastReceiver receiver;

	View mView;
	ImageView mImageView;
	TextView mTitleView;
//	TextView mEpisodeTitleView;
	FloatingActionButton mJumpBackwardButton;
	FloatingActionButton mPlayPauseButton;
	FloatingActionButton mJumpForwardButton;
	SeekBar mSeekbar;

	Handler mProgressHandler = new Handler();
	PodcastEpisode currentEpisode;
	boolean isAudioPlaying;
	long mCurrentProgress;
	long mMaxProgress;

	public NowPlayingFragment() {

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		trackQueueService = TrackQueueService.getInstance();

		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				Log.d("ma", "NowPlaying received Intent: " + action);

				//TODO update %complete and stuff like that
				if (action.equals(AudioService.DESTROYED_STATUS)) {
					setAudioIsPlaying(false);
				} else if (action.equals(AudioService.ERROR_STATUS)) {
					setAudioIsPlaying(false);
				} else if (action.equals(AudioService.PAUSED_STATUS)) {
					setAudioIsPlaying(false);
				} else if (action.equals(AudioService.STOPPED_STATUS)) {
					setAudioIsPlaying(false);
				} else if (action.equals(AudioService.PREPARING_STATUS)) {
					//setAudioIsPlaying(false); TODO remove?
//					setLoadingVisible(); TODO add spinner
				} else if (action.equals(AudioService.PLAYING_STATUS)) {
					setAudioIsPlaying(true);
				} else if (action.equals(AudioService.NEW_TRACK_STATUS)) {
					updateCurrentTrackInfo();
				} else if (action.equals(AudioService.COMPLETED_STATUS)) {
					setAudioIsPlaying(false);
				}
				if (action.equals(AudioService.NEW_TRACK_STATUS)) {
					updateCurrentTrackInfo();
				}

				mCurrentProgress = intent.getIntExtra(AudioService.EXTRA_CURRENT_PROGRESS, 0) / 1000;
				mMaxProgress = intent.getIntExtra(AudioService.EXTRA_MAX_PROGRESS, 0) / 1000;
				updateProgress();
			}
		};

		IntentFilter filter = new IntentFilter();
		filter.addAction(AudioService.PLAYING_STATUS);
		filter.addAction(AudioService.PREPARING_STATUS);
		filter.addAction(AudioService.STOPPED_STATUS);
		filter.addAction(AudioService.PAUSED_STATUS);
		filter.addAction(AudioService.ERROR_STATUS);
		filter.addAction(AudioService.DESTROYED_STATUS);
		filter.addAction(AudioService.NEW_TRACK_STATUS);
		filter.addAction(AudioService.COMPLETED_STATUS);
		getActivity().registerReceiver(receiver, filter);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		getActivity().unregisterReceiver(receiver);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d("npf", "NowPlayingFragment created");

		mView = inflater.inflate(R.layout.view_now_playing, container, false);

		mImageView = (ImageView) mView.findViewById(R.id.podcastImage);
		mTitleView = (TextView) mView.findViewById(R.id.podcastTitle);
//		mEpisodeTitleView = (TextView) mView.findViewById(R.id.episodeTitle);
		mJumpBackwardButton = (FloatingActionButton) mView.findViewById(R.id.jumpBackwardButton);
		mPlayPauseButton = (FloatingActionButton) mView.findViewById(R.id.playPauseButton);
		mJumpForwardButton = (FloatingActionButton) mView.findViewById(R.id.jumpForwardButton);
		mSeekbar = (SeekBar) mView.findViewById(R.id.seekbar);

		updateCurrentTrackInfo();

		return mView;
	}

	private void setAudioIsPlaying(boolean playing) {
		isAudioPlaying = playing;

		mPlayPauseButton.setVisibility(View.VISIBLE);
//		mProgressCircle.setVisibility(View.INVISIBLE); //TODO Loading track spinner
		if (playing) {
			mPlayPauseButton.setImageResource(android.R.drawable.ic_media_pause);
		}else {
			mPlayPauseButton.setImageResource(android.R.drawable.ic_media_play);
		}
	}

	private void updateCurrentTrackInfo() {
		currentEpisode = trackQueueService.getFirstEpisode();

		if (currentEpisode != null) {

			mTitleView.setText(currentEpisode.mPodcast.mTitle);
//			mEpisodeTitleView.setText(currentEpisode.mTitle);

			setImage();
		}

	}

	private void setImage() {
		PicassoService.loadLargeImage(currentEpisode.mPodcast.mImageUrl, mImageView);
	}

	private void updateProgress() {
		//UPDATE SEEKBAR
		mSeekbar.setMax((int)mMaxProgress);
		mSeekbar.setProgress((int)mCurrentProgress);

		mProgressHandler.removeCallbacks(mUpdateProgressTask);
		if (isAudioPlaying == true) {
			mProgressHandler.postDelayed(mUpdateProgressTask, 1000);
		} else {

		}
	}

	private Runnable mUpdateProgressTask = new Runnable() {
		@Override
		public void run() {
			mCurrentProgress += 1;
			mSeekbar.setMax((int)mMaxProgress);
			mSeekbar.setProgress((int)mCurrentProgress);
			mProgressHandler.postDelayed(mUpdateProgressTask, 1000);
		}
	};

}
