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

            String createCompanyTableSQL = "CREATE TABLE IF NOT EXISTS COMPANY (" +
                    "ID INT PRIMARY KEY AUTO_INCREMENT, " +
                    "NAME VARCHAR(255) UNIQUE NOT NULL)";
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(createCompanyTableSQL);
            }

            String createCarTableSQL = "CREATE TABLE IF NOT EXISTS CAR (" +
                    "ID INT PRIMARY KEY AUTO_INCREMENT, " +
                    "NAME VARCHAR(255) UNIQUE NOT NULL, " +
                    "COMPANY_ID INT NOT NULL, " +
                    "FOREIGN KEY (COMPANY_ID) REFERENCES COMPANY(ID))";
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(createCarTableSQL);
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
        try (Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
             ResultSet rs = stmt.executeQuery(selectSQL)) {

            if (!rs.next()) {
                System.out.println("The company list is empty!");
                return;
            }

            System.out.println("Choose the company:");
            int index = 1;
            do {
                System.out.println(index + ". " + rs.getString("NAME"));
                index++;
            } while (rs.next());

            System.out.println("0. Back");
            System.out.print("> ");
            Scanner scanner = new Scanner(System.in);
            int choice = scanner.nextInt();

            if (choice == 0) {
                return;
            }

            rs.beforeFirst();
            index = 1;
            while (rs.next()) {
                if (index == choice) {
                    String companyName = rs.getString("NAME");
                    int companyId = rs.getInt("ID");
                    companyMenu(connection, companyName, companyId);
                    break;
                }
                index++;
            }
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

    private static void companyMenu(Connection connection, String companyName, int companyId) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("'" + companyName + "' company:");
            System.out.println("1. Car list");
            System.out.println("2. Create a car");
            System.out.println("0. Back");
            System.out.print("> ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 0) {
                break;
            }

            if (choice == 1) {
                listCars(connection, companyId);
            }

            if (choice == 2) {
                System.out.print("Enter the car name:\n> ");
                String carName = scanner.nextLine();
                createCar(connection, companyId, carName);
            }
        }
    }

    private static void listCars(Connection connection, int companyId) throws SQLException {
        String selectSQL = "SELECT * FROM CAR WHERE COMPANY_ID = ? ORDER BY ID";
        try (PreparedStatement stmt = connection.prepareStatement(selectSQL)) {
            stmt.setInt(1, companyId);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                System.out.println("The car list is empty!");
                return;
            }

            System.out.println("'" + rs.getString("NAME") + "' cars:");
            int index = 1;
            do {
                System.out.println(index + ". " + rs.getString("NAME"));
                index++;
            } while (rs.next());
        }
    }

    private static void createCar(Connection connection, int companyId, String carName) throws SQLException {
        String insertSQL = "INSERT INTO CAR (NAME, COMPANY_ID) VALUES (?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
            preparedStatement.setString(1, carName);
            preparedStatement.setInt(2, companyId);
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("The car was added!");
            } else {
                System.out.println("Failed to add the car.");
            }
        }
    }
}
