
package michaeltadeo.View_Controller;


import michaeltadeo.Util.DBConnection;
import michaeltadeo.Model.User;
import michaeltadeo.Model.Customer;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import michaeltadeo.Scheduler;


public class CustomersController {

    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, Integer> idColumn;
    @FXML private TableColumn<Customer, String> nameColumn;
    @FXML private TableColumn<Customer, String> streetColumn;
    @FXML private TableColumn<Customer, String> address2Column;
    @FXML private TableColumn<Customer, String> cityColumn;
    @FXML private TableColumn<Customer, String> countryColumn;
    @FXML private TableColumn<Customer, String> postalColumn;
    @FXML private TableColumn<Customer, String> phoneColumn;
    private Scheduler application;
    private User currentUser;
    private static Customer selectedCustomer;
    
    
    @FXML private void newButtonPushed (ActionEvent e) throws IOException {
        resetCustomer();
        application.customerFieldsScreen(currentUser);
    }
    
    /*This item configures the Modify Button. Once the button is pushed
    & a customer is selected in the table view, it will open the Customer Field Screen. 
    If not, it will prompt the user to select.*/
    
    @FXML private void modifyButtonPushed (ActionEvent e) throws IOException {
        
        selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        
        if (selectedCustomer == null) {
            Alert noCustomerAlert = new Alert (Alert.AlertType.WARNING);
            noCustomerAlert.setTitle("No Customers Selected");
            noCustomerAlert.setHeaderText("Error: No Customers Selected");
            noCustomerAlert.setContentText("Please select a customer and try again.");
            noCustomerAlert.showAndWait();
        }
        else 
          application.customerFieldsScreen(currentUser);
    }
    
    /*This item configures the Delete Button. Once the button is pushed
    & a customer is selected in the table view, it will ask for deletion confirmation. 
    */
    
    @FXML private void deleteButtonPushed (ActionEvent e) {
        
   
        selectedCustomer = customerTable.getSelectionModel().getSelectedItem();

        if (selectedCustomer == null) {
            Alert noCustomerAlert = new Alert (Alert.AlertType.WARNING);
            noCustomerAlert.setTitle("No Customers Selected");
            noCustomerAlert.setHeaderText("Error: No Customers Selected");
            noCustomerAlert.setContentText("Please select a customer and try again.");
            noCustomerAlert.showAndWait();
        }
        else {
            Alert deleteCustomerAlert = new Alert (Alert.AlertType.CONFIRMATION);
            deleteCustomerAlert.setTitle("Confirm Customer Deletion");
            deleteCustomerAlert.setHeaderText("Are you sure you want to delete " + selectedCustomer.getName() + "?");
            deleteCustomerAlert.setContentText("This cannot be undone! All of " + selectedCustomer.getName() + " will also be deleted.");
            Optional<ButtonType> result = deleteCustomerAlert.showAndWait();
            if (result.get() == ButtonType.OK) {
                try {
                String deleteQuery = "DELETE customer.*, address.*"
                        + " FROM customer, address"
                        + " WHERE customer.customerId = ?"
                        + " AND customer.addressId = address.addressId";
                PreparedStatement deleteSmt = DBConnection.getConn().prepareStatement(deleteQuery);
                deleteSmt.setInt(1, selectedCustomer.getId());
                deleteSmt.executeUpdate();
               } catch (SQLException exc) {
                   exc.printStackTrace();;
               }
               // reload the customers list
               showCustomers (application, currentUser);
            }
        }
    }
    
    @FXML private void backButtonPushed (ActionEvent e) {
        application.appointmentScreen (currentUser);
    }

    // This item fills the table with customer data.
    
    public void showCustomers (Scheduler application, User activeUser) {
        
        this.application = application;
        this.currentUser = activeUser;
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        streetColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        address2Column.setCellValueFactory(new PropertyValueFactory<>("address2"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        countryColumn.setCellValueFactory(new PropertyValueFactory<>("country"));
        postalColumn.setCellValueFactory(new PropertyValueFactory<>("zip"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        customerTable.getItems().setAll(getData());
        
    }
    
    public List<Customer> getData (){
        ObservableList<Customer> customerList = FXCollections.observableArrayList();
        try {
            String customerQuery = "SELECT customer.customerId, customer.customerName, "
                + "address.address, address.address2, address.postalCode, city.cityId, "
                + "city.city, country.country, address.phone " 
                + "FROM customer, address, city, country " 
                + "WHERE customer.addressId = address.addressId "
                + "AND address.cityId = city.cityId AND city.countryId = country.countryId "
                + "ORDER BY customer.customerId";
        
            PreparedStatement smt = DBConnection.getConn().prepareStatement(customerQuery);
            ResultSet customersFound = smt.executeQuery();

            while (customersFound.next()) {
                Integer dCustomerId = customersFound.getInt("customer.customerId");
                String dCustomerName = customersFound.getString("customer.customerName");
                String dAddress = customersFound.getString("address.address");
                String dAddress2 = customersFound.getString("address.address2");
                String dCity = customersFound.getString("city.city");
                String dPostalCode = customersFound.getString("address.postalCode");
                String dCountry = customersFound.getString("country.country");
                String dPhone = customersFound.getString("address.phone");
                customerList.add(new Customer (dCustomerId, dCustomerName, 
                        dAddress, dAddress2, dCity, dCountry, dPostalCode, dPhone));
            }
        } catch (SQLException sqe) {
            sqe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return customerList;
    }
    
    public static Customer modifyCustomer() {
        return selectedCustomer;
    }
    public static void resetCustomer() {
        selectedCustomer = null;
    }
    public static void setCustomer(Customer c) {
        selectedCustomer = c;
    }
}