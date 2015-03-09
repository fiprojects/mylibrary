package com.mylibrary;

import java.util.List;

/**
 * @author Radim Kratochvil
 * @author Michael Le <lemichael@mail.muni.cz>
 */
public interface CustomerManager {
    void createCustomer(Customer customer);
    void updateCustomer(Customer customer);
    void deleteCustomer(Customer customer);
    List<Customer> findAllCustomers();
    Customer findCustomerById(Long id);
    Customer findCustomerByName(String name);
}
