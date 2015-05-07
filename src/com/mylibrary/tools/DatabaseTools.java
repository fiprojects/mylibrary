package com.mylibrary.tools;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Radim Kratochvil
 * @author Michael Le <lemichael@mail.muni.cz>
 */
public class DatabaseTools {
    private static String[] readSqlStatementsFromFile(String scriptName) throws IOException {
        StringBuilder statements = new StringBuilder();
        String line;
        
        try(
            FileReader reader = new FileReader("sql/" + scriptName);
            BufferedReader bufferedReader = new BufferedReader(reader)
        ) {
            while((line = bufferedReader.readLine()) != null) {
                statements.append(line);
            }
        }

        return statements.toString().split(";");
    }

    public static void executeSqlFromFile(DataSource dataSource, String scriptName) throws SQLException, IOException {
        try(Connection connection = dataSource.getConnection()) {
            for(String statement : readSqlStatementsFromFile(scriptName)) {
                if(!statement.trim().isEmpty()) {
                    connection.prepareStatement(statement).executeUpdate();
                }
            }
        }
    }
}
