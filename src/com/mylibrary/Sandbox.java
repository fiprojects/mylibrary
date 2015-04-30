package com.mylibrary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.ResourceBundle;

public class Sandbox {
    private final static Logger log = LoggerFactory.getLogger(Sandbox.class);

    public static void main(String[] args) {
        logging();
        i18n();
    }

    public static void logging() {
        log.debug("I ty debuzire!");
        log.info("Informuji te!");
        log.warn("Dokonce te varuji!");
        log.error("Mas tam chybu!");
    }

    public static void i18n() {
        ResourceBundle lang = ResourceBundle.getBundle("i18n", new Locale("cs_CZ"));
        System.out.println(lang.getString("surname"));
    }
}
