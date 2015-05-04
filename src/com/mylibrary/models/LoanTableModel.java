package com.mylibrary.models;

import com.mylibrary.*;

import com.mylibrary.tools.DataSourceFactory;
import com.mylibrary.tools.Localization;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Radim Kratochvil
 * @author Michael Le <lemichael@mail.muni.cz>
 */
public class LoanTableModel extends AbstractTableModel {
	private Customer customer;
	private LoanManager loanManager;
	private List<Loan> loans = new ArrayList<>();

	public LoanTableModel(Long id) throws IOException {
		customer = (new CustomerManagerImpl(DataSourceFactory.getDataSource())).findCustomerById(id);
		loanManager = new LoanManagerImpl(DataSourceFactory.getDataSource());

		loadData();
	}

	@Override
	public int getRowCount() {
		return loans.size();
	}

	@Override
	public int getColumnCount() {
		return 6;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Loan loan = loans.get(rowIndex);
		Book book = loan.getBook();

		switch (columnIndex) {
			case 0:
				return loan.getId();
			case 1:
				return book.getName();
			case 2:
				return book.getAuthor();
			case 3:
				return book.getIsbn();
			case 4:
				return loan.getStartDate();
			case 5:
				return loan.getEndDate();
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
			case 4:
				return Localization.get("startDate");
			case 5:
				return Localization.get("endDate");
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
				loans = loanManager.findLendingsByCustomer(customer);
				for (int i = 0; i < loans.size(); i++){
					if (loans.get(i).getRealEndDate() != null){
						loans.remove(i);
						--i;
					}
				}
				return null;
			}

			protected void done() {
				fireTableDataChanged();
			}
		}.execute();
	}
}
