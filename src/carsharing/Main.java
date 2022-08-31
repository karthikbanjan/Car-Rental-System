package carsharing;

import carsharing.dao.CarSharingDaoImpl;
import carsharing.model.Car;
import carsharing.model.Company;
import carsharing.model.Customer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import static java.sql.Types.NULL;

public class Main {

    static Scanner sc = new Scanner(System.in);
    static CarSharingDaoImpl cd;

    static ArrayList<Company> compList = new ArrayList<>();
    static ArrayList<Customer> cusList = new ArrayList<>();
    static ArrayList<Car> carList = new ArrayList<>();

    static int baseOption = -1, managerOption = -1, companyOption = -1, customerOption = -1;
    static int customerChoice = -1, carChoice = -1, companyChoice = -1;
    static User u = User.ABSENT;

    enum User {
        MANAGER,
        CUSTOMER,
        ABSENT
    }

    public static void main(String[] args) throws SQLException {
        cd = new CarSharingDaoImpl();
        cd.createDatabase();

        baseMenu();
    }

    public static void baseMenu() throws SQLException {
        printBaseOptions();
        baseOption = sc.nextInt();
        System.out.println();

        switch (baseOption) {
            case 0 -> {
                sc.close();
                cd.closeDB();
                System.exit(0);
            }

            case 1 -> {
                u = User.MANAGER;
                managerMenu();
            }

            case 2 -> {
                u = User.CUSTOMER;
                cusList = cd.getAllCustomers();
                if (cusList.isEmpty()) {
                    System.out.println();
                    break;
                }
                customerList();
            }

            case 3 -> {
                System.out.println("Enter the customer name:");
                sc.nextLine();
                cd.createCustomer(sc.nextLine());
                System.out.println();
            }

            default -> System.out.println("Invalid Option! Choose from given options!");
        }

        baseMenu();
    }

    public static void managerMenu() throws SQLException {
        printManagerOptions();
        managerOption = sc.nextInt();
        System.out.println();

        switch (managerOption) {
            case 0 -> baseMenu();

            case 1 -> {
                compList = cd.getAllCompanies();
                if (compList.isEmpty()) {
                    System.out.println();
                    managerMenu();
                }
                companyList();
            }

            case 2 -> {
                System.out.println("Enter the company name:");
                sc.nextLine();
                cd.createCompany(sc.nextLine());
                System.out.println();
            }
        }

        managerMenu();
    }

    public static void companyList() throws SQLException {
        System.out.println("Choose a company:");
        compList.forEach(c -> System.out.println(c.getId() + ". " + c.getName()));
        System.out.println("0. Back");
        companyChoice = sc.nextInt();
        if (companyChoice > compList.size() || companyChoice < 0) {
            System.out.println("Invalid Option! Choose from given options!");
            companyList();
        } else if (companyChoice == 0 && u.equals(User.MANAGER)) {
            System.out.println();
            managerMenu();
        } else if (companyChoice == 0 && u.equals(User.CUSTOMER)) {
            System.out.println();
            customerMenu();
        }

        switch (u) {
            case MANAGER -> companyMenu();

            case CUSTOMER -> {
                System.out.println();
                carList = cd.getAllAvailableCars(companyChoice);
                if (carList.isEmpty()) {
                    companyList();
                }
                carList();
            }
        }
    }

    public static void companyMenu() throws SQLException {
        System.out.println();
        printCompanyOptions();
        companyOption = sc.nextInt();
        System.out.println();

        switch (companyOption) {
            case 0 -> managerMenu();

            case 1 -> {
                carList = cd.getAllCars(companyChoice);
                if (carList.isEmpty()) {
                    break;
                }
                carList();
            }

            case 2 -> {
                System.out.println("Enter the car name:");
                sc.nextLine();
                cd.createCar(sc.nextLine(), companyChoice);
                System.out.println();
            }

            default -> System.out.println("Invalid Option! Choose from given options!");
        }

        companyMenu();
    }

    public static void carList() throws SQLException {
        if (u.equals(User.MANAGER)) System.out.println("Car list:");
        else System.out.println("Choose a car:");
        AtomicInteger i = new AtomicInteger();
        carList.forEach(c -> System.out.println(i.incrementAndGet() + ". " + c.getName()));

        if (u.equals(User.CUSTOMER)) {
            System.out.println("0. Back");
            carChoice = sc.nextInt();
            if (carChoice == 0) {
                customerMenu();
            }
            cd.rentCar(carList.get(carChoice-1).getId(), customerChoice);
            System.out.println();
            System.out.println("You rented " + "'" + carList.get(carChoice-1).getName() + "'");
            customerMenu();
        }
    }

    public static void customerList() throws SQLException {
        System.out.println("Customer list:");
        cusList.forEach(c -> System.out.println(c.getID() + ". " + c.getName()));
        System.out.println("0. Back");
        customerChoice = sc.nextInt();
        if (customerChoice > cusList.size() || customerChoice < 0) {
            System.out.println("Invalid Option! Choose from given options!");
            customerList();
        } else if (customerChoice == 0) {
            System.out.println();
            baseMenu();
        }

        customerMenu();
    }

    public static void customerMenu() throws SQLException {
        System.out.println();
        printCustomerOptions();
        customerOption = sc.nextInt();
        System.out.println();

        cusList = cd.getAllCustomers();
        if (cusList.isEmpty()) {
            System.out.println();
            customerMenu();
        }

        switch (customerOption) {
            case 0 -> baseMenu();

            case 1 -> {
                if (cusList.get(customerChoice-1).getRentedCarId() != NULL) {
                    System.out.println("You've already rented a car!");
                    break;
                }
                compList = cd.getAllCompanies();
                if (compList.isEmpty()) {
                    System.out.println();
                    break;
                }
                companyList();
            }

            case 2 -> {
                if (cusList.get(customerChoice-1).getRentedCarId() == NULL) {
                    System.out.println("You didn't rent a car!");
                    break;
                }
                cd.returnRentedCar(customerChoice);
            }

            case 3 -> {
                if (cusList.get(customerChoice-1).getRentedCarId() == NULL) {
                    System.out.println("You didn't rent a car!");
                    break;
                }
                cd.showRentedCar(cusList.get(customerChoice-1).getRentedCarId());
            }
        }

        customerMenu();
    }

    public static void printCustomerOptions() {
        System.out.println("1. Rent a car");
        System.out.println("2. Return a rented car");
        System.out.println("3. My rented car");
        System.out.println("0. Back");
    }

    public static void printCompanyOptions() {
        System.out.println("'" + compList.get(companyChoice-1).getName() + "'" + " company:");
        System.out.println("1. Car list");
        System.out.println("2. Create a car");
        System.out.println("0. Back");
    }

    public static void printManagerOptions() {
        System.out.println("1. Company list");
        System.out.println("2. Create a company");
        System.out.println("0. Back");
    }

    public static void printBaseOptions() {
        System.out.println("1. Log in as a manager");
        System.out.println("2. Log in as a customer");
        System.out.println("3. Create a customer");
        System.out.println("0. Exit");
    }
}