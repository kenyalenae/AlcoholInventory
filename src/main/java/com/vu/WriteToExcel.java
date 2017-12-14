package com.vu;

import com.sun.xml.internal.ws.api.ha.StickyFeature;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;

// Author: Vu Tran

//I got the code to export result set to Database from
// http://thinktibits.blogspot.com/2012/12/POI-Create-Excel-File-JDBC-Oracle-Table-Data-Example.html
public class WriteToExcel extends InventoryDatabase{

    // Global variables
    private static String db_url = "jdbc:mysql://localhost:3306/";
    private static String JDBC = "com.mysql.jdbc.Driver";
    private static final String db_name = "alcohol_inventory";
    private static final String user = System.getenv("MYSQL_USER");
    private static final String password = System.getenv("MYSQL_PASSWORD");

    // Output file path
    private static final String FILE_NAME = "src/OrderList.xls";

    // Initialize the database connection variables
    private static Statement statement = null;
    private static Connection conn = null;
    private static ResultSet orderQuerySet = null;

    // Class to connect to database and export excel file
    public static void ExportToExcel() throws SQLException{
        // Connects to the database
        try {
            Class.forName(JDBC);
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            System.out.println("JDBC drivers missing");
        }

        try {
            conn = DriverManager.getConnection(db_url + db_name, user, password);
            statement = conn.createStatement();

            // Create excel workbook and worksheer objects
            HSSFWorkbook orderWorkBook = new HSSFWorkbook();
            HSSFSheet orderSheet = orderWorkBook.createSheet("Order List");

            // SQL query to retrieve everything from order_list table
            orderQuerySet = statement.executeQuery("SELECT * FROM order_list");

            // Hashmap to store excel data
            HashMap <String, Object []> excelOrderData = new HashMap<>();
            int rowCounter = 0;
            // Put the result set into Hashmap
            while (orderQuerySet.next()) {
                rowCounter++;
                String brand = orderQuerySet.getString(InventoryDatabase.BRAND_COLUMN);
                String type = orderQuerySet.getString(InventoryDatabase.TYPE_COLUMN);
                String total_amount = orderQuerySet.getString(InventoryDatabase.TOTAL_COLUMN);
                String distributor = orderQuerySet.getString(InventoryDatabase.DISTRIBUTOR_COLUMN);
                excelOrderData.put(Integer.toString(rowCounter),new Object[] {brand, type, total_amount, distributor});
            }

            // CLose Database Objects
            orderQuerySet.close();
            statement.close();
            conn.close();

            // Load data into logical worksheet
            Set <String> keyset = excelOrderData.keySet();
            int rowNum = 0;
            // Loop through the Hashmap data and add them to the cell
            for (String product: keyset) {
                Row row = orderSheet.createRow(rowNum++);
                Object [] productArr = excelOrderData.get(product);
                int cellNum = 0;
                for (Object obj: productArr) {
                    Cell cell = row.createCell(cellNum++);
                    if (obj instanceof Double) {
                        cell.setCellValue((Double)obj);
                    } else {
                        cell.setCellValue((String) obj);
                    }
                }
            }
            try {
                try {
                    // Creates xls file
                    FileOutputStream excelOutputFile = new FileOutputStream(new File(FILE_NAME));
                    // Writes to the file
                    orderWorkBook.write(excelOutputFile);
                    // Closes the file
                    excelOutputFile.close();
                } catch (FileNotFoundException fne) {
                    fne.printStackTrace();
                    System.out.println("File not found");
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
                System.out.println("Could not write to file");
            }

        } catch (SQLException sqle) {
            sqle.printStackTrace();
            System.out.println("Cannot connect to database");
        }
    }

}