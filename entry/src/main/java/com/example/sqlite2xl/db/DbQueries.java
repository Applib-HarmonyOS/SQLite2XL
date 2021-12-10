package com.example.sqlite2xl.db;

import ohos.app.Context;
import ohos.data.rdb.RdbException;
import ohos.data.rdb.RdbStore;
import ohos.data.rdb.ValuesBucket;
import ohos.data.resultset.ResultSet;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import com.example.sqlite2xl.model.Users;
import java.util.ArrayList;
import java.util.List;

/**
 * DbQueries class.
 */
public class DbQueries {
    public static final HiLogLabel HI_LOG_LABEL = new HiLogLabel(0, 0, "SqLite2ExcelSlice");
    private Context context;
    private RdbStore database;
    private DbHelper dbHelper;

    public DbQueries(Context context) {
        this.context = context;
    }

    /**
     * DbQueries open.
     *
     * @return this
     * @throws RdbException exception
     */
    public DbQueries open() throws RdbException {
        dbHelper = new DbHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    /**
     * Insert Users.
     *
     * @param users users
     * @return inserted records
     */
    public boolean insertUser(Users users) {
        ValuesBucket values = new ValuesBucket();
        values.putString(DbConstants.CONTACT_PERSON_NAME, users.getContactPersonName());
        values.putString(DbConstants.CONTACT_NO, users.getContactNumber());
        values.putByteArray(DbConstants.CONTACT_PHOTO, users.getContactPhoto());
        return database.insert(DbConstants.USER_TABLE, values) > -1;
    }

    /**
     * Read Users.
     *
     * @return list
     */
    public List<Users> readUsers() {
        List<Users> list = new ArrayList<>();
        try {
            ResultSet cursor;
            database = dbHelper.getReadableDatabase();
            cursor = database.querySql(DbConstants.SELECT_QUERY, null);
            list.clear();
            if (cursor.getRowCount() > 0 && cursor.goToFirstRow()) {
                do {
                    String contactId = cursor.getString(cursor.getColumnIndexForName(DbConstants.CONTACT_ID));
                    String conPerson = cursor.getString(
                            cursor.getColumnIndexForName(DbConstants.CONTACT_PERSON_NAME));
                    String conNo = cursor.getString(cursor.getColumnIndexForName(DbConstants.CONTACT_NO));
                    byte[] conPhoto = cursor.getBlob(cursor.getColumnIndexForName(DbConstants.CONTACT_PHOTO));
                    Users users = new Users(contactId, conPerson, conNo, conPhoto);
                    list.add(users);
                } while (cursor.goToNextRow());
            }
            cursor.close();
        } catch (Exception exception) {
            HiLog.error(HI_LOG_LABEL, "exception in readUsers ");
        }
        return list;
    }
}