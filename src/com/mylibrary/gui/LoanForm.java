package com.mylibrary.gui;

import com.jgoodies.looks.windows.WindowsLookAndFeel;

import com.mylibrary.*;

import com.mylibrary.models.BookTableModel;
import com.mylibrary.models.LoanTableModel;
import com.mylibrary.tools.DataSourceFactory;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Radim Kratochvil
 * @author Michael Le <lemichael@mail.muni.cz>
 */
public class LoanForm {
    private JPanel defaultPanel;
    private JTable loanTable;
    private JTable availableBooksTable;
    private JTextField bookAuthorFilter;
	private JButton returnBook;
	private JButton exit;
	private JLabel readerName;
	private JTextField bookNameFilter;
	private JButton findBook;
	private JButton borrowBook;

	private TableRowSorter<BookTableModel> bookTableSorter;
	private TableRowSorter<LoanTableModel> loanTableSorter;

	private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	private BookManager bookManager;
	private CustomerManager customerManager;
	private LoanManager loanManager;

	private Long customerID = null;
	private Long loanID = null;
	private Long bookID = null;

	public LoanForm(){
		EventQueue.invokeLater(() -> {
			JFrame frame = new JFrame("My Library");
			frame.setContentPane(defaultPanel);
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frame.pack();
			frame.setVisible(true);
		});



		customerID = (long) 2;




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

		exit.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Container frame = exit.getParent();
				do
					frame = frame.getParent();
				while (!(frame instanceof JFrame));
				((JFrame) frame).dispose();
			}
		});

		loanTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (loanTable.getSelectedRow() > -1) {
					loanID = selectedItemId(loanTable);
					if (loanID == null) return;

					returnBook.setEnabled(true);
				} else {
					returnBook.setEnabled(false);
				}
			}
		});

		returnBook.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(loanID != null && loanManager.findLoanById(loanID) == null) return;

				returnBook.setEnabled(false);
				new SwingWorker<Void, Void>() {
					@Override
					protected Void doInBackground() {
						Loan loan;
						if(loanID != null) loan = loanManager.findLoanById(loanID);
						else return null;

						Date today = new Date();
						String todayString = dateFormat.format(today);
						try {
							today = dateFormat.parse(todayString);
						} catch (ParseException e) {
							throw new ServiceFailureException("Parser error exception.", e);
						}

						loan.setRealEndDate(today);

						loanManager.updateLoan(loan);

						return null;
					}

					protected void done() {
						((LoanTableModel) loanTable.getModel()).loadData();
						returnBook.setEnabled(true);
					}
				}.execute();
			}
		});

		availableBooksTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (availableBooksTable.getSelectedRow() > -1) {
					bookID = selectedItemId(availableBooksTable);
					if (bookID == null) return;

					borrowBook.setEnabled(true);
				} else {
					borrowBook.setEnabled(false);
				}
			}
		});

		borrowBook.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (bookID != null && bookManager.findBookById(bookID) == null) return;

				borrowBook.setEnabled(false);
				new SwingWorker<Void, Void>() {
					@Override
					protected Void doInBackground() {
						Loan loan = new Loan();

						loan.setCustomer(customerManager.findCustomerById(customerID));
						loan.setBook(bookManager.findBookById(bookID));

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
							throw new ServiceFailureException("Parser error exception.", e);
						}

						loan.setStartDate(today);
						loan.setEndDate(nextMonth);

						loanManager.createLoan(loan);

						return null;
					}

					protected void done() {
						((BookTableModel) availableBooksTable.getModel()).loadData();
						((LoanTableModel) loanTable.getModel()).loadData();
						borrowBook.setEnabled(true);
					}
				}.execute();
			}
		});
	}

	private void createUIComponents() throws IOException {
		customerID = (long) 2;

		try {
			bookManager = new BookManagerImpl(DataSourceFactory.getDataSource());
			customerManager = new CustomerManagerImpl(DataSourceFactory.getDataSource());
			loanManager = new LoanManagerImpl(DataSourceFactory.getDataSource());
		} catch (IOException e) {
			e.printStackTrace();
		}

		//readerName.setText(customerManager.findCustomerById(customerID).getName());

		createBookTable();
		createLoanTable();
	}

	private void createBookTable() throws IOException {
		BookTableModel model = new BookTableModel(){
			@Override
			public void loadData() {
				new SwingWorker<Void, Void>() {
					@Override
					protected Void doInBackground() {
						setBooks(loanManager.findAllAvailableBooks());
						return null;
					}

					protected void done() {
						fireTableDataChanged();
					}
				}.execute();
			}
		};

		availableBooksTable = new JTable();
		availableBooksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		availableBooksTable.setModel(model);

		availableBooksTable.getColumnModel().getColumn(0).setPreferredWidth(30);
		availableBooksTable.getColumnModel().getColumn(1).setPreferredWidth(200);
		availableBooksTable.getColumnModel().getColumn(2).setPreferredWidth(200);
		availableBooksTable.getColumnModel().getColumn(3).setPreferredWidth(70);

		bookTableSorter = new TableRowSorter<>(model);
		availableBooksTable.setRowSorter(bookTableSorter);
	}

	private void createLoanTable() throws IOException {
		LoanTableModel model = new LoanTableModel(customerID){};

		loanTable = new JTable();
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

	private Long selectedItemId(JTable tbl) {
		int rowIndex = tbl.getSelectedRow();
		if(rowIndex < 0) {
			return null;
		}

		return (Long) tbl.getValueAt(rowIndex, 0);
	}

	public void setCustomerID(Long id){
		customerID = id;
	}
}
