import java.sql.*;
import java.util.Scanner;

public class LibrarySystem {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/library";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Somewhere1202!?$";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Connected to the database!");

            while (true) {
                System.out.println("\nLibrary Management System");
                System.out.println("1. Add a Member");
                System.out.println("2. Add a Loan");
                System.out.println("3. Return a Book");
                System.out.println("4. View Overdue Books");
                System.out.println("5. View Entries");
                System.out.println("6. Exit");
                System.out.print("Enter your choice: ");
                int choice = scanner.nextInt();

                switch (choice) {
                    case 1 -> addMember(conn, scanner);
                    case 2 -> addLoan(conn, scanner);
                    case 3 -> returnBook(conn, scanner);
                    case 4 -> viewOverdueBooks(conn);
                    case 5 -> viewTableEntries(conn, scanner);
                    case 6 -> {
                        System.out.println("Exiting...");
                        return;
                    }
                    default -> System.out.println("Invalid choice. Try again.");
                }
            }

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }

    private static void addMember(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter member name: ");
        scanner.nextLine(); // Clear buffer
        String name = scanner.nextLine();
        System.out.print("Enter phone: ");
        String phone = scanner.nextLine();
        System.out.print("Enter email: ");
        String email = scanner.nextLine();

        String sql = "INSERT INTO Members (Name, Phone, Email) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, phone);
            stmt.setString(3, email);
            stmt.executeUpdate();
            System.out.println("Member added successfully!");
        }
    }

    private static void addLoan(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter book ID: ");
        int bookId = scanner.nextInt();
        System.out.print("Enter member ID: ");
        int memberId = scanner.nextInt();

        String call = "{CALL AddLoan(?, ?)}";
        try (CallableStatement stmt = conn.prepareCall(call)) {
            stmt.setInt(1, bookId);
            stmt.setInt(2, memberId);
            stmt.execute();
            System.out.println("Loan added successfully!");
        }
    }

    private static void returnBook(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter loan ID: ");
        int loanId = scanner.nextInt();

        String call = "{CALL ReturnBook(?)}";
        try (CallableStatement stmt = conn.prepareCall(call)) {
            stmt.setInt(1, loanId);
            stmt.execute();
            System.out.println("Book returned successfully!");
        }
    }

    private static void viewOverdueBooks(Connection conn) throws SQLException {
        String call = "{CALL GetOverdueBooks()}";
        try (CallableStatement stmt = conn.prepareCall(call)) {
            ResultSet rs = stmt.executeQuery();

            System.out.println("\nOverdue Books:");
            while (rs.next()) {
                String memberName = rs.getString("MemberName");
                String bookTitle = rs.getString("BookTitle");
                Date loanDate = rs.getDate("LoanDate");
                Date returnDate = rs.getDate("ReturnDate");

                System.out.printf("Member: %s, Book: %s, Loan Date: %s, Return Date: %s%n",
                        memberName, bookTitle, loanDate, returnDate == null ? "Not Returned" : returnDate);
            }
        }
    }

    private static void viewTableEntries(Connection conn, Scanner scanner) {
        try {
            // Clear any leftover newline character
            scanner.nextLine(); // This is important to avoid issues after reading numbers
    
            System.out.println("Select a table to view:");
            System.out.println("1. Members");
            System.out.println("2. Books");
            System.out.println("3. Loans");
            System.out.print("Enter your choice: ");
            
            String input = scanner.nextLine();  // Read the entire line to avoid leftover newline issues
            int choice;
    
            // Try to parse the input as an integer
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                return;
            }
    
            String tableName;
            switch (choice) {
                case 1:
                    tableName = "Members";
                    break;
                case 2:
                    tableName = "Books";
                    break;
                case 3:
                    tableName = "Loans";
                    break;
                default:
                    System.out.println("Invalid choice. Returning to menu.");
                    return;
            }
    
            System.out.println("Preparing SQL query for table: " + tableName);
            String sql = "SELECT * FROM " + tableName;
    
            // Debugging: Print the SQL query
            System.out.println("Executing SQL: " + sql);
    
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
    
            // Print column headers
            System.out.println("\n" + tableName + " Table:");
            int columnCount = rs.getMetaData().getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                System.out.print(rs.getMetaData().getColumnName(i) + "\t");
            }
            System.out.println();
    
            // Print rows
            boolean hasResults = false;
            while (rs.next()) {
                hasResults = true;
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(rs.getString(i) + "\t");
                }
                System.out.println();
            }
    
            if (!hasResults) {
                System.out.println("No data found in " + tableName + " table.");
            }
    
            rs.close();
            stmt.close();
    
        } catch (SQLException e) {
            System.out.println("Error retrieving table entries.");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
    
}
