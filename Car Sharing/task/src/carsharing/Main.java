package carsharing;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {
        String databaseName = "default_db";

        if (args.length > 1 && args[0].equals("-databaseFileName")) {
            databaseName = args[1];
        }

        String url = "jdbc:h2:./src/carsharing/db/" + databaseName;

        try (Connection connection = DriverManager.getConnection(url)) {
            connection.setAutoCommit(true);

            String createTableSQL = "CREATE TABLE IF NOT EXISTS COMPANY (" +
                    "ID INT PRIMARY KEY," +
                    "NAME VARCHAR(255))";

            try (Statement stmt = connection.createStatement()) {
                stmt.execute(createTableSQL);
                System.out.println("Table COMPANY created successfully.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
