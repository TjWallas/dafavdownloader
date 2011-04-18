package com.dragoniade.deviantart.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class DebugDialog extends JDialog {

	
	private static final long serialVersionUID = -1926285902087869932L;
	private JTextArea area;
	private JButton button;
	
	public DebugDialog(JFrame parent, Throwable e) {
		super (parent,true);
		JPanel genPanel = new JPanel();
		BorderLayout genLayout = new BorderLayout(); 
		genPanel.setLayout(genLayout);
		
		setTitle("Error trace");
		StringWriter sw = new StringWriter();
		PrintWriter pw =  new PrintWriter(sw);
		e.printStackTrace(pw);
		pw.close();
		
		area = new JTextArea(sw.getBuffer().toString());
		area.setEditable(false);
		button = new JButton("Close");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DebugDialog.this.setVisible(false);
				DebugDialog.this.dispose();
			}
		});
		
		genPanel.add(area,BorderLayout.CENTER );
		genPanel.add(button,BorderLayout.SOUTH );
		add(genPanel, BorderLayout.CENTER);
		pack();
		setResizable(false);
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((d.width-getWidth())/2, (d.height-getHeight())/2);
		setVisible(true);
	}
}
