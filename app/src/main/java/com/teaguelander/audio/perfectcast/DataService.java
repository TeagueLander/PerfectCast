package com.teaguelander.audio.perfectcast;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * Created by Teague-Win10 on 7/28/2016.
 */
public class DataService {

	public static String ITUNES_URL = "https://itunes.apple.com/search";

	private static DataService instance;
	private RequestQueue requestQueue;
	private ImageLoader imageLoader;
	private static Context ctx;

	private DataService(Context context) {
		ctx = context;
		requestQueue = getRequestQueue();

		imageLoader = new ImageLoader(requestQueue, new ImageLoader.ImageCache() {
			private final LruCache<String, Bitmap> cache = new LruCache<String, Bitmap>(20);
			@Override
			public Bitmap getBitmap(String url) {
				return cache.get(url);
			}
			@Override
			public void putBitmap(String url, Bitmap bitmap) {
				cache.put(url, bitmap);
			}
		});
	}

	public static synchronized DataService getInstance(Context context) {
		if (instance == null) {
			instance = new DataService(context);
		}
		return instance;
	}

	public RequestQueue getRequestQueue() {
		if (requestQueue == null) {
			requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
		}
		return requestQueue;
	}

	public <T> void addToRequestQueue(Request<T> req) {
		getRequestQueue().add(req);
	}

	public ImageLoader getImageLoader() {
		return imageLoader;
	}


	//OLD METHODS
	public static void searchPodcasts(String searchTerm, Response.Listener<String> listener) {


		String strParams = "";
		try {
			JSONObject params = new JSONObject();
			params.put("term", searchTerm);
			params.put("country", "CA");
			params.put("media", "podcast");

			strParams = paramSerializer(params);
		}
		catch (Exception e) {
			Log.d("ds", e.toString());
			Log.d("ds", "Failed to parse JSON");
		}

		Log.d("ds", ITUNES_URL + strParams);
		StringRequest stringRequest = new StringRequest(Request.Method.GET, ITUNES_URL + strParams, listener, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.e("ds", "Some went wrong with Volley Request");
				Log.e("ds", error.toString());
			}
		});
		Volley.newRequestQueue(ctx).add(stringRequest);
	}

	private static String paramSerializer(JSONObject params) throws JSONException {

		if (params == null) { return ""; }
		StringBuilder sb = new StringBuilder("?");

		try {
			Iterator<String> keys = params.keys();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				sb.append(key);
				sb.append("=");
				sb.append(params.getString(key));
				sb.append("&");
			}
			sb.deleteCharAt(sb.length() - 1);
		}
		catch (Exception e) {
			throw e;
		}

		return sb.toString().replaceAll(" ", "+");
	}

}
