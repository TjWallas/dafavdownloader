/**
 *    SwingThread - Provide thread safe call to Swing
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

import javax.swing.SwingUtilities;

public abstract class SwingThread<O> implements Runnable {

	public abstract O getResult();
	
	
	public static void runSync(Runnable command) {
		if (SwingUtilities.isEventDispatchThread()) {
			command.run();
		} else {
			try {
				SwingUtilities.invokeAndWait(command);
			} catch (Exception e) {
					
			}
		}		
	}
	
	public static void runASync(Runnable command) {
		if (SwingUtilities.isEventDispatchThread()) {
			command.run();
		} else {
			try {
				SwingUtilities.invokeLater(command);
			} catch (Exception e) {
					
			}
		}		
	}
}
