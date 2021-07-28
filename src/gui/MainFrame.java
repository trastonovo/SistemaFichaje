package gui;

import javax.swing.*;

public class MainFrame extends JFrame {	

	public MainFrame() {
		
		setTitle("Sistema de Fichaje");
		
		setSize(330,360);
		setLocationRelativeTo(null);
		setResizable(false);
		
		MainPanel mPanel = new MainPanel();		
		add(mPanel);
		
		setVisible(true);
	}
	
}