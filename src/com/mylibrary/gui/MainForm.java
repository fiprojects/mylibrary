package com.mylibrary.gui;

import com.jgoodies.looks.windows.WindowsLookAndFeel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * Created by Michael on 27. 4. 2015.
 */
public class MainForm {
    private JPanel mainPanel;
    private JTabbedPane navtabs;
    private JTable userTable;
    private JTextField textField1;
    private JButton button1;
    private JButton smazatButton;
    private JButton novýČtenářButton;
    private JTextField textField2;
    private JTextField textField3;
    private JTextField textField4;
    private JTextField textField5;
    private JTextField textField6;
    private JButton button2;
    private JTable bookTable;
    private JTextField textField7;
    private JTextField textField8;
    private JTextField textField9;

    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel(new WindowsLookAndFeel());

        MainForm mainForm = new MainForm();

        JFrame frame = new JFrame("My Library - Katalog");
        frame.setContentPane(mainForm.mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);


        DefaultTableModel model = new DefaultTableModel();
        mainForm.userTable.setModel(model);
        model.addColumn("Jméno");
        model.addRow(new String[]{"Novák Jan"});
        model.addRow(new String[]{"Novotný Petr"});


        DefaultTableModel model2 = new DefaultTableModel();
        mainForm.bookTable.setModel(model2);
        model2.addColumn("Název");
        model2.addColumn("Autor");
        model2.addColumn("ISBN");
        model2.addRow(new String[]{"RUR", "Čapek Karel", "1234566789"});
        model2.addRow(new String[]{"Bílá nemoc", "Čapek Karel", "1234566789"});
    }
}
