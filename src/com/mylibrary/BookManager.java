package com.mylibrary;

import java.sql.SQLException;
import java.util.List;

/**
 * @author Radim Kratochvil
 * @author Michael Le <lemichael@mail.muni.cz>
 */
public interface BookManager {
    void createBook(Book book) throws ServiceFailureException;
    void updateBook(Book book) throws ServiceFailureException;
    void deleteBook(Book book) throws ServiceFailureException;
    List<Book> findAllBooks() throws ServiceFailureException;
    Book findBookById(Long id) throws ServiceFailureException;
    List<Book> findBookByName(String name) throws ServiceFailureException;
    List<Book> findBookByAuthor(String author) throws ServiceFailureException;
}
