package com.mylibrary.tools;

import javax.swing.*;

/**
 * @author Radim Kratochvil
 * @author Michael Le <lemichael@mail.muni.cz>
 */
public class Validator {
    private final String newLine = System.getProperty("line.separator");

    private JPanel mainPanel;
    private StringBuilder validationErrors = new StringBuilder();
    private Boolean valid = true;

    public Validator(JPanel mainPanel) {
        this.mainPanel = mainPanel;
    }

    public boolean mandatory(JTextField input) {
        if(input.getText().trim().equals("")) {
            validationErrors.append(Localization.get("mandatory")).append(" ").append(input.getName()).append(newLine);
            valid = false;
            return false;
        }

        return true;
    }

    public boolean positiveInteger(JTextField input) {
        int value;
        try {
            value = Integer.parseInt(input.getText());
        } catch(NumberFormatException e) {
            validationErrors.append(Localization.get("invalidNumber")).append(" ").append(input.getName()).append(newLine);
            valid = false;
            return false;
        }

        if(value <= 0) {
            validationErrors.append(Localization.get("invalidNumber")).append(" ").append(input.getName()).append(newLine);
            valid = false;
            return false;
        }

        return true;
    }

    public boolean getConclusion() {
        if(!valid) {
            JOptionPane.showMessageDialog(mainPanel, validationErrors.toString(), "My Library", JOptionPane.ERROR_MESSAGE);
        }

        return valid;
    }
}
