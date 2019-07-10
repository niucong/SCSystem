package com.niucong.scsystem.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.niucong.scsystem.util.CnToSpell;

import de.greenrobot.dao.AbstractDaoMaster;
import de.greenrobot.dao.identityscope.IdentityScopeType;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * Master of DAO (schema version 1): knows all DAOs.
 */
public class DaoMaster extends AbstractDaoMaster {
    public static final int SCHEMA_VERSION = 3;

    /**
     * Creates underlying database table using DAOs.
     */
    public static void createAllTables(SQLiteDatabase db, boolean ifNotExists) {
        DrugInfoDao.createTable(db, ifNotExists);
        StoreListDao.createTable(db, ifNotExists);
        EnterRecordDao.createTable(db, ifNotExists);
        SellRecordDao.createTable(db, ifNotExists);
    }

    /**
     * Drops underlying database table using DAOs.
     */
    public static void dropAllTables(SQLiteDatabase db, boolean ifExists) {
        DrugInfoDao.dropTable(db, ifExists);
        StoreListDao.dropTable(db, ifExists);
        EnterRecordDao.dropTable(db, ifExists);
        SellRecordDao.dropTable(db, ifExists);
    }

    public static abstract class OpenHelper extends SQLiteOpenHelper {

        public OpenHelper(Context context, String name, CursorFactory factory) {
            super(context, name, factory, SCHEMA_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.i("greenDAO", "Creating tables for schema version " + SCHEMA_VERSION);
            createAllTables(db, false);
        }
    }

    /**
     * WARNING: Drops all table on Upgrade! Use only during development.
     */
    public static class DevOpenHelper extends OpenHelper {
        public DevOpenHelper(Context context, String name, CursorFactory factory) {
            super(context, name, factory);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i("greenDAO", "Upgrading schema from version " + oldVersion + " to " + newVersion + " by dropping all tables");
            if (oldVersion == 2 && newVersion == 3) {
                twoToThree(db);
            } else if (oldVersion == 1 && newVersion == 3) {
                oneToTwo(db);
                twoToThree(db);
            } else {
                dropAllTables(db, true);
                onCreate(db);
            }
        }

        private void twoToThree(SQLiteDatabase db) {
            db.execSQL("alter table " + DrugInfoDao.TABLENAME + " add UPDATE_TIME LONG");
            db.execSQL("alter table " + StoreListDao.TABLENAME + " add UPDATE_TIME LONG");

            Cursor cursor = db.rawQuery("select * from  " + DrugInfoDao.TABLENAME, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    db.execSQL("update " + DrugInfoDao.TABLENAME
                            + " set UPDATE_TIME = ? where BAR_CODE = ?", new Object[]{
                            System.currentTimeMillis(), cursor.getLong(0)});
                    db.execSQL("update " + StoreListDao.TABLENAME
                            + " set UPDATE_TIME = ? where BAR_CODE = ?", new Object[]{
                            System.currentTimeMillis(), cursor.getLong(0)});
                } while (cursor.moveToNext());
            }
        }

        private void oneToTwo(SQLiteDatabase db) {
            db.execSQL("alter table " + DrugInfoDao.TABLENAME + " add NAME_PY TEXT");
            db.execSQL("alter table " + DrugInfoDao.TABLENAME + " add NAME_PYF TEXT");

            Cursor cursor = db.rawQuery("select * from  " + DrugInfoDao.TABLENAME, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    db.execSQL("update " + DrugInfoDao.TABLENAME
                            + " set NAME_PY = ? where BAR_CODE = ?", new Object[]{
                            CnToSpell.getPinYin(cursor.getString(1)).toLowerCase(), cursor.getLong(0)});
                    db.execSQL("update " + DrugInfoDao.TABLENAME
                            + " set NAME_PYF = ? where BAR_CODE = ?", new Object[]{
                            CnToSpell.getPinYinHeadChar(cursor.getString(1)).toLowerCase(), cursor.getLong(0)});
                } while (cursor.moveToNext());
            }
        }
    }

    public DaoMaster(SQLiteDatabase db) {
        super(db, SCHEMA_VERSION);
        registerDaoClass(DrugInfoDao.class);
        registerDaoClass(StoreListDao.class);
        registerDaoClass(EnterRecordDao.class);
        registerDaoClass(SellRecordDao.class);
    }

    public DaoSession newSession() {
        return new DaoSession(db, IdentityScopeType.Session, daoConfigMap);
    }

    public DaoSession newSession(IdentityScopeType type) {
        return new DaoSession(db, type, daoConfigMap);
    }

}
