package com.mylibrary;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Radim Kratochvil
 * @author Michael Le <lemichael@mail.muni.cz>
 */
public class CustomerManagerImplTest {
    private CustomerManagerImpl customerManager;
    private DataSource dataSource;

	@Before
	public void setUp() throws SQLException, IOException {
		dataSource = DataSourceFactory.getDbcpMemoryDataSource();
		try (Connection connection = dataSource.getConnection()) {
			connection.prepareStatement("CREATE TABLE CUSTOMER (" +
					"ID BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY," +
					"IDCARD VARCHAR(50) NOT NULL," +
					"\"NAME\" VARCHAR(50) NOT NULL," +
					"ADDRESS VARCHAR(50) NOT NULL," +
					"TELEPHONE VARCHAR(50) NOT NULL," +
					"EMAIL VARCHAR(50) NOT NULL" +
					")").executeUpdate();
		}
		customerManager = new CustomerManagerImpl(dataSource);
	}

	@After
	public void tearDown() throws SQLException {
		try (Connection connection = dataSource.getConnection()) {
			connection.prepareStatement("DROP TABLE CUSTOMER").executeUpdate();
		}
	}

    @Test
    public void createCustomer() throws Exception {
        Customer customer = new Customer();
        customer.setIdCard("AB12345");
        customer.setName("Jan Novák");
        customer.setAddress("Novákova 10, 110 00 Praha");
        customer.setTelephone("+420 123 456 789");
        customer.setEmail("jan.novak@email.cz");
        customerManager.createCustomer(customer);

        Long customerId = customer.getId();
        assertNotNull(customerId);
        Customer testedCustomer = customerManager.findCustomerById(customerId);

        assertCustomerEquals(customer, testedCustomer);
    }

    @Test
    public void updateCustomer() throws Exception {
        Customer customer = new Customer();
        customer.setIdCard("AB23456");
        customer.setName("Honza Novák");
        customer.setAddress("Novákova 12, 110 00 Praha");
        customer.setTelephone("+420 123 123 123");
        customer.setEmail("honzik.novak@email.cz");
        customerManager.createCustomer(customer);
        Long customerId = customer.getId();

        Customer anotherCustomer = new Customer();
		anotherCustomer.setIdCard("AX87654");
		anotherCustomer.setName("Josef Fesoj");
		anotherCustomer.setAddress("Novákova 7, 110 00 Praha");
		anotherCustomer.setTelephone("+420 111 222 333");
		anotherCustomer.setEmail("josef.fesoj@email.cz");
        customerManager.createCustomer(anotherCustomer);
        Long anotherCustomerId = anotherCustomer.getId();

        // ID card
        Customer testedCustomer = customerManager.findCustomerById(customerId);
        customer.setIdCard("DE54321");
        testedCustomer.setIdCard("DE54321");
        customerManager.updateCustomer(testedCustomer);

        testedCustomer = customerManager.findCustomerById(customerId);
        assertCustomerEquals(customer, testedCustomer);

        // Name
        testedCustomer = customerManager.findCustomerById(customerId);
        customer.setName("Jana Nováková");
        testedCustomer.setName("Jana Nováková");
        customerManager.updateCustomer(testedCustomer);

        testedCustomer = customerManager.findCustomerById(customerId);
        assertCustomerEquals(customer, testedCustomer);

        // Address
        testedCustomer = customerManager.findCustomerById(customerId);
        customer.setAddress("Pražská 10, 111 11 Novákovice");
        testedCustomer.setAddress("Pražská 10, 111 11 Novákovice");
        customerManager.updateCustomer(testedCustomer);

        testedCustomer = customerManager.findCustomerById(customerId);
        assertCustomerEquals(customer, testedCustomer);

        // Telephone
        testedCustomer = customerManager.findCustomerById(customerId);
        customer.setTelephone("+421 987 654 321");
        testedCustomer.setTelephone("+421 987 654 321");
        customerManager.updateCustomer(testedCustomer);

        testedCustomer = customerManager.findCustomerById(customerId);
        assertCustomerEquals(customer, testedCustomer);

        // E-mail
        testedCustomer = customerManager.findCustomerById(customerId);
        customer.setEmail("email@jan.novak.cz");
        testedCustomer.setEmail("email@jan.novak.cz");
        customerManager.updateCustomer(testedCustomer);

        testedCustomer = customerManager.findCustomerById(customerId);
        assertCustomerEquals(customer, testedCustomer);

        // Another customer must be untouched
        assertCustomerEquals(anotherCustomer, customerManager.findCustomerById(anotherCustomerId));
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateCustomerIncorrect() {
        customerManager.updateCustomer(null);
    }

    @Test
    public void deleteCustomer() throws Exception {
        Customer customer1 = new Customer();
        customer1.setIdCard("BA21546");
        customer1.setName("Yoko Ono");
        customer1.setAddress("Novákova 40, 110 00 Praha");
        customer1.setTelephone("+420 777 666 555");
        customer1.setEmail("yoko.ono@beatles.com");
        customerManager.createCustomer(customer1);
        assertNotNull(customerManager.findCustomerById(customer1.getId()));

        Customer customer2 = new Customer();
        customer2.setIdCard("BA21547");
        customer2.setName("John Lemon");
        customer2.setAddress("Novákova 40, 110 00 Praha");
        customer2.setTelephone("+420 777 666 556");
        customer2.setEmail("lemonade@beatles.com");
        customerManager.createCustomer(customer2);
        assertNotNull(customerManager.findCustomerById(customer2.getId()));

        customerManager.deleteCustomer(customer1);
        assertNull(customerManager.findCustomerById(customer1.getId()));
        assertNotNull(customerManager.findCustomerById(customer2.getId()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteCustomerInvalid() throws Exception {
        customerManager.deleteCustomer(null);
    }

    @Test
    public void findAllCustomers() throws Exception {
        List<Customer> reference = customerManager.findAllCustomers();

        Customer customer1 = new Customer();
        customer1.setIdCard("12ABABA");
        customer1.setName("Pavel Levak");
        customer1.setAddress("Novákova 21, 110 00 Praha");
        customer1.setTelephone("+420 111 111 111");
        customer1.setEmail("left@paul.cz");
        customerManager.createCustomer(customer1);

        Customer customer2 = new Customer();
        customer2.setIdCard("21BABAB");
        customer2.setName("Pavel Pravak");
        customer2.setAddress("Novákova 12, 110 00 Praha");
        customer2.setTelephone("+420 222 222 222");
        customer2.setEmail("right@paul.cz");
        customerManager.createCustomer(customer2);

        reference.add(customer1);
        reference.add(customer2);

        List<Customer> tested = customerManager.findAllCustomers();

        Collections.sort(reference, customerIdComparator);
        Collections.sort(tested, customerIdComparator);

        for(int i = 0; i < reference.size(); i++) {
            Customer c1 = reference.get(i);
            Customer c2 = tested.get(i);

            assertEquals(c1.getId(), c2.getId());
            assertCustomerEquals(c1, c2);
        }
    }

    @Test
    public void findCustomerById() throws Exception {
        Customer customer = new Customer();
        customer.setIdCard("RATATATA");
        customer.setName("Štěpán Novotný");
        customer.setAddress("Novákova 90, 110 00 Praha");
        customer.setTelephone("+420 123 321 123");
        customer.setEmail("ratatata@email.cz");
        customerManager.createCustomer(customer);
        Long customerId = customer.getId();

        Customer testedCustomer = customerManager.findCustomerById(customerId);
        assertNotNull(testedCustomer);
        assertEquals(customerId, testedCustomer.getId());
        assertCustomerEquals(customer, testedCustomer);
    }

    @Test
    public void findCustomerByName() throws Exception {
        List<Customer> reference = new ArrayList<>();

		Customer customer1 = new Customer();
		customer1.setIdCard("12ABABA");
		customer1.setName("Pavel Levak");
		customer1.setAddress("Novákova 21, 110 00 Praha");
		customer1.setTelephone("+420 111 111 111");
		customer1.setEmail("left@paul.cz");
		customerManager.createCustomer(customer1);

		Customer customer2 = new Customer();
		customer2.setIdCard("21BABAB");
		customer2.setName("Pavel Pravak");
		customer2.setAddress("Novákova 12, 110 00 Praha");
		customer2.setTelephone("+420 222 222 222");
		customer2.setEmail("right@paul.cz");
		customerManager.createCustomer(customer2);

		Customer customer3 = new Customer();
		customer3.setIdCard("21BABAB");
		customer3.setName("Pavel Levak");
		customer3.setAddress("Novákova 12, 110 00 Praha");
		customer3.setTelephone("+420 222 222 222");
		customer3.setEmail("right@paul.cz");
		customerManager.createCustomer(customer3);

		reference.add(customer1);
		reference.add(customer3);

		List<Customer> tested = customerManager.findCustomerByName(customer1.getName());

		Collections.sort(reference, customerIdComparator);
		Collections.sort(tested, customerIdComparator);

		for(int i = 0; i < reference.size(); i++) {
			Customer c1 = reference.get(i);
			Customer c2 = tested.get(i);

			assertEquals(c1.getId(), c2.getId());
			assertCustomerEquals(c1, c2);
		}
    }


    private void assertCustomerEquals(Customer customer1, Customer customer2) {
        assertEquals(customer1.getId(), customer2.getId());
        assertEquals(customer1.getIdCard(), customer2.getIdCard());
        assertEquals(customer1.getName(), customer2.getName());
        assertEquals(customer1.getEmail(), customer2.getEmail());
        assertEquals(customer1.getAddress(), customer2.getAddress());
        assertEquals(customer1.getEmail(), customer2.getEmail());
    }

    private static Comparator<Customer> customerIdComparator = new Comparator<Customer>() {
        @Override
        public int compare(Customer o1, Customer o2) {
            return o1.getId().compareTo(o2.getId());
        }
    };
}