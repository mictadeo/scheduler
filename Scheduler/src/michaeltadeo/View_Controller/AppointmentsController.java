
package michaeltadeo.View_Controller;


import michaeltadeo.Util.DBConnection;
import michaeltadeo.Model.Appointment;
import michaeltadeo.Model.User;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import michaeltadeo.Scheduler;



public class AppointmentsController {

    //Upper Table Configurations.
    
    @FXML private TableView<Appointment> upperTable;
    @FXML private TableColumn<Appointment, LocalDateTime> startColumn;
    @FXML private TableColumn<Appointment, LocalDateTime> endColumn;
    @FXML private TableColumn<Appointment, String> titleColumn;
    @FXML private TableColumn<Appointment, String> descriptionColumn;
    @FXML private TableColumn<Appointment, String> typeColumn;
    @FXML private TableColumn<Appointment, String> customerColumn;
    @FXML private TableColumn<Appointment, String> consultantColumn;
    private Scheduler application;
    private User currentUser;
    private static Appointment selectedAppointment;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy HH:mm");
    
    
    @FXML private void customerButtonPushed (ActionEvent e) throws IOException {
        application.customerScreen(currentUser);
    }
    
    @FXML private void insightsButtonPushed (ActionEvent e) throws IOException {
        application.insightScreen(currentUser);
    }
    
    @FXML private void xButtonPushed (ActionEvent event) throws Exception {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logging out...");
        alert.setHeaderText("Are you sure you want to Logout from Scheduler?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK)
        application.loginScreen();
        
    }
    
    @FXML private void newButtonPushed (ActionEvent e) throws IOException {
        resetSelectedItem();
        application.appointmentFieldsScreen(currentUser);
    }
    
      
    @FXML private void modifyButtonPushed (ActionEvent e) throws IOException {
        selectedAppointment = upperTable.getSelectionModel().getSelectedItem();
       
        if (selectedAppointment != null)
            application.appointmentFieldsScreen(currentUser);
        

        if (lowerTable.getSelectionModel().getSelectedItem().isLeaf()) {
           AppointmentsController.setSelectedItem(lowerTable.getSelectionModel().getSelectedItem().getValue());
           application.appointmentFieldsScreen(currentUser);
        }           
            
    }
    
    @FXML private void deleteButtonPushed (ActionEvent e) throws IOException {
        selectedAppointment = upperTable.getSelectionModel().getSelectedItem();      
        
        if (selectedAppointment != null)
        {
            Alert deleteAlert = new Alert (Alert.AlertType.CONFIRMATION);
            deleteAlert.setTitle("Delete Appointment?");
            deleteAlert.setHeaderText("Are you sure you want to delete this appointment?");
            Optional<ButtonType> result = deleteAlert.showAndWait();
            if (result.get() == ButtonType.OK)
            {
               try {
                String deleteQuery = "DELETE appointment.*"
                        + " FROM appointment"
                        + " WHERE appointment.appointmentId = ?";
                PreparedStatement deleteStatement = DBConnection.getConn().prepareStatement(deleteQuery);
                deleteStatement.setInt(1, selectedAppointment.getAppointmentId());
                deleteStatement.executeUpdate();
                application.appointmentScreen(currentUser);
               } catch (SQLException exc)
               {
                   exc.printStackTrace();;
               }
            }
        } else if (lowerTable.getSelectionModel().getSelectedItem().isLeaf()){
            
            AppointmentsController.setSelectedItem(lowerTable.getSelectionModel().getSelectedItem().getValue());
            Alert deleteAlert = new Alert (Alert.AlertType.CONFIRMATION);
            deleteAlert.setTitle("Delete Appointment?");
            deleteAlert.setHeaderText("Are you sure you want to delete this appointment?");
            Optional<ButtonType> result = deleteAlert.showAndWait();
            if (result.get() == ButtonType.OK)
            {
               try {
                String deleteQuery = "DELETE appointment.*"
                        + " FROM appointment"
                        + " WHERE appointment.appointmentId = ?";
                PreparedStatement deleteStatement = DBConnection.getConn().prepareStatement(deleteQuery);
                deleteStatement.setInt(1, selectedAppointment.getAppointmentId());
                deleteStatement.executeUpdate();
                application.appointmentScreen(currentUser);
               } catch (SQLException exc)
               {
                   exc.printStackTrace();;
               }
            }
        }
        else
        {
            Alert nonSelectedAlert = new Alert (Alert.AlertType.WARNING);
            nonSelectedAlert.setTitle("No Appointment Selected");
            nonSelectedAlert.setHeaderText("Error: No Appointment Selected");
            nonSelectedAlert.setContentText("Please select an appointment and try again.");
            nonSelectedAlert.showAndWait();   
        }
    }
    
    
    //Lower table Configurations.
    
    @FXML private RadioButton monthButton; 
    @FXML private RadioButton weekButton;
    @FXML private CheckBox consultationBox;
    @FXML private CheckBox clientIntakeBox;
    @FXML private CheckBox phoneInterviewBox;
    @FXML private CheckBox accountClosureBox;
    @FXML private Label resultLabel;
    @FXML private TreeTableView<Appointment> lowerTable;
    @FXML private TreeTableColumn<Appointment, String> monthTreeColumn;
    @FXML private TreeTableColumn<Appointment, LocalDateTime> startTreeColumn;
    @FXML private TreeTableColumn<Appointment, LocalDateTime> endTreeColumn;
    @FXML private TreeTableColumn<Appointment, String> titleTreeColumn;
    @FXML private TreeTableColumn<Appointment, String> customerTreeColumn;
    @FXML private TreeTableColumn<Appointment, String> consultantTreeColumn;
    @FXML private TreeTableColumn<Appointment, String> typeTreeColumn;
    ObservableList<Appointment> appointmentList = FXCollections.observableArrayList();

    
    @FXML private void monthButtonPushed() {
        
        Appointment mainAppointment = new Appointment("Month");
        TreeItem<Appointment> mainRoot = new TreeItem(mainAppointment);
        
        ObservableList<TreeItem<Appointment>> subRootMonth = FXCollections.observableArrayList();
        ObservableList<TreeItem<Appointment>> removeSubRootMonth = FXCollections.observableArrayList(); 

        String[] monthArray = {"January", "February", "March", "April", "May", "June", "July", "August",
             "September", "October", "November", "December", "Over 1 Year", "Elapsed"}; 
        for (int i = 0; i < monthArray.length; i++)
        {    
            subRootMonth.add(new TreeItem(new Appointment(monthArray[i])));
        }
        
        /* The "mlamb" & "mlamb2" variables are assigned as objects of the Consumer interface
        using Lambda expressions. They can be used as objects to be excecuted on demand 
        in order to simplify processes.
        */

        Consumer<Appointment> mLamb = (Appointment a) -> {
                    if (a.getStart().isBefore(LocalDateTime.now())) 
                        subRootMonth.get(13).getChildren().add(new TreeItem(a));
                    
                    else if (a.getStart().isAfter(LocalDateTime.now().plusYears(1))) { 
                    
                        subRootMonth.get(12).getChildren().add(new TreeItem(a));
                        subRootMonth.get(12).setExpanded(true);
                    }
                    else {
                        for (int i = 0; i<monthArray.length; i++)
                        {
                            if(a.getStart().getMonth().toString().equalsIgnoreCase(monthArray[i]))
                            {
                               subRootMonth.get(i).getChildren().add(new TreeItem(a));
                               subRootMonth.get(i).setExpanded(true);
                               break;
                            }
                        }
                    } 
                };
        
        Consumer<TreeItem<Appointment>> mLamb2 = (TreeItem<Appointment> a) -> {
                    if (a.isLeaf())removeSubRootMonth.add(a);
                };
       appointmentList.forEach(mLamb); 
        
        // These items will clear empty month nodes.
        subRootMonth.forEach(mLamb2);
        subRootMonth.removeAll(removeSubRootMonth);  
        
        /* These items will set available month values in the lower table
        & expand the root to show its values by default.
        */
        mainRoot.getChildren().addAll(subRootMonth);
        mainRoot.setExpanded(true);
        lowerTable.setRoot(mainRoot);
    }
    
    @FXML private void weekButtonPushed () {
        
        TreeItem<Appointment> mainRoot = new TreeItem(new Appointment("Week"));
        ObservableList<TreeItem<Appointment>> subRootWeek = FXCollections.observableArrayList();
        ObservableList<TreeItem<Appointment>> removeSubRootWeek = FXCollections.observableArrayList();
        String [] weekString = new String[53];
        for (int i = 0; i<weekString.length; i++) {
            if (i == 0)
                weekString[i] = "This Week";
            else if (i == 1)
                weekString [i] = "Next Week";
            else if(i == 52)
                weekString [i] = "Elasped";
            else if (i == 51)
                weekString[i] = "Over 1 Year";
            else 
                weekString[i] = (i+1) + " Weeks";
            subRootWeek.add(new TreeItem(new Appointment(weekString[i])));
        }
        
        Consumer<Appointment> wLamb = (Appointment a) -> {
                    if (a.getStart().isBefore(LocalDateTime.now())) 
                        subRootWeek.get(52).getChildren().add(new TreeItem(a));
                    else if (a.getStart().isAfter(LocalDateTime.now().plusYears(1))) {
                        subRootWeek.get(51).getChildren().add(new TreeItem(a));
                        subRootWeek.get(51).setExpanded(true);
                    }
                    else {
                        for (int i = 0; i<weekString.length; i++)
                        {
                            if (a.getStart().isAfter(LocalDateTime.now().plusWeeks(i)) 
                                    && a.getStart().isBefore(LocalDateTime.now().plusWeeks(i+1)))
                            {
                               subRootWeek.get(i).getChildren().add(new TreeItem(a));
                               subRootWeek.get(i).setExpanded(true);
                               break;
                            }
                        }
                    }
                };
        
        Consumer<TreeItem<Appointment>> wLamb2 = (TreeItem<Appointment> a) -> {
                    if (a.isLeaf())
                        removeSubRootWeek.add(a);
                };
        
        appointmentList.forEach(wLamb);
        
        // These items will clear empty week nodes.
        subRootWeek.forEach(wLamb2);
        subRootWeek.removeAll(removeSubRootWeek);  
        
        /* These items will set available week values in the lower table
        & expand the root to show its values by default.
        */
        mainRoot.getChildren().addAll(subRootWeek);
        mainRoot.setExpanded(true);
        lowerTable.setRoot(mainRoot);

    }
        
        /* This configures the filter button that will 
        filter appointments based on user selection. */
   
    @FXML private void filterButtonPushed () {
            appointmentList.clear();
            try {
                String apptQuery = "SELECT appointment.appointmentId, appointment.title,"
                    + " appointment.description, customer.customerName, appointment.customerId,"
                    + " appointment.location, appointment.contact," 
                    + " appointment.start, appointment.end, user.username"   
                    + " FROM appointment, customer, user"
                    + " WHERE appointment.customerId = customer.customerId"
                    + " AND user.userId = appointment.contact";
            PreparedStatement smt = DBConnection.getConn().prepareStatement(apptQuery);
            
            ResultSet appointmentsFound = smt.executeQuery();
            while (appointmentsFound.next()) {
                Integer appointmentId = appointmentsFound.getInt("appointment.appointmentId");
                String customer = appointmentsFound.getString("customer.customerName");
                Integer customerId = appointmentsFound.getInt("appointment.customerId");
                String title = appointmentsFound.getString("appointment.title");
                String description = appointmentsFound.getString("appointment.description");
                String type = appointmentsFound.getString("appointment.location");  
                Integer userId = appointmentsFound.getInt("appointment.contact"); 
                LocalDateTime start = appointmentsFound.getTimestamp("appointment.start").toLocalDateTime();
                LocalDateTime end  = appointmentsFound.getTimestamp("appointment.end").toLocalDateTime();
                String user = appointmentsFound.getString("user.userName");
                                
                if(!consultationBox.isSelected())
                    if (type.equalsIgnoreCase("consultation"))
                        continue;
                if(!clientIntakeBox.isSelected())
                    if (type.equalsIgnoreCase("kickoff"))
                        continue;
                if(!phoneInterviewBox.isSelected())
                    if (type.equalsIgnoreCase("meeting"))
                        continue;
                if(!accountClosureBox.isSelected())
                    if (type.equalsIgnoreCase("update"))
                        continue;
                
                appointmentList.add(new Appointment (appointmentId, customerId, customer,
                        title, description, type, start, end, userId, user));
            }
        } catch (SQLException sqe) {
            sqe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();        
        }
            
        resultLabel.setText(appointmentList.size() + " results found.");
           
        if (weekButton.isSelected())
            weekButtonPushed();
        else if (monthButton.isSelected())
            monthButtonPushed ();

    }
    
    public void setAppointments (Scheduler application, User currentUser) {
        
        /* These items will populate & update the upper table with existing 
        appointments once the Application window is loaded.
        */
        
        this.application = application;
        this.currentUser = currentUser;
        resetSelectedItem();

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
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        consultantColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        
        upperTable.getItems().setAll(getAppointmentValues());
        
        /* These items will set & update the lower table & trigger functions 
        that will fill the it with existing Appointments, showing month view as default.
        */
        
        monthTreeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("calendar"));
        startTreeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("start"));
        startTreeColumn.setCellFactory(column -> {
            return new TreeTableCell<Appointment, LocalDateTime>(){
                @Override
                protected void updateItem(LocalDateTime date, boolean empty) {
                    super.updateItem(date, empty);     
                    if (date == null || empty)
                        setText("");
                    else
                        setText(formatter.format(date));
                }
            };
        });
        endTreeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("end"));
        endTreeColumn.setCellFactory(column -> {
            return new TreeTableCell<Appointment, LocalDateTime>(){
                @Override
                protected void updateItem(LocalDateTime date, boolean empty) {
                    super.updateItem(date, empty);     
                    if (date == null || empty)
                        setText("");
                    else
                        setText(formatter.format(date));
                }
            };
        });
        titleTreeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("title"));
        customerTreeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("customerName"));
        consultantTreeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("userName"));
        typeTreeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("type"));
        
        monthButtonPushed();
        filterButtonPushed(); 
        
        //These items will enable one Table selection at a time.
        
        upperTable.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1) {
                    lowerTable.getSelectionModel().clearSelection();
                }
            });
            
        lowerTable.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1) {
                    upperTable.getSelectionModel().clearSelection();
                }
            });
        
    }
    
    public static List<Appointment> getAppointmentValues () {
        ObservableList<Appointment> appointmentList = FXCollections.observableArrayList();
        try {
            String apptQuery = "SELECT appointment.appointmentId, appointment.title,"
                    + " appointment.description, customer.customerName, appointment.customerId,"
                    + " appointment.location, appointment.contact,"
                    + " appointment.start, appointment.end, user.username"   
                    + " FROM appointment, customer, user"
                    + " WHERE appointment.customerId = customer.customerId"
                    + " AND user.userId = appointment.contact"
                    + " ORDER BY appointment.start";
            
            PreparedStatement statement = DBConnection.getConn().prepareStatement(apptQuery);
            ResultSet resultsFound = statement.executeQuery();
            while (resultsFound.next()) {
          
                Integer appointmentId = resultsFound.getInt("appointment.appointmentId");
                String customer = resultsFound.getString("customer.customerName");
                Integer customerId = resultsFound.getInt("appointment.customerId");
                String title = resultsFound.getString("appointment.title");
                String description = resultsFound.getString("appointment.description");
                String type = resultsFound.getString("appointment.location");
                Integer userId = resultsFound.getInt("appointment.contact"); 
                LocalDateTime start = resultsFound.getTimestamp("appointment.start").toLocalDateTime();
                LocalDateTime end  = resultsFound.getTimestamp("appointment.end").toLocalDateTime();
                String user = resultsFound.getString("user.userName");
                appointmentList.add(new Appointment (appointmentId, customerId, customer,
                        title, description, type, start, end, userId, user));
            }
          
        } catch (SQLException sqe) {
            System.out.println("Check SQL");
            sqe.printStackTrace();
        } catch (Exception e) {
            System.out.println("Something besides the SQL went wrong.");
        }
        return appointmentList;
    }
    
    public static void resetSelectedItem() {
        selectedAppointment = null;
    }
    
    public static Appointment getSelectedItem() {
        return selectedAppointment;
    }
    
    public static void setSelectedItem(Appointment a) {
        selectedAppointment = a;
    }
    
}