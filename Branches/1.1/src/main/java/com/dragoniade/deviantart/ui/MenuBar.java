/**
 *    MenuBar - Swing menubar for the application
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

import java.awt.event.InputEvent;
import java.util.HashMap;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;


public class MenuBar extends JMenuBar{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6373216223078033003L;


	public enum ACTION {
		EXIT (0),
		OPEN (1),
		SAVE (2),
		DEFAULT (3),
		PREFERENCES (4),
		HELP (5),
		ABOUT (6),
		WEBSITE (7),
		DEVIANTART (8),
		TOTO(Integer.MAX_VALUE);
		
		public String toString() {
			return String.valueOf(value);
		}
		private final Integer value;
		ACTION(int value) {
			this.value = value;
		}
		public Integer getValue() {
			return value;
		}
	}
	
	private HashMap<JMenuItem,ACTION> actionMap;
	private JMenu lookAndFeelMenu;
	
	public MenuBar(DownloaderGUI owner) {
		actionMap = new HashMap<JMenuItem,ACTION>();
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('F');
		add(fileMenu);
		
		JMenuItem openItem = new JMenuItem("Open Session");
		openItem.setAccelerator(KeyStroke.getKeyStroke('O',InputEvent.CTRL_DOWN_MASK));
		actionMap.put(openItem,ACTION.OPEN);
		openItem.addActionListener(owner);
		fileMenu.add(openItem);

		JMenuItem saveItem = new JMenuItem("Save Session");
		saveItem.setAccelerator(KeyStroke.getKeyStroke('S',InputEvent.CTRL_DOWN_MASK));
		actionMap.put(saveItem,ACTION.SAVE);
		saveItem.addActionListener(owner);
		fileMenu.add(saveItem);
		
		JMenuItem defaultItem = new JMenuItem("Save Session as Default");
		defaultItem.setAccelerator(KeyStroke.getKeyStroke('S',InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		actionMap.put(defaultItem,ACTION.DEFAULT);
		defaultItem.addActionListener(owner);
		fileMenu.add(defaultItem);
		
		JMenuItem closeItem = new JMenuItem("Exit");
		actionMap.put(closeItem,ACTION.EXIT);
		closeItem.addActionListener(owner);
		fileMenu.add(closeItem);
		
		
		JMenu preferencesMenu = new JMenu("Preferences");
		preferencesMenu.setMnemonic('P');
		add(preferencesMenu);
		
		JMenuItem optionItem = new JMenuItem("Options");
		optionItem.setAccelerator(KeyStroke.getKeyStroke('P',InputEvent.CTRL_DOWN_MASK));
		actionMap.put(optionItem,ACTION.PREFERENCES);
		optionItem.addActionListener(owner);
		preferencesMenu.add(optionItem);
		
		
		JMenu lookFeelMenu = new JMenu("Look & Feel");
		preferencesMenu.add(lookFeelMenu);
		loadLnF(lookFeelMenu, owner);
		
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic('H');
		add(helpMenu);
		
		JMenuItem helpItem = new JMenuItem("Help");
		helpItem.setAccelerator(KeyStroke.getKeyStroke("F1"));
		actionMap.put(helpItem,ACTION.HELP);
		helpItem.addActionListener(owner);
		helpMenu.add(helpItem);
		
		JMenuItem webItem = new JMenuItem("Website");
		actionMap.put(webItem,ACTION.WEBSITE);
		webItem.addActionListener(owner);
		helpMenu.add(webItem);
		
		JMenuItem daItem = new JMenuItem("Goto deviantART");
		daItem.setAccelerator(KeyStroke.getKeyStroke('D',InputEvent.CTRL_DOWN_MASK));
		actionMap.put(daItem,ACTION.DEVIANTART);
		daItem.addActionListener(owner);
		helpMenu.add(daItem);
		
		
		JMenuItem aboutItem = new JMenuItem("About");
		actionMap.put(aboutItem,ACTION.ABOUT);
		aboutItem.addActionListener(owner);
		helpMenu.add(aboutItem);
	}
	
	private void loadLnF(JMenu modelSubMenu, DownloaderGUI ui) {
		lookAndFeelMenu = modelSubMenu;
		LookAndFeelInfo[] lafInfo = UIManager.getInstalledLookAndFeels();
		LookAndFeel current = UIManager.getLookAndFeel();
		 
		for (LookAndFeelInfo info : lafInfo) {
			JRadioButtonMenuItem lookAndFeelMenuItem = new JRadioButtonMenuItem(info.getName());
			lookAndFeelMenuItem.addActionListener(ui.new ChangeLookAndFeelAction(info.getClassName()));
			modelSubMenu.add(lookAndFeelMenuItem);
			lookAndFeelMenuItem.setSelected(current.getClass().getName().equals(info.getClassName()));
		}
	}
	
	public void selectLnF(Object source) {
		for (int i=0; i< lookAndFeelMenu.getItemCount();i++) {
			JRadioButtonMenuItem item = (JRadioButtonMenuItem) lookAndFeelMenu.getItem(i);
			item.setSelected(item == source);
		}
	}
	
	
	public ACTION getAction(JMenuItem item) {
		return actionMap.get(item);
	}

}
