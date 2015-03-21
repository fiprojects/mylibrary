package com.mylibrary;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Radim Kratochvil
 * @author Michael Le <lemichael@mail.muni.cz>
 */
public class DataSourceFactory {
    public static DataSource getDerbyDataSource(String serverName, int port, String databaseName,
                                                String user, String password) {
        org.apache.derby.jdbc.ClientDataSource dataSource = new org.apache.derby.jdbc.ClientDataSource();
        dataSource.setServerName(serverName);
        dataSource.setPortNumber(port);
        dataSource.setDatabaseName(databaseName);
        dataSource.setUser(user);
        dataSource.setPassword(password);

        return dataSource;
    }

    public static DataSource getDataSource() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream("resources/db.properties"));

        String driver = properties.getProperty("db.driver");
        String serverName = properties.getProperty("db.serverName");
        String portString = properties.getProperty("db.port");
        String databaseName = properties.getProperty("db.databaseName");
        String user = properties.getProperty("db.user");
        String password = properties.getProperty("db.password");

        int port = Integer.parseInt(portString);

        if(driver.equals("derby"))
            return getDerbyDataSource(serverName, port, databaseName, user, password);
        else
            throw new IllegalArgumentException("Unknown database driver");
    }
}
