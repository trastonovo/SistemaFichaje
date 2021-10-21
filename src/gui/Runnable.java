package gui;

import java.awt.event.*;
import java.io.IOException;
import java.sql.*;
import javax.swing.JFrame;

public class Runnable {

	public static void main(String[] args) {

		try {
			myLog = new Log("log.txt");
		} catch (SecurityException e1) {
			myLog.logger.warning(e1 + " - " +  myLog.stackTraceToString(e1));
		} catch (IOException e1) {
			myLog.logger.warning(e1 + " - " + myLog.stackTraceToString(e1));
		}
		
		MainFrame mFrame = new MainFrame();
		
		mFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mFrame.addWindowListener(new WindowAdapter() {
			
			public void windowClosing(WindowEvent we) {
				
				try {
					if(SQL.getConnection() != null)
						SQL.closeConnection();
				} catch (ClassNotFoundException e) {
					myLog.logger.warning(e + " - " +  myLog.stackTraceToString(e));
				} catch (SQLException e) {
					myLog.logger.warning(e + " - " +  myLog.stackTraceToString(e));
				}
			}
		});
	}
		
	public static Log myLog;
}