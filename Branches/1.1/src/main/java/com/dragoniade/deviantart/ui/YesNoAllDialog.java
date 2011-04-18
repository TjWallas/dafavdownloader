/**
 *    YesNoAllDialog - Provide a Yes,No,All Yes, All no prompt dialog
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

import java.awt.Component;

import javax.swing.JOptionPane;

public class YesNoAllDialog {

	private final static String[] options = {"Yes", "Yes to all", "No", "No to All", "Cancel"};
	public final static int YES = 0;
	public final static int YES_ALL = 1;
	public final static int NO = 2;
	public final static int NO_ALL = 3;
	public final static int CANCEL = 4;
	
	private boolean all = false;
	private int selection = -1;
	
	public YesNoAllDialog() {
		
	}
	
	public int displayDialog(final Component parentComponent, final String title, final String message) {
		
		if (all) {
			return selection;
		}
		
		Runnable command = new Runnable() {
			public void run() {
				int result = JOptionPane.showOptionDialog(parentComponent,message, title, JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE,null,options,options[0]);
				
				switch (result) {
				case YES:
					all = false;
					selection = YES;
					break;
				case YES_ALL:
					all = true;
					selection = YES;
					break;
				case NO:
					all = false;
					selection = NO;
					break;
				case NO_ALL:
					all = true;
					selection = NO;
					break;
				default:
					all = false;
					selection = CANCEL;
					break;
				}
			}
		};
		
		SwingThread.runSync(command);
		
		return selection;
		
		
	}
}
