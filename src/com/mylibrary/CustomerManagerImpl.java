package com.mylibrary;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
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
    public void createCustomer(Customer customer) throws ServiceFailureException {
		checkCustomerValues(customer);
		if (customer.getId() != null) {
            throw new IllegalArgumentException("customer id is already set");
        }

		try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(
					    "INSERT INTO CUSTOMER (IDCARD, \"NAME\", ADDRESS, TELEPHONE, EMAIL) " +
						"VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)
        ) {
            statement.setString(1, customer.getIdCard());
            statement.setString(2, customer.getName());
            statement.setString(3, customer.getAddress());
            statement.setString(4, customer.getTelephone());
            statement.setString(5, customer.getEmail());

            int addedRows = statement.executeUpdate();
            if(addedRows == 0) {
                throw new ServiceFailureException("Customer was not inserted: " + customer);
            }
            if(addedRows > 1) {
                throw new ServiceFailureException("Internal error! More customers added than expected: " + customer);
            }

            ResultSet keyRS = statement.getGeneratedKeys();
            customer.setId(getGeneratedId(keyRS, customer));
		} catch (SQLException ex){
			throw new ServiceFailureException("Database connection error.", ex);
		}
    }

    @Override
    public void updateCustomer(Customer customer) throws ServiceFailureException {
		checkCustomerValues(customer);
		if (customer.getId() == null) {
            throw new IllegalArgumentException("customer id is not set");
        }

		try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "UPDATE CUSTOMER SET IDCARD = ?, \"NAME\" = ?, ADDRESS = ?, TELEPHONE = ?, EMAIL = ? WHERE ID = ?",
                        Statement.RETURN_GENERATED_KEYS)
        ) {
            statement.setString(1, customer.getIdCard());
            statement.setString(2, customer.getName());
            statement.setString(3, customer.getAddress());
            statement.setString(4, customer.getTelephone());
            statement.setString(5, customer.getEmail());
            statement.setLong(6, customer.getId());

            int addedRows = statement.executeUpdate();
            if(addedRows == 0) {
                throw new ServiceFailureException("Customer was not updated: " + customer);
            }
            if(addedRows > 1) {
                throw new ServiceFailureException("Internal error! More customers updated than expected: " + customer);
            }
		} catch (SQLException ex){
			throw new ServiceFailureException("Database connection error.", ex);
		}
    }

    @Override
    public void deleteCustomer(Customer customer) throws ServiceFailureException {
		if(customer == null) {
            throw new IllegalArgumentException("Customer is null.");
        }

		if(customer.getId() == null) {
            throw new IllegalArgumentException("Customer ID is not set.");
        }

		try(
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement("DELETE FROM CUSTOMER WHERE ID = ?")
        ) {
            statement.setLong(1, customer.getId());

            int addedRows = statement.executeUpdate();
            if(addedRows == 0) {
                throw new ServiceFailureException("Customer was not deleted: " + customer);
            }
            if(addedRows > 1) {
                throw new ServiceFailureException("Internal error! More customers deleted than expected: " + customer);
            }
		} catch(SQLException e) {
			throw new ServiceFailureException("Database connection error.", e);
		}
    }

    @Override
    public List<Customer> findAllCustomers() throws ServiceFailureException {
		try(
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT ID, IDCARD, \"NAME\", ADDRESS, TELEPHONE, EMAIL FROM CUSTOMER")
        ) {
            ResultSet result = statement.executeQuery();
            List<Customer> customers = new ArrayList<>();

            while(result.next()) {
                customers.add(resultSetToCustomer(result));
            }
            return customers;
		} catch(SQLException e) {
			throw new ServiceFailureException("Database connection error.", e);
		}
    }

    @Override
    public Customer findCustomerById(Long id) throws ServiceFailureException {
		if(id < 0)
			throw new IllegalArgumentException("Customer id is invalid.");

		try(
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT ID, IDCARD, \"NAME\", ADDRESS, TELEPHONE, EMAIL FROM CUSTOMER WHERE ID = ?")
        ) {
            statement.setLong(1, id);

            ResultSet result = statement.executeQuery();
            if(result.next()) {
                Customer customer = resultSetToCustomer(result);
                if(result.next()) {
                    throw new ServiceFailureException("More customers with the same ID was found; customer: " + customer);
                }
                return customer;
            }
            return null;
		} catch(SQLException e) {
			throw new ServiceFailureException("Database connection error.", e);
		}
    }

    @Override
    public List<Customer> findCustomerByName(String name) throws ServiceFailureException {
		try(
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT ID, IDCARD, \"NAME\", ADDRESS, TELEPHONE, EMAIL FROM CUSTOMER WHERE \"NAME\" = ?")
        ) {
            statement.setString(1, name);

            ResultSet result = statement.executeQuery();
            List<Customer> customers = new ArrayList<>();

            while(result.next()) {
                customers.add(resultSetToCustomer(result));
            }
            return customers;
		} catch(SQLException e) {
			throw new ServiceFailureException("Database connection error.", e);
		}
    }

	private void checkCustomerValues(Customer customer) {
		if (customer == null) {
            throw new IllegalArgumentException("customer is null");
        }

		if (customer.getIdCard() == null) {
            throw new IllegalArgumentException("customer idCard is not set");
        }

		if (customer.getName() == null) {
            throw new IllegalArgumentException("customer name is not set");
        }

		if (customer.getAddress() == null) {
            throw new IllegalArgumentException("customer address is not set");
        }

		if (customer.getTelephone() == null) {
            throw new IllegalArgumentException("customer telephone is not set");
        }

		if (customer.getEmail() == null) {
            throw new IllegalArgumentException("customer email is not set");
        }
	}

	private Long getGeneratedId(ResultSet result, Customer customer) throws SQLException, ServiceFailureException {
		if(result.next()) {
			if(result.getMetaData().getColumnCount() != 1) {
                throw new ServiceFailureException("Invalid number of generated keys of the " +
                        "inserted customer cannot be fetched; customer" + customer);
            }

			Long id = result.getLong(1);
			if(result.next()) {
                throw new ServiceFailureException("Too many generated keys of the inserted " +
                        "customer found; customer" + customer);
            }

			return id;
		} else {
            throw new ServiceFailureException("Generated key of the inserted customer cannot " +
                    "be fetched; customer: " + customer);
        }
	}

	private Customer resultSetToCustomer(ResultSet result) throws SQLException {
		Customer customer = new Customer();
		customer.setId(result.getLong("ID"));
		customer.setIdCard(result.getString("IDCARD"));
		customer.setName(result.getString("NAME"));
		customer.setAddress(result.getString("ADDRESS"));
		customer.setTelephone(result.getString("TELEPHONE"));
		customer.setEmail(result.getString("EMAIL"));
		return customer;
	}
}
