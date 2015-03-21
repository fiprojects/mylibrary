package com.mylibrary;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;

public class DbTest {
    public static void main(String[] args) throws SQLException, IOException {
        // Pouziti DataSource
        DataSource dataSource = (DataSource) DataSourceFactory.getDataSource();
        try(Connection connection = dataSource.getConnection()) {
            String query = "SELECT \"NAME\", AUTHOR FROM BOOK";
            try(PreparedStatement statement = connection.prepareStatement(query)) {
                try(ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        System.out.println(result.getString(1));
                        System.out.println(result.getString(2));
                    }
                }
            }
        }
    }
}
