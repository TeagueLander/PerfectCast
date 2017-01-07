package com.teaguelander.audio.perfectcast.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Teague-Win10 on 1/6/2017.
 */

public class DBHelper extends SQLiteOpenHelper{

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "perfect_cast";

	//Track Status table name
	private static final String TABLE_TRACK_STATUS = "track_status";
	//Track Status column names
	private static final String KEY_CURRENT_POSITION = "current_position";
	private static final String KEY_CURRENT_TRACK = "current_track";

	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	//Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_TRACK_STATUS_TABLE = "CREATE TABLE " + TABLE_TRACK_STATUS + "(" +
				KEY_CURRENT_POSITION + " INTEGER," +
				KEY_CURRENT_TRACK + "TEXT" + ")";
		db.execSQL(CREATE_TRACK_STATUS_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRACK_STATUS);

		onCreate(db);
	}

//	public void add

}
