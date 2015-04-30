package com.mylibrary;

import com.mylibrary.tools.DataSourceFactory;
import com.mylibrary.tools.DatabaseTools;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Radim Kratochvil
 * @author Michael Le <lemichael@mail.muni.cz>
 */
public class BookManagerImplTest {
	private BookManagerImpl manager;
    private DataSource dataSource;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Before
	public void setUp() throws SQLException, IOException {
		dataSource = DataSourceFactory.getMemoryDataSource();
		DatabaseTools.executeSqlFromFile(dataSource, "createBookTable.sql");
		manager = new BookManagerImpl(dataSource);
	}

	@After
	public void tearDown() throws SQLException {
		try (Connection connection = dataSource.getConnection()) {
			connection.prepareStatement("DROP TABLE BOOK").executeUpdate();
		}
	}

	@Test
	public void testCreateBook(){
		Book book = newBook("isbn", "Name", "Author", "Publisher", 2015, "English", 255);
		manager.createBook(book);
		assertDeepEquals(book, manager.findBookById(book.getId()));
	}

	@Test
	public void testCreateBookWithNullBook(){
		exception.expect(IllegalArgumentException.class);
		manager.createBook(null);
	}

	@Test
	public void testCreateBookWithNullId(){
		Book book = newBook(null, "Name", "Author", "Publisher", 2015, "English", 255);
		exception.expect(IllegalArgumentException.class);
		manager.createBook(book);
	}

	@Test
	public void testCreateBookWithNullName(){
		Book book = newBook("isbn", null, "Author", "Publisher", 2015, "English", 255);
		exception.expect(IllegalArgumentException.class);
		manager.createBook(book);
	}

	@Test
	public void testCreateBookWithNullAuthor(){
		Book book = newBook("isbn", "Name", null, "Publisher", 2015, "English", 255);
		exception.expect(IllegalArgumentException.class);
		manager.createBook(book);
	}

	@Test
	public void testCreateBookWithNullPublisher(){
		Book book = newBook("isbn", "Name", "Author", null, 2015, "English", 255);
		exception.expect(IllegalArgumentException.class);
		manager.createBook(book);
	}

	@Test
	public void testCreateBookWithWrongYearOfPublication(){
		Book book = newBook("isbn", "Name", "Author", "Publisher", -1, "English", 255);
		exception.expect(IllegalArgumentException.class);
		manager.createBook(book);
	}

	@Test
	public void testCreateBookWithNullLanguage(){
		Book book = newBook("isbn", "Name", "Author", "Publisher", 2015, null, 255);
		exception.expect(IllegalArgumentException.class);
		manager.createBook(book);
	}

	@Test
	public void testCreateBookWithWrongPagesNumber(){
		Book book = newBook("isbn", "Name", "Author", "Publisher", 2015, "English", -1);
		exception.expect(IllegalArgumentException.class);
		manager.createBook(book);
	}

	@Test
	public void testUpdateBook(){
		Book book = newBook("isbn", "Name", "Author", "Publisher", 2015, "English", 255);
		manager.createBook(book);

		book.setName("Test Name");
		book.setLanguage("Czech");
		manager.updateBook(book);
		assertDeepEquals(book, manager.findBookById(book.getId()));

		book.setAuthor("Test Author");
		book.setPagesNumber(552);
		manager.updateBook(book);
		assertDeepEquals(book, manager.findBookById(book.getId()));
	}

	@Test
	public void testUpdateBookWithNullBook(){
		exception.expect(IllegalArgumentException.class);
		manager.updateBook(null);
	}

	@Test
	public void testUpdateBookWithNullId(){
		Book book = newBook("isbn", "Name", "Author", "Publisher", 2015, "English", 255);
		manager.createBook(book);

		book.setId(null);
		exception.expect(IllegalArgumentException.class);
		manager.updateBook(book);
	}

	@Test
	public void testUpdateBookWithWrongId(){
		Book book = newBook("isbn", "Name", "Author", "Publisher", 2015, "English", 255);
		manager.createBook(book);

		book.setId(book.getId() + 5);
		exception.expect(ServiceFailureException.class);
		manager.updateBook(book);
	}

	@Test
	public void testUpdateBookWithNullName(){
		Book book = newBook("isbn", "Name", "Author", "Publisher", 2015, "English", 255);
		manager.createBook(book);

		book.setName(null);
		exception.expect(IllegalArgumentException.class);
		manager.updateBook(book);
	}

	@Test
	public void testUpdateBookWithNullAuthor(){
		Book book = newBook("isbn", "Name", "Author", "Publisher", 2015, "English", 255);
		manager.createBook(book);

		book.setAuthor(null);
		exception.expect(IllegalArgumentException.class);
		manager.updateBook(book);
	}

	@Test
	public void testUpdateBookWithNullPublisher(){
		Book book = newBook("isbn", "Name", "Author", "Publisher", 2015, "English", 255);
		manager.createBook(book);

		book.setPublisher(null);
		exception.expect(IllegalArgumentException.class);
		manager.updateBook(book);
	}

	@Test
	public void testUpdateBookWithWrongYearOfPublication(){
		Book book = newBook("isbn", "Name", "Author", "Publisher", 2015, "English", 255);
		manager.createBook(book);

		book.setYearOfPublication(-1);
		exception.expect(IllegalArgumentException.class);
		manager.updateBook(book);
	}

	@Test
	public void testUpdateBookWithNullLanguage(){
		Book book = newBook("isbn", "Name", "Author", "Publisher", 2015, "English", 255);
		manager.createBook(book);

		book.setLanguage(null);
		exception.expect(IllegalArgumentException.class);
		manager.updateBook(book);
	}

	@Test
	public void testUpdateBookWithWrongPagesNumber(){
		Book book = newBook("isbn", "Name", "Author", "Publisher", 2015, "English", 255);
		manager.createBook(book);

		book.setPagesNumber(-1);
		exception.expect(IllegalArgumentException.class);
		manager.updateBook(book);
	}

	@Test
	public void testDeleteBook(){
		Book book = newBook("isbn", "Name", "Author", "Publisher", 2015, "English", 255);
		manager.createBook(book);

		manager.deleteBook(book);
		assertNull(manager.findBookById(book.getId()));
	}

	@Test
	public void testDeleteBookWithNullBook(){
		Book book = newBook("isbn", "Name", "Author", "Publisher", 2015, "English", 255);
		manager.createBook(book);

		exception.expect(IllegalArgumentException.class);
		manager.deleteBook(null);
	}

	@Test
	public void testDeleteBookWithWrongId(){
		Book book = newBook("isbn", "Name", "Author", "Publisher", 2015, "English", 255);
		manager.createBook(book);

		book.setId(book.getId() + 5);
		exception.expect(ServiceFailureException.class);
		manager.deleteBook(book);
	}

	@Test
	public void testFindAllBooks(){
		Book book1 = newBook("isbn", "Name", "Author", "Publisher", 2015, "English", 255);
		Book book2 = newBook("isbn2", "Name2", "Author2", "Publisher2", 2015, "English", 255);
		manager.createBook(book1);
		manager.createBook(book2);

		assertDeepEquals(Arrays.asList(book1, book2), manager.findAllBooks());
	}

	@Test
	public void testFindBookById(){
		Book book1 = newBook("isbn", "Name", "Author", "Publisher", 2015, "English", 255);
		Book book2 = newBook("isbn2", "Name2", "Author2", "Publisher2", 2015, "English", 255);
		manager.createBook(book1);
		manager.createBook(book2);

		assertDeepEquals(book2, manager.findBookById(book2.getId()));
		assertDeepEquals(book1, manager.findBookById(book1.getId()));
	}

	@Test
	public void testFindBookByName(){
		Book book1 = newBook("isbn", "Name", "Author", "Publisher", 2015, "English", 255);
		Book book2 = newBook("isbn2", "Name2", "Author2", "Publisher2", 2015, "English", 255);
		Book book3 = newBook("isbn3", "Name", "Author3", "Publisher3", 2015, "English", 255);
		manager.createBook(book1);
		manager.createBook(book2);
		manager.createBook(book3);

		assertDeepEquals(Arrays.asList(book1, book3), manager.findBookByName(book1.getName()));
		assertDeepEquals(Arrays.asList(book2), manager.findBookByName(book2.getName()));
	}

	@Test
	public void testFindBookByAuthor(){
		Book book1 = newBook("isbn", "Name", "Author", "Publisher", 2015, "English", 255);
		Book book2 = newBook("isbn2", "Name2", "Author2", "Publisher2", 2015, "English", 255);
		Book book3 = newBook("isbn3", "Name3", "Author", "Publisher3", 2015, "English", 255);
		manager.createBook(book1);
		manager.createBook(book2);
		manager.createBook(book3);

		assertDeepEquals(Arrays.asList(book1, book3), manager.findBookByAuthor(book1.getAuthor()));
		assertDeepEquals(Arrays.asList(book2), manager.findBookByAuthor(book2.getAuthor()));
	}

	private Book newBook(String isbn, String name, String author, String publisher,
						 int yearOfPublication, String language, int pagesNumber){
		Book book = new Book();
		book.setId(null);
		book.setIsbn(isbn);
		book.setName(name);
		book.setAuthor(author);
		book.setPublisher(publisher);
		book.setYearOfPublication(yearOfPublication);
		book.setLanguage(language);
		book.setPagesNumber(pagesNumber);
		return book;
	}

	private void assertDeepEquals(Book expectedBook, Book actualBook) {
		assertEquals(expectedBook.getId(), actualBook.getId());
		assertEquals(expectedBook.getIsbn(), actualBook.getIsbn());
		assertEquals(expectedBook.getName(), actualBook.getName());
		assertEquals(expectedBook.getAuthor(), actualBook.getAuthor());
		assertEquals(expectedBook.getPublisher(), actualBook.getPublisher());
		assertEquals(expectedBook.getYearOfPublication(), actualBook.getYearOfPublication());
		assertEquals(expectedBook.getLanguage(), actualBook.getLanguage());
		assertEquals(expectedBook.getPagesNumber(), actualBook.getPagesNumber());
	}

	private void assertDeepEquals(List<Book> expectedList, List<Book> actualList) {
		Collections.sort(expectedList,idComparator);
		Collections.sort(actualList, idComparator);

		for (int i = 0; i < expectedList.size(); i++) {
			Book expectedBook = expectedList.get(i);
			Book actualBook = actualList.get(i);
			assertDeepEquals(expectedBook, actualBook);
		}
	}

	public static Comparator<Book> idComparator = new Comparator<Book>() {

		@Override
		public int compare(Book book1, Book book2) {
			return book1.getId().compareTo(book2.getId());
		}
	};
}