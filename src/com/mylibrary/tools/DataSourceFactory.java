package com.mylibrary.tools;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.ResourceBundle;
import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

/**
 * @author Radim Kratochvil
 * @author Michael Le <lemichael@mail.muni.cz>
 */
public class DataSourceFactory {
    public static DataSource getDataSource(String driver, String url, String username, String password) {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(driver);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    public static DataSource getDataSource() throws IOException {
        Properties properties = new Properties();
        properties.load(DataSourceFactory.class.getResourceAsStream("/dbcp.properties"));

        return getDataSource(
                properties.getProperty("jdbc.driver"),
                properties.getProperty("jdbc.url"),
                properties.getProperty("jdbc.user"),
                properties.getProperty("jdbc.password")
        );
    }

	public static DataSource getMemoryDataSource() throws IOException {
		Properties properties = new Properties();
		properties.load(DataSourceFactory.class.getResourceAsStream("/dbcp.properties"));

        return getDataSource(
                properties.getProperty("jdbc.driver"),
                properties.getProperty("jdbc.memoryUrl"),
                properties.getProperty("jdbc.user"),
                properties.getProperty("jdbc.password")
        );
	}
}
