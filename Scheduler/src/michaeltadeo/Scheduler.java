
package michaeltadeo;

import michaeltadeo.Util.DBConnection;
import michaeltadeo.Model.User;
import michaeltadeo.Util.LoggerUtil;
import java.io.IOException;
import java.sql.Connection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import michaeltadeo.View_Controller.AppointmentFormController;
import michaeltadeo.View_Controller.AppointmentsController;
import michaeltadeo.View_Controller.CustomerFormController;
import michaeltadeo.View_Controller.CustomersController;
import michaeltadeo.View_Controller.LoginController;
import michaeltadeo.View_Controller.InsightsController;

public class Scheduler extends Application {
    
    private static Connection connection;
    private Stage primaryStage;
    private Stage showStage = new Stage();
    private Scene loginScene;    
    private Scene customerScene;     
    private Scene appointmentScene;
    private Scene calendarScene;
    private Scene insightScene;
            
    @Override
    public void start(Stage primaryStage) throws Exception {        
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Scheduler");           
        loginScreen();
        
    }
    
    public void loginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/michaeltadeo/View_Controller/Login.fxml"));
            Pane loginPane = (Pane) loader.load();
            LoginController lController = loader.getController();
            lController.login (this);
            loginScene = new Scene(loginPane);
            primaryStage.setScene(loginScene);
            primaryStage.show();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    
    public void customerScreen (User currentUser) {
         try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/michaeltadeo/View_Controller/Customers.fxml"));
            Pane customersLayout = (Pane) loader.load();
            customerScene = new Scene(customersLayout);
            primaryStage.setScene(customerScene);
            CustomersController controller = loader.getController();
            controller.showCustomers(this, currentUser);
            primaryStage.show();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    
    public void appointmentScreen (User currentUser) {
         try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/michaeltadeo/View_Controller/Appointments.fxml"));
            Pane apptLayout = (Pane) loader.load();
            appointmentScene = new Scene(apptLayout);
            primaryStage.setScene(appointmentScene);
            AppointmentsController controller = loader.getController();
            controller.setAppointments(this, currentUser);
            primaryStage.show();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    
    
    public void insightScreen (User activeUser) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/michaeltadeo/View_Controller/Insights.fxml"));
            Pane reportsLayout = (Pane) loader.load();
            insightScene = new Scene(reportsLayout);
            primaryStage.setScene(insightScene);
            InsightsController controller = loader.getController();
            controller.setReports(this, activeUser);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void customerFieldsScreen (User currentUser) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/michaeltadeo/View_Controller/CustomerForm.fxml"));
            Pane customerFLayout = (Pane) loader.load();
            Scene scene = new Scene(customerFLayout);
            showStage.setScene(scene);     
            CustomerFormController controller = loader.getController();
            controller.setCustomerFields(this, currentUser);
            showStage.show();
        } catch (IOException e){
            e.printStackTrace();
        }    
    }
    
    public void appointmentFieldsScreen (User currentUser) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/michaeltadeo/View_Controller/AppointmentForm.fxml"));
            Pane appointmentFLayout = (Pane) loader.load();
            Scene scene = new Scene(appointmentFLayout);
            showStage.setScene(scene);
            AppointmentFormController controller = loader.getController();
            controller.setAppointmentFields(this, currentUser);
            showStage.show();
        } catch (IOException e){
            e.printStackTrace();
        }    
    }
    
    /*This function will close the stage or popup form 
    and refresh the data of the current screen*/
    
    public void closeStage (User currentUser) {
        showStage.close();
        
        Scene currentScene = primaryStage.getScene();
        if (currentScene == appointmentScene)
            appointmentScreen(currentUser);
        else if (currentScene == insightScene)
            insightScreen(currentUser);
        else if (currentScene == customerScene)
            customerScreen(currentUser);
            
    }
    
    
    public static void main(String[] args) {
       
        DBConnection.init();
        connection = DBConnection.getConn();
        LoggerUtil.init();

        launch(args);
        DBConnection.closeConn();
    }
    
}