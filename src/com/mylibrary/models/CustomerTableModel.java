package com.mylibrary.models;

import com.mylibrary.Customer;
import com.mylibrary.CustomerManager;
import com.mylibrary.CustomerManagerImpl;
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
public class CustomerTableModel extends AbstractTableModel {
	private CustomerManager manager;
	private List<Customer> customers = new ArrayList<>();

	public CustomerTableModel(CustomerManager customerManager) {
		this.manager = customerManager;
		loadData();
	}

	@Override
	public int getRowCount() {
		return customers.size();
	}

	@Override
	public int getColumnCount() {
		return 6;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Customer customer = customers.get(rowIndex);
		switch (columnIndex) {
			case 0:
				return customer.getId();
			case 1:
				return customer.getName();
			case 2:
				return customer.getIdCard();
			case 3:
				return customer.getAddress();
			case 4:
				return customer.getTelephone();
			case 5:
				return customer.getEmail();
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
				return Localization.get("idCard");
			case 3:
				return Localization.get("address");
			case 4:
				return Localization.get("telephone");
			case 5:
				return Localization.get("email");
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
				customers = manager.findAllCustomers();
				return null;
			}

			protected void done() {
				fireTableDataChanged();
			}
		}.execute();
	}
}
