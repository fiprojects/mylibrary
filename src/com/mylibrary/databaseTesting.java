package com.mylibrary;

import java.sql.*;

public class databaseTesting {

    public static void main(String[] args) throws SQLException{

        String url="jdbc:derby://derby.michael-le.net:1527/mylibrary";
		String userPass = "mylibrary";
        Connection conn = DriverManager.getConnection(url, userPass, userPass);

		Statement st = conn.createStatement();

		ResultSet resultSet = st.executeQuery("SELECT * FROM CUSTOMER");
		while (resultSet.next()) {
			Long id = resultSet.getLong(1);
			String idcard = resultSet.getString(2);
			String name = resultSet.getString(3);
			String address = resultSet.getString(4);
			String telephone = resultSet.getString(5);
			String email = resultSet.getString(6);
			System.out.println("id = " + id + ", idcard = " + idcard + ", address = " + address + ", telephone = " + telephone
			+ ", email = " + email);
		}

		resultSet = st.executeQuery("SELECT * FROM BOOK");
		while (resultSet.next()) {
			Long id = resultSet.getLong(1);
			String idcard = resultSet.getString(2);
			String name = resultSet.getString(3);
			String address = resultSet.getString(4);
			String telephone = resultSet.getString(5);
			String email = resultSet.getString(6);
			System.out.println("id = " + id + ", idcard = " + idcard + ", address = " + address + ", telephone = " + telephone
					+ ", email = " + email);
		}

        conn.close();
    }
}
