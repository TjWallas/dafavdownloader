package com.dragoniade.deviantart.ui;

import java.awt.Component;

import javax.swing.JOptionPane;

public class DialogHelper {
	public static void showMessageDialog(final Component parentComponent,
			final Object message, final String title, final int messageType) {
		Runnable command = new Runnable() {
			public void run() {
				JOptionPane.showMessageDialog(parentComponent,message,title,messageType);
			}
		};
		SwingThread.runSync(command);
	}	
	
	public static int showConfirmDialog(final Component parentComponent,
	        final Object message, final String title, int optionType) {
		
		final 
		SwingThread<Integer> command = new SwingThread<Integer>() {
			int result = -1;
			public void run() {
				result = JOptionPane.showConfirmDialog(parentComponent, message,title,JOptionPane.YES_NO_OPTION );
			}
			public Integer getResult() {
				return result;
			}
		};
		SwingThread.runSync(command);
		
		return command.getResult();
		
	}
}
