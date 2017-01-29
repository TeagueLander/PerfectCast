package com.teaguelander.audio.perfectcast.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.teaguelander.audio.perfectcast.MainActivity;
import com.teaguelander.audio.perfectcast.R;
import com.teaguelander.audio.perfectcast.objects.PodcastEpisode;
import com.teaguelander.audio.perfectcast.services.TrackQueueService;

/**
 * Created by Teague-Win10 on 1/17/2017.
 */

public class EpisodeDetailFragment extends Fragment {

	PodcastEpisode mEpisode;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public void setEpisode(PodcastEpisode episode) {
		mEpisode = episode;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.view_episode_detail, container, false);

		if (mEpisode != null) {
			TextView titleTextView = (TextView) v.findViewById(R.id.episode_title);
			if (mEpisode.mTitle != null) {
				titleTextView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
				titleTextView.setSingleLine(true);
				titleTextView.setMarqueeRepeatLimit(5);
				titleTextView.setSelected(true);
				titleTextView.setText(mEpisode.mTitle);
			}

			TextView descriptionTextView = (TextView) v.findViewById(R.id.episode_description);
			if (mEpisode.mDescription != null) {
				descriptionTextView.setText(Html.fromHtml(mEpisode.mDescription).toString());
			}

			Button playEpisodeButton = (Button) v.findViewById(R.id.playEpisodeButton);
			Button addToQueueButton = (Button) v.findViewById(R.id.addtoQueueButton);

			playEpisodeButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
//					((MainActivity) getActivity()).playEpisode(mEpisode);
//					TrackQueueService.ad
					TrackQueueService.getInstance().addEpisode(0, mEpisode);
					((MainActivity) getActivity()).playEpisodeInQueue();
				}
			});
			addToQueueButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					TrackQueueService.getInstance().addEpisodeAtEnd(mEpisode);
				}
			});


		}

		return v;
	}

}
