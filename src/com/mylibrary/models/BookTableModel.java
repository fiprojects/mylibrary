package com.mylibrary.models;

import com.mylibrary.Book;
import com.mylibrary.BookManager;
import com.mylibrary.tools.Localization;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Radim Kratochvil
 * @author Michael Le <lemichael@mail.muni.cz>
 */
public class BookTableModel extends AbstractTableModel {
    private BookManager manager;
    protected List<Book> books = new ArrayList<>();

    public BookTableModel(BookManager bookManager) {
        this.manager = bookManager;
        loadData();
    }

    @Override
    public int getRowCount() {
        return books.size();
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Book book = books.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return book.getId();
            case 1:
                return book.getName();
            case 2:
                return book.getAuthor();
            case 3:
                return book.getIsbn();
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Localization.get("id");
            case 1:
                return Localization.get("name");
            case 2:
                return Localization.get("author");
            case 3:
                return Localization.get("isbn");
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public void loadData() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
				books = manager.findAllBooks();
                return null;
            }

            protected void done() {
                fireTableDataChanged();
            }
        }.execute();
    }
}
