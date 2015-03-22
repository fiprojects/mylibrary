package com.mylibrary;

import java.util.List;

/**
 * @author Radim Kratochvil
 * @author Michael Le <lemichael@mail.muni.cz>
 */
public interface CustomerManager {
    void createCustomer(Customer customer) throws ServiceFailureException;
    void updateCustomer(Customer customer) throws ServiceFailureException;
    void deleteCustomer(Customer customer) throws ServiceFailureException;
    List<Customer> findAllCustomers() throws ServiceFailureException;
    Customer findCustomerById(Long id) throws ServiceFailureException;
	List<Customer> findCustomerByName(String name) throws ServiceFailureException;
}
