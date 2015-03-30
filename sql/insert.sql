------------------------------------ CUSTOMER ------------------------------------

INSERT INTO CUSTOMER (IDCARD, NAME, ADDRESS, TELEPHONE, EMAIL)
VALUES ('idCard01', 'Petr01', 'Ceska 01', '123-466-789-01', 'xxx@yyy.01');
INSERT INTO CUSTOMER (IDCARD, NAME, ADDRESS, TELEPHONE, EMAIL)
VALUES ('idCard02', 'Petr02', 'Ceska 02', '123-466-789-02', 'xxx@yyy.02');
INSERT INTO CUSTOMER (IDCARD, NAME, ADDRESS, TELEPHONE, EMAIL)
VALUES ('idCard03', 'Petr03', 'Ceska 03', '123-466-789-03', 'xxx@yyy.03');

------------------------------------ BOOK ------------------------------------

INSERT INTO BOOK (ISBN, NAME, AUTHOR, PUBLISHER, "YEAR", LANGUAGE, PAGESNUMBER)
VALUES ('123456-01', 'TestBook.01', 'TestAuthor.01', 'Unknown-01', 1901, 'English', 251);
INSERT INTO BOOK (ISBN, NAME, AUTHOR, PUBLISHER, "YEAR", LANGUAGE, PAGESNUMBER)
VALUES ('123456-02', 'TestBook.02', 'TestAuthor.02', 'Unknown-02', 1902, 'English', 252);
INSERT INTO BOOK (ISBN, NAME, AUTHOR, PUBLISHER, "YEAR", LANGUAGE, PAGESNUMBER)
VALUES ('123456-03', 'TestBook.03', 'TestAuthor.03', 'Unknown-03', 1903, 'English', 253);

------------------------------------ LOAN ------------------------------------

INSERT INTO LOAN (IDCUSTOMER, IDBOOK, STARTDATE, ENDDATE, REALENDDATE)
VALUES (111, 221, '2015-04-21 10:00:00', '2015-05-21 10:00:00', '2015-05-18 09:27:14');
INSERT INTO LOAN (IDCUSTOMER, IDBOOK, STARTDATE, ENDDATE, REALENDDATE)
VALUES (112, 222, '2015-05-22 11:00:00', '2015-06-21 11:00:00', null);
INSERT INTO LOAN (IDCUSTOMER, IDBOOK, STARTDATE, ENDDATE, REALENDDATE)
VALUES (113, 223, '2015-06-23 12:00:00', '2015-07-21 12:00:00', null);