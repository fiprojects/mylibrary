package com.mylibrary.gui;

import com.mylibrary.*;

import com.mylibrary.models.BookTableModel;
import com.mylibrary.models.CustomerTableModel;

import com.mylibrary.tools.DataSourceFactory;
import com.mylibrary.tools.Localization;
import com.mylibrary.tools.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Radim Kratochvil
 * @author Michael Le <lemichael@mail.muni.cz>
 */
// TODO: Ověřit ošetření výjimek vyhazovaných databází
// TODO: Proklikat, zda vše funguje po refaktoringu
// TODO: Lokalizace data podle nastavení OS?
public class MainForm {
    private final static Logger log = LoggerFactory.getLogger(MainForm.class);
    private final BookManager bookManager;
    private final CustomerManager customerManager;
    private final LoanManager loanManager;

    private Long bookEditId = null;
    private Long customerEditId = null;

    private TableRowSorter<BookTableModel> bookTableSorter;
    private TableRowSorter<CustomerTableModel> readerTableSorter;

    private JPanel mainPanel;
    private JTable readerTable;
    private JTextField readerNameFilter;
    private JButton findReaderButton;
    private JButton deleteReaderButton;
    private JButton newReaderButton;
    private JTextField readerName;
    private JTextField readerCard;
    private JTextField readerAddress;
    private JTextField readerPhone;
    private JTextField readerEmail;
    private JButton saveReaderButton;
    private JTable bookTable;
    private JTextField bookAuthorFilter;
    private JTextField bookLanguage;
    private JTextField bookPagesNumber;
    private JButton findBookButton;
    private JTextField bookNameFilter;
    private JButton newBookButton;
    private JTabbedPane booksControl;
    private JTextField bookName;
    private JTextField bookAuthor;
    private JTextField bookIsbn;
    private JTextField bookPublisher;
    private JTextField bookYearOfPublication;
    private JButton deleteBookButton;
    private JButton saveBookButton;
    private JTabbedPane readersControl;
    private JButton checkOutButton;


    public MainForm(BookManager bookManager, CustomerManager customerManager, LoanManager loanManager) {
        this.bookManager = bookManager;
        this.customerManager = customerManager;
        this.loanManager = loanManager;

        addBookListeners(); // Book tab listeners
        addReaderListeners(); // Reader tab listeners
        checkOutButton.addActionListener(e -> checkOut());

        initializeBookTable();
        initializeReaderTable();
    }

    public static void main(String[] args) {
        final BookManager bookManager;
        final CustomerManager customerManager;
        final LoanManager loanManager;

        try {
            DataSource dataSource = DataSourceFactory.getDataSource();
            bookManager = new BookManagerImpl(dataSource);
            customerManager = new CustomerManagerImpl(dataSource);
            loanManager = new LoanManagerImpl(dataSource);
        } catch (IOException e) {
            String message = "Error when connecting to database.";
            System.out.println(message);
            log.error(message + e);

            return;
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            String message = "Error when setting user interface.";
            System.out.println(message);
            log.error(message + e);

            return;
        }

        EventQueue.invokeLater(() -> {
            MainForm mainForm = new MainForm(bookManager, customerManager, loanManager);
            JFrame frame = new JFrame("My Library");
            frame.setContentPane(mainForm.mainPanel);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
        });
    }


    // Initialize tables
    private void initializeBookTable() {
        BookTableModel model = new BookTableModel(bookManager);

        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookTable.setModel(model);

        bookTable.getColumnModel().getColumn(0).setPreferredWidth(30);
        bookTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        bookTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        bookTable.getColumnModel().getColumn(3).setPreferredWidth(70);

        bookTableSorter = new TableRowSorter<>(model);
        bookTable.setRowSorter(bookTableSorter);
    }

    private void initializeReaderTable() {
        CustomerTableModel model = new CustomerTableModel(customerManager);

        readerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        readerTable.setModel(model);

        readerTable.getColumnModel().getColumn(0).setPreferredWidth(20);
        readerTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        readerTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        readerTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        readerTable.getColumnModel().getColumn(4).setPreferredWidth(90);
        readerTable.getColumnModel().getColumn(5).setPreferredWidth(90);

        readerTableSorter = new TableRowSorter<>(model);
        readerTable.setRowSorter(readerTableSorter);
    }


    // Listeners
    private void addBookListeners() {
        // New book
        newBookButton.addActionListener(e -> {
            bookTable.clearSelection();
            clearBookDetailsForm();
            booksControl.setSelectedIndex(1); // Switch to details tab
        });

        // Delete book
        deleteBookButton.addActionListener(e -> {
            final Long id = UICommon.getSelectedItemId(bookTable);
            if (id == null) return;
            if (!confirmDeletion()) return;

            deleteBookButton.setEnabled(false);
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    Book book = bookManager.findBookById(id);
                    if (book == null) return null;

                    bookManager.deleteBook(book);
                    log.info("Book deleted: " + book);

                    return null;
                }

                protected void done() {
                    getBookTableModel().loadData();
                    clearBookDetailsForm();
                }
            }.execute();
        });

        // Find book
        // TODO: Není potřeba SwingWorker?
        findBookButton.addActionListener(e -> {
            String nameFilter = UICommon.getFilterValue(bookNameFilter);
            String authorFilter = UICommon.getFilterValue(bookAuthorFilter);

            ArrayList<RowFilter<Object, Object>> filters = new ArrayList<>();
            filters.add(RowFilter.regexFilter(nameFilter, 1));
            filters.add(RowFilter.regexFilter(authorFilter, 2));

            RowFilter<BookTableModel, Object> filter = RowFilter.andFilter(filters);
            bookTableSorter.setRowFilter(filter);
        });

        // Save book
        saveBookButton.addActionListener(e -> {
            if (!validateBook()) return;

            saveBookButton.setEnabled(false);
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    Book book;
                    if (bookEditId != null) {
                        book = bookManager.findBookById(bookEditId);
                        if (book == null) return null;
                    } else {
                        book = new Book();
                    }

                    setBookDataFromForm(book);

                    if (bookEditId != null) {
                        bookManager.updateBook(book);
                        log.info("Book updated: " + book);
                    } else {
                        bookManager.createBook(book);
                        log.info("Book added: " + book);
                    }

                    return null;
                }

                protected void done() {
                    getBookTableModel().loadData();
                    clearBookDetailsForm();
                    saveBookButton.setEnabled(true);
                }
            }.execute();
        });

        // Table selection
        bookTable.getSelectionModel().addListSelectionListener(e -> {
            if (bookTable.getSelectedRow() < 0) {
                deleteBookButton.setEnabled(false);
                return;
            }

            final Long id = UICommon.getSelectedItemId(bookTable);
            updateBookDetailsForm(id);
        });
    }

    private void addReaderListeners() {
        // New reader
        newReaderButton.addActionListener(e -> {
            readerTable.clearSelection();
            clearReaderDetailsForm();
            readersControl.setSelectedIndex(1); // Switch to details tab
        });

        // Delete reader
        deleteReaderButton.addActionListener(e -> {
            final Long id = UICommon.getSelectedItemId(readerTable);
            if (id == null) return;
            if (!confirmDeletion()) return;

            deleteReaderButton.setEnabled(false);
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    Customer customer = customerManager.findCustomerById(id);
                    if (customer == null) return null;

                    customerManager.deleteCustomer(customer);
                    log.info("Customer deleted: " + customer);

                    return null;
                }

                protected void done() {
                    getCustomerTableModel().loadData();
                    clearReaderDetailsForm();
                }
            }.execute();
        });

        // Find reader
        // TODO: Není potřeba SwingWorker?
        findReaderButton.addActionListener(e -> {
            String nameFilter = UICommon.getFilterValue(readerNameFilter);

            ArrayList<RowFilter<Object, Object>> filters = new ArrayList<>();
            filters.add(RowFilter.regexFilter(nameFilter, 1));

            RowFilter<CustomerTableModel, Object> filter = RowFilter.andFilter(filters);
            readerTableSorter.setRowFilter(filter);
        });

        // Save reader
        saveReaderButton.addActionListener(e -> {
            if (!validateReader()) return;

            saveReaderButton.setEnabled(false);
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    Customer customer;
                    if (customerEditId != null) {
                        customer = customerManager.findCustomerById(customerEditId);
                        if (customer == null) return null;
                    } else {
                        customer = new Customer();
                    }

                    setCustomerDataFromForm(customer);

                    if (customerEditId != null) {
                        customerManager.updateCustomer(customer);
                        log.info("Customer updated: " + customer);
                    } else {
                        customerManager.createCustomer(customer);
                        log.info("Customer added: " + customer);
                    }

                    return null;
                }

                protected void done() {
                    getCustomerTableModel().loadData();
                    clearReaderDetailsForm();
                    saveReaderButton.setEnabled(true);
                }
            }.execute();
        });

        // Table selection
        readerTable.getSelectionModel().addListSelectionListener(e -> {
            if (readerTable.getSelectedRow() < 0) {
                deleteReaderButton.setEnabled(false);
                return;
            }

            final Long id = UICommon.getSelectedItemId(readerTable);
            updateReaderDetailsForm(id);
        });
    }

    private void checkOut() {
        Long customerId = UICommon.getSelectedItemId(readerTable);
        if (customerId == null) {
            errorDialog("readerNotSelected");
            return;
        }

        new SwingWorker<Customer, Void>() {
            @Override
            protected Customer doInBackground() {
                return customerManager.findCustomerById(customerEditId);
            }

            protected void done() {
                Customer customer;
                try {
                    customer = get();
                } catch(Exception e) {
                    errorDialog("customerLoadError");
                    log.error("Customer cannot be loaded when checking out." + e);
                    return;
                }

                if (customer == null) {
                    errorDialog("customerLoadError");
                    log.error("Customer cannot be loaded when checking out.");
                    return;
                }

                new LoanForm(bookManager, loanManager, customer);
            }
        }.execute();
    }


    // Common
    private boolean confirmDeletion() {
        int answer = JOptionPane.showConfirmDialog(
                mainPanel,
                Localization.get("confirmDelete"),
                "My Library",
                JOptionPane.YES_NO_OPTION);

        return answer == 0;
    }

    private void errorDialog(String localizationKey) {
        JOptionPane.showMessageDialog(
                mainPanel,
                Localization.get(localizationKey),
                "My Library",
                JOptionPane.ERROR_MESSAGE);
    }


    // Retrieve data from form
    private void setBookDataFromForm(Book book) {
        int yearOfPublication = Integer.parseInt(bookYearOfPublication.getText());
        int pagesNumber = Integer.parseInt(bookPagesNumber.getText());

        book.setName(bookName.getText());
        book.setAuthor(bookAuthor.getText());
        book.setIsbn(bookIsbn.getText());
        book.setPublisher(bookPublisher.getText());
        book.setYearOfPublication(yearOfPublication);
        book.setLanguage(bookLanguage.getText());
        book.setPagesNumber(pagesNumber);
    }

    private void setCustomerDataFromForm(Customer customer) {
        customer.setIdCard(readerCard.getText());
        customer.setName(readerName.getText());
        customer.setAddress(readerAddress.getText());
        customer.setTelephone(readerPhone.getText());
        customer.setEmail(readerEmail.getText());
    }


    // Update edit forms
    private void updateBookDetailsForm(Long id) {
        if (id == null) return;
        new SwingWorker<Book, Void>() {
            @Override
            protected Book doInBackground() {
                return bookManager.findBookById(id);
            }

            protected void done() {
                Book book;
                try {
                    book = get();
                } catch (Exception e) {
                    errorDialog("bookLoadError");
                    log.error("Retrieving book failed." + e);
                    return;
                }

                bookEditId = id;
                bookName.setText(book.getName());
                bookAuthor.setText(book.getAuthor());
                bookIsbn.setText(book.getIsbn());
                bookPublisher.setText(book.getPublisher());
                bookYearOfPublication.setText(String.valueOf(book.getYearOfPublication()));
                bookLanguage.setText(book.getLanguage());
                bookPagesNumber.setText(String.valueOf(book.getPagesNumber()));

                booksControl.setTitleAt(1, Localization.get("edit"));
                booksControl.setSelectedIndex(1); // Switch to details tab
                deleteBookButton.setEnabled(true);
            }
        }.execute();
    }

    private void updateReaderDetailsForm(Long id) {
        if (id == null) return;
        new SwingWorker<Customer, Void>() {
            @Override
            protected Customer doInBackground() {
                return customerManager.findCustomerById(id);
            }

            protected void done() {
                Customer customer;
                try {
                    customer = get();
                } catch (Exception e) {
                    errorDialog("customerLoadError");
                    log.error("Retrieving customer failed." + e);
                    return;
                }

                customerEditId = id;
                readerName.setText(customer.getName());
                readerCard.setText(customer.getIdCard());
                readerAddress.setText(customer.getAddress());
                readerPhone.setText(customer.getTelephone());
                readerEmail.setText(customer.getEmail());

                readersControl.setTitleAt(1, Localization.get("edit"));
                readersControl.setSelectedIndex(1); // Switch to details tab
                deleteReaderButton.setEnabled(true);
            }
        }.execute();
    }


    // Clear edit forms
    private void clearBookDetailsForm() {
        bookEditId = null;
        clearFields(bookName, bookAuthor, bookIsbn,
                bookPublisher, bookYearOfPublication, bookLanguage,
                bookPagesNumber);
        booksControl.setTitleAt(1, Localization.get("newBook"));
    }

	private void clearReaderDetailsForm() {
		customerEditId = null;
        clearFields(readerName, readerCard, readerAddress,
                readerPhone, readerEmail);
		readersControl.setTitleAt(1, Localization.get("newReader"));
	}

    private void clearFields(JTextField... fields) {
        for (JTextField field : fields) {
            field.setText("");
        }
    }


    // Validation
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

	private boolean validateReader() {
		Validator validator = new Validator(mainPanel);

		validator.mandatory(readerName);
		validator.mandatory(readerCard);
		validator.mandatory(readerAddress);
		validator.mandatory(readerPhone);
		validator.mandatory(readerEmail);

		return validator.getConclusion();
	}


    // Get table models
    private BookTableModel getBookTableModel() {
        return (BookTableModel) bookTable.getModel();
    }

    private CustomerTableModel getCustomerTableModel() {
        return (CustomerTableModel) readerTable.getModel();
    }
}
