package com.teaguelander.audio.perfectcast.services;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.teaguelander.audio.perfectcast.objects.PodcastDetail;
import com.teaguelander.audio.perfectcast.objects.PodcastEpisode;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Created by Teague-Win10 on 1/22/2017.
 */

public class DatabaseService extends SQLiteOpenHelper {

	private static DatabaseService mInstance;
	private static Context mContext;

	private static final int DATABASE_VERSION = 9;
	private static final String DATABASE_NAME = "PerfectCast";
	private static final String TABLE_UP_NEXT = "up_next";
	private static final String TABLE_PODCASTS = "podcasts";

	public DatabaseService(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mContext = context;
	}

	public static synchronized DatabaseService getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new DatabaseService (context);
		}
		return mInstance;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		//SQL statements to create tables
		String CREATE_TABLE_UP_NEXT = "CREATE TABLE " + TABLE_UP_NEXT + " ( " +
//										"id INTEGER PRIMARY KEY AUTOINCREMENT, " +
											  PodcastEpisode.KEY_TITLE + " TEXT," +
											  PodcastEpisode.KEY_DESCRIPTION + " TEXT," +
											  PodcastEpisode.KEY_URL + " TEXT UNIQUE)";
//											  "UNIQUE (" + TextUtils.join(",", PodcastEpisode.COLUMNS)  +") );";
		String CREATE_TABLE_PODCASTS = "CREATE TABLE " + TABLE_PODCASTS + " ( " +
											   PodcastDetail.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
											   PodcastDetail.KEY_URL + " TEXT UNIQUE, " +
											   PodcastDetail.KEY_TITLE + " TEXT," +
											   PodcastDetail.KEY_IMAGE_URL + " TEXT," +
											   PodcastDetail.KEY_DESCRIPTION + " TEXT," +
											   PodcastDetail.KEY_SUBSCRIBED + " TEXT," +
											   PodcastDetail.KEY_XML + " TEXT);";
//											   "UNIQUE (" + TextUtils.join(",", PodcastDetail.COLUMNS)  +") );";

		db.execSQL(CREATE_TABLE_UP_NEXT);
		db.execSQL(CREATE_TABLE_PODCASTS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_UP_NEXT);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_PODCASTS);
		this.onCreate(db);
	}


	//UP_NEXT
	public void addUpNext(PodcastEpisode episode) {
		Log.d("dbs", "Adding episode " + episode.mTitle);

		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(PodcastEpisode.KEY_TITLE, episode.mTitle);
		values.put(PodcastEpisode.KEY_DESCRIPTION, episode.mDescription);
		values.put(PodcastEpisode.KEY_URL, encode(episode.mUrl) );

		db.insert(TABLE_UP_NEXT, null, values);

		db.close();
	}

	//UP_NEXT
	public PodcastEpisode getNextEpisode() {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_UP_NEXT,
								 PodcastEpisode.COLUMNS,
								 null,
								 null, null, null, null, null
				);

		if (cursor != null) {
			cursor.moveToFirst();
		}

		PodcastEpisode episode = null;
		try {
			episode = new PodcastEpisode(cursor.getString(0),
										 cursor.getString(1),
										 cursor.getString(2),
										 null,
										 null,
										 null,
										 null);
			Log.d("dbs", "Got next episode from database" + episode.toString());

		} catch (CursorIndexOutOfBoundsException e) {
			Log.d("dbs", "No next episode to find");
//			e.printStackTrace();
			Log.e("dbs", "Skipping stack trace");
		}

		return episode;
	}

	//ADD PODCAST
	public long addPodcast(PodcastDetail podcast) {
		Log.d("dbs", "Adding podcast " + podcast.mTitle);

		SQLiteDatabase db = this.getWritableDatabase();
		long id = -1;

		Cursor cursor = db.query(TABLE_PODCASTS,
								 new String[] {PodcastDetail.KEY_ID},
								 PodcastDetail.KEY_URL + " = ?",
								 new String[] { podcast.mUrl },
								 null, null, null);
		int rows = 0;

		if (cursor != null) { //Podcast already exists
			Log.d("dbs", "Cursor not null!");
			//TODO We should do an update statement
			cursor.moveToFirst();
			if (cursor.getCount() > 0) {
				db.close();
				return cursor.getLong(0);
			}
		}
		//Podcast doesnt exist in db, insert statement
		ContentValues values = new ContentValues();
		values.put(PodcastDetail.KEY_URL, podcast.mUrl);
		values.put(PodcastDetail.KEY_TITLE, podcast.mTitle);
		values.put(PodcastDetail.KEY_IMAGE_URL, podcast.mImageUrl);
		values.put(PodcastDetail.KEY_DESCRIPTION, podcast.mDescription);
		values.put(PodcastDetail.KEY_SUBSCRIBED, podcast.mSubscribed);
		values.put(PodcastDetail.KEY_XML, podcast.mXml );

		id = db.insert(TABLE_PODCASTS, null, values);

		db.close();

		return id;
	}

	//GET PODCAST BY URL
	public PodcastDetail getPodcast(long id) { //TODO add a parameter to get XML or not
		Log.d("dbs", "Requesting podcast by url: " + id);

		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.rawQuery("SELECT ? FROM ? WHERE ? = ?;",
									new String[] {
//											PodcastDetail.KEY_ID,
											TextUtils.join(",", PodcastDetail.COLUMNS),
											TABLE_PODCASTS,
											PodcastDetail.KEY_ID,
											String.valueOf(id)
									}
		);

		if (cursor != null) {
			cursor.moveToFirst();
		}

		PodcastDetail podcast = null;
		try {
			podcast = new PodcastDetail(cursor.getLong(0),
										cursor.getString(1),
										cursor.getString(2),
										cursor.getString(3),
										cursor.getString(4),
										Boolean.parseBoolean(cursor.getString(5)),
										cursor.getString(6));
			Log.d("dbs", "Got podcast from database");
		} catch (CursorIndexOutOfBoundsException e) {
			Log.d("dbs", "No podcast or error parsing");
//			e.printStackTrace();
			Log.e("dbs", "Skipping podcast error stack trace");
		}

		return podcast;
	}

	private String encode(String string) {
		String encodedString = "";
		try {
			encodedString = URLEncoder.encode(string, StorageService.CHARSET);
		}catch (Exception e) {
			e.printStackTrace();
		}
		return encodedString;
	}

	private String decode(String string) {
		String decodedString = "";
		try {
			decodedString = URLDecoder.decode(string, StorageService.CHARSET);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return decodedString;
	}


}
