package gui;

import java.sql.*;

public class SQL {
	
	public void setClockIn(String date, String time) throws ClassNotFoundException, SQLException {
		
		getConnection();			
		Statement st = conn.createStatement();			
		st.executeUpdate("INSERT INTO entrada VALUES('" + date + "', '" + time + "' )");
	}
	
	public void setClockOut(String date, String time) throws ClassNotFoundException, SQLException {
				
		getConnection();		
		Statement st = conn.createStatement();			
		st.executeUpdate("INSERT INTO salida VALUES('" + date + "', '" + time + "' )");
	}
	
	public ResultSet getClockIn() throws ClassNotFoundException, SQLException {
		
		getConnection();		
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery("SELECT * FROM entrada");	
		
		return rs;
	}
	
	public ResultSet getClockOut() throws ClassNotFoundException, SQLException {
		
		getConnection();		
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery("SELECT * FROM salida");	
		
		return rs;
	}
	//Returns cuota for a given date
	public ResultSet[] calculateHours(String date) throws SQLException, ClassNotFoundException {
		
		getConnection();		
		Statement st1 = conn.createStatement();
		ResultSet rs1 = st1.executeQuery("SELECT * FROM entrada WHERE fecha='" + date + "'");		
		Statement st2 = conn.createStatement();
		ResultSet rs2 = st2.executeQuery("SELECT * FROM salida WHERE fecha='" + date + "'");
		
		ResultSet[] rs = new ResultSet[2];
		rs[0] = rs1;
		rs[1] = rs2;
		
		return rs;
	}	
		
	public ResultSet[] checkButtonDisable() throws SQLException {

		Statement st1 = conn.createStatement();
		ResultSet rs1 = st1.executeQuery("SELECT * FROM entrada");
		Statement st2 = conn.createStatement();
		ResultSet rs2 = st2.executeQuery("SELECT * FROM salida");
		
		ResultSet[] rs = new ResultSet[2];
		rs[0] = rs1;
		rs[1] = rs2;
		
		return rs;
	}
	
	public static Connection getConnection() throws ClassNotFoundException, SQLException {
		
		Class.forName("org.sqlite.JDBC");
		
		if(conn==null) {
			conn = DriverManager.getConnection("jdbc:sqlite:sistema_fichaje.db");		
		}
		
		if(!initialized) {
			initialize();
		}
		
		return conn;
	}
	
	public static void closeConnection() throws SQLException {
		
		conn.close();
	}
	
	private static void initialize() throws SQLException {
							
			Statement st = conn.createStatement();
			st.execute("CREATE TABLE IF NOT EXISTS entrada (" +
					"fecha	TEXT NOT NULL," +
					"hora	TEXT NOT NULL," +
					"PRIMARY KEY(fecha,hora))"
					);
			
			Statement st2 = conn.createStatement();
			st2.execute("CREATE TABLE IF NOT EXISTS salida (" +
					"fecha	TEXT NOT NULL," +
					"hora	TEXT NOT NULL," +
					"PRIMARY KEY(fecha,hora))"
					);	
			
			initialized = true;
	}	

	private static Connection conn;
	private static boolean initialized = false;
}