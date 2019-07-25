
package michaeltadeo.View_Controller;


import michaeltadeo.Util.DBConnection;
import michaeltadeo.Model.Address;
import michaeltadeo.Model.User;
import michaeltadeo.Model.Customer;
import michaeltadeo.Model.Country;
import michaeltadeo.Model.City;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import michaeltadeo.Scheduler;


public class CustomerFormController {

    @FXML private TextField nameField;
    @FXML private TextField streetField;
    @FXML private TextField street2Field;
    @FXML private TextField cityField;
    @FXML private TextField postalField;
    @FXML private TextField countryField;
    @FXML private TextField phoneField;
    @FXML private Text errorMessage;
    private Scheduler application;
    private User currentUser;
    private final  Connection connection = DBConnection.getConn();
    
    
    /*This method will validate user input & prompt an
    error message if values are incorrect.*/
    
    private String userInput ()
    {
        String name = nameField.getText();
        String street = streetField.getText();
        String city = cityField.getText();
        String postal = postalField.getText();
        String country = countryField.getText();
        String phone = phoneField.getText();
        String errorMsg = "";
        
        if (name.length() == 0)
            errorMsg += "Please enter the customer's name. \n";
        if (street.length() == 0)
            errorMsg += "Please enter the customer's street address. \n";
        if (city.length() == 0)
            errorMsg += "Please enter the customer's city. \n";
        /*Added regular expression "\\D+" which checks for one or more characters
        that is not a number*/
        if (postal.length() == 0)
            errorMsg += "Please enter the customer's postal code. \n";
        else if (postal.length() < 4 || postal.length() > 10 || postal.matches("\\D+"))
            errorMsg += "Please enter a valid postal code. \n";
        if (country.length() == 0)
            errorMsg += "Please enter the customer's country. \n";
        if (phone.length() == 0)
            errorMsg += "Please enter the customer's phone number. \n";
        else if (phone.length() < 8 || phone.length() > 15 || phone.matches("\\D+"))  
            errorMsg += "Please enter a valid phone number including "
                    + "\n country code and area code. \n";

        return errorMsg;
    }
    
    /*This configures the save Button that saves customer data.
    It checks for invalid input & makes sure that fields are filled out.*/
    
    @FXML private void saveButtonPushed (ActionEvent e) {
        
        String name = nameField.getText();
        String street = streetField.getText();
        String street2 = street2Field.getText();
        String city = cityField.getText();
        String postal = postalField.getText();
        String country = countryField.getText();
        String phone = phoneField.getText();
        
        String addressQuery = null;
        Country currentCountry = null;
        Address currentAddress = null;
        
        String errorInput = userInput ();
        if (!errorInput.isEmpty())
            errorMessage.setText(errorInput);
        
        else {
            if (CustomersController.modifyCustomer()== null){ 
            try {            
                currentCountry = Country.validateCountry(country);
                if (currentCountry == null)
                {
                    String countryQuery = "INSERT INTO country ("
                    + " country, createDate, createdBy,"
                    + " lastUpdate, lastUpdateBy) VALUES ("
                    + " ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?)"; 

                    try (PreparedStatement sCountry = connection.prepareStatement(countryQuery)) {
                         sCountry.setString(1, country);
                         sCountry.setString(2, currentUser.getUsername());
                         sCountry.setString(3, currentUser.getUsername());
                         sCountry.executeUpdate();
                         currentCountry = Country.validateCountry(country);
                     }
                }          
               
               City currentCity = City.validateCity(city, currentCountry.getCountryId());
               if (currentCity == null) 
               {
                    String cityQuery = "INSERT INTO city ("
                    + " city, countryId, createDate, createdBy,"
                    + " lastUpdate, lastUpdateBy) VALUES ("
                    + " ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?)"; 
                    PreparedStatement sCity = connection.prepareStatement(cityQuery);
                    sCity.setString(1, city);
                    sCity.setInt(2, currentCountry.getCountryId());
                    sCity.setString(3, currentUser.getUsername());
                    sCity.setString(4, currentUser.getUsername());
                    sCity.executeUpdate();
                    currentCity = City.validateCity(city, currentCountry.getCountryId());
                    sCity.close();
               }
               
               currentAddress = Address.validateAddress(street, street2, 
                       currentCity.getCityId(), postal, phone);
               if (currentAddress == null) 
               {
                    addressQuery = "INSERT INTO address ("
                    + " address, address2, cityId,"
                    + " postalCode, phone, createDate, createdBy,"
                    + " lastUpdate, lastUpdateBy) VALUES ("
                    + " ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?)";
               }
              
                PreparedStatement sAddress = connection.prepareStatement(addressQuery);
                sAddress.setString(1, street);
                sAddress.setString(2, street2);
                sAddress.setInt(3, currentCity.getCityId());
                sAddress.setString(4, postal);
                sAddress.setString(5, phone);
                sAddress.setString(6, currentUser.getUsername());
                sAddress.setString(7, currentUser.getUsername());
                
                sAddress.executeUpdate();
                currentAddress = Address.validateAddress(street, street2, currentCity.getCityId(), postal, phone);
                sAddress.close();
                
                String query = "INSERT INTO customer ("              
                + " customerName,"
                + " addressId,"
                + " active,"
                + " createDate,"
                + " createdBy,"
                + " lastUpdate,"
                + " lastUpdateBy) VALUES ("
                + "?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?)";            
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, name);
                statement.setInt(2, currentAddress.getAddressId());  
                statement.setInt(3, 1); 
                statement.setString(4, currentUser.getUsername());
                statement.setString(5, currentUser.getUsername());
                statement.executeUpdate();
                statement.close();
                
            } catch (SQLException ex) {
                ex.printStackTrace();
                }
        }  
               
        else if (CustomersController.modifyCustomer()!= null) 
        {
            try {
                String updateQuery = "UPDATE customer, address, city, country"
                + " SET customerName = ?,"
                + " customer.lastUpdate = CURRENT_TIMESTAMP,"
                + " customer.lastUpdateBy = ?,"
                + " address.address = ?,"
                + " address.address2 = ?,"
                + " address.postalCode = ?,"
                + " address.phone = ?,"        
                + " address.lastUpdate = CURRENT_TIMESTAMP,"
                + " address.lastUpdateBy = ?,"
                + " city.city = ?,"
                + " city.lastUpdate = CURRENT_TIMESTAMP,"
                + " city.lastUpdateby = ?,"
                + " country.country = ?,"
                + " country.lastUpdate = CURRENT_TIMESTAMP,"
                + " country.lastUpdateBy = ?"
                + " WHERE customer.customerId = ?"
                + " AND customer.addressId = address.addressId"
                + " AND address.cityId = city.cityId"
                + " AND city.countryId = country.countryId";
                int i = 1;
                PreparedStatement statement = connection.prepareStatement(updateQuery);
                statement.setString(i++, name);
                statement.setString(i++, currentUser.getUsername());
                statement.setString(i++, street);
                statement.setString(i++, street2);
                statement.setString(i++, postal);
                statement.setString(i++, phone);
                statement.setString(i++, currentUser.getUsername());
                statement.setString(i++, city);
                statement.setString(i++, currentUser.getUsername());
                statement.setString(i++, country);
                statement.setString(i++, currentUser.getUsername());
                statement.setInt(i++, CustomersController.modifyCustomer().getId());
                statement.executeUpdate();
                statement.close();
             } catch (SQLException ex) {
                ex.printStackTrace();
                }
            }
            CustomersController.resetCustomer();
            application.closeStage(currentUser);  
        }
    }
    
    @FXML private void cancelButtonPushed(ActionEvent e) {
        Alert cancelAlert = new Alert (AlertType.CONFIRMATION);
        cancelAlert.setTitle("Cancel Adding/Modifying Customer Data?");
        cancelAlert.setHeaderText("Are you sure you want to cancel "
                + "and return to the customers screen?");
        Optional<ButtonType> result = cancelAlert.showAndWait();
        if (result.get() == ButtonType.OK) {
            CustomersController.resetCustomer();
            application.closeStage(currentUser);

        }
    }
    
    public void setCustomerFields(Scheduler application, User activeUser){
        this.application = application;
        this.currentUser = activeUser;
        
        Customer editCustomer = CustomersController.modifyCustomer();
        if (editCustomer != null)
        {
            nameField.setText(editCustomer.getName());
            streetField.setText(editCustomer.getAddress());
            street2Field.setText(editCustomer.getAddress2());
            cityField.setText(editCustomer.getCity());
            postalField.setText(editCustomer.getZip());
            countryField.setText(editCustomer.getCountry());
            phoneField.setText(editCustomer.getPhone());
        }
    }
}
