package com.vu;

import com.sun.org.apache.xerces.internal.xs.datatypes.ObjectList;
import jdk.nashorn.internal.scripts.JO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.WindowListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class InventoryGUI extends JFrame implements WindowListener{

    private JLabel productTypeLabel;
    private JLabel alcoholBrandLabel;
    private JLabel alcoholTypeLabel;
    private JLabel officeCountLabel;
    private JLabel barCountLabel;
    private JLabel distributorLabel;

    private JPanel rootPane;
    private JTextField brandTextField;
    private JTextField typeTextField;
    private JTextField officeCountTextField;
    private JTextField barCountTextField;
    private JButton productAddButton;
    private JButton editProductButton;
    private JButton deleteProductButton;
    private JButton exportToExcelButton;
    private JTable inventoryDataTable;
    private JComboBox<String> productTypeComboBox;
    private JComboBox<String> distributorComboBox;

    // This GUI design was based on MovieRatingGUI
    // I got the idea to use the JOptionPane class to edit the product from
    // https://docs.oracle.com/javase/7/docs/api/javax/swing/JOptionPane.html
    // Initialize JTextField objects for the editProductButton
    private JTextField newOfficeCount = new JTextField();
    private JTextField newBarCount = new JTextField();
    // Stores the edit message objects in Array
    private Object [] editMessage = {"Enter new Office count ", newOfficeCount, "Enter new Bar count", newBarCount};

    private final String LIQUOR = "Liquor";
    private final String BEER = "Beer";
    private final String WINE = "Wine";

    private double par = 3;

    private final String HOHENSTEINS = "Hohensteins";
    private final String BREAKTHRU = "BreakThru";
    private final String J_J_TAYLORS = "J. J Taylors";
    private final String SOUTHERN = "Southern Wine and Spirits";
    private final String JOHNSON_BROTHERS = "Johnson Brothers";




    InventoryGUI(final InventoryDataModel inventoryDataTableModel) {

        setContentPane(rootPane);
        pack();
        setTitle("Inventory Database Application");
        addWindowListener(this);
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Sets upt the JTtable
        inventoryDataTable.setGridColor(Color.BLUE);
        inventoryDataTable.setModel(inventoryDataTableModel);

        // Sets up the product type combo box
        productTypeComboBox.addItem(LIQUOR);
        productTypeComboBox.addItem(BEER);
        productTypeComboBox.addItem(WINE);

        // Sets up the distributor combo box
        distributorComboBox.addItem(HOHENSTEINS);
        distributorComboBox.addItem(BREAKTHRU);
        distributorComboBox.addItem(J_J_TAYLORS);
        distributorComboBox.addItem(SOUTHERN);
        distributorComboBox.addItem(JOHNSON_BROTHERS);

        // Sets up product add button
        productAddButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                // Gets product combo box selection and stores it in a string
                String product = (String) productTypeComboBox.getSelectedItem();
                // Gets data from brand text field and stores it in a string
                String brandData = brandTextField.getText();

                // Validation input to make sure the text field is not empty
                if (brandData == null || brandData.trim().equals("")) {
                    JOptionPane.showMessageDialog(rootPane, "Please enter a brand");
                    return;
                }
                // Gets data from type textfield and stores it in a string
                String typeData = typeTextField.getText();

                // Validation input to make sure the text field is not empty
                if (typeData == null || typeData.trim().equals("")) {
                    JOptionPane.showMessageDialog(rootPane, "Please enter a type of product");
                    return;
                }

                // Office count is going to be a double, because 2.7 bottles of vodka is a valid count
                double officeCountData;

                // Try block to catch the number format exception to make sure data input in the text field is
                // in numerical format and a positive number 0 or greater.
                try {
                    officeCountData = Double.parseDouble(officeCountTextField.getText());
                    if (officeCountData < 0) {
                        throw new NumberFormatException("Office count needs to be a number 0 or greater");
                    }
                } catch (NumberFormatException ne) {
                    JOptionPane.showMessageDialog(rootPane, "Office count needs to be a number 0 or greater");
                    return;
                }

                // Bar count is also going to be a double
                double barCountData;

                // Try block to catch the number format exception to make sure data input in the text field is
                // in numerical format and a positive number 0 or greater.
                try {
                    barCountData = Double.parseDouble(barCountTextField.getText());
                    if (barCountData < 0) {
                        throw new NumberFormatException("Office count needs to be a number 0 or greater");
                    }
                } catch (NumberFormatException ne) {
                    JOptionPane.showMessageDialog(rootPane, "Office count needs to be a number 0 or greater");
                    return;
                }

                String distributorData = (String)distributorComboBox.getSelectedItem();

                // Message on the backend to confirm the data
                System.out.println("Adding " + product + " " + brandData + " " + typeData + " " + officeCountData + " " + " " + barCountData +
                        " " + distributorData);

                // Calculates the total amount of bar and office count, calls the getTotalAmount method from
                // the InventoryDatabase class
                double totalAmount = InventoryDatabase.getTotalAmount(officeCountData, barCountData);

                // Determines whether or not the product needs to be ordered, calls the the getOrder method
                // from the InventoryDatabase class, par amount is pre-determined
                String orderMore = InventoryDatabase.getOrder(totalAmount, par);

                // Insert the data to the inventoryDataTableModel by calling the insertRow method from the
                // InventoryDataModel class, boolean insertRow to confirm the addition of new data
                boolean insertRow = inventoryDataTableModel.insertRow(product, brandData, typeData, officeCountData, barCountData, totalAmount,
                        orderMore, distributorData);

                // If adding new product is unsuccessful, show this message.
                // This message will be displayed if user attempts to add duplicate product, since there is
                // a unique constraint set up on the database level
                if (!insertRow) {
                    JOptionPane.showMessageDialog(rootPane, "Error adding new product");
                }
            }

        });

        // Set up the edit product button
        editProductButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                // The row on the JTable that user selected
                int editRow = inventoryDataTable.getSelectedRow();
                double newOfficeEditCount;
                double newBarEditCount;

                // Make sure user selects a product row to edit
                if (editRow == -1 ) {
                    JOptionPane.showMessageDialog(rootPane, "Please choose a product to edit");

                } else {
                    // Displays edit product in a popup window with text fields for new office count and new bar count
                    int option = JOptionPane.showConfirmDialog(null, editMessage, "Edit Product Quantity", JOptionPane.OK_CANCEL_OPTION);
                    // Try block to catch the number format exception to make sure data input in the text field is
                    // in numerical format and a positive number 0 or greater.
                    try {
                        newOfficeEditCount = Double.parseDouble(newOfficeCount.getText());
                        if (newOfficeEditCount < 0) {
                            throw new NumberFormatException("Office count must be 0 or greater");
                        }
                    } catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(rootPane, "Office count must be 0 or greater");
                        return;
                    }
                    // Try block to catch the number format exception to make sure data input in the text field is
                    // in numerical format and a positive number 0 or greater.
                    try {
                        newBarEditCount = Double.parseDouble(newBarCount.getText());
                        if (newBarEditCount < 0) {
                            throw new NumberFormatException("Bar count must be 0 or greater");
                        }
                    } catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(rootPane, "Bar count must be 0 or greater");
                        return;
                    }

                    // Calculates the new total
                    double newTotal = newOfficeEditCount + newBarEditCount;
                    // Changes the par
                    String orderUpdate = InventoryDatabase.getOrder(newTotal, par);
                    // Updates the data to the inventoryDataTableModel by calling the insertRow method from the
                    // InventoryDataModel class, boolean insertRow to confirm the addition of new data
                    boolean updateRow = inventoryDataTableModel.updateRow(newOfficeEditCount, newBarEditCount, newTotal, orderUpdate);

                    // If updating the row unsuccessful, show this message
                    if (!updateRow) {
                        JOptionPane.showMessageDialog(rootPane, "Error editing quantity");
                    }
                }


                }

        });

        // Set up the delete button product
        deleteProductButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Select a row from JTable to delete
                int deleteRow = inventoryDataTable.getSelectedRow();
                // Make sure user select a product row to delete
                if (deleteRow == -1) {
                    JOptionPane.showMessageDialog(rootPane, "Please choose a product to delete");
                } else {
                    try {
                        // Popup window with a yes or no option to confirm row deletion
                        int confirmDelete = JOptionPane.showConfirmDialog(null, "Are you sure " +
                                "you want to delete item? ","Delete",JOptionPane.YES_NO_OPTION);
                        if (confirmDelete == JOptionPane.YES_OPTION) {
                            // Deletes the row from JTable
                            inventoryDataTableModel.deleteRow(deleteRow);
                        }
                        // Updates the database and JTable
                        InventoryDatabase.loadAllProduct();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(rootPane, "Error deleting product");
                    }
                }
            }
        });

        // Set up the export to excel button
        exportToExcelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                // if JTable is empty, cannot export an empty table to excel file
                if (inventoryDataTable == null) {
                    JOptionPane.showMessageDialog(rootPane,"inventory empty");
                }
                // Try block to catch SQLException
                try {

                    // Popup window to confirm exporting data from database to excel file
                    int exportData = JOptionPane.showConfirmDialog(null,"Export data to Excel file?",
                            "Export to Excel",JOptionPane.YES_NO_OPTION);
                    // If user clicks yes
                    if (exportData == JOptionPane.YES_OPTION) {
                        // Create a table called order_list in the database
                        InventoryDatabase.createOrderTable();
                        // Loads order_list table ResultSet
                        InventoryDatabase.loadOrderProduct();
                        // Writes data to excel file
                        WriteToExcel.ExportToExcel();
                    }
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                }

            }
        });




    }


    @Override
    public void windowClosing(WindowEvent e){
        // Try block to catch SQLException
        try {
            // JOptionPane yes no option to delete order_list table upon exiting the application
            int deleteOrderTable = JOptionPane.showConfirmDialog(null, "Delete the order table?",
                    "Delete order table", JOptionPane.YES_NO_OPTION);
            if (deleteOrderTable == JOptionPane.YES_OPTION) {
                InventoryDatabase.deleteOrderTable();
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }
            // Shutdown database on closing
            System.out.println("closing");
            InventoryDatabase.shutdown();

        }

    @Override
    public void windowClosed(WindowEvent e) {}
    @Override
    public void windowOpened(WindowEvent e) {}
    @Override
    public void windowIconified(WindowEvent e) {}
    @Override
    public void windowDeiconified(WindowEvent e) {}
    @Override
    public void windowActivated(WindowEvent e) {}
    @Override
    public void windowDeactivated(WindowEvent e) {}

        }


