package com.teaguelander.audio.perfectcast.services;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;

/**
 * Created by Teague-Win10 on 1/31/2017.
 */

public class PicassoService {

	private static PicassoService instance;
	private static Context mContext;
	private static int IMAGE_SIZE = 256;
	private static int LARGE_IMAGE_SIZE = 512;

	private PicassoService(Context context) {
		mContext = context;

		//Picasso Settings here
		Picasso.with(mContext);
//			.setIndicatorsEnabled(true); //TODO get images from storage
		//If we need to set our cache size then see https://square.github.io/picasso/2.x/picasso/com/squareup/picasso/Picasso.html
	}

	public static synchronized PicassoService getInstance(Context context) {
		if (instance == null) {
			instance = new PicassoService (context);
		}
		return instance;
	}

	public static void loadImage(String url, ImageView imageView) {
		Picasso.with(mContext)
			.load(url)
			.resize(IMAGE_SIZE,IMAGE_SIZE)
			.into(imageView);
	}

	public static void loadLargeImage(String url, ImageView imageView) {
		Picasso.with(mContext)
			.load(url)
			.resize(LARGE_IMAGE_SIZE,LARGE_IMAGE_SIZE)
			.into(imageView);
	}

	public static void loadIntoTarget(String url, Target target) {
		Picasso.with(mContext)
			.load(url)
			.resize(IMAGE_SIZE,IMAGE_SIZE)
			.into(target);
	}

	public static Bitmap getBitmap(String url) {
		Bitmap bmp = null;
		try {
			bmp = Picasso.with(mContext)
				.load(url)
				.get();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bmp;
	}


}
