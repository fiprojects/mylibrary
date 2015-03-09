package com.mylibrary;

import java.util.List;

/**
 * @author Radim Kratochvil
 * @author Michael Le <lemichael@mail.muni.cz>
 */
public interface BookManager {
    void createBook(Book book);
    void updateBook(Book book);
    void deleteBook(Book book);
    List<Book> findAllBooks();
    Book findBookById(Long id);
    List<Book> findBookByName(String name);
    List<Book> findBookByAuthor(String author);
}
