package com.vu;



import javax.swing.*;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;

// This Database class was based off of movieRatingGUI application by Clara James
public class InventoryDatabase {

    // Global variables
    private static String db_url = "jdbc:mysql://localhost:3306/";
    private static String JDBC = "com.mysql.jdbc.Driver";
    private static final String db_name = "alcohol_inventory";
    private static final String user = System.getenv("MYSQL_USER");
    private static final String password = System.getenv("MYSQL_PASSWORD");

    // String variables to store column names
    public static final String PK_COLUMN = "id";
    public static final String PRODUCT_COLUMN = "product";
    public static final String BRAND_COLUMN = "brand";
    public static final String TYPE_COLUMN = "product_type";
    public static final String OFFICE_COUNT_COLUMN = "amount_in_office";
    public static final String BAR_COUNT_COLUMN = "amount_in_bar";
    public static final String TOTAL_COLUMN = "total_amount";
    public static final String ORDER_COLUMN = "order_more";
    public static final String DISTRIBUTOR_COLUMN = "distributor";

    // Initializes statement, connection, and resultSet as null
    private static Statement statement = null;
    private static Connection conn = null;
    private static ResultSet rs = null;

    // String variables to store table names
    private final static String ALCOHOL_TABLE_NAME = "alcohol";
    private final static String ORDER_TABLE_NAME = "order_list";


    // Inventory counts stored as doubles, because there can be 1.5 bottles of Jameson
    static double amountInOffice, amountInBar, totalAmount;

    // A theoretical par for to test the method
    public double par = 3;

    private static InventoryDataModel inventoryDataModel;

    public static void main(String[] args) throws SQLException{
        // Creates a table if not exist, retrieves the result set in the database
        setup();
        loadAllProduct();
        createOrderTable();

        // Start the GUI
        InventoryGUI inventoryGUI = new InventoryGUI(inventoryDataModel);


    }


    // Method to calculate the total amount for a product in the database and return the value as a double
    static double getTotalAmount(double amountInOffice, double amountInBar) {
        totalAmount = amountInOffice + amountInBar;
        return totalAmount;
    }

    // Method to determine the order status of a product and returns a string
    static String getOrder(double totalAmount, double par) {
        String order;
        if (totalAmount <= par) {
            order = "yes";
        } else {
            order = "no";
        }
        return order;
    }

    // Create or recreate a ResultSet containing the whole database, and gives it to the inventoryDataModel
    static void loadAllProduct() throws SQLException {
        if (rs != null) {
            rs.close(); // Close any currently-open result sets
        }

        String getAllData = "SELECT * FROM " + ALCOHOL_TABLE_NAME;
        rs = statement.executeQuery(getAllData);

        // Creates an inventoryDataModel if one down't exist
        if (inventoryDataModel == null) {
            inventoryDataModel = new InventoryDataModel(rs);
        } else {
            // Updates current existing inventoryDataModel
            inventoryDataModel.updateResultSet(rs);
        }
    }

    // Method that returns the ResultSet from the order_list table
    static void loadOrderProduct() throws SQLException {
        if (rs != null) {
            rs.close(); // Close any currently-open result sets
        }

        // SQL statement to retrieve data from order_list table
        String getOrderData = "SELECT * FROM " + ORDER_TABLE_NAME;
        rs = statement.executeQuery(getOrderData);


    }


    // Method to create the order_list table in the database
    public static void createOrderTable() throws SQLException {
        // Load Driver class
        // Try block to catch the class not found exception if no database driver is found
        try {
            String Driver = JDBC;
            Class.forName(Driver);
        } catch (ClassNotFoundException cnfe) {
            System.out.println("No Database drivers found. quiting");
            System.exit(-1);
        }
        // Try block to catch SQLException
        try {
            // Creates connection with database
            conn = DriverManager.getConnection(db_url + db_name, user, password);
            // ResultSet.TYPE_SCROLL_SENSITIVE allows movement of cursor forward and backwards through RowSet
            // ResultSet can be changed and updated to the database
            statement = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            // Create order_list table in the database if not exists, by selecting the values from primary key, brand,
            // type, total amount, and distributor column, where order more column value is yes for all the products
            // under less than the par amount
            String createOrderTableSQL = "CREATE TABLE IF NOT EXISTS " + ORDER_TABLE_NAME + " SELECT "
                    +  PK_COLUMN + ", " + BRAND_COLUMN + ", " +  TYPE_COLUMN + ", " + TOTAL_COLUMN + ", "
                    + DISTRIBUTOR_COLUMN + " from "
                    + ALCOHOL_TABLE_NAME + " where order_more = 'yes'";
            PreparedStatement createOrder = conn.prepareStatement(createOrderTableSQL);
            createOrder.execute(createOrderTableSQL);
            System.out.println("Created order table");
        } catch (SQLException sqle) {
            System.out.println("Could not create order table");
        }

        // Checks for warnings
        if (statement.getWarnings() == null) {
            addOrderData(); // Add order data
        }
    }


    // creates the alcohol table in the database
    private static void setup() throws SQLException {
        // Load Driver class
        // Try block to catch the class not found exception if no database driver is found
        try {
            String Driver = JDBC;
            Class.forName(Driver);
        } catch (ClassNotFoundException cnfe) {
            System.out.println("No database drivers found. Quitting");
            System.exit(-1);
        }
        // Try block to catch SQLException
        try {
            // Creates connection with database
            conn = DriverManager.getConnection(db_url + db_name, user, password);
            // ResultSet.TYPE_SCROLL_SENSITIVE allows movement of cursor forward and backwards through RowSet
            // ResultSet can be changed and updated to the database
            statement = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            // Creates alcohol table
            String createTableSQL = "CREATE TABLE IF NOT EXISTS " + ALCOHOL_TABLE_NAME
                    + " (" + PK_COLUMN + " INT NOT NULL AUTO_INCREMENT, "
                    + PRODUCT_COLUMN + " VARCHAR(50), "
                    + BRAND_COLUMN + " VARCHAR(50), "
                    + TYPE_COLUMN + " VARCHAR(50), "
                    + OFFICE_COUNT_COLUMN + " FLOAT NOT NULL, "
                    + BAR_COUNT_COLUMN + " FLOAT NOT NULL, "
                    + TOTAL_COLUMN + " FLOAT, "
                    + ORDER_COLUMN + " VARCHAR(50), "
                    + DISTRIBUTOR_COLUMN + " VARCHAR(50), CONSTRAINT UC_ALCOHOL UNIQUE ("
                    + BRAND_COLUMN + ", " + TYPE_COLUMN + "), PRIMARY KEY(" + PK_COLUMN + "))";
            PreparedStatement createTableStatement = conn.prepareStatement(createTableSQL);
            createTableStatement.execute(createTableSQL);
        } catch (SQLException sqle) {
            System.out.println("Could not create table");
            sqle.printStackTrace();
        }


            System.out.println("created alcohol table");

        // Checks for warnings
        if (statement.getWarnings() == null) {
            addAlcoholData();
        }

    }
    // Closes ResultSet, statement, and connection
    static void shutdown() {
        try {
            if (rs != null) {
                rs.close();
                System.out.println("Result set closed");
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }

        try {
            if (statement != null) {
                statement.close();
                System.out.println("Statement closed");
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }

        try {
            if (conn != null) {
                conn.close();
                System.out.println("Database connection closed");
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }
    // Method to insert data into the alcohol table
    private static void addAlcoholData() throws SQLException {
        String addDataSQL = "INSERT INTO " + ALCOHOL_TABLE_NAME + "("
                    + PRODUCT_COLUMN + ", "
                    + BRAND_COLUMN + ", "
                    + TYPE_COLUMN + ", "
                    + OFFICE_COUNT_COLUMN + ", "
                    + BAR_COUNT_COLUMN + ", "
                    + TOTAL_COLUMN + ", "
                    + DISTRIBUTOR_COLUMN + ")"
                    + " VALUES (?,?,?,?,?,?,?)";
        PreparedStatement addData = conn.prepareStatement(addDataSQL);
        addData.execute(addDataSQL);

    }

    // Method to insert data into the order table
    private static void addOrderData() throws SQLException {
        String addOrderDataSQL = "INSERT INTO " + ORDER_TABLE_NAME + "("
                + PK_COLUMN + ", "
                + BRAND_COLUMN + ", "
                + TYPE_COLUMN + ", "
                + TOTAL_COLUMN + ", "
                + ORDER_COLUMN + ") "
                + " VALUES (?,?,?,?)";
        PreparedStatement addOrderData = conn.prepareStatement(addOrderDataSQL);
        addOrderData.execute(addOrderDataSQL);
    }

    // Method to delete order table
    static void deleteOrderTable() throws SQLException {
        String deleteOrderTableData = "DROP TABLE " + ORDER_TABLE_NAME;
        PreparedStatement deleteOrderData = conn.prepareStatement(deleteOrderTableData);
        deleteOrderData.execute();
    }













}
