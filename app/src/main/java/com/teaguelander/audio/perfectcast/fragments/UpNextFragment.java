package com.teaguelander.audio.perfectcast.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.teaguelander.audio.perfectcast.R;

/**
 * Created by Teague-Win10 on 1/8/2017.
 */

public class UpNextFragment extends Fragment {
	public UpNextFragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

//		RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.view_recycler, container, false);
//		recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
//		return recyclerView;

		View view = inflater.inflate(R.layout.view_up_next, container, false);
		return view;

	}
}
