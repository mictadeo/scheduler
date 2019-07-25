
package michaeltadeo.View_Controller;


import michaeltadeo.Util.DBConnection;
import michaeltadeo.Util.LoggerUtil;
import michaeltadeo.Model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import michaeltadeo.Scheduler;


public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;
    @FXML private Label usernameLabel;
    @FXML private Label passwordLabel;
    
    private Scheduler application;
    private User user = new User();
    
    /*These items will set the configuration for the locale, DBConnection & logger.
    It sets the current value of the default locale for this instance of the JVM.
    Three bundle languages are created for log in & error messages, Enlgish, Tagalog
    & Italian. Connects to the database & records timestamps for user log-ins.
   */
    ResourceBundle r = ResourceBundle.getBundle("michaeltadeo/View_Controller/bundle", Locale.getDefault());
    private final Connection connection = DBConnection.getConn();
    private final static Logger LOGGER = Logger.getLogger(LoggerUtil.class.getName());
    
       
    /*This configures the Login Button, enables login & prompts an error for incorrect input.
    Triggers the function appointmAlert to alert user if there is an appointment withtin
    15 minutes of user's log in.
    */
    @FXML private void loginButtonPushed (ActionEvent e) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        if (username.length()==0 || password.length()==0)
            errorLabel.setText(r.getString("empty"));
        else {
            User user = userCredentials(username,password);
            if (user == null) {
                errorLabel.setText(r.getString("incorrect"));
                return;
            }
            
            LOGGER.log(Level.INFO, "{0} logged in", user.getUsername());

            appointmentAlert();
            application.appointmentScreen(user);
        }
    }

    //This will validate user credentials.
    private User userCredentials(String username, String password) {
        try {
            PreparedStatement pst = DBConnection.getConn().prepareStatement
                ("SELECT * FROM user WHERE userName=? AND password =?");
            pst.setString(1, username);
            pst.setString(2, password);
            ResultSet rs = pst.executeQuery();
            if (rs.next()){
                String passwordHolder = rs.getString("Password");
                if (passwordHolder.contentEquals(password))
                    System.out.println("Password is a match.");
                else
                    return null;
                user.setUsername(rs.getString("userName"));
                user.setPassword(rs.getString("password"));
                user.setUserID(rs.getInt("userId"));
              }
            else {
                return null;
              }
            } catch (SQLException e) {
                e.printStackTrace();
            }
         return user;
    }

    public void login (Scheduler application) {
        this.application = application;
        usernameLabel.setText(r.getString("username"));
        passwordLabel.setText(r.getString("password"));
        loginButton.setText(r.getString("signin"));
    }

    /* This is the configuration for user alert if there is an 
    appointment within 15 minutes of login.
    */
    private void appointmentAlert(){
        try{
             String upcomingQuery = "SELECT appointment.*"
                         + " FROM appointment"
                         + " WHERE (appointment.contact = ? AND appointment.start BETWEEN ? AND ?)";
                      
             PreparedStatement upcomingSmt = connection.prepareStatement(upcomingQuery);
             int i = 1;
             upcomingSmt.setString(i++, Integer.toString(user.getUserID()));
             upcomingSmt.setTimestamp(i++, Timestamp.valueOf(LocalDateTime.now()));
             upcomingSmt.setTimestamp(i++, Timestamp.valueOf(LocalDateTime.now()
                     .plusMinutes(15)));
             ResultSet upcomingRs = upcomingSmt.executeQuery();
             if (upcomingRs.next()) {
                 Alert upcomingAlert = new Alert(Alert.AlertType.INFORMATION);
                 upcomingAlert.setTitle("Upcoming Appointment");
                 upcomingAlert.setHeaderText("Reminder: Upcoming Appointment");
                 Long minutesUntil = ((upcomingRs.getTimestamp("start").getTime())-
                         Timestamp.valueOf(LocalDateTime.now()
                         ).getTime())/60000;
                 upcomingAlert.setContentText("You have an upcoming appointment in " 
                 + minutesUntil + " minutes. Check the Appointment Table for more details.");                          
                 upcomingAlert.showAndWait();
             }

         } catch (SQLException exc) {
                 exc.printStackTrace();
         }
    }

  }