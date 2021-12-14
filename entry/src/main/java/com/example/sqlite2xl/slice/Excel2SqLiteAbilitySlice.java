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
import ohos.agp.components.TextField;
import com.ajts.ohosmads.library.ExcelToSqLite;
import com.example.sqlite2xl.ResourceTable;
import com.example.sqlite2xl.db.DbHelper;
import com.example.sqlite2xl.db.DbQueries;
import com.example.sqlite2xl.util.Utils;
import java.io.File;

/**
 * Sample app to test the SQLite2XL library functionality.
 */
public class Excel2SqLiteAbilitySlice extends AbilitySlice {
    private DbQueries dbQueries;
    private String directoryPath;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_excel2_sqlite);
        dbQueries = new DbQueries(getApplicationContext());
        File cacheDir = this.getDataDir();
        directoryPath = cacheDir.getAbsolutePath() + "/Backup/users.xls";
        TextField edtFilePath = (TextField) findComponentById(ResourceTable.Id_edt_file_path);
        Button btnImport = (Button) findComponentById(ResourceTable.Id_btn_import);
        edtFilePath.setText(directoryPath);

        btnImport.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                File file = new File(directoryPath);
                if (!file.exists()) {
                    Utils.showSnackBar(component, "No file");
                    return;
                }
                dbQueries.open();
                // Is used to import data from excel without dropping table
                // if you want to add column in excel and import into DB, you must drop the table
                ExcelToSqLite excelToSqlite = new ExcelToSqLite(getApplicationContext(), DbHelper.DB_NAME, false);
                // Import EXCEL FILE to SQLite
                excelToSqlite.importFromFile(directoryPath, new ExcelToSqLite.ImportListener() {
                    @Override
                    public void onStart() {
                        // Do nothing.
                    }

                    @Override
                    public void onCompleted(String dbName) {
                        Utils.showSnackBar(component, "Excel imported into " + dbName);
                    }

                    @Override
                    public void onError(Exception e) {
                        Utils.showSnackBar(component, "Error : " + e.getMessage());
                    }
                });
                dbQueries.close();
            }
        });
    }

    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }
}
