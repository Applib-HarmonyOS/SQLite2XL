package com.example.sqlite2xl.util;

import ohos.agp.components.Component;
import ohos.agp.utils.TextAlignment;
import ohos.agp.window.dialog.ToastDialog;

/**
 * Created by Mushtaq on 14-04-2017.
 */
public class Utils {
    private Utils() {
    }

    /**
     * showSnackBar.
     *
     * @param view view
     * @param message message
     */
    public static void showSnackBar(Component view, String message) {
        ToastDialog toastDialog = new ToastDialog(view.getContext());
        toastDialog.setText(message);
        toastDialog.setAlignment(TextAlignment.BOTTOM);
        toastDialog.show();
    }
}