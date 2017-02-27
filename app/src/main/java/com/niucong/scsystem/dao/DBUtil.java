package com.niucong.scsystem.dao;

import android.database.sqlite.SQLiteDatabase;

import com.niucong.scsystem.app.App;

public class DBUtil {

	private static DaoSession daoSession;

	private DBUtil() {
		DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(
				App.app, "shunchang", null);
		SQLiteDatabase db = helper.getWritableDatabase();
		DaoMaster daoMaster = new DaoMaster(db);
		daoSession = daoMaster.newSession();
	}

	public static DaoSession getDaoSession() {
		if (daoSession == null)
			new DBUtil();
		return daoSession;
	}
}
