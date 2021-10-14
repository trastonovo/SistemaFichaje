package gui;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

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
import java.util.Properties;

public class MainPanel extends JPanel implements ActionListener {
	
	public MainPanel() {
		
		//Ensures the settings file creation on first-time boot
		setAudioSettings("");		
		if(!config.exists())
			setAudioSettings("true");
		
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
		
		JPanel westPanel = new JPanel();
		add(westPanel, BorderLayout.WEST);
		westPanel.setPreferredSize(new Dimension(5,5));
		
		Icon iconBookOpen = new ImageIcon("images/book_open.png");
		clockIn = new JButton("Entrada", iconBookOpen);
		northPanel.add(clockIn);
		clockIn.addActionListener(this);
		clockIn.setPreferredSize(new Dimension(185,40));
		clockIn.setToolTipText("Fichar para entrada");
		
		Icon iconBookClosed = new ImageIcon("images/book_closed.png");
		clockOut = new JButton("Salida", iconBookClosed);
		northPanel.add(clockOut);
		clockOut.addActionListener(this);	
		clockOut.setPreferredSize(new Dimension(185,40));
		clockOut.setToolTipText("Fichar para salida");
		
		Icon iconCuota = new ImageIcon("images/calendar32.png");
		checkCuota = new JButton(iconCuota);
		eastPanel.add(checkCuota).setBackground(Color.WHITE);
		checkCuota.addActionListener(this);	
		checkCuota.setPreferredSize(new Dimension(40,40));
		checkCuota.setToolTipText("Ver cuotas por día en el calendario");
		
		Icon iconList = new ImageIcon("images/list32.png");
		checkClock = new JButton(iconList);
		eastPanel.add(checkClock).setBackground(Color.WHITE);
		checkClock.addActionListener(this);
		checkClock.setPreferredSize(new Dimension(40,40));	
		checkClock.setToolTipText("Ver todas las fechas/horas de entrada/salida");
		
		Icon iconRefresh = new ImageIcon("images/refresh32.png");///////////////////////////////////////////////////////////////////////////
		refresh = new JButton(iconRefresh);
		eastPanel.add(refresh).setBackground(Color.WHITE);
		refresh.addActionListener(this);
		refresh.setPreferredSize(new Dimension(40,40));	
		refresh.setToolTipText("Recarga la lista reciente");
		
		//Icon iconMute = new ImageIcon("images/mute32.png");
		muteAudio = new JButton();
		changeMuteIcon();
		eastPanel.add(muteAudio).setBackground(Color.WHITE);
		muteAudio.addActionListener(this);
		muteAudio.setPreferredSize(new Dimension(40,40));		
				
		centerPanel1 = new JPanel();
		centerPanel2 = new JPanel();
		centerPanel1.setLayout(new BorderLayout());
		centerPanel2.setLayout(new BorderLayout());
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, centerPanel1, centerPanel2);
		split.setEnabled(false);
		split.setDividerSize(0);
		add(split, BorderLayout.CENTER);
		
		recentList(true);				
		
		centerPanel2.add(picLabel);
		palamuteStatus();
		
		buttonDisabled();
		
		JLabel version = new JLabel("  Versión 1.0.1  ");
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
				palamuteStatus();
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
				palamuteStatus();
				recentList(false);
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
				playSound("audio/click.wav");
			} catch(ClassNotFoundException ex) {
				JOptionPane.showMessageDialog(null, ex.getMessage(), "ClassNotFoundException", JOptionPane.ERROR_MESSAGE);
				Runnable.myLog.logger.info(ex + " - " +  Runnable.myLog.stackTraceToString(ex));
			} catch (SQLException ex) {
				JOptionPane.showMessageDialog(null, ex.getMessage(), "SQLException", JOptionPane.ERROR_MESSAGE);
				Runnable.myLog.logger.info(ex + " - " +  Runnable.myLog.stackTraceToString(ex));
			}				
		}
		//Check hour cuota from a calendar
		if(button==checkCuota) {
			playSound("audio/click.wav");
			JFrame cuotaFrame = new JFrame();
			cuotaFrame.setTitle("Cuota");			
			cuotaFrame.setSize(250,250);
			cuotaFrame.setLocation(getMousePosition());
			cuotaFrame.setVisible(true);
			cuotaFrame.setResizable(false);
			cuotaFrame.setIconImage(new ImageIcon("images/calendar16.png").getImage());
			cuotaFrame.setLayout(new BorderLayout());
			cuotaFrame.addWindowListener(new WindowAdapter() {
			    public void windowClosing(WindowEvent windowEvent) {
			        playSound("audio/click_out.wav");
			    }
			});
			
			JTextField text = new JTextField();
			text.setEditable(false);
			cuotaFrame.add(text, BorderLayout.SOUTH);
			
			JCalendar calendar = new JCalendar();
			calendar.setBounds(330,450,1,1);
			cuotaFrame.add(calendar);
			
			//Shows time for current date
	        long[] returnTime;
			returnTime = cuota(date, true);
	        text.setText("Horas registradas: " + returnTime[0] + "h, " + returnTime[1]+"m");
				        
			calendar.addPropertyChangeListener("calendar", new PropertyChangeListener() {
			    public void propertyChange(PropertyChangeEvent e) {	
			    	//If a month is <10, adds a 0 to the left for DB use
			    	String month0 = "";
			    	String day0 = "";
			    	
			    	final Calendar c = (Calendar) e.getNewValue();
			        int year = c.get(Calendar.YEAR);
			        int month = (c.get(Calendar.MONTH))+1;
			        if(month<10)
			        	month0 = "0";
			        int day = c.get(Calendar.DAY_OF_MONTH);
			        if(day<10)
			        	day0 = "0";
			        String iDate = year + "-" + month0 + month + "-" + day0 + day;
			        playSound("audio/click.wav");
			        
			        long[] returnTime;
					returnTime = cuota(iDate, true);
			        text.setText("Horas registradas: " + returnTime[0] + "h, " + returnTime[1]+"m");
			    }
			});
		}
		//Refresh recent list
		if(button==refresh) {
			recentList(false);
			playSound("audio/click.wav");
		}
		
		//Toggle OnOff all output audio
		if(button==muteAudio) {
						
			if(p.getProperty("audio").contains("false"))
				setAudioSettings("true");
			else if (p.getProperty("audio").contains("true"))
				setAudioSettings("false");
			playSound("");
			changeMuteIcon();
			playSound("audio/turn_on.wav");
		}

		buttonDisabled();
	}
	
	//DisableClockIn=Positive; DisableClockOut=Negative,0
	public int buttonDisabled() {
		
		int count1 = 0;
		int count2 = 0;
		int result = 0;
		
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
			
			result = count1-count2;
			
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
		
		return result;
	}
	
	//Emergent window: Clock in-out Query
	public void showClockIO() throws ClassNotFoundException, SQLException {
		
		SQL.getConnection();
		
		JFrame listFrame = new JFrame();
		listFrame.setTitle("Registros");			
		listFrame.setSize(330,450);
		listFrame.setLocation(getMousePosition());
		listFrame.setVisible(true);
		listFrame.setResizable(false);
		listFrame.setIconImage(new ImageIcon("images/list16.png").getImage());
		listFrame.addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent windowEvent) {
		        playSound("audio/click_out.wav");
		    }
		});
		
		JPanel lamina2 = new JPanel();
		listFrame.add(lamina2);
		
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
	
	//Check cuota from a given da (0=h, 1=m)y
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
	
	//Prints recent-date cuota list
	public void recentList(boolean og) {
		
		if(og) {	//avoids label on "reload"
			JLabel label = new JLabel("Lista reciente:");
			label.setForeground(Color.GRAY);
			centerPanel1.add(label, BorderLayout.NORTH);
			recent = new JTextArea(15,12);
		}
		
			recent.selectAll();
			recent.replaceSelection("");
			centerPanel1.add(recent);
			recent.setEditable(false);
			
			LocalDate current = LocalDate.now();
			LocalDate before = current.minusDays(14);		
			
			ArrayList<LocalDate> days = getDaysBetween(before, current);
		
		for(int i=0; i<days.size(); i++) {
			long[] dayCuota = cuota(days.get(i).toString(), true);
			recent.append(days.get(i).toString() + ":  " + dayCuota[0] + "h, " + dayCuota[1] + "m");

			if((days.size()-1)<=i)
				break;
			
			recent.append("\n");
		}
	}

	//Changes gif on the main screen
	public void palamuteStatus() {
				
		if(buttonDisabled()>0) {			
			url = "images/palamute_out.gif";
			picLabel.setIcon(new ImageIcon(url));
			playSound("audio/palamute_song2.wav");
		}
		else {
			url = "images/palamute_in.gif";
			picLabel.setIcon(new ImageIcon(url));
			playSound("audio/palamute_song.wav");
		}			
	}
	
	//Plays any sound providing url
	public void playSound(String url) {

		if(p.getProperty("audio").contains("true")) {
			AudioInputStream ais;
			try {
				if(clip!=null && clip.isActive())
					clip.stop();
				ais = AudioSystem.getAudioInputStream(new File(url).getAbsoluteFile());
				clip = AudioSystem.getClip();
				clip.open(ais);
				clip.start();
			} catch (UnsupportedAudioFileException | IOException e1) {
				Runnable.myLog.logger.info(e1 + " - " +  Runnable.myLog.stackTraceToString(e1));
			} catch (LineUnavailableException e1) {
				Runnable.myLog.logger.info(e1 + " - " +  Runnable.myLog.stackTraceToString(e1));
			}	
		}
		else if(clip!=null)
			clip.stop();
	}
	
	public void setAudioSettings(String audio) {
		
		config = new File("config.properties");
		try {
			p = new Properties();
			if(audio.contains("true"))
				p.setProperty("audio", "true");				
			else if (audio.contains("false"))
				p.setProperty("audio", "false");
			else {
				FileReader fr = new FileReader("config.properties");
				p.load(fr);
			}
			FileWriter fw = new FileWriter(config);
			p.store(fw, "settings");
			fw.close();				
		} catch(FileNotFoundException e1) {
			Runnable.myLog.logger.info(e1 + " - " +  Runnable.myLog.stackTraceToString(e1));
		} catch (IOException e1) {
			Runnable.myLog.logger.info(e1 + " - " +  Runnable.myLog.stackTraceToString(e1));
		}
	}
	
	public void changeMuteIcon() {
		
		if(p.getProperty("audio").contains("true")) {
			muteAudio.setIcon(new ImageIcon("images/unMute32.png"));
			muteAudio.setToolTipText("Silenciar audio");
		}			
		else if(p.getProperty("audio").contains("false")) {
			muteAudio.setIcon(new ImageIcon("images/mute32.png"));			
			muteAudio.setToolTipText("Restaurar audio");
		}			
	}
	
	private Properties p;
	private File config;
	private Clip clip;	
	private JLabel picLabel = new JLabel();	
	private JPanel centerPanel1, centerPanel2;
	private JTextArea recent;
	private Instant start = Instant.now();
	private Instant finish;
	private JButton clockIn, clockOut, checkClock, checkCuota, muteAudio, refresh;
	private String url = null;
	private static SQL db = new SQL();
}
