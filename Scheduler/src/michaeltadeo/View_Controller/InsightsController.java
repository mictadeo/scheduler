
package michaeltadeo.View_Controller;


import michaeltadeo.Util.DBConnection;
import michaeltadeo.Model.User;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import michaeltadeo.Model.Appointment;
import michaeltadeo.Model.AppointmentReport;
import michaeltadeo.Scheduler;
import static michaeltadeo.View_Controller.AppointmentsController.getAppointmentValues;

public class InsightsController {
    
    @FXML private TableView<AppointmentReport> upperTable;   
    @FXML private TableColumn<AppointmentReport, String> monthColumn;
    @FXML private TableColumn<AppointmentReport, String> appointmentTypeColumn;
    @FXML private TableColumn<AppointmentReport, String> amountColumn;
    @FXML private PieChart pieChart;
    @FXML private TableView<Appointment> lowerTable;
    @FXML private TableColumn<Appointment, LocalDateTime> startColumn;
    @FXML private TableColumn<Appointment, LocalDateTime> endColumn;
    @FXML private TableColumn<Appointment, String> typeColumn;
    @FXML private TableColumn<Appointment, String> customerColumn;
    @FXML private TableColumn<Appointment, String> consultantColumn;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy HH:mm");
    private ObservableList<AppointmentReport> appointmentList;
    private Scheduler application;
    private User currentUser;
    
    //These items will load all necessary contents for the insights.
    
    public void setReports (Scheduler application, User currentUser) {
        this.application = application;
        this.currentUser = currentUser;
    
        fillTypeCountTable ();
        fillPieChart();
        lowerTable.getItems().setAll(getAppointmentValues());
        
        monthColumn.setCellValueFactory(new PropertyValueFactory<>("Month"));
        appointmentTypeColumn.setCellValueFactory(new PropertyValueFactory<>("Type"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("Amount"));
        
        consultantColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
        startColumn.setCellValueFactory(new PropertyValueFactory<>("start"));
        startColumn.setCellFactory(column -> new TableCell<Appointment, LocalDateTime>() {
            protected void updateItem (LocalDateTime date, boolean empty) {
                super.updateItem(date, empty);
                if (empty) 
                    setText("");
                else 
                    setText(formatter.format(date));
            }
        });
        endColumn.setCellValueFactory(new PropertyValueFactory<>("end"));
        endColumn.setCellFactory(column -> new TableCell<Appointment, LocalDateTime>() {
        
            protected void updateItem (LocalDateTime date, boolean empty) {
                super.updateItem(date, empty);
                if (empty) 
                    setText("");
                else 
                    setText(formatter.format(date));
            }
        });
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName")); 
    
    }
    
    private void fillTypeCountTable () {
        appointmentList = FXCollections.observableArrayList();
        
        try{
            //This statement will makes the database connection to retrieve data.
        PreparedStatement statement = DBConnection.getConn().prepareStatement(
            "SELECT MONTHNAME(`start`) AS \"Month\", location AS \"Type\", COUNT(*) as \"Amount\" "
            + "FROM appointment "
            + "GROUP BY MONTHNAME(`start`), location");
            ResultSet rs = statement.executeQuery();
           
            
            while (rs.next()) {
                
                String month = rs.getString("Month");
                
                String type = rs.getString("Type");

                String amount = rs.getString("Amount");
                      
                appointmentList.add(new AppointmentReport(month, type, amount));

            }
            
        } catch (SQLException sqe) {
            System.out.println("Check your SQL");
            sqe.printStackTrace();
        } catch (Exception e) {
            System.out.println("Something besides the SQL went wrong.");
        }
        
        upperTable.getItems().setAll(appointmentList);
    }
    //These items configure the pie chart to retrieve data from the database.
        private void fillPieChart() {          
        ObservableList <PieChart.Data> pieChartData = FXCollections.observableArrayList();      
         try { PreparedStatement ps = DBConnection.getConn().prepareStatement(
                  "SELECT country.country, COUNT(country) "
                + "FROM customer, address, city, country "
                + "WHERE customer.addressId = address.addressId "
                + "AND address.cityId = city.cityId "
                + "AND city.countryId = country.countryId "          
                + "GROUP BY country"); 
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                        String country = rs.getString("country");
                        Integer count = rs.getInt("COUNT(country)");
                        pieChartData.add(new PieChart.Data(country, count));
                }

            } catch (SQLException sqe) {
                System.out.println("Check your SQL");
                sqe.printStackTrace();
            } catch (Exception e) {
                System.out.println("Something besides the SQL went wrong.");
                e.printStackTrace();
            }             
        
        pieChart.setData(pieChartData);
        
    }
 
    @FXML private void backButtonPushed (ActionEvent event) {
        application.appointmentScreen (currentUser);
    }
    
}