/**
 *    HelpDialog - Provide a simple web help
 *    Copyright (C) 2009-2010  Philippe Busque
 *    https://sourceforge.net/projects/dafavdownloader/
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

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.URL;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class HelpDialog extends JDialog {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4444282683407354837L;

	private final JTextPane textPane;
	private final JFrame owner;
	public HelpDialog(JFrame frame) {
		 super(frame);
		 setTitle("Help");
		 this.owner = frame;
		 textPane = new JTextPane();
		 JScrollPane scroll = new JScrollPane(textPane);
		 this.add(scroll);
		 this.pack();
		 this.setSize(800,600);
		 Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			this.setLocation((d.width-getWidth())/2, (d.height-getHeight())/2);
		 this.setVisible(true);	
		 
		 textPane.addHyperlinkListener( new HyperlinkListener() {
			
			public void hyperlinkUpdate(HyperlinkEvent ev) {
				if (ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					String uri = ev.getURL().toString();
					
					if (uri.startsWith("http")) {
						
						if (!NavigatorLauncher.launch(uri)) {
							JOptionPane.showMessageDialog(owner,"Unable to start web browser.","Error",JOptionPane.ERROR_MESSAGE);	
						}
						
					} else {
						int pound = uri.indexOf('#');
						String url,mark;
						if (pound > -1) {
							url = uri.substring(0,pound);
							if (pound == uri.length()-1) {
								mark = null;
							} else {
								mark = uri.substring(pound+1);	
							}
							
						} else {
							url = uri;
							mark = null;
						}
						if (url.length() > 0) {
							 try {
									textPane.setPage(ev.getURL());
								} catch (IOException e) {
									e.printStackTrace();
									return;
								}
						}
						if (mark != null) {
							textPane.scrollToReference(mark);
						}
					}
				}
					
			}
		});
		 textPane.setEditable(false);
		 URL page = getClass().getResource("/help/Index.html");
		 setPage(page);
	}
 
	public void setPage(URL page) {
		 try {
				textPane.setPage(page);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(owner,"Unable to load page.","Error",JOptionPane.ERROR_MESSAGE);
			}
	}

}
