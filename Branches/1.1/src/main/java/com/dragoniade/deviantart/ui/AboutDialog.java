/**
 *    AboutDialog - Display the About... dialog
 *    Copyright (C) 2009-2010  Philippe Busque
 *    http://dafavdownloader.sourceforge.net/
 *    
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.dragoniade.deviantart.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class AboutDialog extends JDialog {

	private static final long serialVersionUID = -5882874353251063585L;

	public AboutDialog(final JFrame owner) {
		super(owner,true);
		setTitle("About");
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		JTextPane textPane = new JTextPane();
		textPane.setEditable(false);
		try {
			textPane.setPage(getClass().getResource("/About.html"));
		} catch (IOException e1) {
		}
		textPane.addHyperlinkListener( new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent ev) {
				if (ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					String uri = ev.getURL().toString();
					if (!NavigatorLauncher.launch(uri)) {
						JOptionPane.showMessageDialog(owner,"Unable to start web browser.","Error",JOptionPane.ERROR_MESSAGE);	
					}	
				}
			}
		});
		
		JButton okButton = new JButton("Ok");
		okButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AboutDialog.this.setVisible(false);
				AboutDialog.this.dispose();
				
			}
		});

		JButton licensingButton = new JButton("Licensing");
		licensingButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				AboutDialog.this.setVisible(false);
				AboutDialog.this.dispose();
				new HelpDialog(owner).setPage(getClass().getResource("/help/Copyright.html"));
			}
		});
		
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		panel.add(licensingButton);
		panel.add(okButton);

		this.add(textPane);
		this.add(panel);
		this.pack();
		this.setResizable(false);
		
		int x = (owner.getWidth() - this.getWidth()) / 2;
		int y = (owner.getHeight() - this.getHeight()) / 2;
		
		this.setLocation(owner.getLocation().x + x, owner.getLocation().y + y);
		setVisible(true);
	}
}
