package en.actionsofproject.database;

import java.sql.*;

public class WriteDataIntoDB {

	public void ConnMySql() throws Exception{
		
		Class.forName("com.mysql.jdbc.Driver");
		
        Connection conn = DriverManager.getConnection(
                "jdbc:mysql://127.0.0.1:3306/mydata",
                "root","12345");
        Statement stmt =  conn.createStatement();
        ResultSet rs = stmt.executeQuery("select * from ClassInfo");
        
        while (rs.next()) {
            System.out.println(rs.getInt(1) + "\t"
                    +rs.getString(2) + "\t" );
            }
         
        if (rs != null) {
            rs.close();
        }
        if (stmt != null) {
            stmt.close();   
        }
        if (conn != null) {
            conn.close();   
        }
	}
}
