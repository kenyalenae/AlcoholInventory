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
    private JPanel editPane;
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
    private JTextField newOfficeCount = new JTextField();
    private JTextField newBarCount = new JTextField();
    Object [] editMessage = {"Enter new Office count ", newOfficeCount, "Enter new Bar count", newBarCount};

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

                String product = (String) productTypeComboBox.getSelectedItem();

                String brandData = brandTextField.getText();

                if (brandData == null || brandData.trim().equals("")) {
                    JOptionPane.showMessageDialog(rootPane, "Please enter a brand");
                    return;
                }

                String typeData = typeTextField.getText();
                if (typeData == null || typeData.trim().equals("")) {
                    JOptionPane.showMessageDialog(rootPane, "Please enter a type of product");
                    return;
                }

                double officeCountData;

                try {
                    officeCountData = Double.parseDouble(officeCountTextField.getText());
                    if (officeCountData < 0) {
                        throw new NumberFormatException("Office count needs to be a number 0 or greater");
                    }
                } catch (NumberFormatException ne) {
                    JOptionPane.showMessageDialog(rootPane, "Office count needs to be a number 0 or greater");
                    return;
                }

                double barCountData;

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

                if (distributorData == null || distributorData.trim().equals("")) {
                    JOptionPane.showMessageDialog(rootPane, "Please enter a distributor for the product");
                    return;
                }

                System.out.println("Adding " + product + " " + brandData + " " + typeData + " " + officeCountData + " " + " " + barCountData +
                        " " + distributorData);

                double totalAmount = InventoryDatabase.getTotalAmount(officeCountData, barCountData);

                String orderMore = InventoryDatabase.getOrder(totalAmount, par);

                boolean insertRow = inventoryDataTableModel.insertRow(product, brandData, typeData, officeCountData, barCountData, totalAmount,
                        orderMore, distributorData);

                if (!insertRow) {
                    JOptionPane.showMessageDialog(rootPane, "Error adding new product");
                }
            }

        });

        editProductButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int editRow = inventoryDataTable.getSelectedRow();
                try {
                    if (editRow == -1) {
                        JOptionPane.showMessageDialog(rootPane, "Please choose a product to edit");

                    } else {
                        int option = JOptionPane.showConfirmDialog(null, editMessage, "Edit Product Quantity", JOptionPane.OK_CANCEL_OPTION);

                        double newOfficeEditCount = Double.parseDouble(newOfficeCount.getText());
                        double newBarEditCount = Double.parseDouble(newBarCount.getText());

                        double newTotal = newOfficeEditCount + newBarEditCount;
                        String orderUpdate = InventoryDatabase.getOrder(newTotal,par);

                        boolean updateRow = inventoryDataTableModel.updateRow(newOfficeEditCount, newBarEditCount, newTotal,orderUpdate);
                        if (!updateRow) {
                            JOptionPane.showMessageDialog(rootPane,"Error editing quantity");
                        }
                    }
                } catch (NumberFormatException ne) {
                    JOptionPane.showMessageDialog(rootPane, "Please enter a number 0 or greater");
                }
            }
        });


        deleteProductButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int deleteRow = inventoryDataTable.getSelectedRow();

                if (deleteRow == -1) {
                    JOptionPane.showMessageDialog(rootPane, "Please choose a product to delete");
                } else {
                    try {
                        int confirmDelete = JOptionPane.showConfirmDialog(null, "Are you sure " +
                                "you want to delete item? ","Delete",JOptionPane.YES_NO_OPTION);
                        if (confirmDelete == JOptionPane.YES_OPTION) {
                            inventoryDataTableModel.deleteRow(deleteRow);
                        }
                        InventoryDatabase.loadAllProduct();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(rootPane, "Error deleting product");
                    }
                }
            }
        });

        exportToExcelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (inventoryDataTable == null) {
                    JOptionPane.showMessageDialog(rootPane,"inventory empty");
                }
                try {
                    int exportData = JOptionPane.showConfirmDialog(null,"Export data to Excel file?",
                            "Export to Excel",JOptionPane.YES_NO_OPTION);
                    if (exportData == JOptionPane.YES_OPTION) {
                        InventoryDatabase.createOrderTable();
                        InventoryDatabase.loadOrderProduct();
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


