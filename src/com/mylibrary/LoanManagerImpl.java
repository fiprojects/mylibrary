package com.mylibrary;

import java.util.List;

/**
 * @author Radim Kratochvil
 * @author Michael Le <lemichael@mail.muni.cz>
 */
public class LoanManagerImpl implements LoanManager {
    @Override
    public void createLoan(Loan loan) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateLoan(Loan loan) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteLoan(Loan loan) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Book> findAllAvailableBooks() {
        return null;
    }

    @Override
    public List<Loan> findAllLendings() {
        return null;
    }

    @Override
    public List<Loan> findLendingsByCustomer(Customer customer) {
        return null;
    }

    @Override
    public List<Loan> findLendingsPastDue() {
        return null;
    }
}
