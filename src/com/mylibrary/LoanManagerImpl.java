package com.mylibrary;

import javax.sql.DataSource;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * @author Radim Kratochvil
 * @author Michael Le <lemichael@mail.muni.cz>
 */
public class LoanManagerImpl implements LoanManager {
	private final DataSource dataSource;

	private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public LoanManagerImpl(DataSource dataSource) {
		this.dataSource = dataSource;
	}

    @Override
    public void createLoan(Loan loan) {
		checkLoanValues(loan);
		if (loan.getId() != null) {
			throw new IllegalArgumentException("loan id is already set");
		}
		if (loan.getRealEndDate() != null) {
			throw new IllegalArgumentException("loan real end date is already set");
		}

		try (
				Connection connection = dataSource.getConnection();
				PreparedStatement statement = connection.prepareStatement(
						"INSERT INTO LOAN (IDCUSTOMER, IDBOOK, STARTDATE, ENDDATE, REALENDDATE) " +
								"VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)
		) {
			statement.setLong(1, loan.getCustomer().getId());
			statement.setLong(2, loan.getBook().getId());
			statement.setString(3, dateFormat.format(loan.getStartDate()));
			statement.setString(4, dateFormat.format(loan.getEndDate()));
			statement.setString(5, null);

			int addedRows = statement.executeUpdate();
			if(addedRows == 0) {
				throw new ServiceFailureException("Loan was not inserted: " + loan);
			}
			if(addedRows > 1) {
				throw new ServiceFailureException("Internal error! More loans added than expected: " + loan);
			}

			ResultSet keyRS = statement.getGeneratedKeys();
			loan.setId(getGeneratedId(keyRS, loan));
		} catch (SQLException ex){
			throw new ServiceFailureException("Database connection error.", ex);
		}
    }

    @Override
    public void updateLoan(Loan loan) {
		checkLoanValues(loan);
		if (loan.getId() == null) {
			throw new IllegalArgumentException("loan id is not set");
		}

		try (
				Connection connection = dataSource.getConnection();
				PreparedStatement statement = connection.prepareStatement(
						"UPDATE LOAN SET IDCUSTOMER = ?, IDBOOK = ?, STARTDATE = ?, ENDDATE = ?, REALENDDATE = ? WHERE ID = ?",
						Statement.RETURN_GENERATED_KEYS)
		) {
			statement.setLong(1, loan.getCustomer().getId());
			statement.setLong(2, loan.getBook().getId());
			statement.setString(3, dateFormat.format(loan.getStartDate()));
			statement.setString(4, dateFormat.format(loan.getEndDate()));

			if (loan.getRealEndDate() == null){
				statement.setString(5, null);
			} else {
				statement.setString(5, dateFormat.format(loan.getRealEndDate()));
			}

			statement.setLong(6, loan.getId());

			int addedRows = statement.executeUpdate();
			if(addedRows == 0) {
				throw new ServiceFailureException("Loan was not updated: " + loan);
			}
			if(addedRows > 1) {
				throw new ServiceFailureException("Internal error! More loans updated than expected: " + loan);
			}
		} catch (SQLException ex){
			throw new ServiceFailureException("Database connection error.", ex);
		}
    }

    @Override
    public void deleteLoan(Loan loan) {
		if(loan == null) {
			throw new IllegalArgumentException("Loan is null.");
		}

		if(loan.getId() == null) {
			throw new IllegalArgumentException("Loan ID is not set.");
		}

		try(
				Connection connection = dataSource.getConnection();
				PreparedStatement statement = connection.prepareStatement("DELETE FROM LOAN WHERE ID = ?")
		) {
			statement.setLong(1, loan.getId());

			int addedRows = statement.executeUpdate();
			if(addedRows == 0) {
				throw new ServiceFailureException("Loan was not deleted: " + loan);
			}
			if(addedRows > 1) {
				throw new ServiceFailureException("Internal error! More loans deleted than expected: " + loan);
			}
		} catch(SQLException e) {
			throw new ServiceFailureException("Database connection error.", e);
		}
    }

    @Override
    public Loan findLoanById(Long id) {
		if(id < 0)
			throw new IllegalArgumentException("Loan id is invalid.");

		try(
				Connection connection = dataSource.getConnection();
				PreparedStatement statement = connection.prepareStatement(
						"SELECT ID, IDCUSTOMER, IDBOOK, STARTDATE, ENDDATE, REALENDDATE FROM LOAN WHERE ID = ?")
		) {
			statement.setLong(1, id);

			ResultSet result = statement.executeQuery();
			if(result.next()) {
				Loan loan = resultSetToLoan(result);
				if(result.next()) {
					throw new ServiceFailureException("More loans with the same ID was found; loan: " + loan);
				}
				return loan;
			}
			return null;
		} catch(SQLException e) {
			throw new ServiceFailureException("Database connection error.", e);
		}
	}

    @Override
    public List<Book> findAllAvailableBooks() {
		BookManagerImpl bookManager = new BookManagerImpl(dataSource);
		List<Book> bookList = bookManager.findAllBooks();

		try(
				Connection connection = dataSource.getConnection();
				PreparedStatement statement = connection.prepareStatement(
						"SELECT IDBOOK FROM LOAN WHERE REALENDDATE = ?")
		) {
			statement.setString(1, null);

			ResultSet result = statement.executeQuery();
			Set<Long> unavailableBooks = new TreeSet<>();

			while(result.next()) {
				unavailableBooks.add(result.getLong("IDBOOK"));
			}

			for(Long id : unavailableBooks){
				bookList.remove(bookManager.findBookById(id));
			}

			return bookList;
		} catch(SQLException e) {
			throw new ServiceFailureException("Database connection error.", e);
		}
    }

    @Override
    public List<Loan> findAllLendings() {
		try(
				Connection connection = dataSource.getConnection();
				PreparedStatement statement = connection.prepareStatement(
						"SELECT ID, IDCUSTOMER, IDBOOK, STARTDATE, ENDDATE, REALENDDATE FROM LOAN")
		) {
			ResultSet result = statement.executeQuery();
			List<Loan> loans = new ArrayList<>();

			while(result.next()) {
				loans.add(resultSetToLoan(result));
			}
			return loans;
		} catch(SQLException e) {
			throw new ServiceFailureException("Database connection error.", e);
		}
    }

    @Override
    public List<Loan> findLendingsByCustomer(Customer customer) {
		if(customer.getId() < 0)
			throw new IllegalArgumentException("Customer is invalid.");

		try(
				Connection connection = dataSource.getConnection();
				PreparedStatement statement = connection.prepareStatement(
						"SELECT ID, IDCUSTOMER, IDBOOK, STARTDATE, ENDDATE, REALENDDATE FROM LOAN WHERE IDCUSTOMER = ?")
		) {
			statement.setLong(1, customer.getId());

			ResultSet result = statement.executeQuery();
			List<Loan> loans = new ArrayList<>();

			while(result.next()) {
				loans.add(resultSetToLoan(result));
			}
			return loans;
		} catch(SQLException e) {
			throw new ServiceFailureException("Database connection error.", e);
		}
    }

    @Override
    public List<Loan> findLendingsPastDue() {
		try(
				Connection connection = dataSource.getConnection();
				PreparedStatement statement = connection.prepareStatement(
						"SELECT ID, IDCUSTOMER, IDBOOK, STARTDATE, ENDDATE, REALENDDATE FROM LOAN WHERE REALENDDATE = ?")
		) {
			statement.setString(1, null);

			ResultSet result = statement.executeQuery();
			List<Loan> loans = new ArrayList<>();

			while(result.next()) {
				loans.add(resultSetToLoan(result));
			}
			List<Loan> loansPastDue = new ArrayList<>();

			Date today = new Date();
			String todayString = dateFormat.format(today);
			try {
				today = dateFormat.parse(todayString);
			} catch (ParseException e) {
				throw new ServiceFailureException("Parser error exception.", e);
			}

			for(Loan loan : loans){
				if (loan.getEndDate().compareTo(today) < 0){
					loansPastDue.add(loan);
				}
			}

			return loansPastDue;
		} catch(SQLException e) {
			throw new ServiceFailureException("Database connection error.", e);
		}
    }

	private void checkLoanValues(Loan loan) {
		if (loan == null) {
			throw new IllegalArgumentException("loan is null");
		}

		if (loan.getCustomer() == null) {
			throw new IllegalArgumentException("loan customer is not set");
		}

		if (loan.getBook() == null) {
			throw new IllegalArgumentException("loan book is not set");
		}

		if (loan.getStartDate() == null) {
			throw new IllegalArgumentException("loan start date is not set");
		}

		if (loan.getEndDate() == null) {
			throw new IllegalArgumentException("loan end date is not set");
		}
	}

	private Long getGeneratedId(ResultSet result, Loan loan) throws SQLException, ServiceFailureException {
		if(result.next()) {
			if(result.getMetaData().getColumnCount() != 1) {
				throw new ServiceFailureException("Invalid number of generated keys of the " +
						"inserted loan cannot be fetched; loan" + loan);
			}

			Long id = result.getLong(1);
			if(result.next()) {
				throw new ServiceFailureException("Too many generated keys of the inserted " +
						"loan found; loan" + loan);
			}

			return id;
		} else {
			throw new ServiceFailureException("Generated key of the inserted loan cannot " +
					"be fetched; loan: " + loan);
		}
	}

	private Loan resultSetToLoan(ResultSet result) throws SQLException{
		Loan loan = new Loan();
		loan.setId(result.getLong("ID"));

		try {
			loan.setStartDate(dateFormat.parse(result.getString("STARTDATE")));
			loan.setEndDate(dateFormat.parse(result.getString("ENDDATE")));
			if(result.getString("REALENDDATE") != null){
				loan.setRealEndDate(dateFormat.parse(result.getString("REALENDDATE")));
			} else {
				loan.setRealEndDate(null);
			}
		} catch (ParseException e) {
			throw new ServiceFailureException("Loan date parser error.", e);
		}

		CustomerManagerImpl customerManager = new CustomerManagerImpl(dataSource);
		Customer customer = customerManager.findCustomerById(result.getLong("IDCUSTOMER"));

		BookManagerImpl bookManager = new BookManagerImpl(dataSource);
		Book book = bookManager.findBookById(result.getLong("IDBOOK"));

		loan.setCustomer(customer);
		loan.setBook(book);
		return loan;
	}
}