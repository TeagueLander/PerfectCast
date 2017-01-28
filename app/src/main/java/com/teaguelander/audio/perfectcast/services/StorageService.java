package com.teaguelander.audio.perfectcast.services;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.Response;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by Teague-Win10 on 1/22/2017.
 */

public class StorageService {

	private static StorageService instance;
	private static Context mContext;

	public static final int PNG_QUALITY = 90;
	public static final String IMAGE_DIRECTORY = "images";
	public static final String CHARSET = "utf-8";

	private StorageService(Context context) {
		mContext = context;
	}

	public static synchronized StorageService getInstance(Context context) {
		if (instance == null) {
			instance = new StorageService (context);
		}
		return instance;
	}

	public static void saveImageToStorageAndView(final Context context, final String url, final ImageView imageView) throws UnsupportedEncodingException {

		final DataService ds = DataService.getInstance(context);
		final File directory = context.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE);
		final String filename = URLEncoder.encode(url, CHARSET);

//		Log.d("ss", "Getting " + directory + "/" + filename);
		final File mypath = new File(directory, filename);

		if (mypath.exists()) {
//			Log.d("ss", "File exists! Getting" + directory + "/" + filename);
			loadImageFileIntoView(context, filename, imageView);
		}else {
//			Log.d("ss", "File does not exist. Getting" + url);

			ds.getImage(url, new Response.Listener<Bitmap>() {
				@Override
				public void onResponse(Bitmap response) {

					if (!directory.exists()) {
						directory.mkdir();
					}

					FileOutputStream fos = null;

					try {

						//+ ".jpg"
						fos = new FileOutputStream(mypath);
						response.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, fos);
						fos.close();

						//					Log.d("ss", "Getting " + directory + filename);
						imageView.setImageBitmap(BitmapFactory.decodeFile(directory + "/" + filename));

					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});

		}
	}

	private static void loadImageFileIntoView(Context context, String filename, ImageView imageView) {
		try {
			File directory = context.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE);
			Bitmap bmp = BitmapFactory.decodeFile(directory + "/" + filename);
			imageView.setImageBitmap(bmp);

		}catch (Exception e) {
//			Log.e("ss", "Crash the program!");
			e.printStackTrace();
		}
	}

}
