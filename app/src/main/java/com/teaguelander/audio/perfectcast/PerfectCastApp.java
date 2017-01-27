package com.teaguelander.audio.perfectcast;

import android.app.Application;
import android.content.Context;
import android.text.format.DateFormat;

import com.teaguelander.audio.perfectcast.services.DataService;
import com.teaguelander.audio.perfectcast.services.DatabaseService;
import com.teaguelander.audio.perfectcast.services.StorageService;

import java.text.SimpleDateFormat;

/**
 * Created by Teague-Win10 on 1/8/2017.
 */

public class PerfectCastApp extends Application {
	private static Context mContext;
	public static SimpleDateFormat rssDateFormatter;
	public static SimpleDateFormat basicDateFormatter;

	@Override
	public void onCreate() {
		super.onCreate();
		mContext = getApplicationContext();
		DataService.getInstance(mContext);
		StorageService.getInstance(mContext);
		DatabaseService.getInstance(mContext);
		rssDateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
		basicDateFormatter = new SimpleDateFormat("dd MMM yyyy hh:mm a");
	}

	public static Context getContext() {
		return mContext;
	}
}
