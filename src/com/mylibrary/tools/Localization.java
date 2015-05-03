package com.mylibrary.tools;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Radim Kratochvil
 * @author Michael Le <lemichael@mail.muni.cz>
 */
public class Localization {
    private static ResourceBundle resourceBundle;

    private static void loadResourceBundle() {
        if(resourceBundle == null) {
            resourceBundle = ResourceBundle.getBundle("i18n", Locale.getDefault());
        }
    }

    public static String get(String key) {
        loadResourceBundle();
        return resourceBundle.getString(key);
    }
}
