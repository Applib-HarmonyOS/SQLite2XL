package com.ajts.ohosmads.library;

import ohos.app.Context;
import ohos.data.DatabaseHelper;
import ohos.data.rdb.RdbStore;
import ohos.data.rdb.StoreConfig;
import ohos.data.resultset.ResultSet;
import ohos.data.resultset.ResultSet.ColumnType;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.ClientAnchor;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SqLiteToExcel class.
 */
public class SqLiteToExcel {
    private static EventHandler handler = new EventHandler(EventRunner.getMainEventRunner());
    private RdbStore database;
    private String mExportPath;
    private HSSFWorkbook workbook;
    private List<String> mExcludeColumns = null;
    private Map<String, String> mPrettyNameMapping = null;
    private ExportCustomFormatter mCustomFormatter = null;
    private static final HiLogLabel HI_LOG_LABEL = new HiLogLabel(0, 0, "SqLiteToExcel");

    /**
     * SqLiteToExcel Constructor.
     *
     * @param context - context for SqLiteToExcel constructor
     * @param dbName  - database name
     */
    public SqLiteToExcel(Context context, String dbName) {
        this(context, dbName, context.getExternalCacheDir().toString() + File.separator);
    }

    /**
     * SqLiteToExcel Constructor.
     *
     * @param context    - context for SqLiteToExcel constructor
     * @param dbName     - database name
     * @param exportPath - export file path
     */
    public SqLiteToExcel(Context context, String dbName, String exportPath) {
        mExportPath = exportPath;
        try {
            StoreConfig config = StoreConfig.newReadOnlyConfig(dbName);
            database = new DatabaseHelper(context).getRdbStore(config, 1, null);
        } catch (Exception e) {
            HiLog.error(HI_LOG_LABEL, "Exception in SqLiteToExcel " + e);
        }
    }

    /**
     * Set the exclude columns list.
     *
     * @param excludeColumns excludeColumns
     */
    public void setExcludeColumns(List<String> excludeColumns) {
        mExcludeColumns = excludeColumns;
    }

    /**
     * Set the pretty name mapping.
     *
     * @param prettyNameMapping prettyNameMapping
     */
    public void setPrettyNameMapping(Map<String, String> prettyNameMapping) {
        mPrettyNameMapping = prettyNameMapping;
    }

    /**
     * Set a the custom formatter for the column value output.
     *
     * @param customFormatter customFormatter
     */
    public void setCustomFormatter(ExportCustomFormatter customFormatter) {
        mCustomFormatter = customFormatter;
    }

    private ArrayList<String> getAllTables() {
        ArrayList<String> tables = new ArrayList<>();
        ResultSet cursor = database.querySql("select name from sqlite_master where type='table' order by name", null);
        while (cursor.goToNextRow()) {
            tables.add(cursor.getString(0));
        }
        cursor.close();
        return tables;
    }

    private ArrayList<String> getColumns(String table) {
        ArrayList<String> columns = new ArrayList<>();
        ResultSet cursor = database.querySql("PRAGMA table_info(" + table + ")", null);
        while (cursor.goToNextRow()) {
            columns.add(cursor.getString(1));
        }
        cursor.close();
        return columns;
    }

    private void exportTables(List<String> tables, final String fileName) throws IOException {
        workbook = new HSSFWorkbook();
        for (int i = 0; i < tables.size(); i++) {
            if (!tables.get(i).equals("ohos_metadata")) {
                HSSFSheet sheet = workbook.createSheet(prettyNameMapping(tables.get(i)));
                createSheet(tables.get(i), sheet);
            }
        }
        File file = new File(mExportPath, fileName);
        FileOutputStream fos = new FileOutputStream(file);
        workbook.write(fos);
        fos.flush();
        fos.close();
        workbook.close();
        database.close();
    }

    /**
     * Method to export single table.
     *
     * @param table    - table name
     * @param fileName - file name
     * @param listener - ExportListener
     */
    public void exportSingleTable(final String table, final String fileName, ExportListener listener) {
        List<String> tables = new ArrayList<>();
        tables.add(table);
        startExportTables(tables, fileName, listener);
    }

    /**
     * Method to export specific tables.
     *
     * @param tables    - list of table names
     * @param fileName - file name
     * @param listener - ExportListener
     */
    public void exportSpecificTables(final List<String> tables, String fileName, ExportListener listener) {
        startExportTables(tables, fileName, listener);
    }

    /**
     * Method to export all tables.
     *
     * @param fileName - file name
     * @param listener - ExportListener
     */
    public void exportAllTables(final String fileName, ExportListener listener) {
        ArrayList<String> tables = getAllTables();
        startExportTables(tables, fileName, listener);
    }

    private void startExportTables(final List<String> tables, final String fileName, final ExportListener listener) {
        if (listener != null) {
            listener.onStart();
        }
        new Thread(() -> {
            try {
                exportTables(tables, fileName);
                if (listener != null) {
                    handler.postTask(() -> listener.onCompleted(mExportPath + fileName));
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

    private void createSheet(String table, HSSFSheet sheet) {
        HSSFRow rowA = sheet.createRow(0);
        ArrayList<String> columns = getColumns(table);
        int cellIndex = 0;
        for (int i = 0; i < columns.size(); i++) {
            String columnName = prettyNameMapping("" + columns.get(i));
            if (!excludeColumn(columnName)) {
                HSSFCell cellA = rowA.createCell(cellIndex);
                cellA.setCellValue(new HSSFRichTextString(columnName));
                cellIndex++;
            }
        }
        insertItemToSheet(table, sheet, columns);
    }

    private void insertItemToSheet(String table, HSSFSheet sheet, ArrayList<String> columns) {
        HSSFPatriarch patriarch = sheet.createDrawingPatriarch();
        ResultSet cursor = database.querySql("select * from " + table, null);
        cursor.goToFirstRow();
        int n = 1;
        while (!cursor.isEnded()) {
            HSSFRow rowA = sheet.createRow(n);
            int cellIndex = 0;
            for (int j = 0; j < columns.size(); j++) {
                String columnName = "" + columns.get(j);
                if (!excludeColumn(columnName)) {
                    HSSFCell cellA = rowA.createCell(cellIndex);
                    if (cursor.getColumnTypeForIndex(j) == ColumnType.TYPE_BLOB) {
                        HSSFClientAnchor anchor = new HSSFClientAnchor(0, 0, 0, 0,
                                (short) cellIndex, n, (short) (cellIndex + 1), n + 1);
                        anchor.setAnchorType(ClientAnchor.AnchorType.DONT_MOVE_AND_RESIZE);
                        patriarch.createPicture(anchor, workbook.addPicture(cursor.getBlob(j),
                                HSSFWorkbook.PICTURE_TYPE_JPEG));
                    } else {
                        String value = cursor.getString(j);
                        insertToSheet(value, columnName, cellA);
                    }
                    cellIndex++;
                }
            }
            n++;
            cursor.goToNextRow();
        }
        cursor.close();
    }

    private void insertToSheet(String value, String columnName, HSSFCell cellA) {
        if (null != mCustomFormatter) {
            value = mCustomFormatter.process(columnName, value);
        }
        cellA.setCellValue(new HSSFRichTextString(value));

    }

    /**
     * Do we exclude the specified column from the export.
     *
     * @param column column
     * @return boolean boolean
     */
    private boolean excludeColumn(String column) {
        boolean exclude = false;
        if (null != mExcludeColumns) {
            return mExcludeColumns.contains(column);
        }
        return exclude;
    }

    /**
     * Convert the specified name to a `pretty` name if a mapping exists.
     *
     * @param name name
     * @return string
     */
    private String prettyNameMapping(String name) {
        if (null != mPrettyNameMapping && mPrettyNameMapping.containsKey(name)) {
            name = mPrettyNameMapping.get(name);
        }
        return name;
    }

    /**
     * ExportListener.
     */
    public interface ExportListener {
        void onStart();

        void onCompleted(String filePath);

        void onError(Exception e);
    }

    /**
     * Interface class for the custom formatter.
     */
    public interface ExportCustomFormatter {
        String process(String columnName, String value);
    }
}