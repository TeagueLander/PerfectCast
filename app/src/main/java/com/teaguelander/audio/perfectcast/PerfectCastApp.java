package com.teaguelander.audio.perfectcast;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import com.teaguelander.audio.perfectcast.services.DataService;
import com.teaguelander.audio.perfectcast.services.DatabaseService;
import com.teaguelander.audio.perfectcast.services.PicassoService;
import com.teaguelander.audio.perfectcast.services.TrackQueueService;

import java.text.SimpleDateFormat;

/**
 * Created by Teague-Win10 on 1/8/2017.
 */

public class PerfectCastApp extends Application {
	private static Context mContext;
	public static SimpleDateFormat rssDateFormatter;
	public static SimpleDateFormat basicDateFormatter;
	public static SimpleDateFormat durationFormatter;

	@Override
	public void onCreate() {
		super.onCreate();
		rssDateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
		basicDateFormatter = new SimpleDateFormat("dd MMM yyyy hh:mm a");
//		durationFormatter = new SimpleDateFormat("HH:mm:ss");

		mContext = getApplicationContext();
		DataService.getInstance(mContext);
//		StorageService.getInstance(mContext);
		DatabaseService.getInstance(mContext);
		TrackQueueService.getInstance();
		PicassoService.getInstance(mContext);
	}

	@Override
	public void onTrimMemory(int level) {
		super.onTrimMemory(level);
		Toast.makeText(mContext, "You need to trim the FUCKING MEMORY", Toast.LENGTH_LONG); //TODO implement a better solution
	}

	public static Context getContext() {
		return mContext;
	}
}

//TODO Credit
/*
http://www.flaticon.com/free-icon/fishing-rod_140661#term=fishing rod&page=1&position=2



 */