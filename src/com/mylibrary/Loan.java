package com.mylibrary;

import java.util.Date;

/**
 * @author Radim Kratochvil
 * @author Michael Le <lemichael@mail.muni.cz>
 */
public class Loan {
    private Long id;
    private Customer customer;
    private Book book;
    private Date startDate;
    private Date endDate;
    private Date realEndDate;

    public Loan() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getRealEndDate() {
        return realEndDate;
    }

    public void setRealEndDate(Date realEndDate) {
        this.realEndDate = realEndDate;
    }

	@Override
	public String toString() {
		return "Loan ["
				+ "id: " + getId()
				+ ", customer: " + getCustomer()
				+ ", book: " + getBook()
				+ ", start date: " + getStartDate()
				+ ", end date: " + getEndDate()
				+ ", real end date: " + getRealEndDate()
				+ "]";
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Loan)) {
			return false;
		}

		final Loan second = (Loan)obj;
		return (this.id == second.id || (this.id != null && this.id.equals(second.id)));
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 23 * hash + (this.id != null ? this.id.hashCode() : 0);
		return hash;
	}
}
