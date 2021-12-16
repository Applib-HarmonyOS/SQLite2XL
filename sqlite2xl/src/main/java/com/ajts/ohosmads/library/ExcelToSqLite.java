package com.ajts.ohosmads.library;

import ohos.app.Context;
import ohos.data.DatabaseHelper;
import ohos.data.rdb.RdbStore;
import ohos.data.rdb.StoreConfig;
import ohos.data.rdb.ValuesBucket;
import ohos.data.resultset.ResultSet;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * ExcelToSqLite class.
 */
public class ExcelToSqLite {
    private static EventHandler handler = new EventHandler(EventRunner.getMainEventRunner());
    private Context mContext;
    private RdbStore database;
    private String mDbName;
    private boolean dropTable = false;
    private static final HiLogLabel HI_LOG_LABEL = new HiLogLabel(0, 0, "ExcelToSqLite");

    /**
     * ExcelToSqLite Constructor.
     *
     * @param context - context for ExcelToSqLite constructor
     * @param dbName  - database name
     */
    public ExcelToSqLite(Context context, String dbName) {
        mContext = context;
        mDbName = dbName;
        try {
            StoreConfig config = StoreConfig.newReadOnlyConfig(dbName);
            database = new DatabaseHelper(context).getRdbStore(config, 1, null);
        } catch (Exception e) {
            HiLog.error(HI_LOG_LABEL, "Exception in ExcelToSqLite" + e);
        }
    }

    /**
     * ExcelToSqLite Constructor.
     *
     * @param context   - context for ExcelToSqLite constructor
     * @param dbName    - database name
     * @param dropTable - is drop table
     */
    public ExcelToSqLite(Context context, String dbName, boolean dropTable) {
        mContext = context;
        mDbName = dbName;
        this.dropTable = dropTable;
        try {
            StoreConfig config = StoreConfig.newReadOnlyConfig(dbName);
            database = new DatabaseHelper(context).getRdbStore(config, 1, null);
        } catch (Exception e) {
            HiLog.error(HI_LOG_LABEL, "Exception caught in ExcelToSqLite" + e);
        }
    }

    /**
     * Method to import from raw file entry.
     *
     * @param fileName - file name
     * @param listener - ImportListener
     */
    public void importFromRawFileEntry(final String fileName, final ImportListener listener) {
        if (listener != null) {
            listener.onStart();
        }
        new Thread(() -> {
            try {
                working(mContext.getResourceManager().getRawFileEntry(fileName).openRawFile());
                if (listener != null) {
                    handler.postTask(() -> listener.onCompleted(mDbName));
                }
            } catch (final Exception e) {
                if (database != null && database.isOpen()) {
                    database.close();
                }
                if (listener != null) {
                    handler.postTask(() -> listener.onError(e));
                }
            }
        }).start();
    }


    /**
     * Method to import from file.
     *
     * @param filePath - file path
     * @param listener - ImportListener
     */
    public void importFromFile(String filePath, ImportListener listener) {
        importFromFile(new File(filePath), listener);
    }

    /**
     * Method to import from file.
     *
     * @param file     - file object
     * @param listener - ImportListener
     */
    private void importFromFile(final File file, final ImportListener listener) {
        if (listener != null) {
            listener.onStart();
        }
        new Thread(() -> {
            try {
                working(new FileInputStream(file));
                if (listener != null) {
                    handler.postTask(() -> listener.onCompleted(mDbName));
                }
            } catch (final Exception e) {
                if (database != null && database.isOpen()) {
                    database.close();
                }
                if (listener != null) {
                    handler.postTask(() -> listener.onError(e));
                }
            }
        }).start();
    }

    private void working(InputStream stream) throws IOException {
        HSSFWorkbook workbook = new HSSFWorkbook(stream);
        int sheetNumber = workbook.getNumberOfSheets();
        for (int i = 0; i < sheetNumber; i++) {
            createTable(workbook.getSheetAt(i));
        }
        database.close();
    }

    private void createTable(Sheet sheet) {
        ResultSet cursor = null;
        try {
            StringBuilder createTableSql = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
            createTableSql.append(sheet.getSheetName());
            createTableSql.append("(");
            Iterator<Row> rit = sheet.rowIterator();
            Row rowHeader = rit.next();
            List<String> columns = new ArrayList<>();
            for (int i = 0; i < rowHeader.getPhysicalNumberOfCells(); i++) {
                createTableSql.append(rowHeader.getCell(i).getStringCellValue());
                if (i == rowHeader.getPhysicalNumberOfCells() - 1) {
                    createTableSql.append(" TEXT");
                } else {
                    createTableSql.append(" TEXT,");
                }
                columns.add(rowHeader.getCell(i).getStringCellValue());
            }
            createTableSql.append(")");
            if (dropTable) {
                database.executeSql("DROP TABLE IF EXISTS " + sheet.getSheetName());
            }
            database.executeSql(createTableSql.toString());
            for (String column : columns) {
                cursor = database.querySql("SELECT * FROM " + sheet.getSheetName(), null);
                // grab cursor for all data
                int deleteStateColumnIndex = cursor.getColumnIndexForName(column);  // see if the column is there
                if (deleteStateColumnIndex < 0) {
                    String type = "TEXT";
                    // missing_column not there - add it
                    database.executeSql("ALTER TABLE " + sheet.getSheetName()
                            + " ADD COLUMN " + column + " " + type + " NULL;");
                }
            }
            while (rit.hasNext()) {
                ritInsertNextRow(rit, columns, sheet);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void ritInsertNextRow(Iterator<Row> rit, List<String> columns, Sheet sheet) {
        Row row = rit.next();
        ValuesBucket values = new ValuesBucket();
        for (int n = 0; n < row.getPhysicalNumberOfCells(); n++) {
            if (row.getCell(n).getCellTypeEnum() == CellType.NUMERIC) {
                values.putDouble(columns.get(n), row.getCell(n).getNumericCellValue());
            } else {
                if (n != 0) {
                    String stringCellValue = row.getCell(n).getStringCellValue();
                    values.putString(columns.get(n), stringCellValue);
                }
            }
        }
        long result = database.insertWithConflictResolution(sheet.getSheetName().trim(), values,
                RdbStore.ConflictResolution.ON_CONFLICT_IGNORE);
        if (result < 0) {
            HiLog.error(HI_LOG_LABEL, "Insert value Failed");
        }
    }

    /**
     * Import Listener interface.
     */
    public interface ImportListener {
        void onStart();

        void onCompleted(String dbName);

        void onError(Exception e);
    }
}