
package michaeltadeo.View_Controller;

import michaeltadeo.Util.DBConnection;
import michaeltadeo.Model.Appointment;
import michaeltadeo.Model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import static java.sql.Types.NULL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import michaeltadeo.Scheduler;


public class AppointmentFormController {

    @FXML private Label errorLabel;
    @FXML private ChoiceBox<String> customerBox;
    @FXML private ChoiceBox<String> consultantBox;
    @FXML private TextField titleField;
    @FXML private DatePicker datePicker;
    @FXML private ChoiceBox<Integer> hourBox;
    @FXML private ChoiceBox<Integer> minuteBox;
    @FXML private ChoiceBox<Integer> durationBox;
    @FXML private ChoiceBox<String> typeBox;
    @FXML private TextArea descriptionArea;
    private final  Connection connection = DBConnection.getConn();
    private User currentUser;
    private Scheduler application;
    private Appointment selectedAppointment;
    private Integer timeOffset;

    @FXML private void cancelButtonPushed (ActionEvent e)
    {
        Alert cancelAlert = new Alert (Alert.AlertType.CONFIRMATION);
        cancelAlert.setTitle("Cancel Adding/Editing Appoitnments?");
        cancelAlert.setHeaderText("Are you sure you want to cancel? "
                + "Any unsaved progress will be lost.");
        Optional<ButtonType> result = cancelAlert.showAndWait();
        if (result.get() == ButtonType.OK) {
            AppointmentsController.resetSelectedItem();
            application.closeStage(currentUser);

        }            
    }
    
    /* This configures the save button, by saving the selected entry. It also
    checks for incomplete fields, retrieve entries, verifies selected time slot if
    if it's after businesss hours & checks for overlapping appointments.*/
    
    @FXML private void saveButtonPushed (ActionEvent e)
    {
        String customerName = customerBox.getSelectionModel().getSelectedItem();
        String consultantName = consultantBox.getSelectionModel().getSelectedItem();
        String apptTitle = titleField.getText();
        LocalDate startDate = null;
        if (datePicker.getValue()!=null)
            startDate = datePicker.getValue();
        Integer hour = hourBox.getSelectionModel().getSelectedItem();
        Integer minute = minuteBox.getSelectionModel().getSelectedItem();
        Integer duration = durationBox.getSelectionModel().getSelectedItem();
        String type = typeBox.getSelectionModel().getSelectedItem();
        String description = descriptionArea.getText();
        
        Integer custId = null;
        Integer usrId = null;
        
        if (customerBox.getSelectionModel().isEmpty() || customerName.length() == 0
                || consultantBox.getSelectionModel().isEmpty() || consultantName.length() == 0
                || apptTitle.length() == 0 
                || startDate == null || startDate.toString().length() == 0
                || hourBox.getSelectionModel().isEmpty()
                || minuteBox.getSelectionModel().isEmpty()
                || durationBox.getSelectionModel().isEmpty()
                || typeBox.getSelectionModel().isEmpty())
            errorLabel.setText("All required fields must be completed.");
        else if((hour-timeOffset)*60+minute+duration > 1110)
            errorLabel.setText("Sorry, but the appointment time slot you picked" + "\r\nis after our business hours"
                    + " which is from "+ (9+timeOffset)+"00 to " + (6+timeOffset+12) + "30 Local Time.");
        else {
            LocalDateTime startLocal = LocalDateTime.of(startDate.getYear(), 
                startDate.getMonthValue(), startDate.getDayOfMonth(), hour, minute); 
            LocalDateTime endLocal = startLocal.plusMinutes(duration); 
            
            
            try {
               
                PreparedStatement idStatement = connection.prepareStatement("SELECT customer.customerId"
                        + " FROM customer"
                        + " WHERE customer.customerName = ?");
                idStatement.setString(1, customerName);
                
                ResultSet custIDs = idStatement.executeQuery();
                if (custIDs.next())
                        custId = custIDs.getInt("customer.customerId");
               
                PreparedStatement userIdStatement = connection.prepareStatement("SELECT user.userId"
                        + " FROM user"
                        + " WHERE user.userName = ?");
                userIdStatement.setString(1, consultantName);
                
                ResultSet userIDs = userIdStatement.executeQuery();
                if (userIDs.next())
                        usrId = userIDs.getInt("user.userId");
                              
                String overlappingQuery = "SELECT appointment.*"
                        + " FROM appointment"
                        + " WHERE ((appointment.contact = ?"
                        + " OR appointment.customerId = ?)"
                        + " AND (appointment.start BETWEEN ? AND ?"
                        + " OR appointment.end BETWEEN ? AND ?)"
                        + " AND appointment.appointmentId <> ?)";
                PreparedStatement overlappingSmt = connection.prepareStatement(overlappingQuery);
                int i = 1;
                overlappingSmt.setString(i++, usrId.toString());
                overlappingSmt.setInt(i++, custId);
                overlappingSmt.setTimestamp(i++, Timestamp.valueOf(startLocal));
                overlappingSmt.setTimestamp(i++, Timestamp.valueOf(endLocal.minusMinutes(1)));
                overlappingSmt.setTimestamp(i++, Timestamp.valueOf(startLocal.plusMinutes(1)));
                overlappingSmt.setTimestamp(i++, Timestamp.valueOf(endLocal));
                if (AppointmentsController.getSelectedItem()== null)
                    overlappingSmt.setInt(i++, NULL);
                else
                    overlappingSmt.setInt(i++, AppointmentsController.getSelectedItem().getAppointmentId());

                ResultSet overlappingRs = overlappingSmt.executeQuery();
                
                if (overlappingRs.next()) {
                    String overlappingPerson = null;
                    Integer userId = Integer.parseInt(overlappingRs.getString("contact"));
                    Integer customerIdfromQuery = overlappingRs.getInt("customerId");
                    try {
                    if (customerIdfromQuery == custId && userId == usrId)
                        overlappingPerson = "Both " + consultantName + " and " + customerName + " have ";
                    else if(userId == usrId)
                        overlappingPerson = consultantName + " has " ;
                    else if (customerIdfromQuery == custId)
                        overlappingPerson = customerName + " has ";
                    else {
                        overlappingPerson = "Something went wrong!";
                        throw new Exception("Something went wrong with adding an appointment.");
                    }
                    } catch (Exception E) {
                        E.printStackTrace();
                    }
                    overlappingPerson +="an overlapping appointment" + "\n";
                    System.out.println("Overlapping appointment");
                    Alert overlappingAlert = new Alert (Alert.AlertType.WARNING);
                    overlappingAlert.setTitle("Overlapping Appointment");
                    overlappingAlert.setHeaderText("Cannot add an overlapping appointment.");
                    String overlappingMessage = overlappingPerson + "Appt #" + Integer.toString(overlappingRs.getInt
                            ("appointmentId")) + ": '" + overlappingRs.getString("title")
                            + "' from " + overlappingRs.getTimestamp("start") 
                            + " to " + overlappingRs.getTimestamp("end");
                    overlappingAlert.setContentText(overlappingMessage);
                    overlappingAlert.showAndWait();
                }
                else 
                {
                    selectedAppointment = AppointmentsController.getSelectedItem();
                    String apptQuery = null;
                    if (selectedAppointment == null)
                    {
                        apptQuery = "INSERT INTO appointment ("
                                + " customerId,"
                                + " title,"
                                + " description,"
                                + " location," 
                                + " contact,"  
                                + " url,"
                                + " start,"
                                + " end,"
                                + " createDate,"
                                + " createdBy,"
                                + " lastUpdate,"
                                + " lastUpdateBy) VALUES ("
                                + " ?, ?, ?, ?, ?, ?, ?, ?,"
                                + " CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?)";
                    }
                    else
                    {
                        apptQuery = "UPDATE appointment "
                                + " SET customerId = ?,"
                                + " title = ?,"
                                + " description = ?,"
                                + " location = ?,"
                                + " contact = ?," 
                                + " url = ?,"
                                + " start = ?,"
                                + " end = ?,"
                                + " lastUpdate = CURRENT_TIMESTAMP,"
                                + " lastUpdateBy = ?"
                                + " WHERE appointmentId = ?";
                    }
                    PreparedStatement st = connection.prepareStatement(apptQuery);
                    i = 1;
                    st.setInt(i++, custId);
                    st.setString(i++, apptTitle);
                    st.setString(i++, description);
                    st.setString(i++, type);
                    st.setInt(i++, usrId);
                    st.setString(i++, "N/A");
                    st.setTimestamp(i++, Timestamp.valueOf(startLocal));
                    st.setTimestamp(i++, Timestamp.valueOf(endLocal));
                    st.setString(i++, currentUser.getUsername());
                    if (selectedAppointment == null)
                        st.setString(i++, currentUser.getUsername());
                    else
                        st.setInt(i++, selectedAppointment.getAppointmentId());
                    st.executeUpdate();
                    st.close();

                    application.closeStage(currentUser);
                    
                }
            } catch (SQLException sqe) {
                sqe.printStackTrace();
            }
        }
    }
    
    public void setAppointmentFields (Scheduler scheduler, User activeUser){
        this.application = scheduler;
        this.currentUser = activeUser;
        
        /* These items will set the date picker & fill the choice boxes with 
        its appropriate contents such as the type of appointment
        correct local timezone. */
        
        OffsetDateTime odt = OffsetDateTime.now ();
        ZoneOffset zoneOffset = odt.getOffset ();    
        timeOffset = zoneOffset.getTotalSeconds()/60/60;
        timeOffset = timeOffset -(-7); 
        Callback<DatePicker, DateCell> dayCellFactory = this.getDayCellFactory();
        datePicker.setDayCellFactory(dayCellFactory);
        typeBox.getItems().addAll("Consultation", "Client Intake", "Phone Interview", "Account Closure");
        minuteBox.getItems().addAll(00, 15, 30, 45);
        hourBox.getItems().addAll(9+timeOffset, 10+timeOffset, 
                11+timeOffset, 12+timeOffset, 13+timeOffset, 14+timeOffset, 
                15+timeOffset, 16+timeOffset,17+timeOffset, 18+timeOffset);
        durationBox.getItems().addAll(15, 30, 45, 60);
        
        /* These items will retrieve Customer records from the database &
        fill the Customer name choicebox*/
        try {
            String customerQuery = "SELECT customer.customerName"
                + " FROM customer" 
                + " WHERE customer.active = 1"
                + " ORDER BY customer.customerName";
            PreparedStatement smt = connection.prepareStatement(customerQuery);
            ResultSet customersFound = smt.executeQuery();
            while (customersFound.next()) {
                String dCustomerName = customersFound.getString("customer.customerName");
                customerBox.getItems().add(dCustomerName);
            }
        } catch (SQLException sqe) {
            sqe.printStackTrace();
        } 
        
        /* These items will retrieve Consultant records from the database &
        fill the Consultant choicebox.*/
        try {
            String userQuery = "SELECT user.userName"
                + " FROM user" 
                + " WHERE user.active = 1"
                + " ORDER BY user.userName";
        
            PreparedStatement smt = connection.prepareStatement(userQuery);
            ResultSet usersFound = smt.executeQuery();

            while (usersFound.next()) {
                String dUserName = usersFound.getString("user.userName");
                consultantBox.getItems().add(dUserName);
            }
        } catch (SQLException sqe) {
            sqe.printStackTrace();
        }
        
        //These items will retrieve existing appointment data & fill the textfields.
       
        Appointment modAppt = AppointmentsController.getSelectedItem();
        if (modAppt != null)
        {
            Integer hour = modAppt.getStart().getHour();
            Integer minute = modAppt.getStart().getMinute();
            Integer endHour = modAppt.getEnd().getHour();
            Integer endMinute = modAppt.getEnd().getMinute();
            Integer duration = (endHour-hour)*60 + (endMinute-minute);
            
            customerBox.getSelectionModel().select(modAppt.getCustomerName());
            consultantBox.getSelectionModel().select(modAppt.getUserName());
            titleField.setText(modAppt.getTitle());
            datePicker.setValue(modAppt.getStart().toLocalDate());
            hourBox.getSelectionModel().select(hour);
            minuteBox.getSelectionModel().select(minute);
            durationBox.getSelectionModel().select(duration);
            typeBox.getSelectionModel().select(modAppt.getType());
            descriptionArea.setText(modAppt.getDescription());
        }
    }
    
    /* This function will gray out & disable past dates, weekends & dates that
    are outside normal business hours. */
    
    private Callback<DatePicker, DateCell> getDayCellFactory() {
 
        final Callback<DatePicker, DateCell> dayCellFactory = (final DatePicker datePicker) -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                
                if (item.isBefore(LocalDate.now()) ||
                        item.getDayOfWeek() == DayOfWeek.SUNDAY //
                        || item.getDayOfWeek() == DayOfWeek.SATURDAY) {
                    setDisable(true);
                    setStyle("-fx-background-color: #A9A9A9;");
                }
            }
        };
        return dayCellFactory;
    }
}