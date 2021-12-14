/*
 * Copyright (C) 2020-21 Application Library Engineering Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.sqlite2xl.slice;

import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Button;
import ohos.agp.components.Component;
import ohos.agp.components.ListContainer;
import ohos.agp.components.TextField;
import ohos.agp.components.element.Element;
import ohos.agp.components.element.PixelMapElement;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.image.ImagePacker;
import ohos.media.image.PixelMap;
import com.ajts.ohosmads.library.SqLiteToExcel;
import com.example.sqlite2xl.ResourceTable;
import com.example.sqlite2xl.adapter.CustomAdapter;
import com.example.sqlite2xl.db.DbHelper;
import com.example.sqlite2xl.db.DbQueries;
import com.example.sqlite2xl.model.Users;
import com.example.sqlite2xl.util.Utils;
import com.hmos.compat.utils.ResourceUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sample app to test the SQLite2XL library functionality.
 */
public class SqLite2ExcelSlice extends AbilitySlice {
    public static final HiLogLabel HI_LOG_LABEL = new HiLogLabel(0, 0, "SqLite2ExcelSlice");
    private TextField edtUser;
    private TextField edtContactNo;
    private Button btnExport;
    private Button btnSaveUser;
    private Button btnExportExclude;
    private ListContainer lvUsers;
    private CustomAdapter lvUserAdapter;
    private List<Users> usersList = new ArrayList<>();
    private DbQueries dbQueries;
    private SqLiteToExcel sqliteToExcel;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        setUIContent(ResourceTable.Layout_ability_sqlite2_excel);
        initViews();
        File cacheDir = this.getDataDir();
        String directoryPath = cacheDir.getAbsolutePath() + "/Backup/";
        File file = new File(directoryPath);
        if (!file.exists()) {
            file.mkdirs();
            HiLog.debug(HI_LOG_LABEL, "File Created ");
        }

        btnSaveUser.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                if (validate(edtUser) && validate(edtContactNo)) {
                    dbQueries.open();
                    String compressFormat = SqLite2ExcelSlice.changeParamToCompressFormat(0);
                    ImagePacker imagePacker = ImagePacker.create();
                    ImagePacker.PackingOptions packingOptions = new ImagePacker.PackingOptions();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    imagePacker.initializePacking(stream, packingOptions);
                    packingOptions.format = compressFormat;
                    packingOptions.quality = 100;
                    packingOptions.numberHint = 1;
                    Element d = ResourceUtils.getDrawable(getContext(), ResourceTable.Media_ic_action_github);
                    PixelMap bitmap = ((PixelMapElement) d).getPixelMap();
                    imagePacker.addImage(bitmap);
                    imagePacker.finalizePacking();
                    imagePacker.release();
                    byte[] bitmapData = stream.toByteArray();
                    Users users = new Users(edtUser.getText().toString(),
                            edtContactNo.getText().toString(), bitmapData);
                    dbQueries.insertUser(users);
                    usersList = dbQueries.readUsers();
                    lvUserAdapter = new CustomAdapter(getApplicationContext(), usersList);
                    lvUsers.setItemProvider(lvUserAdapter);
                    dbQueries.close();
                    Utils.showSnackBar(component, "Successfully Inserted");
                }
            }
        });

        btnExport.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                // Export SQLite DB as EXCEL FILE.
                sqliteToExcel = new SqLiteToExcel(getApplicationContext(), DbHelper.DB_NAME, directoryPath);
                sqliteToExcel.exportAllTables("users.xls", new SqLiteToExcel.ExportListener() {
                    @Override
                    public void onStart() {
                        // Do nothing.
                    }

                    @Override
                    public void onCompleted(String filePath) {
                        Utils.showSnackBar(component, "Successfully Exported");
                    }

                    @Override
                    public void onError(Exception e) {
                        Utils.showSnackBar(component, e.getMessage());
                    }
                });

            }
        });

        btnExportExclude.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                // Export SQLite DB as EXCEL FILE.
                sqliteToExcel = new SqLiteToExcel(getApplicationContext(), DbHelper.DB_NAME, directoryPath);
                // Exclude Columns.
                List<String> excludeColumns = new ArrayList<>();
                excludeColumns.add("contact_id");
                // Prettify or Naming Columns.
                Map<String, String> prettyNames = new HashMap<>();
                prettyNames.put("contact_person_name", "Person Name");
                prettyNames.put("contact_no", "Mobile Number");
                sqliteToExcel.setExcludeColumns(excludeColumns);
                sqliteToExcel.setPrettyNameMapping(prettyNames);

                sqliteToExcel.setCustomFormatter(new SqLiteToExcel.ExportCustomFormatter() {
                    @Override
                    public String process(String columnName, String value) {
                        String contactPersonName = "contact_person_name";
                        if (columnName.equals(contactPersonName)) {
                            value = "Sale";
                        }
                        return value;
                    }
                });

                sqliteToExcel.exportAllTables("users1.xls", new SqLiteToExcel.ExportListener() {
                    @Override
                    public void onStart() {
                        // Do nothing.
                    }

                    @Override
                    public void onError(Exception e) {
                        Utils.showSnackBar(component, e.getMessage());
                    }

                    @Override
                    public void onCompleted(String filePath) {
                        Utils.showSnackBar(component, "Successfully Exported");
                    }
                });
            }
        });
    }

    boolean validate(TextField editText) {
        if (editText.getText().toString().length() == 0) {
            editText.requestFocus();
        }
        return editText.getText().toString().length() > 0;
    }

    void initViews() {
        DbHelper dbHelper = new DbHelper(getApplicationContext());
        dbQueries = new DbQueries(getApplicationContext());
        edtUser = (TextField) findComponentById(ResourceTable.Id_edt_user);
        edtContactNo = (TextField) findComponentById(ResourceTable.Id_edt_c_no);
        btnSaveUser = (Button) findComponentById(ResourceTable.Id_btn_save_user);
        btnExport = (Button) findComponentById(ResourceTable.Id_btn_export);
        btnExportExclude = (Button) findComponentById(ResourceTable.Id_btn_export_with_exclude);
        lvUsers = (ListContainer) findComponentById(ResourceTable.Id_lv_users);
        dbQueries.open();
        usersList = dbQueries.readUsers();
        lvUserAdapter = new CustomAdapter(getApplicationContext(), usersList);
        lvUsers.setItemProvider(lvUserAdapter);
        dbQueries.close();
    }

    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }

    private static String changeParamToCompressFormat(int format) {
        if (format == 0) {
            return "image/jpeg";
        } else if (format == 1) {
            return "image/png";
        } else {
            return "image/webp";
        }
    }
}
