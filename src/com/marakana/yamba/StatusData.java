package com.marakana.yamba;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class StatusData {
	private static final String TAG = StatusData.class.getSimpleName();
	
	static final int VERSION = 1;
	static final String DATABASE = "timeline.db";
	static final String TABLE = "timeline";
	static final String C_ID = "_id";
	static final String C_CREATED_AT = "created_at";
	static final String C_TEXT = "txt";
	static final String C_USER = "user";
	
	private static final String GET_ALL_ORDER_BY_STRING = C_CREATED_AT + " DESC";
	
	private static final String[] MAX_CREATED_AT_COLUMNS = { "max(" + StatusData.C_CREATED_AT + ")"};
	
	private static final String[] DB_TEXT_COLUMNS = {C_TEXT};
	
	//DbHelper implementations
	class DbHelper extends SQLiteOpenHelper {

		// Constructor
		public DbHelper(Context context) {
			super(context, DATABASE, null, VERSION);
		}

		// Called only once, first time the DB is created
		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.i(TAG, "Creating database " + DATABASE);			
			db.execSQL("create table " + TABLE + " (" + C_ID
					+ " int primary key, " + C_CREATED_AT + " int, "  +C_USER
					+ " text," + C_TEXT + " text)");
		}

		//Called whenever newVersion != oldVersion
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			//Typically do ALTER TABLE statements, but... we're just in development, so:
			db.execSQL("drop table if exists " + TABLE); // drops the old database
			onCreate(db); //run on Create to get new database
		}
	}
	
	final DbHelper dbHelper;
	
	public StatusData(Context context){
		this.dbHelper = new DbHelper(context);
		Log.i(TAG, "Initialized data");
	}
	
	public void close(){
		this.dbHelper.close();
	}
	
	public void insertOrIgnore(ContentValues values) {
		Log.d(TAG, "insertOrIgnore on "+ values);
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();
		try {
			db.insertWithOnConflict(TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
		} finally {
			db.close();
		}	
	}
	
	public void delete() {
		Log.d(TAG, "deleting all values ");
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();
		db.delete(DATABASE, null, null);
		db.close();
	}
	
	/*
	 * @return Cursor where the columns are _id, created_at, user, txt
	 */
	public Cursor getStatusUpdates() {
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		return db.query(TABLE, null, null, null, null, null, GET_ALL_ORDER_BY_STRING);
	}
	
	/*
	 * @return Timestamp of the latest status we have in the database
	 */
	public  long getLatestStatusCreatedAtTime() {
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		try {
			Cursor cursor = db.query(TABLE, MAX_CREATED_AT_COLUMNS, null, null, null, null, null);
			try {
				return cursor.moveToNext()? cursor.getLong(0): Long.MIN_VALUE;
			} finally {
				cursor.close();
			}
		} finally {
			db.close();
		}
	}
	
	/*
	 * @param id of the status we are looking for
	 * @return Text of the status
	 */
	public String getStatusTextById(long id) {
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		try {
			Cursor cursor = db.query(TABLE, DB_TEXT_COLUMNS, C_ID+"="+id, null,null, null, null);
			try{
				return cursor.moveToNext()? cursor.getString(0): null;
			} finally {
				cursor.close();
			}
		} finally {
			db.close();
		}
	}
}
