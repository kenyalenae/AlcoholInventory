package com.vu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.WindowListener;
import java.util.ArrayList;

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

        inventoryDataTable.setGridColor(Color.BLUE);
        inventoryDataTable.setModel(inventoryDataTableModel);

        productTypeComboBox.addItem(LIQUOR);
        productTypeComboBox.addItem(BEER);
        productTypeComboBox.addItem(WINE);

        distributorComboBox.addItem(HOHENSTEINS);
        distributorComboBox.addItem(BREAKTHRU);
        distributorComboBox.addItem(J_J_TAYLORS);
        distributorComboBox.addItem(SOUTHERN);
        distributorComboBox.addItem(JOHNSON_BROTHERS);


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


        deleteProductButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int currentRow = inventoryDataTable.getSelectedRow();

                if (currentRow == -1) {
                    JOptionPane.showMessageDialog(rootPane, "Please choose a product to delete");
                    // TODO add yes or no box to delete
                } else {
                    try {
                        inventoryDataTableModel.deleteRow(currentRow);
                        InventoryDatabase.loadAllProduct();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(rootPane, "Error deleting product");
                    }
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


