package com.mylibrary;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;

/**
 * @author Radim Kratochvil
 * @author Michael Le <lemichael@mail.muni.cz>
 */
public class CustomerManagerImpl implements CustomerManager {
	private final DataSource dataSource;

	public CustomerManagerImpl(DataSource dataSource) {
		this.dataSource = dataSource;
	}

    @Override
    public void createCustomer(Customer customer) {
		if (customer == null) {
			throw new IllegalArgumentException("customer is null");
		}
		if (customer.getId() != null) {
			throw new IllegalArgumentException("customer id is already set");
		}
		if (customer.getIdCard() == null) {
			throw new IllegalArgumentException("customer idCard is null");
		}
		if (customer.getName() == null) {
			throw new IllegalArgumentException("customer name is null");
		}
		if (customer.getAddress() == null) {
			throw new IllegalArgumentException("customer address is null");
		}
		if (customer.getTelephone() == null) {
			throw new IllegalArgumentException("customer telephone is null");
		}
		if (customer.getEmail() == null) {
			throw new IllegalArgumentException("customer email is null");
		}


		try (Connection connection = dataSource.getConnection()) {
			try (PreparedStatement st = connection.prepareStatement(
					"INSERT INTO CUSTOMER (IDCARD, \"NAME\", ADDRESS, TELEPHONE, EMAIL) " +
							"VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
				st.setString(1, customer.getIdCard());
				st.setString(2, customer.getName());
				st.setString(3, customer.getAddress());
				st.setString(4, customer.getTelephone());
				st.setString(5, customer.getEmail());

				int addedRows = st.executeUpdate();
				if (addedRows != 1) {
					// pridat vyjimku
				}

				ResultSet keyRS = st.getGeneratedKeys();
				keyRS.next();
				customer.setId(keyRS.getLong(1));
			}
		} catch (SQLException ex){
			// pridat vyjimku
		}
    }

    @Override
    public void updateCustomer(Customer customer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteCustomer(Customer customer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Customer> findAllCustomers() {
        return null;
    }

    @Override
    public Customer findCustomerById(Long id) {
        return null;
    }

    @Override
    public Customer findCustomerByName(String name) {
        return null;
    }
}
