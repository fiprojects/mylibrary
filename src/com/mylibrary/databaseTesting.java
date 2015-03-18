package com.mylibrary;

import java.sql.*;

public class databaseTesting {

    public static void main(String[] args) throws SQLException{

        String url="jdbc:derby:db;create=true";
        Connection conn = DriverManager.getConnection(url);

		Statement st = conn.createStatement();
		st.execute("DROP TABLE CUSTOMER");
		st.execute("CREATE TABLE CUSTOMER (" +
				"ID BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY," +
				"IDCARD VARCHAR(50)," +
				"ADDRESS VARCHAR(50)," +
				"TELEPHONE VARCHAR(50)," +
				"EMAIL VARCHAR(50))");
		st.execute("INSERT INTO CUSTOMER (IDCARD, ADDRESS, TELEPHONE, EMAIL) VALUES " +
				"('test123', 'brno', '123456', 'xxx@yyy.zzz')");
		st.execute("INSERT INTO CUSTOMER (IDCARD, ADDRESS, TELEPHONE, EMAIL) VALUES " +
				"('testDruhy', 'Praha', '999999', 'aaa@bbb.ccc')");

		ResultSet resultSet = st.executeQuery("SELECT * FROM CUSTOMER");
		while (resultSet.next()) {
			Long id = resultSet.getLong(1);
			String idcard = resultSet.getString(2);
			String address = resultSet.getString(3);
			String telephone = resultSet.getString(4);
			String email = resultSet.getString(5);
			System.out.println("id = " + id + ", idcard = " + idcard + ", address = " + address + ", telephone = " + telephone
			+ ", email = " + email);
		}

        conn.close();
    }
}
