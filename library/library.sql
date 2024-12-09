-- Use the database
CREATE DATABASE IF NOT EXISTS library;
USE library;

-- Drop existing tables to avoid conflicts
DROP TABLE IF EXISTS Loans;
DROP TABLE IF EXISTS Books;
DROP TABLE IF EXISTS Members;
DROP TABLE IF EXISTS Authors;

-- Drop existing stored procedures to avoid conflicts
DROP PROCEDURE IF EXISTS GetLoansForMember;
DROP PROCEDURE IF EXISTS GetTotalBooks;
DROP PROCEDURE IF EXISTS GetOverdueBooks;
DROP PROCEDURE IF EXISTS AddLoan;
DROP PROCEDURE IF EXISTS ReturnBook;

-- Create Authors table
CREATE TABLE Authors (
    AuthorID INT AUTO_INCREMENT PRIMARY KEY,
    Name VARCHAR(100) NOT NULL,
    BirthYear SMALLINT NOT NULL -- Changed from YEAR to SMALLINT
);

-- Create Books table
CREATE TABLE Books (
    BookID INT AUTO_INCREMENT PRIMARY KEY,
    Title VARCHAR(200) NOT NULL,
    AuthorID INT NOT NULL,
    PublishedYear YEAR NOT NULL,
    Genre VARCHAR(50) NOT NULL,
    CopiesAvailable INT NOT NULL,
    FOREIGN KEY (AuthorID) REFERENCES Authors(AuthorID)
);

-- Create Members table
CREATE TABLE Members (
    MemberID INT AUTO_INCREMENT PRIMARY KEY,
    Name VARCHAR(100) NOT NULL,
    Phone VARCHAR(15),
    Email VARCHAR(100)
);

-- Create Loans table
CREATE TABLE Loans (
    LoanID INT AUTO_INCREMENT PRIMARY KEY,
    BookID INT NOT NULL,
    MemberID INT NOT NULL,
    LoanDate DATE NOT NULL,
    ReturnDate DATE,
    FOREIGN KEY (BookID) REFERENCES Books(BookID),
    FOREIGN KEY (MemberID) REFERENCES Members(MemberID)
);

-- Insert sample data into Authors table
INSERT INTO Authors (Name, BirthYear) VALUES
('J.K. Rowling', 1965),
('George R.R. Martin', 1948),
('J.R.R. Tolkien', 1892);

-- Insert sample data into Books table
INSERT INTO Books (Title, AuthorID, PublishedYear, Genre, CopiesAvailable) VALUES
('Harry Potter and the Philosopher\'s Stone', 1, 1997, 'Fantasy', 5),
('A Game of Thrones', 2, 1996, 'Fantasy', 3),
('The Hobbit', 3, 1937, 'Fantasy', 4);

-- Insert sample data into Members table
INSERT INTO Members (Name, Phone, Email) VALUES
('Alice Smith', '1234567890', 'alice@example.com'),
('Bob Johnson', '9876543210', 'bob@example.com');

-- Insert sample data into Loans table
INSERT INTO Loans (BookID, MemberID, LoanDate, ReturnDate) VALUES
(1, 1, '2024-12-01', NULL),
(2, 2, '2024-11-28', '2024-12-05');

-- Create stored procedure: Get loans for a specific member
DELIMITER $$
CREATE PROCEDURE GetLoansForMember(IN member_id INT)
BEGIN
    SELECT Loans.LoanID, Books.Title, Loans.LoanDate, Loans.ReturnDate
    FROM Loans
    JOIN Books ON Loans.BookID = Books.BookID
    WHERE Loans.MemberID = member_id;
END$$
DELIMITER ;

-- Create stored procedure: Count total books in the library
DELIMITER $$
CREATE PROCEDURE GetTotalBooks(OUT total_books INT)
BEGIN
    SELECT COUNT(*) INTO total_books FROM Books;
END$$
DELIMITER ;

-- Create stored procedure: Get overdue books
DELIMITER $$
CREATE PROCEDURE GetOverdueBooks()
BEGIN
    SELECT Members.Name AS MemberName, Books.Title AS BookTitle, Loans.LoanDate, Loans.ReturnDate
    FROM Loans
    JOIN Members ON Loans.MemberID = Members.MemberID
    JOIN Books ON Loans.BookID = Books.BookID
    WHERE Loans.ReturnDate IS NULL AND Loans.LoanDate < CURDATE() - INTERVAL 14 DAY;
END$$
DELIMITER ;

-- Create stored procedure: Add a loan
DELIMITER $$
CREATE PROCEDURE AddLoan(IN book_id INT, IN member_id INT)
BEGIN
    INSERT INTO Loans (BookID, MemberID, LoanDate, ReturnDate)
    VALUES (book_id, member_id, CURDATE(), NULL);
END$$
DELIMITER ;

-- Create stored procedure: Return a book
DELIMITER $$
CREATE PROCEDURE ReturnBook(IN loan_id INT)
BEGIN
    UPDATE Loans
    SET ReturnDate = CURDATE()
    WHERE LoanID = loan_id;
END$$
DELIMITER ;
