USE employee_db;

CREATE TABLE employees (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    position VARCHAR(100) NOT NULL,
    hire_date DATE NOT NULL
);

CREATE TABLE timekeeping (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT,
    check_in DATETIME NOT NULL,
    check_out DATETIME,
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);
