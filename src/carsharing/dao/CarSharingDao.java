package carsharing.dao;

import carsharing.model.Car;
import carsharing.model.Company;
import carsharing.model.Customer;

import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public interface CarSharingDao {

    void createDatabase() throws SQLException;
    void createCompany(String name) throws SQLException;
    void createCar(String name, int comId) throws SQLException;
    void createCustomer(String name) throws SQLException;
    void rentCar(int carId, int cusId) throws SQLException;
    void returnRentedCar(int cusId) throws SQLException;
    void showRentedCar(int carId) throws SQLException;
    ArrayList<Company> getAllCompanies() throws SQLException;
    ArrayList<Car> getAllCars(int comId) throws SQLException;
    ArrayList<Car> getAllAvailableCars(int comId) throws SQLException;
    ArrayList<Customer> getAllCustomers() throws SQLException;
    void closeDB() throws SQLException;
}
