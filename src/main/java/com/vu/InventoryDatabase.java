package com.vu;



import javax.swing.*;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;

public class InventoryDatabase {

    // Global variables
    private static String db_url = "jdbc:mysql://localhost:3306/";
    private static String JDBC = "com.mysql.jdbc.Driver";
    private static final String db_name = "alcohol_inventory";
    private static final String user = System.getenv("MYSQL_USER");
    private static final String password = System.getenv("MYSQL_PASSWORD");

    public static final String PK_COLUMN = "id";
    public static final String PRODUCT_COLUMN = "product";
    public static final String BRAND_COLUMN = "brand";
    public static final String TYPE_COLUMN = "product_type";
    public static final String OFFICE_COUNT_COLUMN = "amount_in_office";
    public static final String BAR_COUNT_COLUMN = "amount_in_bar";
    public static final String TOTAL_COLUMN = "total_amount";
    public static final String ORDER_COLUMN = "order_more";
    public static final String DISTRIBUTOR_COLUMN = "distributor";


    private static Statement statement = null;
    private static Connection conn = null;
    private static ResultSet rs = null;

    private final static String ALCOHOL_TABLE_NAME = "alcohol";



    static double amountInOffice, amountInBar, totalAmount;

    public double par = 3;

    private static InventoryDataModel inventoryDataModel;

    public static void main(String[] args) throws SQLException{
        setup();
        loadAllProduct();

        InventoryGUI inventoryGUI = new InventoryGUI(inventoryDataModel);


    }

    static double getTotalAmount(double amountInOffice, double amountInBar) {
        totalAmount = amountInOffice + amountInBar;
        return totalAmount;
    }

    static String getOrder(double totalAmount, double par) {
        String order;
        if (totalAmount <= par) {
            order = "yes";
        } else {
            order = "no";
        }
        return order;
    }

    static void loadAllProduct() throws SQLException {
        if (rs != null) {
            rs.close();
        }

        String getAllData = "SELECT * FROM " + ALCOHOL_TABLE_NAME;
        rs = statement.executeQuery(getAllData);

        if (inventoryDataModel == null) {
            inventoryDataModel = new InventoryDataModel(rs);
        } else {
            inventoryDataModel.updateResultSet(rs);
        }
    }

    private static void setup() throws SQLException {
        try {
            String Driver = JDBC;
            Class.forName(Driver);
        } catch (ClassNotFoundException cnfe) {
            System.out.println("No database drivers found. Quitting");
            System.exit(-1);
        }
        conn = DriverManager.getConnection(db_url + db_name, user, password);
        statement = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

        String createTableSQL = "CREATE TABLE IF NOT EXISTS " + ALCOHOL_TABLE_NAME
                    + " (" + PK_COLUMN + " int NOT NULL AUTO_INCREMENT, "
                    + PRODUCT_COLUMN + " VARCHAR(50), "
                    + BRAND_COLUMN + " VARCHAR(50), "
                    + TYPE_COLUMN + " VARCHAR(50), "
                    + OFFICE_COUNT_COLUMN + " double, "
                    + BAR_COUNT_COLUMN + " double, "
                    + TOTAL_COLUMN + " double, "
                    + ORDER_COLUMN + " VARCHAR(50), "
                    + DISTRIBUTOR_COLUMN + " VARCHAR(50), PRIMARY KEY(" + PK_COLUMN + "))";
            System.out.println(createTableSQL);
            statement.executeUpdate(createTableSQL);

            System.out.println("created liquor table");


        if (statement.getWarnings() == null) {
            addAlcoholData();
        }

    }

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
        statement.executeUpdate(addDataSQL);
    }












}
