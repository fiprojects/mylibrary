package com.mylibrary.gui;

import javax.swing.*;
import java.util.regex.Pattern;

/**
 * @author Radim Kratochvil
 * @author Michael Le <lemichael@mail.muni.cz>
 */
public class UICommon {
    public static Long getSelectedItemId(JTable tbl) {
        int rowIndex = tbl.getSelectedRow();
        if(rowIndex < 0) {
            return null;
        }

        return (Long) tbl.getValueAt(rowIndex, 0);
    }

    public static String getFilterValue(JTextField filterField) {
        String filterValue = filterField.getText();

        if(filterValue.equals("%")) return "";
        return Pattern.quote(filterValue);
    }
}
