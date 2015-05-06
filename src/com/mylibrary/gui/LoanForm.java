package com.mylibrary.gui;

import com.mylibrary.*;

import com.mylibrary.models.AvailableBooksTableModel;
import com.mylibrary.models.BookTableModel;
import com.mylibrary.models.LoanTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Radim Kratochvil
 * @author Michael Le <lemichael@mail.muni.cz>
 */
public class LoanForm {
    private final static Logger log = LoggerFactory.getLogger(MainForm.class);
	private BookManager bookManager;
	private LoanManager loanManager;

    private Customer customer;
	private Long loanID = null;
	private Long bookID = null;

	private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	private TableRowSorter<BookTableModel> bookTableSorter;
	private TableRowSorter<LoanTableModel> loanTableSorter;

    private JPanel defaultPanel;
    private JTable loanTable;
    private JTable availableBooksTable;
    private JTextField bookAuthorFilter;
	private JButton returnBook;
	private JButton exit;
	private JLabel readerName;
	private JTextField bookNameFilter;
	private JButton findBookButton;
	private JButton borrowBookButton;

	public LoanForm(BookManager bookManager, LoanManager loanManager, Customer customer) {
		EventQueue.invokeLater(() -> {
			JFrame frame = new JFrame("My Library");
			frame.setContentPane(defaultPanel);
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frame.pack();
			frame.setVisible(true);
		});

        this.customer = customer;
        updateTitle();

        this.bookManager = bookManager;
        this.loanManager = loanManager;

        addLoanListeners();
        addAvailableBookListeners();

        initializeBookTable();
        initializeLoanTable();
	}


    // Listeners
    private void addLoanListeners() {
        // Return book
        returnBook.addActionListener(e -> {
            returnBook.setEnabled(false);
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    if (loanID == null) return null;

                    Loan loan = loanManager.findLoanById(loanID);
                    if (loan == null) return null;

                    Date today = new Date();
                    String todayString = dateFormat.format(today);
                    try {
                        today = dateFormat.parse(todayString);
                    } catch (ParseException e) {
                        log.error("Date parse error." + e);
                        throw new ServiceFailureException("Parser error exception.", e);
                    }

                    loan.setRealEndDate(today);
                    loanManager.updateLoan(loan);

                    return null;
                }

                protected void done() {
                    getLoanTableModel().loadData();
                    getBookTableModel().loadData();
                    returnBook.setEnabled(true);
                }
            }.execute();
        });

        // Table selection
        loanTable.getSelectionModel().addListSelectionListener(e -> {
            if (loanTable.getSelectedRow() < 0) {
                returnBook.setEnabled(false);
            }

            loanID = UICommon.getSelectedItemId(loanTable);
            if (loanID == null) return;
            returnBook.setEnabled(true);
        });

        // Exit form
        exit.addActionListener(e -> {
            Container frame = exit.getParent();
            do {
                frame = frame.getParent();
            } while (!(frame instanceof JFrame));
            ((JFrame) frame).dispose();
        });
    }

    private void addAvailableBookListeners() {
        // Find book
        // TODO: Není potřeba SwingWorker?
        findBookButton.addActionListener(e -> {
            findBookButton.setEnabled(false);

            String nameFilter = UICommon.getFilterValue(bookNameFilter);
            String authorFilter = UICommon.getFilterValue(bookAuthorFilter);

            ArrayList<RowFilter<Object, Object>> filters = new ArrayList<>();
            filters.add(RowFilter.regexFilter(nameFilter, 1));
            filters.add(RowFilter.regexFilter(authorFilter, 2));

            RowFilter<BookTableModel, Object> filter = RowFilter.andFilter(filters);
            bookTableSorter.setRowFilter(filter);
        });

        // Borrow book
        borrowBookButton.addActionListener(e -> {
            borrowBookButton.setEnabled(false);
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    if (bookID == null || bookManager.findBookById(bookID) == null) {
                        return null;
                    }

                    Date today = new Date();
                    Date nextMonth;

                    String todayString = dateFormat.format(today);
                    String nextMonthString;

                    Calendar c = Calendar.getInstance();
                    c.setTime(today);
                    c.add(Calendar.MONTH, 1);
                    nextMonthString = dateFormat.format(c.getTime());

                    try {
                        today = dateFormat.parse(todayString);
                        nextMonth = dateFormat.parse(nextMonthString);
                    } catch (ParseException e) {
                        log.error("Date parse error." + e);
                        throw new ServiceFailureException("Parser error exception.", e);
                    }

                    Loan loan = new Loan();
                    loan.setCustomer(customer);
                    loan.setBook(bookManager.findBookById(bookID));
                    loan.setStartDate(today);
                    loan.setEndDate(nextMonth);
                    loanManager.createLoan(loan);

                    return null;
                }

                protected void done() {
                    getBookTableModel().loadData();
                    getLoanTableModel().loadData();
                    borrowBookButton.setEnabled(true);
                }
            }.execute();
        });

        // Table selection
        availableBooksTable.getSelectionModel().addListSelectionListener(e -> {
            if (availableBooksTable.getSelectedRow() < 0) {
                borrowBookButton.setEnabled(false);
            }

            bookID = UICommon.getSelectedItemId(availableBooksTable);
            if (bookID == null) return;
            borrowBookButton.setEnabled(true);
        });
    }


    // Initialize tables
    private void initializeLoanTable() {
        LoanTableModel model = new LoanTableModel(loanManager, customer);

        loanTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        loanTable.setModel(model);

        loanTable.getColumnModel().getColumn(0).setPreferredWidth(25);
        loanTable.getColumnModel().getColumn(1).setPreferredWidth(50);
        loanTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        loanTable.getColumnModel().getColumn(3).setPreferredWidth(50);
        loanTable.getColumnModel().getColumn(4).setPreferredWidth(125);
        loanTable.getColumnModel().getColumn(5).setPreferredWidth(125);

        loanTableSorter = new TableRowSorter<>(model);
        loanTable.setRowSorter(loanTableSorter);
    }

	private void initializeBookTable() {
		BookTableModel model = new AvailableBooksTableModel(bookManager, loanManager);

		availableBooksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		availableBooksTable.setModel(model);

		availableBooksTable.getColumnModel().getColumn(0).setPreferredWidth(30);
		availableBooksTable.getColumnModel().getColumn(1).setPreferredWidth(200);
		availableBooksTable.getColumnModel().getColumn(2).setPreferredWidth(200);
		availableBooksTable.getColumnModel().getColumn(3).setPreferredWidth(70);

		bookTableSorter = new TableRowSorter<>(model);
		availableBooksTable.setRowSorter(bookTableSorter);
	}


    // Common
    private void updateTitle() {
        readerName.setText(customer.getName());
    }


    // Get table models
    private LoanTableModel getLoanTableModel() {
        return (LoanTableModel) loanTable.getModel();
    }

    private BookTableModel getBookTableModel() {
        return (BookTableModel) availableBooksTable.getModel();
    }
}
