
package michaeltadeo.Util;


import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class LoggerUtil {
 
    
 private final static Logger LOGGER = Logger.getLogger(LoggerUtil.class.getName());
 private static FileHandler handler = null;
 
 public static void init(){

    try {
    handler = new FileHandler("Scheduler-Userlog.%u.%g.txt", true);
    } catch (SecurityException | IOException e) {
        e.printStackTrace();
        }
    Logger logger = Logger.getLogger("");
    handler.setFormatter(new SimpleFormatter());
    logger.addHandler(handler);
    logger.setLevel(Level.INFO);
 }
    
}