package com.mylibrary;

import java.sql.*;

/**
 * Created by xkratoc5 on 17.3.15.
 */
public class test {

    public static void main(String[] args) throws SQLException{

        String url="jdbc:derby:db;create=true";
        Connection conn = DriverManager.getConnection(url);

        conn.close();
    }
}
