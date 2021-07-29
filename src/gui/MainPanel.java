package gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;

import com.toedter.calendar.JCalendar;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;

public class MainPanel extends JPanel implements ActionListener {
	
	public MainPanel() {
		
		setLayout(new BorderLayout());
		
		JPanel northPanel = new JPanel();
		add(northPanel, BorderLayout.NORTH);
		
		JPanel eastPanel = new JPanel();
		add(eastPanel, BorderLayout.EAST);
		eastPanel.setPreferredSize(new Dimension(50,50));
		
		JPanel southPanel = new JPanel();
		southPanel.setLayout(new BorderLayout());
		add(southPanel, BorderLayout.SOUTH);
		southPanel.setPreferredSize(new Dimension(25,25));
		
		Icon iconBookOpen = new ImageIcon("images/book_open.png");
		clockIn = new JButton("Entrada", iconBookOpen);
		northPanel.add(clockIn);
		clockIn.addActionListener(this);
		clockIn.setPreferredSize(new Dimension(150,40));
		
		Icon iconBookClosed = new ImageIcon("images/book_closed.png");
		clockOut = new JButton("Salida", iconBookClosed);
		northPanel.add(clockOut);
		clockOut.addActionListener(this);	
		clockOut.setPreferredSize(new Dimension(150,40));
		
		Icon iconCuota = new ImageIcon("images/clock.png");
		checkCuota = new JButton(iconCuota);
		eastPanel.add(checkCuota).setBackground(Color.WHITE);
		checkCuota.addActionListener(this);	
		checkCuota.setPreferredSize(new Dimension(40,40));
		
		Icon iconList = new ImageIcon("images/list.png");
		checkClock = new JButton(iconList);
		eastPanel.add(checkClock).setBackground(Color.WHITE);
		checkClock.addActionListener(this);
		checkClock.setPreferredSize(new Dimension(40,40));		
		
		centerPanel1 = new JPanel();
		centerPanel2 = new JPanel();
		centerPanel1.setLayout(new BorderLayout());
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, centerPanel1, centerPanel2);
		split.setEnabled(false);
		split.setDividerSize(0);
		add(split, BorderLayout.CENTER);
		
		////////////////
		/////////////// IMAGE TEST [rise dog gif]
		String url = "images/carbotZergling.gif";
		JLabel picLabel = new JLabel(new ImageIcon(url));
		centerPanel2.add(picLabel);
		////////////////
		////////////////

		recentList();				
		
		buttonDisabled();
		
		JLabel version = new JLabel("  Versión preliminar 0.3  ");
		version.setFont(new Font("Arial", Font.ITALIC, 11));
		southPanel.add(version, BorderLayout.WEST);		
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
			////////////////////////
			///////////////////////AUDIO TEST
			String uri = "audio/Final Fantasy VII - Victory Fanfare [HQ].wav";
			AudioInputStream ais;
			try {
				ais = AudioSystem.getAudioInputStream(new File(uri).getAbsoluteFile());
				Clip clip = AudioSystem.getClip();
				clip.open(ais);
				clip.start();
			} catch (UnsupportedAudioFileException | IOException e1) {
				e1.printStackTrace();
			} catch (LineUnavailableException e1) {
				e1.printStackTrace();
			}			

			///////////////////////
			///////////////////////
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
			long[] returnTime;
			String iDate = JOptionPane.showInputDialog("Introduce fecha 'yyyy-MM-dd'\nVacío para fecha actual");
			if(iDate.equals("")) {			
				iDate = date;
			}
			returnTime = cuota(iDate, true);				
			JOptionPane.showMessageDialog(null, returnTime[0]+"h, "+returnTime[1]+"m", "Horas registradas " + iDate, JOptionPane.INFORMATION_MESSAGE);
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
		if(clockOut.isEnabled()){
			taOut.append("\n  -SESIÓN EN CURSO-");	
		}
	}
	
	//Return Date dates between two String dates
	public ArrayList<LocalDate> getDaysBetween(LocalDate start, LocalDate end) {
								
		ArrayList<LocalDate> totalDates = new ArrayList<>();
		
		while (!start.isAfter(end)) {
			totalDates.add(start);
			start = start.plusDays(1);
		}
		return totalDates;	
	}
	
	//Check cuota from a given day
	public long[] cuota(String date, boolean unfinishedTime) {
		
		long[] returnTime = new long[2];
		long diffMs = 0;
		long diffM = 0;
		long diffMTotal = 0;
		long diffHTotal = 0;
		long minutesRemaining = 0;
		java.util.Date date1 = null;
		java.util.Date date2 = null;		
		try {
			ResultSet[] rs = db.calculateHours(date);
			while(rs[0].next() && rs[1].next()) {
				
				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
				date1 = sdf.parse(rs[0].getString(2));
				date2 = sdf.parse(rs[1].getString(2));
				diffMs = date2.getTime() - date1.getTime();
				
				//If statement: adds time w/o clocking out
				if(clockOut.isEnabled() && unfinishedTime){
					finish = Instant.now();
					long timeElapsed = Duration.between(start, finish).toMillis();
					diffMs += timeElapsed;
					unfinishedTime = false;		
				}
				
				diffM = (diffMs/1000)/60;
				diffMTotal += diffM;
				diffHTotal = diffMTotal/60;
				minutesRemaining = diffMTotal%60;							
			}
			returnTime[0] = diffHTotal;	
			returnTime[1] = minutesRemaining;
		} catch (ClassNotFoundException | SQLException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "ClassNotFoundException", JOptionPane.ERROR_MESSAGE);
			Runnable.myLog.logger.info(e + " - " +  Runnable.myLog.stackTraceToString(e));
		} catch (ParseException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "ParseException", JOptionPane.ERROR_MESSAGE);
			Runnable.myLog.logger.info(e + " - " +  Runnable.myLog.stackTraceToString(e));
		}
		unfinishedTime = false;
		return returnTime;
	}
	
	public void recentList() {
		
		JLabel label = new JLabel("Lista reciente:");
		label.setForeground(Color.GRAY);
		centerPanel1.add(label, BorderLayout.NORTH);
		
		recent = new JTextArea(15,12);
		centerPanel1.add(recent);
		recent.setEditable(false);

		JScrollPane sp = new JScrollPane(recent);
		centerPanel1.add(sp);
		
		LocalDate current = LocalDate.now();
		LocalDate before = current.minusDays(14);		
		
		ArrayList<LocalDate> days = getDaysBetween(before, current);
				
		for(int i=0; i<days.size(); i++) {
			long[] dayCuota = cuota(days.get(i).toString(), false);
			recent.append(days.get(i).toString() + ":  " + dayCuota[0] + "h, " + dayCuota[1] + "m");
			
			if((days.size()-1)<=i)
				break;
			
			recent.append("\n");
		}
	}
	
	private JPanel centerPanel1, centerPanel2;
	private JTextArea recent;
	private Instant start, finish;
	private JButton clockIn, clockOut, checkClock, checkCuota;
	private static SQL db = new SQL();
}
