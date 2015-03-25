package com.mylibrary;

import java.util.List;

/**
 * @author Radim Kratochvil
 * @author Michael Le <lemichael@mail.muni.cz>
 */
public interface LoanManager {
    void createLoan(Loan loan);
    void updateLoan(Loan loan);
    void deleteLoan(Loan loan);
    Loan findLoanById(Long id);
    List<Book> findAllAvailableBooks();
    List<Loan> findAllLendings();
    List<Loan> findLendingsByCustomer(Customer customer);
    List<Loan> findLendingsPastDue();
}
