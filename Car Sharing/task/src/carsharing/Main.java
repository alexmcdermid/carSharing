package carsharing;

import java.sql.*;
import java.util.Scanner;

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
                    "ID INT PRIMARY KEY AUTO_INCREMENT, " +
                    "NAME VARCHAR(255) UNIQUE NOT NULL)";
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(createTableSQL);
            }

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("1. Log in as a manager");
                System.out.println("0. Exit");
                System.out.print("> ");
                int choice = scanner.nextInt();
                scanner.nextLine();

                if (choice == 0) {
                    break;
                }

                if (choice == 1) {
                    while (true) {
                        System.out.println("1. Company list");
                        System.out.println("2. Create a company");
                        System.out.println("0. Back");
                        System.out.print("> ");
                        choice = scanner.nextInt();
                        scanner.nextLine();

                        if (choice == 0) {
                            break;
                        }

                        if (choice == 1) {
                            listCompanies(connection);
                        }

                        if (choice == 2) {
                            System.out.print("Enter the company name:\n> ");
                            String companyName = scanner.nextLine();
                            createCompany(connection, companyName);
                        }
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void listCompanies(Connection connection) throws SQLException {
        String selectSQL = "SELECT * FROM COMPANY ORDER BY ID";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(selectSQL)) {
            if (!rs.next()) {
                System.out.println("The company list is empty!");
                return;
            }

            System.out.println("Company list:");
            int index = 1;
            do {
                System.out.println(index + ". " + rs.getString("NAME"));
                index++;
            } while (rs.next());
        }
    }

    private static void createCompany(Connection connection, String companyName) throws SQLException {
        String insertSQL = "INSERT INTO COMPANY (NAME) VALUES (?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
            preparedStatement.setString(1, companyName);
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("The company was created!");
            } else {
                System.out.println("Failed to create the company.");
            }
        }
    }
}
