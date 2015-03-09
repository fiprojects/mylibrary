package com.mylibrary;

import java.util.List;

/**
 * @author Radim Kratochvil
 * @author Michael Le <lemichael@mail.muni.cz>
 */
public class BookManagerImpl implements BookManager {
    @Override
    public void createBook(Book book) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBook(Book book) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteBook(Book book) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Book> findAllBooks() { return null; }

    @Override
    public Book findBookById(Long id) {
        return null;
    }

    @Override
    public List<Book> findBookByName(String name) {
        return null;
    }

    @Override
    public List<Book> findBookByAuthor(String author) {
        return null;
    }
}
