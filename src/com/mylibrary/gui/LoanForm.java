package com.mylibrary.gui;

import com.jgoodies.looks.windows.WindowsLookAndFeel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * Created by Michael on 28. 4. 2015.
 */
public class LoanForm {
    private JPanel defaultPanel;
    private JTable table1;
    private JTable table2;
    private JTextField textField7;

    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel(new WindowsLookAndFeel());

        LoanForm loanForm = new LoanForm();

        JFrame frame = new JFrame("My Library - Výpůjčky [Čtenář: Novák Jan]");
        frame.setContentPane(loanForm.defaultPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);


        DefaultTableModel model2 = new DefaultTableModel();
        loanForm.table1.setModel(model2);
        model2.addColumn("Název");
        model2.addColumn("Autor");
        model2.addColumn("ISBN");
        model2.addColumn("Datum výpůjčky");
        model2.addColumn("Výpůjční lhůta");
        model2.addRow(new String[]{"RUR", "Čapek Karel", "1234566789", "10. 1. 2000", "10. 1. 2000"});
        model2.addRow(new String[]{"Bílá nemoc", "Čapek Karel", "1234566789", "10. 1. 2000", "10. 1. 2000"});

        DefaultTableModel model3 = new DefaultTableModel();
        loanForm.table2.setModel(model3);
        model3.addColumn("Název");
        model3.addColumn("Autor");
        model3.addColumn("ISBN");
        model3.addColumn("Nakladatelství");
        model3.addRow(new String[]{"RUR", "Čapek Karel", "1234566789", "Albatros"});
        model3.addRow(new String[]{"Bílá nemoc", "Čapek Karel", "1234566789", "Albatros"});
    }
}
