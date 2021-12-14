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
import com.example.sqlite2xl.ResourceTable;

/**
 * Sample app to test the SQLite2XL library functionality.
 */
public class SqlAbilitySlice extends AbilitySlice {
    private Button btnSql2Xl;
    private Button btnXl2Sql;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        setUIContent(ResourceTable.Layout_sql_ablity);
        btnXl2Sql = (Button) findComponentById(ResourceTable.Id_btnXL2SQL);
        btnSql2Xl = (Button) findComponentById(ResourceTable.Id_btnSQL2XL);

        btnXl2Sql.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                present(new Excel2SqLiteAbilitySlice(), new Intent());
            }
        });

        btnSql2Xl.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                present(new SqLite2ExcelSlice(), new Intent());
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
