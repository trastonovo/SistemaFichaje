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
			myLog.logger.info(e1 + " - " +  myLog.stackTraceToString(e1));
		} catch (IOException e1) {
			myLog.logger.info(e1 + " - " + myLog.stackTraceToString(e1));
		}
		
		MainFrame mFrame = new MainFrame();
		
		mFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mFrame.addWindowListener(new WindowAdapter() {
			
			public void windowClosing(WindowEvent we) {
				
				try {
					if(SQL.getConnection() != null)
						SQL.closeConnection();
				} catch (ClassNotFoundException e) {
					myLog.logger.info(e + " - " +  myLog.stackTraceToString(e));
				} catch (SQLException e) {
					myLog.logger.info(e + " - " +  myLog.stackTraceToString(e));
				}
			}
		});
	}
		
	public static Log myLog;
}

//TODO:
//Botón de pausa
//Formatear & filtrar ventana emergente fichajes (+invertir orden)
//Clarificar mensajes error
//Manejar calendario gráfico (checkCuota()) [JSpinner?] + error al introducir valor inválido
//Optimizar conexiones

//Look up: Hibernate; Maven (POM.xml)