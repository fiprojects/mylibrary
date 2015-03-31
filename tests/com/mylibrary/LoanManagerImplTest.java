package com.mylibrary;

import com.mylibrary.tools.DataSourceFactory;
import com.mylibrary.tools.DatabaseTools;
import org.apache.derby.database.Database;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Radim Kratochvil
 * @author Michael Le <lemichael@mail.muni.cz>
 */
public class LoanManagerImplTest {
    private LoanManagerImpl loanManager;
    private CustomerManagerImpl customerManager;
    private BookManagerImpl bookManager;

	private DataSource dataSource;

    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Before
    public void setUp() throws Exception {
		dataSource = DataSourceFactory.getDbcpMemoryDataSource();
        DatabaseTools.executeSqlFromFile(dataSource, "createBookTable.sql");
        DatabaseTools.executeSqlFromFile(dataSource, "createCustomerTable.sql");
        DatabaseTools.executeSqlFromFile(dataSource, "createLoanTable.sql");

		loanManager = new LoanManagerImpl(dataSource);
		customerManager = new CustomerManagerImpl(dataSource);
		bookManager = new BookManagerImpl(dataSource);
    }

    @After
    public void tearDown() throws SQLException {
		try (Connection connection = dataSource.getConnection()) {
			connection.prepareStatement("DROP TABLE LOAN").executeUpdate();
			connection.prepareStatement("DROP TABLE BOOK").executeUpdate();
			connection.prepareStatement("DROP TABLE CUSTOMER").executeUpdate();
		}
    }

    @Test
    public void createLoan() throws Exception {
        Customer customer = createCustomer();
        Book book = createBook();

        Loan loan = new Loan();
        loan.setCustomer(customer);
        loan.setBook(book);
        loan.setStartDate(getDate("2015-01-01 10:00:00"));
        loan.setEndDate(getDate("2015-02-01 12:00:00"));
        loan.setRealEndDate(null);
        loanManager.createLoan(loan);

        Long loanId = loan.getId();
        assertNotNull(loanId);
        Loan testedLoan = loanManager.findLoanById(loanId);

        assertLoanEquals(loan, testedLoan);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createLoanCustomerNull() throws Exception {
        Book book = createBook();

        Loan loan = new Loan();
        loan.setCustomer(null);
        loan.setBook(book);
        loan.setStartDate(getDate("2014-01-01"));
        loan.setEndDate(getDate("2014-02-01"));
        loan.setRealEndDate(null);
        loanManager.createLoan(loan);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createLoanBookNull() throws Exception {
        Customer customer = createCustomer();

        Loan loan = new Loan();
        loan.setCustomer(customer);
        loan.setStartDate(getDate("2014-01-01"));
        loan.setEndDate(getDate("2014-02-01"));
        loan.setRealEndDate(null);
        loanManager.createLoan(loan);
    }

    @Test
    public void updateLoan() throws Exception {
        Customer customer = createCustomer();
        Book book = createBook();

        String startDate = "2014-01-01";
        String endDate = "2014-02-01";

        Loan loan = new Loan();
        loan.setCustomer(customer);
        loan.setBook(book);
        loan.setStartDate(dateFormat.parse(startDate));
        loan.setEndDate(dateFormat.parse(endDate));
        loan.setRealEndDate(null);
        loanManager.createLoan(loan);
        Long loanId = loan.getId();

        String anotherStartDate = "2014-04-01";
        String anotherEndDate = "2014-05-01";

        Loan anotherLoan = new Loan();
        anotherLoan.setCustomer(customer);
        anotherLoan.setBook(book);
        anotherLoan.setStartDate(dateFormat.parse(anotherStartDate));
        anotherLoan.setEndDate(dateFormat.parse(anotherEndDate));
        anotherLoan.setRealEndDate(null);
        loanManager.createLoan(anotherLoan);
        Long anotherLoanId = anotherLoan.getId();

        // Customer
        Customer anotherCustomer = createAnotherCustomer();
        Loan testedLoan = loanManager.findLoanById(loanId);
        loan.setCustomer(anotherCustomer);
        testedLoan.setCustomer(anotherCustomer);
        loanManager.updateLoan(testedLoan);

        assertLoanEquals(loan, testedLoan);

        // Book
        Book anotherBook = createAnotherBook();
        testedLoan = loanManager.findLoanById(loanId);
        loan.setBook(anotherBook);
        testedLoan.setBook(anotherBook);
        loanManager.updateLoan(testedLoan);

        assertLoanEquals(loan, testedLoan);

        // Start Date
        String newStartDate = "2012-11-10";
        testedLoan = loanManager.findLoanById(loanId);
        loan.setStartDate(dateFormat.parse(newStartDate));
        testedLoan.setStartDate(dateFormat.parse(newStartDate));
        loanManager.updateLoan(testedLoan);

        assertLoanEquals(loan, testedLoan);

        // End Date
        String newEndDate = "2012-12-12";
        testedLoan = loanManager.findLoanById(loanId);
        loan.setEndDate(dateFormat.parse(newEndDate));
        testedLoan.setEndDate(dateFormat.parse(newEndDate));
        loanManager.updateLoan(testedLoan);

        assertLoanEquals(loan, testedLoan);

        // Real End Date
        String newRealEndDate = "2012-12-12";
        testedLoan = loanManager.findLoanById(loanId);
        loan.setRealEndDate(dateFormat.parse(newRealEndDate));
        testedLoan.setRealEndDate(dateFormat.parse(newRealEndDate));
        loanManager.updateLoan(testedLoan);

        assertLoanEquals(loan, testedLoan);

        // Another loan must be untouched
        assertLoanEquals(anotherLoan, loanManager.findLoanById(anotherLoanId));
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateLoanNull() {
        loanManager.updateLoan(null);
    }

    @Test(expected = IllegalArgumentException.class)
     public void updateLoanCustomerNull() throws Exception {
        Customer customer = createCustomer();
        Book book = createBook();

        String startDate = "2014-02-01";
        String endDate = "2014-03-01";

        Loan loan = new Loan();
        loan.setCustomer(customer);
        loan.setBook(book);
        loan.setStartDate(dateFormat.parse(startDate));
        loan.setEndDate(dateFormat.parse(endDate));
        loan.setRealEndDate(null);
        loanManager.createLoan(loan);
        Long loanId = loan.getId();

        // Customer
        Loan testedLoan = loanManager.findLoanById(loanId);
        testedLoan.setCustomer(null);
        loanManager.updateLoan(testedLoan);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateLoanBookNull() throws Exception {
        Customer customer = createCustomer();
        Book book = createBook();

        String startDate = "2014-02-01";
        String endDate = "2014-03-01";

        Loan loan = new Loan();
        loan.setCustomer(customer);
        loan.setBook(book);
        loan.setStartDate(dateFormat.parse(startDate));
        loan.setEndDate(dateFormat.parse(endDate));
        loan.setRealEndDate(null);
        loanManager.createLoan(loan);
        Long loanId = loan.getId();

        // Book
        Loan testedLoan = loanManager.findLoanById(loanId);
        testedLoan.setBook(null);
        loanManager.updateLoan(testedLoan);
    }

    @Test
    public void deleteLoan() throws Exception {
        Customer customer = createAnotherCustomer();
        Book book = createAnotherBook();

        String startDate = "2014-01-03";
        String endDate = "2014-02-03";

        Loan loan = new Loan();
        loan.setCustomer(customer);
        loan.setBook(book);
        loan.setStartDate(dateFormat.parse(startDate));
        loan.setEndDate(dateFormat.parse(endDate));
        loan.setRealEndDate(null);
        loanManager.createLoan(loan);
        assertNotNull(loanManager.findLoanById(loan.getId()));

        String anotherStartDate = "2014-04-01";
        String anotherEndDate = "2014-05-01";

        Loan anotherLoan = new Loan();
        anotherLoan.setCustomer(customer);
        anotherLoan.setBook(book);
        anotherLoan.setStartDate(dateFormat.parse(anotherStartDate));
        anotherLoan.setEndDate(dateFormat.parse(anotherEndDate));
        anotherLoan.setRealEndDate(null);
        loanManager.createLoan(anotherLoan);
        assertNotNull(loanManager.findLoanById(anotherLoan.getId()));

        loanManager.deleteLoan(loan);
        assertNull(loanManager.findLoanById(loan.getId()));
        assertNotNull(loanManager.findLoanById(anotherLoan.getId()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteLoanInvalid() throws Exception {
        loanManager.deleteLoan(null);
    }

    @Test
    public void findLoanById() throws Exception {
        Customer customer = createAnotherCustomer();
        Book book = createAnotherBook();

        String startDate = "2010-01-03";
        String endDate = "2010-02-03";

        Loan loan = new Loan();
        loan.setCustomer(customer);
        loan.setBook(book);
        loan.setStartDate(dateFormat.parse(startDate));
        loan.setEndDate(dateFormat.parse(endDate));
        loan.setRealEndDate(null);
        loanManager.createLoan(loan);
        Long loanId = loan.getId();

        Loan testedLoan = loanManager.findLoanById(loanId);
        assertNotNull(testedLoan);
        assertEquals(loanId, testedLoan.getId());
        assertLoanEquals(loan, testedLoan);
    }

    @Test
    public void findAllAvailableBooks() throws Exception {
        List<Book> reference = loanManager.findAllAvailableBooks();
        Book newBook = createAnotherBook();
        reference.add(newBook);

        List<Book> tested = loanManager.findAllAvailableBooks();

        Collections.sort(reference, BookManagerImplTest.idComparator);
        Collections.sort(tested, BookManagerImplTest.idComparator);

        for(int i = 0; i < reference.size(); i++) {
            Book b1 = reference.get(i);
            Book b2 = tested.get(i);

            assertEquals(b1.getId(), b2.getId());
        }
    }

    @Test
    public void findAllLendings() throws Exception {
        List<Loan> reference = loanManager.findAllLendings();

        Customer customer = createCustomer();
        Book book = createBook();

        Loan loan = new Loan();
        loan.setCustomer(customer);
        loan.setBook(book);
        loan.setStartDate(getDate("2009-01-03"));
        loan.setEndDate(getDate("2009-02-03"));
        loan.setRealEndDate(null);
        loanManager.createLoan(loan);
        reference.add(loan);

        List<Loan> tested = loanManager.findAllLendings();

        Collections.sort(reference, loanIdComparator);
        Collections.sort(tested, loanIdComparator);

        for(int i = 0; i < reference.size(); i++) {
            Loan l1 = reference.get(i);
            Loan l2 = tested.get(i);

            assertEquals(l1.getId(), l2.getId());
            assertLoanEquals(l1, l2);
        }
    }

    @Test
    public void findLendingsByCustomer() throws Exception {
        Customer customer = createCustomer();
        Book book = createBook();
        List<Loan> reference = loanManager.findLendingsByCustomer(customer);

        Loan loan = new Loan();
        loan.setCustomer(customer);
        loan.setBook(book);
        loan.setStartDate(getDate("2008-01-03"));
        loan.setEndDate(getDate("2008-02-03"));
        loan.setRealEndDate(null);
        loanManager.createLoan(loan);
        reference.add(loan);

        Loan anotherLoan = new Loan();
        anotherLoan.setCustomer(createAnotherCustomer());
        anotherLoan.setBook(book);
        anotherLoan.setStartDate(getDate("2008-04-03"));
        anotherLoan.setEndDate(getDate("2008-08-03"));
        anotherLoan.setRealEndDate(null);
        loanManager.createLoan(anotherLoan);

        List<Loan> tested = loanManager.findLendingsByCustomer(customer);

        Collections.sort(reference, loanIdComparator);
        Collections.sort(tested, loanIdComparator);

        for(int i = 0; i < reference.size(); i++) {
            Loan l1 = reference.get(i);
            Loan l2 = tested.get(i);

            assertEquals(l1.getId(), l2.getId());
            assertLoanEquals(l1, l2);
        }
    }

    private void assertLoanEquals(Loan loan1, Loan loan2) {
        assertEquals(loan1.getId(), loan2.getId());
        assertEquals(loan1.getCustomer(), loan2.getCustomer());
        assertEquals(loan1.getBook(), loan2.getBook());
        assertEquals(loan1.getStartDate(), loan2.getStartDate());
        assertEquals(loan1.getEndDate(), loan2.getEndDate());
        assertEquals(loan1.getRealEndDate(), loan2.getRealEndDate());
    }

    private Date getDate(String text) throws ParseException {
        return dateFormat.parse(text);
    }

    private Customer createCustomer() throws Exception {
        Customer customer = new Customer();
        customer.setIdCard("AB12345");
        customer.setName("Jan Novák");
        customer.setAddress("Novákova 10, 110 00 Praha");
        customer.setTelephone("+420 123 456 789");
        customer.setEmail("jan.novak@email.cz");
        customerManager.createCustomer(customer);

        return customer;
    }

    private Customer createAnotherCustomer() throws Exception {
        Customer customer = new Customer();
        customer.setIdCard("BA12300");
        customer.setName("Jana Nováková");
        customer.setAddress("Novákova 11, 110 00 Praha");
        customer.setTelephone("+420 123 654 789");
        customer.setEmail("jana.novakova@email.cz");
        customerManager.createCustomer(customer);

        return customer;
    }

    private Book createBook() throws Exception {
        Book book = new Book();
        book.setIsbn("978-80-7321-868-3");
        book.setName("Zvířátka");
        book.setAuthor("Davenport Maxine");
        book.setPublisher("Fortuna Libri");
        book.setYearOfPublication(2014);
        book.setLanguage("Czech");
        book.setPagesNumber(10);
        bookManager.createBook(book);

        return book;
    }

    private Book createAnotherBook() throws Exception {
        Book book = new Book();
        book.setIsbn("978-80-7321-868-4");
        book.setName("Zvířátka 2");
        book.setAuthor("Davenport Maxine");
        book.setPublisher("Fortuna Libri");
        book.setYearOfPublication(2015);
        book.setLanguage("Czech");
        book.setPagesNumber(12);
        bookManager.createBook(book);

        return book;
    }

    private static Comparator<Loan> loanIdComparator = new Comparator<Loan>() {
        @Override
        public int compare(Loan o1, Loan o2) {
            return o1.getId().compareTo(o2.getId());
        }
    };
}