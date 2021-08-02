package gui;

import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.*;

public class MainFrame extends JFrame {	

	public MainFrame() {
		
		setTitle("Sistema de Fichaje");
		
		setIconImage(new ImageIcon("images/fichaje16.png").getImage());
		
		setSize(400,375);
		setLocationRelativeTo(null);
		setResizable(false);
		
		MainPanel mPanel = new MainPanel();		
		add(mPanel);
		
		setVisible(true);
	}
}