//Author: Karthik Banjan

package carsharing.dao;

import carsharing.model.Car;
import carsharing.model.Company;
import carsharing.model.Customer;

import java.sql.*;
import java.util.ArrayList;

public class CarSharingDaoImpl implements CarSharingDao {

    private Connection conn = null;
    private Statement stmt = null;
    final String DB_URL = "jdbc:h2:./src/carsharing/db/carsharing";

    /*
    final static String sqlDropAll =
            """
            drop table if exists CAR;
            drop table if exists COMPANY;
            drop table if exists CUSTOMER;
            """;
    */

    final static String sqlCreateCompany =
            """
            create table if not exists COMPANY (
            ID int primary key auto_increment,
            NAME varchar unique not null);
            """;

    final static String sqlCreateCar =
            """
            create table if not exists CAR (
            ID int primary key auto_increment,
            NAME varchar unique not null,
            COMPANY_ID int not null,
            foreign key (COMPANY_ID) references COMPANY(ID));
            """;

    final static String sqlCreateCustomer =
            """
            create table if not exists CUSTOMER (
            ID INT PRIMARY KEY AUTO_INCREMENT,
            NAME varchar unique not null,
            RENTED_CAR_ID int,
            foreign key (RENTED_CAR_ID) references CAR(ID));
            """;

    final static String sqlInsertCompany =
            """
            insert into COMPANY (NAME)
            values (?);
            """;
    private PreparedStatement sInsertCompany = null;

    final static String sqlInsertCar =
            """
            insert into CAR (NAME, COMPANY_ID)
            values (?, ?);
            """;
    private PreparedStatement sInsertCar = null;

    final static String sqlInsertCustomer =
            """
            insert into CUSTOMER (NAME, RENTED_CAR_ID)
            values (?, NULL);
            """;
    private PreparedStatement sInsertCustomer = null;

    final static String sqlRentCar =
            """
            update CUSTOMER
            set RENTED_CAR_ID = ?
            where ID = ?;
            """;
    private PreparedStatement sRentCar = null;

    final static String sqlReturnCar =
            """
            update CUSTOMER
            set RENTED_CAR_ID = null
            where ID = ?;
            """;
    private PreparedStatement sReturnCar = null;

    final static String sqlShowCar =
            """
            select C.NAME as CAR_NAME, CO.NAME AS COMPANY_NAME
            from CAR as C, COMPANY as CO
            where C.ID = ? and C.COMPANY_ID = CO.ID;
            """;
    private PreparedStatement sShowCar = null;

    final static String sqlAllCompany =
            """
            select *
            from COMPANY
            order by ID;
            """;

    final static String sqlAllCar =
            """
            select *
            from CAR
            where COMPANY_ID = ?
            order by ID;
            """;
    private static PreparedStatement sAllCar = null;

    final static String sqlAllAvailableCar =
            """
            select *
            from CAR
            where COMPANY_ID = ? and ID not in (select RENTED_CAR_ID
                                                from CUSTOMER
                                                where RENTED_CAR_ID is not null);
            """;
    private static PreparedStatement sAllAvailableCar = null;

    final static String sqlAllCustomers =
            """
            select *
            from CUSTOMER
            order by ID;
            """;

    final static String sqlRefresh =
            """
            alter table if exists COMPANY
            alter column ID restart with 1;
            """;

    public CarSharingDaoImpl() throws SQLException {
        conn = DriverManager.getConnection(DB_URL);
        stmt = conn.createStatement();
        conn.setAutoCommit(true);
    }

    @Override
    public void createDatabase() throws SQLException {
        stmt.execute(sqlRefresh);
        stmt.execute(sqlCreateCompany);
        stmt.execute(sqlCreateCar);
        stmt.execute(sqlCreateCustomer);

        sInsertCompany = conn.prepareStatement(sqlInsertCompany);
        sInsertCar = conn.prepareStatement(sqlInsertCar);
        sInsertCustomer = conn.prepareStatement(sqlInsertCustomer);
        sAllCar = conn.prepareStatement(sqlAllCar);
        sAllAvailableCar = conn.prepareStatement(sqlAllAvailableCar);
        sRentCar = conn.prepareStatement(sqlRentCar);
        sReturnCar = conn.prepareStatement(sqlReturnCar);
        sShowCar = conn.prepareStatement(sqlShowCar);
    }

    @Override
    public void createCompany(String name) throws SQLException {
        sInsertCompany.setString(1, name);
        sInsertCompany.executeUpdate();
        System.out.println("The company was created!");
    }

    @Override
    public void createCar(String name, int comId) throws SQLException {
        sInsertCar.setString(1, name);
        sInsertCar.setInt(2, comId);
        sInsertCar.executeUpdate();
        System.out.println("The car was added!");
    }

    @Override
    public void createCustomer(String name) throws SQLException {
        sInsertCustomer.setString(1, name);
        sInsertCustomer.executeUpdate();
        System.out.println("The customer was added!");
    }

    @Override
    public void rentCar(int carId, int cusId) throws SQLException {
        sRentCar.setInt(1, carId);
        sRentCar.setInt(2, cusId);
        sRentCar.executeUpdate();
    }

    @Override
    public void returnRentedCar(int cusId) throws SQLException {
        sReturnCar.setInt(1, cusId);
        sReturnCar.executeUpdate();
        System.out.println("You've returned a rented car!");
    }

    @Override
    public void showRentedCar(int carId) throws SQLException {
        sShowCar.setInt(1, carId);
        ResultSet rs = sShowCar.executeQuery();

        while (rs.next()) {
            System.out.println("Your rented car:");
            System.out.println(rs.getString("CAR_NAME"));
            System.out.println("Company:");
            System.out.println(rs.getString("COMPANY_NAME"));
        }
    }

    @Override
    public ArrayList<Company> getAllCompanies() throws SQLException {
        stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sqlAllCompany);
        if (!rs.isBeforeFirst() && rs.getRow() == 0) {
            System.out.println("The company list is empty!");
            return new ArrayList<>();
        }

        ArrayList<Company> comList = new ArrayList<>();

        while (rs.next()) {
            Company c = new Company();
            c.setId(rs.getInt("ID"));
            c.setName(rs.getString("NAME"));
            comList.add(c);
        }

        return comList;
    }

    @Override
    public ArrayList<Car> getAllCars(int comId) throws SQLException {
        return getCars(comId, sAllCar);
    }

    @Override
    public ArrayList<Car> getAllAvailableCars(int comId) throws SQLException {
        return getCars(comId, sAllAvailableCar);
    }

    private ArrayList<Car> getCars(int comId, PreparedStatement sPrep) throws SQLException {
        sPrep.setInt(1, comId);
        ResultSet rs = sPrep.executeQuery();

        if (!rs.isBeforeFirst() && rs.getRow() == 0) {
            System.out.println("The car list is empty!");
            return new ArrayList<>();
        }

        ArrayList<Car> carList = new ArrayList<>();

        while (rs.next()) {
            Car c = new Car();
            c.setId(rs.getInt("ID"));
            c.setName(rs.getString("NAME"));
            c.setCompanyId(rs.getInt("COMPANY_ID"));
            carList.add(c);
        }

        return carList;
    }

    @Override
    public ArrayList<Customer> getAllCustomers() throws SQLException {
        stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sqlAllCustomers);
        if (!rs.isBeforeFirst() && rs.getRow() == 0) {
            System.out.println("The customer list is empty!");
            return new ArrayList<>();
        }

        ArrayList<Customer> cusList = new ArrayList<>();

        while (rs.next()) {
            Customer c = new Customer();
            c.setID(rs.getInt("ID"));
            c.setName(rs.getString("NAME"));
            c.setRentedCarId(rs.getInt("RENTED_CAR_ID"));
            cusList.add(c);
        }

        return cusList;
    }

    @Override
    public void closeDB() throws SQLException {
        if (stmt != null) stmt.close();
        if (conn != null) conn.close();
    }
}
