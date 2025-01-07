package carsharing;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
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
                    "RENTED_CAR_ID INT, " +
                    "FOREIGN KEY (COMPANY_ID) REFERENCES COMPANY(ID))";
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(createCarTableSQL);
            }

            String createCustomerTableSQL = "CREATE TABLE IF NOT EXISTS CUSTOMER (" +
                    "ID INT PRIMARY KEY AUTO_INCREMENT, " +
                    "NAME VARCHAR(255) UNIQUE NOT NULL, " +
                    "RENTED_CAR_ID INT, " +
                    "FOREIGN KEY (RENTED_CAR_ID) REFERENCES CAR(ID))";
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(createCustomerTableSQL);
            }

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("1. Log in as a manager");
                System.out.println("2. Log in as a customer");
                System.out.println("3. Create a customer");
                System.out.println("0. Exit");
                System.out.print("> ");
                int choice = scanner.nextInt();
                scanner.nextLine();
                if (choice == 0) {
                    break;
                }
                if (choice == 1) {
                    managerMenu(connection);
                } else if (choice == 2) {
                    customerLogin(connection);
                } else if (choice == 3) {
                    createCustomer(connection, scanner);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void managerMenu(Connection connection) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("1. Company list");
            System.out.println("2. Create a company");
            System.out.println("0. Back");
            System.out.print("> ");
            int choice = scanner.nextInt();
            scanner.nextLine();
            if (choice == 0) {
                break;
            }
            if (choice == 1) {
                listCompanies(connection);
            } else if (choice == 2) {
                System.out.print("Enter the company name:\n> ");
                String companyName = scanner.nextLine();
                createCompany(connection, companyName);
            }
        }
    }

    private static void listCompanies(Connection connection) throws SQLException {
        String selectSQL = "SELECT * FROM COMPANY ORDER BY ID";
        try (
                Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet rs = stmt.executeQuery(selectSQL)
        ) {
            if (!rs.next()) {
                System.out.println("The company list is empty!");
                return;
            }
            System.out.println("Choose the company:");
            List<int[]> companies = new ArrayList<>();
            do {
                companies.add(new int[]{rs.getInt("ID")});
                System.out.println(companies.size() + ". " + rs.getString("NAME"));
            } while (rs.next());
            System.out.println("0. Back");
            System.out.print("> ");
            Scanner scanner = new Scanner(System.in);
            int choice = scanner.nextInt();
            if (choice == 0) {
                return;
            }
            if (choice < 1 || choice > companies.size()) {
                return;
            }
            int companyId = companies.get(choice - 1)[0];
            String nameSQL = "SELECT NAME FROM COMPANY WHERE ID = ?";
            try (PreparedStatement ps = connection.prepareStatement(nameSQL)) {
                ps.setInt(1, companyId);
                try (ResultSet rsName = ps.executeQuery()) {
                    if (rsName.next()) {
                        String companyName = rsName.getString("NAME");
                        companyMenu(connection, companyName, companyId);
                    }
                }
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
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("The car list is empty!");
                    return;
                }
                String firstCarName = rs.getString("NAME");
                System.out.println("'" + firstCarName + "' cars:");
                List<String> carNames = new ArrayList<>();
                carNames.add(firstCarName);
                while (rs.next()) {
                    carNames.add(rs.getString("NAME"));
                }
                for (int i = 0; i < carNames.size(); i++) {
                    System.out.println((i + 1) + ". " + carNames.get(i));
                }
            }
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

    private static void createCustomer(Connection connection, Scanner scanner) throws SQLException {
        System.out.print("Enter the customer name:\n> ");
        String customerName = scanner.nextLine();
        String insertSQL = "INSERT INTO CUSTOMER (NAME, RENTED_CAR_ID) VALUES (?, NULL)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
            preparedStatement.setString(1, customerName);
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("The customer was added!");
            } else {
                System.out.println("Failed to add the customer.");
            }
        }
    }

    private static void customerLogin(Connection connection) throws SQLException {
        String selectSQL = "SELECT * FROM CUSTOMER ORDER BY ID";
        try (
                Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet rs = stmt.executeQuery(selectSQL)
        ) {
            if (!rs.next()) {
                System.out.println("The customer list is empty!");
                return;
            }
            System.out.println("Choose a customer:");
            List<int[]> customers = new ArrayList<>();
            do {
                customers.add(new int[]{rs.getInt("ID")});
                System.out.println(customers.size() + ". " + rs.getString("NAME"));
            } while (rs.next());
            System.out.println("0. Back");
            System.out.print("> ");
            Scanner scanner = new Scanner(System.in);
            int choice = scanner.nextInt();
            if (choice == 0) {
                return;
            }
            if (choice < 1 || choice > customers.size()) {
                return;
            }
            int customerId = customers.get(choice - 1)[0];
            String nameSQL = "SELECT NAME FROM CUSTOMER WHERE ID = ?";
            try (PreparedStatement ps = connection.prepareStatement(nameSQL)) {
                ps.setInt(1, customerId);
                try (ResultSet rsName = ps.executeQuery()) {
                    if (rsName.next()) {
                        String customerName = rsName.getString("NAME");
                        customerMenu(connection, customerName, customerId);
                    }
                }
            }
        }
    }

    private static void customerMenu(Connection connection, String customerName, int customerId) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("'" + customerName + "' customer:");
            System.out.println("1. Rent a car");
            System.out.println("2. Return a rented car");
            System.out.println("3. My rented car");
            System.out.println("0. Back");
            System.out.print("> ");
            int choice = scanner.nextInt();
            scanner.nextLine();
            if (choice == 0) {
                break;
            }
            if (choice == 1) {
                rentCar(connection, customerId);
            }
            if (choice == 2) {
                returnCar(connection, customerId);
            }
            if (choice == 3) {
                showRentedCar(connection, customerId);
            }
        }
    }

    private static boolean isCustomerAlreadyRenting(Connection connection, int customerId) throws SQLException {
        String sql = "SELECT RENTED_CAR_ID FROM CUSTOMER WHERE ID = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, customerId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("RENTED_CAR_ID") != 0;
                }
            }
        }
        return false;
    }

    private static void rentCar(Connection connection, int customerId) throws SQLException {
        if (isCustomerAlreadyRenting(connection, customerId)) {
            System.out.println("You've already rented a car!");
            return;
        }
        String selectCompanySQL = "SELECT ID, NAME FROM COMPANY ORDER BY ID";
        try (
                Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet rs = stmt.executeQuery(selectCompanySQL)
        ) {
            if (!rs.next()) {
                System.out.println("The company list is empty!");
                return;
            }
            List<int[]> companies = new ArrayList<>();
            System.out.println("Choose a company:");
            do {
                companies.add(new int[]{rs.getInt("ID")});
                System.out.println(companies.size() + ". " + rs.getString("NAME"));
            } while (rs.next());
            System.out.println("0. Back");
            System.out.print("> ");
            Scanner scanner = new Scanner(System.in);
            int choice = scanner.nextInt();
            if (choice == 0) {
                return;
            }
            if (choice < 1 || choice > companies.size()) {
                return;
            }
            int companyId = companies.get(choice - 1)[0];
            rentCarFromCompany(connection, customerId, companyId);
        }
    }

    private static void rentCarFromCompany(Connection connection, int customerId, int companyId) throws SQLException {
        String selectAvailableCarsSQL = "SELECT ID, NAME FROM CAR WHERE COMPANY_ID = ? AND RENTED_CAR_ID IS NULL ORDER BY ID";
        try (PreparedStatement stmt = connection.prepareStatement(selectAvailableCarsSQL)) {
            stmt.setInt(1, companyId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<int[]> cars = new ArrayList<>();
                while (rs.next()) {
                    cars.add(new int[]{rs.getInt("ID")});
                }
                if (cars.isEmpty()) {
                    System.out.println("No available cars in this company.");
                    return;
                }
                stmt.clearParameters();
            }
        }
        String selectCarsAgainSQL = "SELECT ID, NAME FROM CAR WHERE COMPANY_ID = ? AND RENTED_CAR_ID IS NULL ORDER BY ID";
        try (PreparedStatement stmt = connection.prepareStatement(selectCarsAgainSQL)) {
            stmt.setInt(1, companyId);
            try (ResultSet rs2 = stmt.executeQuery()) {
                System.out.println("Choose a car:");
                List<Object[]> carData = new ArrayList<>();
                while (rs2.next()) {
                    carData.add(new Object[]{rs2.getInt("ID"), rs2.getString("NAME")});
                }
                for (int i = 0; i < carData.size(); i++) {
                    Object[] c = carData.get(i);
                    System.out.println((i + 1) + ". " + c[1]);
                }
                System.out.println("0. Back");
                System.out.print("> ");
                Scanner scanner = new Scanner(System.in);
                int carChoice = scanner.nextInt();
                if (carChoice == 0) {
                    return;
                }
                if (carChoice < 1 || carChoice > carData.size()) {
                    return;
                }
                Object[] chosenCar = carData.get(carChoice - 1);
                int carId = (int) chosenCar[0];
                String carName = (String) chosenCar[1];
                String updateCarSQL = "UPDATE CAR SET RENTED_CAR_ID = ? WHERE ID = ?";
                try (PreparedStatement updateCarStmt = connection.prepareStatement(updateCarSQL)) {
                    updateCarStmt.setInt(1, customerId);
                    updateCarStmt.setInt(2, carId);
                    updateCarStmt.executeUpdate();
                }
                String updateCustomerSQL = "UPDATE CUSTOMER SET RENTED_CAR_ID = ? WHERE ID = ?";
                try (PreparedStatement updateCustomerStmt = connection.prepareStatement(updateCustomerSQL)) {
                    updateCustomerStmt.setInt(1, carId);
                    updateCustomerStmt.setInt(2, customerId);
                    updateCustomerStmt.executeUpdate();
                }
                System.out.println("You rented '" + carName + "'");
            }
        }
    }

    private static void returnCar(Connection connection, int customerId) throws SQLException {
        String selectCustomerCarSQL = "SELECT RENTED_CAR_ID FROM CUSTOMER WHERE ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(selectCustomerCarSQL)) {
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int rentedCarId = rs.getInt("RENTED_CAR_ID");
                    if (rentedCarId == 0) {
                        System.out.println("You didn't rent a car!");
                        return;
                    }
                    String updateCarSQL = "UPDATE CAR SET RENTED_CAR_ID = NULL WHERE ID = ?";
                    try (PreparedStatement updateCarStmt = connection.prepareStatement(updateCarSQL)) {
                        updateCarStmt.setInt(1, rentedCarId);
                        updateCarStmt.executeUpdate();
                    }
                    String updateCustomerSQL = "UPDATE CUSTOMER SET RENTED_CAR_ID = NULL WHERE ID = ?";
                    try (PreparedStatement updateCustomerStmt = connection.prepareStatement(updateCustomerSQL)) {
                        updateCustomerStmt.setInt(1, customerId);
                        updateCustomerStmt.executeUpdate();
                    }
                    System.out.println("You've returned a rented car!");
                }
            }
        }
    }

    private static void showRentedCar(Connection connection, int customerId) throws SQLException {
        String selectCustomerCarSQL = "SELECT RENTED_CAR_ID FROM CUSTOMER WHERE ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(selectCustomerCarSQL)) {
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int rentedCarId = rs.getInt("RENTED_CAR_ID");
                    if (rentedCarId == 0) {
                        System.out.println("You didn't rent a car!");
                        return;
                    }
                    String selectCarSQL = "SELECT NAME, COMPANY_ID FROM CAR WHERE ID = ?";
                    try (PreparedStatement selectCarStmt = connection.prepareStatement(selectCarSQL)) {
                        selectCarStmt.setInt(1, rentedCarId);
                        try (ResultSet carRs = selectCarStmt.executeQuery()) {
                            if (carRs.next()) {
                                String carName = carRs.getString("NAME");
                                int compId = carRs.getInt("COMPANY_ID");
                                String selectCompanySQL = "SELECT NAME FROM COMPANY WHERE ID = ?";
                                try (PreparedStatement selectCompanyStmt = connection.prepareStatement(selectCompanySQL)) {
                                    selectCompanyStmt.setInt(1, compId);
                                    try (ResultSet companyRs = selectCompanyStmt.executeQuery()) {
                                        if (companyRs.next()) {
                                            String companyName = companyRs.getString("NAME");
                                            System.out.println("Your rented car:");
                                            System.out.println(carName);
                                            System.out.println("Company:");
                                            System.out.println(companyName);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
