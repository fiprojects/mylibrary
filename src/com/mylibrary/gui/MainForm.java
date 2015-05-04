package com.mylibrary.gui;

import com.jgoodies.looks.windows.WindowsLookAndFeel;

import com.mylibrary.Book;
import com.mylibrary.BookManager;
import com.mylibrary.BookManagerImpl;
import com.mylibrary.Customer;
import com.mylibrary.CustomerManager;
import com.mylibrary.CustomerManagerImpl;

import com.mylibrary.gui.LoanForm;

import com.mylibrary.models.BookTableModel;
import com.mylibrary.models.CustomerTableModel;

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
    private JButton saveReader;
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
	private CustomerManager customerManager;
    private Long bookEditId = null;
	private Long customerEditId = null;

    private TableRowSorter<BookTableModel> bookTableSorter;
	private TableRowSorter<CustomerTableModel> readerTableSorter;

    public MainForm() {
        try {
            bookManager = new BookManagerImpl(DataSourceFactory.getDataSource());
			customerManager = new CustomerManagerImpl(DataSourceFactory.getDataSource());
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
				bookTable.clearSelection();
                cleanBookEdit();
                booksControl.setSelectedIndex(1);
            }
        });

        bookTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (bookTable.getSelectedRow() > -1) {
                    final Long id = selectedItemId(bookTable);
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
                final Long id = selectedItemId(bookTable);
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
						cleanBookEdit();
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

		readerTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (readerTable.getSelectedRow() > -1) {
					final Long id = selectedItemId(readerTable);
					if (id == null) return;

					new SwingWorker<Void, Void>() {
						@Override
						protected Void doInBackground() {
							fillReaderEdit(id);
							return null;
						}

						protected void done() {
							readersControl.setSelectedIndex(1);
						}
					}.execute();

					deleteReader.setEnabled(true);
				} else {
					deleteReader.setEnabled(false);
				}
			}
		});

		newReader.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				readerTable.clearSelection();
				cleanReaderEdit();
				readersControl.setSelectedIndex(1);
			}
		});

		deleteReader.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				final Long id = selectedItemId(readerTable);
				if (id == null) return;

				int answer = JOptionPane.showConfirmDialog(
						mainPanel,
						Localization.get("confirmDelete"),
						"My Library",
						JOptionPane.YES_NO_OPTION);
				if (answer != 0) return;

				deleteReader.setEnabled(false);
				new SwingWorker<Void, Void>() {
					@Override
					protected Void doInBackground() {
						Customer customer = customerManager.findCustomerById(id);
						if (customer == null) return null;

						customerManager.deleteCustomer(customer);
						return null;
					}

					protected void done() {
						((CustomerTableModel) readerTable.getModel()).loadData();
						cleanReaderEdit();
					}
				}.execute();
			}
		});

		saveReader.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(!validateReader()) return;
				if(customerEditId != null && customerManager.findCustomerById(customerEditId) == null) return;

				saveReader.setEnabled(false);
				new SwingWorker<Void, Void>() {
					@Override
					protected Void doInBackground() {
						Customer customer;
						if(customerEditId != null) customer = customerManager.findCustomerById(customerEditId);
						else customer = new Customer();

						customer.setIdCard(readerCard.getText());
						customer.setName(readerName.getText());
						customer.setAddress(readerAddress.getText());
						customer.setTelephone(readerPhone.getText());
						customer.setEmail(readerEmail.getText());

						if(customerEditId != null) customerManager.updateCustomer(customer);
						else customerManager.createCustomer(customer);

						return null;
					}

					protected void done() {
						((CustomerTableModel) readerTable.getModel()).loadData();
						cleanReaderEdit();
						saveReader.setEnabled(true);
					}
				}.execute();
			}
		});

		findReader.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				findReader.setEnabled(false);

				new SwingWorker<Void, Void>() {
					@Override
					protected Void doInBackground() {
						String nameFilter = readerNameFilter.getText();
						if (nameFilter.equals("%")) nameFilter = "";

						ArrayList<RowFilter<Object, Object>> filters = new ArrayList<>();
						filters.add(RowFilter.regexFilter(Pattern.quote(nameFilter), 1));

						RowFilter<CustomerTableModel, Object> filter = RowFilter.andFilter(filters);
						readerTableSorter.setRowFilter(filter);

						return null;
					}

					protected void done() {
						findReader.setEnabled(true);
					}
				}.execute();
			}
		});

		checkOutButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					UIManager.setLookAndFeel(new WindowsLookAndFeel());
				} catch (UnsupportedLookAndFeelException e1) {
					e1.printStackTrace();
				}

				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						LoanForm loanForm = new LoanForm();
						try {
							loanForm.start();
						} catch (UnsupportedLookAndFeelException e1) {
							e1.printStackTrace();
						}
					}
				});
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
		createReaderTable();
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

    private Long selectedItemId(JTable tbl) {
        int rowIndex = tbl.getSelectedRow();
        if(rowIndex < 0) {
            return null;
        }

        return (Long) tbl.getValueAt(rowIndex, 0);
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

	private void createReaderTable() throws IOException {
		CustomerTableModel model = new CustomerTableModel();

		readerTable = new JTable();
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

	private void cleanReaderEdit() {
		customerEditId = null;

		readerName.setText("");
		readerCard.setText("");
		readerAddress.setText("");
		readerPhone.setText("");
		readerEmail.setText("");

		readersControl.setTitleAt(1, Localization.get("newReader"));
	}

	private void fillReaderEdit(Long id) {
		Customer customer = customerManager.findCustomerById(id);
		if(customer == null) return;

		customerEditId = id;

		readerName.setText(customer.getName());
		readerCard.setText(customer.getIdCard());
		readerAddress.setText(customer.getAddress());
		readerPhone.setText(customer.getTelephone());
		readerEmail.setText(customer.getEmail());

		readersControl.setTitleAt(1, Localization.get("edit"));
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
}
