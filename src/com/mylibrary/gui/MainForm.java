package com.mylibrary.gui;

import com.jgoodies.looks.windows.WindowsLookAndFeel;
import com.mylibrary.Book;
import com.mylibrary.BookManager;
import com.mylibrary.BookManagerImpl;
import com.mylibrary.models.BookTableModel;
import com.mylibrary.tools.DataSourceFactory;
import com.mylibrary.tools.Localization;
import com.mylibrary.tools.Validator;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * @author Radim Kratochvil
 * @author Michael Le <lemichael@mail.muni.cz>
 */
public class MainForm {
    private JPanel mainPanel;
    private JTabbedPane navtabs;
    private JTable readerTable;
    private JTextField readerNameFilter;
    private JButton findReader;
    private JButton deleteReader;
    private JButton newReader;
    private JTextField readerName;
    private JTextField readerCard;
    private JTextField readerAddress;
    private JTextField readerPhone;
    private JTextField readerEmail;
    private JButton saveButton;
    private JTable bookTable;
    private JTextField bookAuthorFilter;
    private JTextField bookLanguage;
    private JTextField bookPagesNumber;
    private JButton findBook;
    private JTextField bookNameFilter;
    private JButton newBook;
    private JTabbedPane booksControl;
    private JTextField bookName;
    private JTextField bookAuthor;
    private JTextField bookIsbn;
    private JTextField bookPublisher;
    private JTextField bookYearOfPublication;
    private JPanel newBookTab;
    private JButton deleteBook;
    private JButton saveBook;
    private JTabbedPane readersControl;
    private JButton checkOutButton;

    private BookManager bookManager;
    private Long bookEditId = null;
    private TableRowSorter<BookTableModel> bookTableSorter;

    public MainForm() {
        try {
            bookManager = new BookManagerImpl(DataSourceFactory.getDataSource());
        } catch (IOException e) {
            e.printStackTrace();
        }

        findBook.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                findBook.setEnabled(false);

                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() {
                        String nameFilter = bookNameFilter.getText();
                        if (nameFilter.equals("%")) nameFilter = "";

                        String authorFilter = bookAuthorFilter.getText();
                        if (authorFilter.equals("%")) authorFilter = "";

                        ArrayList<RowFilter<Object, Object>> filters = new ArrayList<>();
                        filters.add(RowFilter.regexFilter(Pattern.quote(nameFilter), 1));
                        filters.add(RowFilter.regexFilter(Pattern.quote(authorFilter), 2));

                        RowFilter<BookTableModel, Object> filter = RowFilter.andFilter(filters);
                        bookTableSorter.setRowFilter(filter);

                        return null;
                    }

                    protected void done() {
                        findBook.setEnabled(true);
                    }
                }.execute();
            }
        });

        newBook.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cleanBookEdit();
                booksControl.setSelectedIndex(1);
            }
        });

        bookTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (bookTable.getSelectedRow() > -1) {
                    final Long id = selectedItemId();
                    if (id == null) return;

                    new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() {
                            fillBookEdit(id);
                            return null;
                        }

                        protected void done() {
                            booksControl.setSelectedIndex(1);
                        }
                    }.execute();

                    deleteBook.setEnabled(true);
                } else {
                    deleteBook.setEnabled(false);
                }
            }
        });

        deleteBook.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                final Long id = selectedItemId();
                if (id == null) return;

                int answer = JOptionPane.showConfirmDialog(
                        mainPanel,
                        Localization.get("confirmDelete"),
                        "My Library",
                        JOptionPane.YES_NO_OPTION);
                if (answer != 0) return;

                deleteBook.setEnabled(false);
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() {
                        Book book = bookManager.findBookById(id);
                        if (book == null) return null;

                        bookManager.deleteBook(book);
                        return null;
                    }

                    protected void done() {
                        ((BookTableModel) bookTable.getModel()).loadData();
                    }
                }.execute();
            }
        });

        saveBook.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(!validateBook()) return;
                if(bookEditId != null && bookManager.findBookById(bookEditId) == null) return;

                saveBook.setEnabled(false);
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() {
                        int yearOfPublication = Integer.parseInt(bookYearOfPublication.getText());
                        int pagesNumber = Integer.parseInt(bookPagesNumber.getText());

                        Book book;
                        if(bookEditId != null) book = bookManager.findBookById(bookEditId);
                        else book = new Book();

                        book.setName(bookName.getText());
                        book.setAuthor(bookAuthor.getText());
                        book.setIsbn(bookIsbn.getText());
                        book.setPublisher(bookPublisher.getText());
                        book.setYearOfPublication(yearOfPublication);
                        book.setLanguage(bookLanguage.getText());
                        book.setPagesNumber(pagesNumber);

                        if(bookEditId != null) bookManager.updateBook(book);
                        else bookManager.createBook(book);

                        return null;
                    }

                    protected void done() {
                        ((BookTableModel) bookTable.getModel()).loadData();
                        cleanBookEdit();
                        saveBook.setEnabled(true);
                    }
                }.execute();
            }
        });
    }

    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException, IOException {
        UIManager.setLookAndFeel(new WindowsLookAndFeel());

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                MainForm mainForm = new MainForm();
                JFrame frame = new JFrame("My Library - Katalog");
                frame.setContentPane(mainForm.mainPanel);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setVisible(true);
            }
        });
    }

    private void createUIComponents() throws IOException {
        createBookTable();
    }

    private void createBookTable() throws IOException {
        BookTableModel model = new BookTableModel();

        bookTable = new JTable();
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookTable.setModel(model);

        bookTable.getColumnModel().getColumn(0).setPreferredWidth(30);
        bookTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        bookTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        bookTable.getColumnModel().getColumn(3).setPreferredWidth(70);

        bookTableSorter = new TableRowSorter<>(model);
        bookTable.setRowSorter(bookTableSorter);
    }

    private Long selectedItemId() {
        int rowIndex = bookTable.getSelectedRow();
        if(rowIndex < 0) {
            return null;
        }

        return (Long) bookTable.getValueAt(rowIndex, 0);
    }

    private void cleanBookEdit() {
        bookEditId = null;

        bookName.setText("");
        bookAuthor.setText("");
        bookIsbn.setText("");
        bookPublisher.setText("");
        bookYearOfPublication.setText("");
        bookLanguage.setText("");
        bookPagesNumber.setText("");

        booksControl.setTitleAt(1, Localization.get("newBook"));
    }

    private void fillBookEdit(Long id) {
        Book book = bookManager.findBookById(id);
        if(book == null) return;

        bookEditId = id;
        bookName.setText(book.getName());
        bookAuthor.setText(book.getAuthor());
        bookIsbn.setText(book.getIsbn());
        bookPublisher.setText(book.getPublisher());
        bookYearOfPublication.setText(String.valueOf(book.getYearOfPublication()));
        bookLanguage.setText(book.getLanguage());
        bookPagesNumber.setText(String.valueOf(book.getPagesNumber()));

        booksControl.setTitleAt(1, Localization.get("edit"));
    }

    private boolean validateBook() {
        Validator validator = new Validator(mainPanel);

        validator.mandatory(bookName);
        validator.mandatory(bookAuthor);
        validator.mandatory(bookIsbn);
        validator.mandatory(bookPublisher);
        validator.mandatory(bookLanguage);

        validator.positiveInteger(bookYearOfPublication);
        validator.positiveInteger(bookPagesNumber);

        return validator.getConclusion();
    }
}
