package com.mylibrary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Radim Kratochvil
 * @author Michael Le <lemichael@mail.muni.cz>
 */
public class BookManagerImpl implements BookManager {
    private final DataSource dataSource;
    private final static Logger log = LoggerFactory.getLogger(BookManagerImpl.class);

    public BookManagerImpl(DataSource dataSource) { this.dataSource = dataSource; }

    @Override
    public void createBook(Book book) throws ServiceFailureException {
        checkBook(book);
        if(book.getId() != null) {
            throw new IllegalArgumentException("Book ID is already set.");
        }

        String query = "INSERT INTO book(isbn, \"NAME\", author, publisher, \"YEAR\", language, pagesNumber) "
                + " VALUES(?, ?, ?, ?, ?, ?, ?)";
        try(
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)
        ) {
            statement.setString(1, book.getIsbn());
            statement.setString(2, book.getName());
            statement.setString(3, book.getAuthor());
            statement.setString(4, book.getPublisher());
            statement.setInt(5, book.getYearOfPublication());
            statement.setString(6, book.getLanguage());
            statement.setInt(7, book.getPagesNumber());

            int addedRows = statement.executeUpdate();
            if(addedRows == 0) {
                throw new ServiceFailureException("Book was not inserted: " + book);
            }
            if(addedRows > 1) {
                throw new ServiceFailureException("Internal error! More books added than expected: " + book);
            }

            ResultSet result = statement.getGeneratedKeys();
            book.setId(getGeneratedId(result, book));
        } catch(SQLException e) {
            String msg = "Database connection error.";
            log.error(msg, e);
            throw new ServiceFailureException(msg, e);
        }
    }

    @Override
    public void updateBook(Book book) throws ServiceFailureException {
        checkBook(book);
        if(book.getId() == null) {
            throw new IllegalArgumentException("Book ID is not set.");
        }

        String query = "UPDATE book SET isbn = ?, \"NAME\" = ?, author = ?, publisher = ?, \"YEAR\" = ?, language = ?,"
                + "pagesNumber = ? WHERE id = ?";
        try(
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)
        ) {
            statement.setString(1, book.getIsbn());
            statement.setString(2, book.getName());
            statement.setString(3, book.getAuthor());
            statement.setString(4, book.getPublisher());
            statement.setInt(5, book.getYearOfPublication());
            statement.setString(6, book.getLanguage());
            statement.setInt(7, book.getPagesNumber());
            statement.setLong(8, book.getId());

            int affectedRows = statement.executeUpdate();
            if(affectedRows == 0) {
                throw new ServiceFailureException("Book was not updated: " + book);
            }
            if(affectedRows > 1) {
                throw new ServiceFailureException("Internal error! More books updated than expected: " + book);
            }
        } catch(SQLException e) {
            String msg = "Database connection error.";
            log.error(msg, e);
            throw new ServiceFailureException(msg, e);
        }
    }

    @Override
    public void deleteBook(Book book) throws ServiceFailureException {
        if(book == null) {
            throw new IllegalArgumentException("Book is null.");
        }
        if(book.getId() == null) {
            throw new IllegalArgumentException("Book ID is not set.");
        }

        String query = "DELETE FROM book WHERE id = ?";
        try(
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)
        ) {
            statement.setLong(1, book.getId());

            int affectedRows = statement.executeUpdate();
            if(affectedRows == 0) {
                throw new ServiceFailureException("Book was not deleted: " + book);
            }
            if(affectedRows > 1) {
                throw new ServiceFailureException("Internal error! More books deleted than expected: " + book);
            }
        } catch(SQLException e) {
            String msg = "Database connection error.";
            log.error(msg, e);
            throw new ServiceFailureException(msg, e);
        }
    }

    @Override
    public List<Book> findAllBooks() throws ServiceFailureException {
        String query = getBaseSelectString();
        try(
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)
        ) {
            ResultSet result = statement.executeQuery();
            List<Book> books = new ArrayList<>();

            while(result.next()) {
                books.add(resultToBook(result));
            }

            return books;
        } catch(SQLException e) {
            String msg = "Database connection error.";
            log.error(msg, e);
            throw new ServiceFailureException(msg, e);
        }
    }

    @Override
    public Book findBookById(Long id) throws ServiceFailureException {
        String query = getBaseSelectString() + " WHERE id = ?";
        try(
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)
        ) {
            statement.setLong(1, id);

            ResultSet result = statement.executeQuery();
            if(result.next()) {
                Book book = resultToBook(result);
                if(result.next()) {
                    throw new ServiceFailureException("More books with the same ID was found: " + book);
                }

                return book;
            }

            return null;
        } catch(SQLException e) {
            String msg = "Database connection error.";
            log.error(msg, e);
            throw new ServiceFailureException(msg, e);
        }
    }

    @Override
    public List<Book> findBookByName(String name) throws ServiceFailureException {
        String query = getBaseSelectString() + " WHERE \"NAME\" = ?";
        try(
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)
        ) {
            statement.setString(1, name);

            ResultSet result = statement.executeQuery();
            List<Book> books = new ArrayList<>();

            while(result.next()) {
                books.add(resultToBook(result));
            }

            return books;
        } catch(SQLException e) {
            String msg = "Database connection error.";
            log.error(msg, e);
            throw new ServiceFailureException(msg, e);
        }
    }

    @Override
    public List<Book> findBookByAuthor(String author) throws ServiceFailureException {
        String query = getBaseSelectString() + " WHERE author = ?";
        try(
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)
        ) {
            statement.setString(1, author);

            ResultSet result = statement.executeQuery();
            List<Book> books = new ArrayList<>();

            while(result.next()) {
                books.add(resultToBook(result));
            }

            return books;
        } catch(SQLException e) {
            String msg = "Database connection error.";
            log.error(msg, e);
            throw new ServiceFailureException(msg, e);
        }
    }

    private void checkBook(Book book) {
        if(book == null) {
            throw new IllegalArgumentException("Book is null.");
        }
        if(book.getIsbn() == null) {
            throw new IllegalArgumentException("Book ISBN is not set.");
        }
        if(book.getName() == null) {
            throw new IllegalArgumentException("Book Name is not set.");
        }
        if(book.getAuthor() == null) {
            throw new IllegalArgumentException("Book Author is not set.");
        }
        if(book.getPublisher() == null) {
            throw new IllegalArgumentException("Book Publisher is not set.");
        }
        if(book.getYearOfPublication() < 0) {
            throw new IllegalArgumentException("Book Year of Publication is invalid.");
        }
        if(book.getLanguage() == null) {
            throw new IllegalArgumentException("Book Language is not set.");
        }
        if(book.getPagesNumber() < 1) {
            throw new IllegalArgumentException("Book Pages Number is not set.");
        }
    }

    private Book resultToBook(ResultSet result) throws SQLException {
        Book book = new Book();
        book.setId(result.getLong("id"));
        book.setIsbn(result.getString("isbn"));
        book.setName(result.getString("name"));
        book.setAuthor(result.getString("author"));
        book.setPublisher(result.getString("publisher"));
        book.setYearOfPublication(result.getInt("year"));
        book.setLanguage(result.getString("language"));
        book.setPagesNumber(result.getInt("pagesNumber"));

        return book;
    }

    private String getBaseSelectString() {
        return "SELECT id, isbn, \"NAME\", author, publisher, \"YEAR\", language, pagesNumber FROM book";
    }

    private Long getGeneratedId(ResultSet result, Book book) throws SQLException, ServiceFailureException {
        if(result.next()) {
            if(result.getMetaData().getColumnCount() != 1) {
                throw new ServiceFailureException("Invalid number of generated keys of the inserted book cannot be"
                        + "fetched; book" + book);
            }

            Long id = result.getLong(1);

            if(result.next()) {
                throw new ServiceFailureException("Too many generated keys of the inserted book found; book" + book);
            }

            return id;
        } else {
            throw new ServiceFailureException("Generated key of the inserted book cannot be fetched; book: " + book);
        }
    }
}
