package com.mylibrary.models;

import com.mylibrary.BookManager;
import com.mylibrary.LoanManager;

import javax.swing.*;

/**
 * @author Radim Kratochvil
 * @author Michael Le <lemichael@mail.muni.cz>
 */
public class AvailableBooksTableModel extends BookTableModel {
    private LoanManager loanManager;

    public AvailableBooksTableModel(BookManager bookManager, LoanManager loanManager) {
        super(bookManager);
        this.loanManager = loanManager;
    }

    @Override
    public void loadData() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                books = loanManager.findAllAvailableBooks();
                return null;
            }

            protected void done() {
                fireTableDataChanged();
            }
        }.execute();
    }
}
