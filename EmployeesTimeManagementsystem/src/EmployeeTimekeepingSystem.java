import java.sql.*;
import java.util.Scanner;

/*
 * Group Members:
 * 1. Rovic C. Deloy
 * 2. Alver Bactong
 * 3. Carl Jhayrlle Marano
 * 4. Andrei Blanca
 */

public class EmployeeTimekeepingSystem {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/employee_db";
    private static final String USER = "root";
    private static final String PASSWORD = "Rovic420";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            System.out.println("You are Connected successfully!");

            while (true) {
                System.out.println("=============================================================");
                System.out.println("=              TIMEKEEPING MANAGEMENT SYSTEM                =");
                System.out.println("=============================================================");
                System.out.println("1. Register as Employee");
                System.out.println("2. Check In");
                System.out.println("3. Check Out");
                System.out.println("4. Exit");
                System.out.print("Choose an option: ");

                if (!scanner.hasNextInt()) {
                    System.out.println("Invalid input. Please enter a number only!.");
                    scanner.next();
                    continue;
                }

                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1:
                        registerEmployee(connection, scanner);
                        break;
                    case 2:
                        checkInEmployee(connection, scanner);
                        break;
                    case 3:
                        checkOutEmployee(connection, scanner);
                        break;
                    case 4:
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
        }
    }

    private static void registerEmployee(Connection connection, Scanner scanner) {
        System.out.print("Enter your name: ");
        String name = scanner.nextLine().trim();
        if (!name.matches("[a-zA-Z ]+")) {
            System.out.println("Error: Name should contain letters only!");
            return;
        }

        System.out.print("Enter position: ");
        String position = scanner.nextLine().trim();
        if (!position.matches("[a-zA-Z ]+")) {
            System.out.println("Error: Position should contain letters only.");
            return;
        }

        System.out.print("Enter hire date (YYYY-MM-DD): ");
        String hireDate = scanner.nextLine().trim();

        try {
            Date.valueOf(hireDate);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: Invalid date format.");
            return;
        }

        String sql = "INSERT INTO employees (name, position, hire_date) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.setString(2, position);
            statement.setDate(3, Date.valueOf(hireDate));
            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                int employeeId = generatedKeys.getInt(1);
                System.out.println("Employee registered successfully! Here is your unique Employee ID: " + employeeId);
            }
        } catch (SQLException e) {
            System.out.println("Failed to register employee: " + e.getMessage());
        }
    }

    private static void checkInEmployee(Connection connection, Scanner scanner) {
        System.out.print("Enter employee ID for check-in: ");
        if (!scanner.hasNextInt()) {
            System.out.println("Invalid ID. Please enter a number.");
            scanner.next();
            return;
        }
        int employeeId = scanner.nextInt();

        if (!isEmployeeValid(connection, employeeId)) {
            System.out.println("Error: Invalid employee ID.");
            return;
        }

        if (hasUncheckedCheckIn(connection, employeeId)) {
            System.out.println("Error: Already checked in. Please proceed to check out.");
            return;
        }

        String sql = "INSERT INTO timekeeping (employee_id, check_in) VALUES (?, NOW())";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, employeeId);
            statement.executeUpdate();
            System.out.println("Check-in recorded successfully.");
        } catch (SQLException e) {
            System.out.println("Failed to check-in: " + e.getMessage());
        }
    }

    private static void checkOutEmployee(Connection connection, Scanner scanner) {
        System.out.print("Enter employee ID for check-out: ");
        if (!scanner.hasNextInt()) {
            System.out.println("Invalid ID. Please enter a number only.");
            scanner.next();
            return;
        }
        int employeeId = scanner.nextInt();

        if (!isEmployeeValid(connection, employeeId)) {
            System.out.println("Error: Invalid employee ID.");
            return;
        }

        if (!hasUncheckedCheckIn(connection, employeeId)) {
            System.out.println("Error: No active check-in found! You should check-in first before you check-out.");
            return;
        }

        String sql = "UPDATE timekeeping SET check_out = NOW() WHERE employee_id = ? AND check_out IS NULL";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, employeeId);
            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Check-out recorded successfully.");
            } else {
                System.out.println("No check-in found for this employee. Please check-in first.");
            }
        } catch (SQLException e) {
            System.out.println("Failed to check-out: " + e.getMessage());
        }
    }

    private static boolean isEmployeeValid(Connection connection, int employeeId) {
        String sql = "SELECT * FROM employees WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, employeeId);
            ResultSet rs = statement.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println("Error: Unable to validate employee - " + e.getMessage());
            return false;
        }
    }

    private static boolean hasUncheckedCheckIn(Connection connection, int employeeId) {
        String sql = "SELECT * FROM timekeeping WHERE employee_id = ? AND check_out IS NULL";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, employeeId);
            ResultSet rs = statement.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println("Error: Unable to validate check-in status - " + e.getMessage());
            return false;
        }
    }
}