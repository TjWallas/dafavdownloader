/**
 *    ProgressDialog - Provide an advanced Progressbar dialog.
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

import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

public class ProgressDialog extends JDialog implements ActionListener, WindowListener{
    /**
	 * 
	 */
	private static final long serialVersionUID = -377411823229486966L;
	JProgressBar progressBarUnit = new JProgressBar(0); 
	JProgressBar progressBarTotal = new JProgressBar(0);
	JLabel message;
	JButton cancel;
	private boolean cancelled = false;
	
    public ProgressDialog(JFrame owner) throws HeadlessException{ 
        super(owner, true); 
        init();
       } 
    private void init() {
    	setTitle("Downloading");
    	
        JPanel contents = (JPanel)getContentPane();
        JPanel progress = new JPanel();
        progress.setLayout(new BoxLayout(progress,BoxLayout.Y_AXIS));
        
        progressBarUnit.setStringPainted(true);
        progressBarTotal.setStringPainted(true);
        
        progress.add(progressBarUnit);
        progress.add(progressBarTotal);
        
        setLayout(new BorderLayout());
        contents.add(progress,BorderLayout.NORTH); 
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); 
        setResizable(false);
        setSize(400, 100);
        addWindowListener(this);
        
        message = new JLabel("");
        cancel = new JButton("Cancel");
        cancel.addActionListener(this);
        
        message.setVerticalAlignment(SwingConstants.TOP);
        contents.add(message,BorderLayout.CENTER);
        contents.add(cancel,BorderLayout.EAST);

        
        setLocationRelativeTo(null);
 	
    }
    public ProgressDialog(JFrame owner,int maxUnit, int maxTotal) throws HeadlessException{ 
        super(owner, true); 
        setUnitMax(maxUnit);
        setTotalMax(maxTotal);
        init();

    } 
    public void setUnitMax(final int max) {
    	
		Runnable command = new Runnable() {
			public void run() {
		    	if (max > 0) {
		    		progressBarUnit.setIndeterminate(false);	
		    		progressBarUnit.setMaximum(max);	
		    	} else {
		    		progressBarUnit.setIndeterminate(true);	
		    	}
			}
		};
		SwingThread.runSync(command);
    }
    
    public void setTotalText(final String text) {
    	
		Runnable command = new Runnable() {
			public void run() {
				progressBarTotal.setStringPainted(text != null);
		    	progressBarTotal.setString(text);
			}
		};
		SwingThread.runSync(command);
    }
    
    public void setUnitText(final String text) {
		Runnable command = new Runnable() {
			public void run() {
		    	progressBarUnit.setStringPainted(text != null);
		    	progressBarUnit.setString(text);		
			}
		};
		SwingThread.runSync(command);
    }
    
    public void setTotalMax(final int max) {
		Runnable command = new Runnable() {
			public void run() {
		    	if (max > 0) {
		    		progressBarUnit.setIndeterminate(false);
		    		progressBarTotal.setMaximum(max);	
		    	} else {
		    		progressBarTotal.setIndeterminate(true);	
		    	}		
			}
		};
		SwingThread.runSync(command);

    }
    
    public void incremUnit(){
		Runnable command = new Runnable() {
			public void run() {
				progressBarUnit.setValue(progressBarUnit.getValue()+1);
			}
		};
		SwingThread.runASync(command);
    }
    
    public void incremTotal(){
		Runnable command = new Runnable() {
			public void run() {
				progressBarTotal.setValue(progressBarTotal.getValue()+1);
		    	progressBarTotal.setString(progressBarTotal.getValue() + " / " + progressBarTotal.getMaximum());
			}
		};
		SwingThread.runASync(command);
    
    }
    
    public void setUnitValue(final int value){
		Runnable command = new Runnable() {
			public void run() {
				progressBarUnit.setValue(value);
			}
		};
		SwingThread.runASync(command);
    	
    }
    public void setTotalValue(final int value){
		Runnable command = new Runnable() {
			public void run() {
				progressBarTotal.setValue(value);
			}
		};
		SwingThread.runASync(command);
    }
    
    public void setText(final String text) {
		Runnable command = new Runnable() {
			public void run() {
				message.setText("<html>" + text + "</html>");
			}
		};
		SwingThread.runASync(command);
    }
    
    public boolean isCancelled () {
    	return cancelled;
    }
    public void close() {
		Runnable command = new Runnable() {
			public void run() {
				dispose(); 
			}
		};
		SwingThread.runSync(command);
    }
	public void actionPerformed(ActionEvent e) {
		cancel.setEnabled(false);
		cancelled = true;
		
	}
	public void windowActivated(WindowEvent e) {
	}
	public void windowClosed(WindowEvent e) {
	}
	public void windowClosing(WindowEvent e) {
		cancel.setEnabled(false);
		cancelled = true;
		
	}
	public void windowDeactivated(WindowEvent e) {
	}
	public void windowDeiconified(WindowEvent e) {
	}
	public void windowIconified(WindowEvent e) {
	}
	public void windowOpened(WindowEvent e) {
	}


}
