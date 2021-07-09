package gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.Calendar;
import java.util.Locale;

public class MainPanel extends JPanel implements ActionListener {
	
	public MainPanel() {
		
		clockIn = new JButton("Fichar entrada");
		add(clockIn);
		clockIn.addActionListener(this);
		
		clockOut = new JButton("Fichar salida");
		add(clockOut);
		clockOut.addActionListener(this);	
		
		checkClock = new JButton("Lista de fichajes");
		add(checkClock).setBackground(Color.YELLOW);
		checkClock.addActionListener(this);
		
		checkCuota = new JButton("Cuota");
		add(checkCuota).setBackground(Color.ORANGE);
		checkCuota.addActionListener(this);	
		
		buttonDisabled();
		
		JLabel version = new JLabel("Versi�n preliminar 0.2");
		add(version);		
	}	

	public void actionPerformed(ActionEvent e) {
		
		Object button = e.getSource();		
		
		Calendar cal = Calendar.getInstance();		
		String date = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
		String time = new SimpleDateFormat("HH:mm:ss").format(cal.getTime());
		
		if(button==clockIn) {		
			try {				
				start = Instant.now(); //Counter for current clockIn session (not closed)
				db.setClockIn(date, time);							
			} catch(ClassNotFoundException ex) {
				JOptionPane.showMessageDialog(null, ex.getMessage(), "ClassNotFoundException", JOptionPane.ERROR_MESSAGE);
				Runnable.myLog.logger.info(ex + " - " +  Runnable.myLog.stackTraceToString(ex));
			} catch (SQLException ex) {
				JOptionPane.showMessageDialog(null, ex.getMessage(), "SQLExceptionL", JOptionPane.ERROR_MESSAGE);
				Runnable.myLog.logger.info(ex + " - " +  Runnable.myLog.stackTraceToString(ex));
			}
		}
		if(button==clockOut) {
			try {
				db.setClockOut(date, time);
			} catch(ClassNotFoundException ex) {
				JOptionPane.showMessageDialog(null, ex.getMessage(), "ClassNotFoundException", JOptionPane.ERROR_MESSAGE);
				Runnable.myLog.logger.info(ex + " - " +  Runnable.myLog.stackTraceToString(ex));
			} catch (SQLException ex) {
				JOptionPane.showMessageDialog(null, ex.getMessage(), "SQLException", JOptionPane.ERROR_MESSAGE);
				Runnable.myLog.logger.info(ex + " - " +  Runnable.myLog.stackTraceToString(ex));
			}				
		}	
		//Check clock in and clock out list
		if(button==checkClock) {
			try {
				showClockIO();
			} catch(ClassNotFoundException ex) {
				JOptionPane.showMessageDialog(null, ex.getMessage(), "ClassNotFoundException", JOptionPane.ERROR_MESSAGE);
				Runnable.myLog.logger.info(ex + " - " +  Runnable.myLog.stackTraceToString(ex));
			} catch (SQLException ex) {
				JOptionPane.showMessageDialog(null, ex.getMessage(), "SQLException", JOptionPane.ERROR_MESSAGE);
				Runnable.myLog.logger.info(ex + " - " +  Runnable.myLog.stackTraceToString(ex));
			}				
		}
		//Check hour cuota from a given day
		if(button==checkCuota) {
			try {			
				long diffMs = 0;
				long diffM = 0;
				long diffMTotal = 0;
				long diffHTotal = 0;
				long minutesRemaining = 0;
				boolean oneTime = true;
				java.util.Date date1 = null;
				java.util.Date date2 = null;
				String iDate = JOptionPane.showInputDialog("Introduce fecha 'yyyy-MM-dd'\nVac�o para fecha actual");
				if(iDate.equals("")) {			
					iDate = date;
				}
				ResultSet[] rs = db.calculateHours(iDate);			
				while(rs[0].next() && rs[1].next()) {
					
					SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
					date1 = sdf.parse(rs[0].getString(2));
					date2 = sdf.parse(rs[1].getString(2));					
					diffMs = date2.getTime() - date1.getTime();
					
					//If statement: adds time w/o clocking out
					if(clockOut.isEnabled() && oneTime){
						finish = Instant.now();
						long timeElapsed = Duration.between(start, finish).toMillis();
						diffMs += timeElapsed;
						oneTime = false;		
					}
					
					diffM = (diffMs/1000)/60;
					diffMTotal += diffM;
					diffHTotal = diffMTotal/60;
					minutesRemaining = diffMTotal%60;
				}				
				JOptionPane.showMessageDialog(null, diffHTotal+"h, "+minutesRemaining+"m", "Horas registradas " + iDate, JOptionPane.INFORMATION_MESSAGE);
			} catch(ClassNotFoundException ex) {
				JOptionPane.showMessageDialog(null, ex.getMessage(), "ClassNotFoundException", JOptionPane.ERROR_MESSAGE);
				Runnable.myLog.logger.info(ex + " - " +  Runnable.myLog.stackTraceToString(ex));
			} catch (SQLException ex) {
				JOptionPane.showMessageDialog(null, ex.getMessage(), "SQLException", JOptionPane.ERROR_MESSAGE);
				Runnable.myLog.logger.info(ex + " - " +  Runnable.myLog.stackTraceToString(ex));
			} catch (ParseException ex) {
				JOptionPane.showMessageDialog(null, ex.getMessage(), "ParseException", JOptionPane.ERROR_MESSAGE);
				Runnable.myLog.logger.info(ex + " - " +  Runnable.myLog.stackTraceToString(ex));
			} catch (NullPointerException ex) {
				Runnable.myLog.logger.info(ex + " - " +  Runnable.myLog.stackTraceToString(ex));
			}	
		}
		
		buttonDisabled();
	}
	
	//DisableClockIn=Positive; DisableClockOut=Negative,0
	public void buttonDisabled() {
		
		int count1 = 0;
		int count2 = 0;
		
		try {
			SQL.getConnection();
		} catch (ClassNotFoundException e) {
			Runnable.myLog.logger.info(e + " - " +  Runnable.myLog.stackTraceToString(e));
		} catch (SQLException e) {
			Runnable.myLog.logger.info(e + " - " +  Runnable.myLog.stackTraceToString(e));
		}
		
		try {
			ResultSet[] rs = new ResultSet[2];
			rs = db.checkButtonDisable();
			
			while (rs[0].next()) {
			    ++count1;
			}
			
			while (rs[1].next()) {
			    ++count2;
			}
			
			int result = count1-count2;
			
			if(result>0) {
				clockIn.setEnabled(false);
				clockOut.setEnabled(true);
			}				
			else {
				clockIn.setEnabled(true);
				clockOut.setEnabled(false);
			}
		} catch (SQLException e1) {
			Runnable.myLog.logger.info(e1 + " - " +  Runnable.myLog.stackTraceToString(e1));
		}
	}
	
	//Emergent window: Clock in-out Query
	public void showClockIO() throws ClassNotFoundException, SQLException {
		
		SQL.getConnection();
		
		JFrame marco2 = new JFrame();
		marco2.setTitle("Registros");			
		marco2.setSize(330,450);
		marco2.setLocation(getMousePosition());
		marco2.setVisible(true);
		marco2.setResizable(false);
		
		JPanel lamina2 = new JPanel();
		marco2.add(lamina2);
		
		JTextArea taIn = new JTextArea("Entrada\n",25,12);			
		taIn.setEditable(false);
		
		JTextArea taOut = new JTextArea("Salida\n",25,12);		
		taOut.setEditable(false);		
		
		JScrollPane sp1 = new JScrollPane(taIn);
		lamina2.add(sp1);
		JScrollPane sp2 = new JScrollPane(taOut);
		lamina2.add(sp2);
		
		//Sync both scroll panels
		JScrollBar sb1 = sp1.getVerticalScrollBar();
		JScrollBar sb2 = sp2.getVerticalScrollBar();
		sb1.setModel(sb2.getModel());
		
		ResultSet rsIn = db.getClockIn();
		ResultSet rsOut = db.getClockOut();
		
		boolean checkIn = false;
		boolean checkOut = false;
		boolean oneTime = true;
		
		while(rsIn.next()) {
			
			if(checkIn)
				taIn.append("\n");
			
			taIn.append(rsIn.getString("fecha") + " @" + rsIn.getString("hora"));
			checkIn = true;
		}
		
		while(rsOut.next()) {
			
			if(checkOut)
				taOut.append("\n");
			
			taOut.append(rsOut.getString("fecha") + " @" + rsOut.getString("hora"));
			checkOut = true;
		}
		
		//Retrieves placeholder text w/o clocking out
		if(clockOut.isEnabled() && oneTime){
			taOut.append("\n  -SESI�N EN CURSO-");
			oneTime = false;		
		}
	}
	
	private Instant start, finish;
	private JButton clockIn, clockOut, checkClock, checkCuota;
	private static SQL db = new SQL();
}