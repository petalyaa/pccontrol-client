package org.pet.pccontrol;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ContextHolder {
	
	private static final String TAG = "ContextHolder";

	private String hostnameColumnName = "hostname";
	private String hostname;
	
	private String portColumnName = "port";
	private int port;
	
	private String timeoutColumnName = "timeout";
	private int timeout;
	
	private String sensitivityColumnName = "sensitivity";
	private int sensitivity;
	
	private String tappingEnabledColumnName = "tappingEnabled";
	private boolean isTappingEnabled;
	
	private DBHelper dbHelper;
	
	private static ContextHolder instance;
	
	private ContextHolder(Context context){
		dbHelper = new DBHelper(context);
		init();
	}
	
	public static final ContextHolder getInstance(Context context){
		if(instance == null)
			instance = new ContextHolder(context);
		return instance;
	}
	
	private void init(){
		hostname = getDbValue(hostnameColumnName);
		String portStr = getDbValue(portColumnName);
		String sensitivityStr = getDbValue(sensitivityColumnName);
		String tappingEnabledStr = getDbValue(tappingEnabledColumnName);
		String timeoutStr = getDbValue(timeoutColumnName);
		port = 0;
		try {
			port = portStr != null ? Integer.parseInt(portStr) : 0;
		} catch (NumberFormatException e) {
			Log.e(TAG, "Invalid port number.", e);
		}
		sensitivity = 0;
		try {
			sensitivity = sensitivityStr != null ? Integer.parseInt(sensitivityStr) : 0;
		} catch (NumberFormatException e) {
			Log.e(TAG, "Invalid sensitivity number.", e);
		}
		timeout = 0;
		try {
			timeout = timeoutStr != null ? Integer.parseInt(timeoutStr) : 0;
		} catch (NumberFormatException e) {
			Log.e(TAG, "Invalid timeout number.", e);
		}
		isTappingEnabled = tappingEnabledStr != null ? Boolean.valueOf(tappingEnabledStr) : false;
	}
	
	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
		setDbValue(hostnameColumnName, hostname);
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
		setDbValue(portColumnName, String.valueOf(port));
	}

	public int getSensitivity() {
		return sensitivity;
	}

	public void setSensitivity(int sensitivity) {
		this.sensitivity = sensitivity;
		setDbValue(sensitivityColumnName, String.valueOf(sensitivity));
	}

	public boolean isTappingEnabled() {
		return isTappingEnabled;
	}

	public void setTappingEnabled(boolean isTappingEnabled) {
		this.isTappingEnabled = isTappingEnabled;
		setDbValue(tappingEnabledColumnName, String.valueOf(isTappingEnabled));
	}
	
	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
		setDbValue(timeoutColumnName, String.valueOf(timeout));
	}
	
	private String getDbValue(String key){
		String s = null;
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(DBHelper.TABLE_NAME, new String[]{"key","value"}, "key=" + "'" + key + "'", null, null, null, "key");
		try{
			if(cursor.moveToNext())
				s = cursor.getString(1);
		} catch (Exception e){
			Log.e(TAG, "Fail to get db value.", e);
		} finally {
			cursor.close();
			db.close();
		}
		return s;
	}
	
	private void setDbValue(String key, String value){
		String s = getDbValue(key);
		if(s == null){ // Insert it
			insertDBValue(key, value);
		} else { // Update it
			updateDbValue(key, value);
		}
	}
	
	private void insertDBValue(String key, String value){
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues contentValue = new ContentValues();
		contentValue.put("key", key);
		contentValue.put("value", value);
		db.insert(DBHelper.TABLE_NAME, null, contentValue);
		db.close();
	}
	
	private void updateDbValue(String key, String value){
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues contentValue = new ContentValues();
		contentValue.put("key", key);
		contentValue.put("value", value);
		db.update(DBHelper.TABLE_NAME, contentValue, "key='"+key+"'", null);
		db.close();
	}

}
