package com.example.sqlite2xl.db;

import ohos.app.Context;
import ohos.data.DatabaseHelper;
import ohos.data.rdb.RdbOpenCallback;
import ohos.data.rdb.RdbStore;
import ohos.data.rdb.StoreConfig;

/**
 * DbHelper class.
 */
public class DbHelper extends RdbOpenCallback {
    public static final String DB_NAME = "sqlite2ExcelDemo";
    private static final int DB_VERSION = 1;
    private DatabaseHelper hmosDatabaseHelper;
    private RdbStore hmosRdbStore;

    /**
     * DbHelper constructor.
     *
     * @param context context
     */
    public DbHelper(Context context) {
        StoreConfig config = StoreConfig.newDefaultConfig(DB_NAME);
        hmosDatabaseHelper = new DatabaseHelper(context);
        hmosRdbStore = hmosDatabaseHelper.getRdbStore(config, DB_VERSION, this, null);
    }

    @Override
    public void onCreate(RdbStore db) {
        db.executeSql(DbConstants.CREATE_USER_TABLE);
    }

    @Override
    public void onUpgrade(RdbStore db, int oldVersion, int newVersion) {
        db.executeSql("DROP TABLE IF EXISTS " + DbConstants.USER_TABLE);
    }

    public RdbStore getReadableDatabase() {
        return hmosRdbStore;
    }

    public RdbStore getWritableDatabase() {
        return hmosRdbStore;
    }

    public void close() {
        hmosRdbStore.close();
    }
}
