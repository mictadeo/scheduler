
package michaeltadeo.Util;

import java.sql.Connection;
import java.sql.DriverManager;


public class DBConnection {
    
    private static Connection connDB;
    
    public DBConnection(){}
    public static void init(){
        System.out.println("Connecting to the database");
        try{
            Class.forName("com.mysql.jdbc.Driver");
            connDB = DriverManager.getConnection("jdbc:mysql://52.206.157.109/U053sX?" + 
                    "user=U053sX&password=53688417272");
        }catch (ClassNotFoundException ce){
            System.out.println("Cannot find the right class.  Did you remember to add the mysql library to your Run Configuration?");
            ce.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
}
    }
    
    public static Connection getConn(){
    
        return connDB;
    }
    
    public static void closeConn(){
        try{
            connDB.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        finally{
            System.out.println("Database connection closed.");
        }
    }
    
}