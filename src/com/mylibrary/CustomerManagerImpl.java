package com.mylibrary;

import java.util.List;

/**
 * @author Radim Kratochvil
 * @author Michael Le <lemichael@mail.muni.cz>
 */
public class CustomerManagerImpl implements CustomerManager {
    @Override
    public void createCustomer(Customer customer) {
        throw new UnsupportedOperationException();
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
