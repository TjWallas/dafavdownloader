/**
 *    DownloaderGUI - Provide the GUI to the software.
 *    Copyright (C) 2009-2011  Philippe Busque
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
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Locale;
import java.util.Properties;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.filechooser.FileFilter;

import com.dragoniade.clazz.SearcherClassCache;
import com.dragoniade.deviantart.deviation.Search;
import com.dragoniade.deviantart.deviation.Search.SEARCH;
import com.dragoniade.deviantart.favorites.FavoritesDownloader;
import com.dragoniade.deviantart.ui.MenuBar.ACTION;
import com.dragoniade.exceptions.LoggableException;


public class DownloaderGUI extends JFrame implements ActionListener {

	/**
	 * 
	 */
	private static final String WEBSITE_URL = "https://sourceforge.net/projects/dafavdownloader";
	private static final String DEVIANTART_URL = "http://www.deviantart.com";
	
	private static final long serialVersionUID = 8356316629221850066L;
	private JTextField location;
	private JButton jButton;
	private JLabel statusLabel;
	private Properties properties;
	private final DownloaderGUI instance; 
	private JTextPane textPane;
	private JSpinner spinner;
	private OffsetSpinnerModel spinnerModel;
	
	public DownloaderGUI(File config) {
		Locale.setDefault(Locale.ENGLISH);
		properties = getDefaults();
		
		if (config == null) {
			File home = new File(System.getProperty("user.home"),".DaFavorites");
			config = new File(home,"default.fss");
		} 
		
		if (config.exists()) {
			try {
				loadConfig(config);
			} catch (IOException e1) {
			}
		}

		try {
			UIManager.setLookAndFeel(properties.getProperty(Constants.LNF,UIManager.getSystemLookAndFeelClassName()));
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error loading the native Look and Feel.","Error",JOptionPane.ERROR_MESSAGE);
		}
		
		ImageIcon icon = new ImageIcon(getClass().getResource("/deviantart.png"));
		setIconImage(icon.getImage());
		setTitle("deviantART's Favorite Downloader");
		setLayout(new BorderLayout());
		textPane = new JTextPane();
		textPane.setContentType("text/html");
		textPane.setEditable(false);
		textPane.setText("<html><body></body></html>");
		textPane.addHyperlinkListener( new HyperlinkListener() {

			public void hyperlinkUpdate(HyperlinkEvent ev) {
				EventType et = ev.getEventType();
				if (et == EventType.ACTIVATED) {
					String uri = ev.getURL().toString();
					if (!NavigatorLauncher.launch(uri)) {
						JOptionPane.showMessageDialog(DownloaderGUI.this,"Unable to start web browser.","Error",JOptionPane.ERROR_MESSAGE);	
					}
				}
			}
			
		});
		JScrollPane scroll = new JScrollPane(textPane);
		scroll.setAutoscrolls(true);
		
		JPanel topPanel = new JPanel(true);
		topPanel.setLayout(new BoxLayout(topPanel,BoxLayout.X_AXIS));
		
		JLabel locationLab = new JLabel("Location   ");
		location = new JTextField(properties.getProperty(Constants.LOCATION));
		location.setEditable(false);
		JLabel skipLabel = new JLabel("Skip:");
		
		spinner = new JSpinner();
		spinnerModel = new OffsetSpinnerModel(getSearchOffset());
		spinner.setModel(spinnerModel);
		
		jButton = new JButton("Download");
		jButton.setMnemonic('D');
		jButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				
				final ProgressDialog progress = new ProgressDialog(instance);
				Runnable r = new Runnable() {
					public void run() {
						String username = properties.getProperty(Constants.USERNAME); 
						String location = properties.getProperty(Constants.LOCATION);
						String mature = properties.getProperty(Constants.MATURE,location);
						int throttle = Integer.parseInt(properties.getProperty(Constants.THROTTLE,"5"));
						Search searcher = getSearcher();
						if (searcher == null) {
							return;
						}
						searcher.setSearch(SEARCH.lookup(properties.getProperty(Constants.SEARCH,SEARCH.FAVORITE.toString())));
						FavoritesDownloader downloader = new FavoritesDownloader(username,location,mature, searcher);
						downloader.setThrottle(throttle);
						downloader.setGUI(instance, textPane, progress);
						
						try {
							downloader.execute((Integer)spinner.getValue());	
						} finally {
							progress.close();	
						}
					}
				};
				
				Thread t = new Thread(r);
				t.start();
				progress.setVisible(true);
			}
		});
		
		topPanel.add(locationLab );
		topPanel.add(location);
		topPanel.add(skipLabel);
		topPanel.add(spinner);
		topPanel.add(jButton);
		
		
		JPanel statusPanel = new JPanel(true);
		statusPanel.setLayout(new BoxLayout(statusPanel,BoxLayout.X_AXIS));
		statusLabel = new JLabel(properties.getProperty(Constants.USERNAME));
		statusPanel.add(statusLabel);
		
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		this.setJMenuBar(new MenuBar(this));
		this.add(topPanel,BorderLayout.NORTH);
		this.add(statusPanel,BorderLayout.SOUTH);
		this.add(scroll,BorderLayout.CENTER);
		this.setSize(640, 480);
		
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((d.width-getWidth())/2, (d.height-getHeight())/2);
		this.setVisible(true);
		instance = this;
		
	}

	public void updateGUI() {
		statusLabel.setText(properties.getProperty(Constants.USERNAME));
		location.setText(properties.getProperty(Constants.LOCATION));
		
		int offset = getSearchOffset();
		spinnerModel.setOffset(offset);
		Integer value = (Integer)spinner.getValue();
		value = (value / offset )* offset;
		spinner.setValue(value);
	}
	
	public static void main (String[] args) {
		File config = null;
		if (args.length == 1) {
			config = new File(args[0]);
			if (!config.exists()) {
				config = null;
			}
		}
		String version = System.getProperty("java.version");
		int indexMajor = version.indexOf('.') + 1 ;
		int indexMinor = version.indexOf('.',indexMajor);
		int minor = Integer.parseInt(version.substring(indexMajor,indexMinor));
		if (minor < 6) {
			JOptionPane.showMessageDialog(null,"<html>Incompatible Java version. You must use Java 1.6 or later. <br>Your current version is " + System.getProperty("java.version") + ". Please upgrade or install the bundled version.</html>","Java version error",JOptionPane.ERROR_MESSAGE );
			System.exit(100);
		}
		SearcherClassCache.getInstance();
		final DownloaderGUI gui = new DownloaderGUI(config);
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			
			public void uncaughtException(Thread t, Throwable e) {
				
				if (System.getProperty("debug") == null) {
					File error = new File(System.getProperty("java.io.tmpdir") + File.separatorChar + "error" + System.currentTimeMillis() + ".log");
					error.getParentFile().mkdirs();
					PrintWriter pw;
					try {
						pw = new PrintWriter(error);
						e.printStackTrace(pw);
						pw.close();
					} catch (FileNotFoundException e1) {
					}
					if (!(e instanceof LoggableException)) {
						JOptionPane.showMessageDialog(gui, "An internal error has occured and has been saved to  '" + error.getAbsolutePath() + "'. If this keep happening, please send us a copy of those report.");	
					}
				} else {
					new DebugDialog(gui,e); 
				}
			}
		});
	}

	public void actionPerformed(ActionEvent e) {
		MenuBar bar = (MenuBar) getJMenuBar();
		Object source = e.getSource();
		if (source instanceof JMenuItem) {
			JMenuItem jMenuItem = (JMenuItem) source;
			ACTION action = bar.getAction(jMenuItem);

			switch (action) {
			case PREFERENCES: {
				new PreferencesDialog(this,properties);
			}
			break;
			case SAVE: {
				JFileChooser jfs = new JFileChooser(new File(System.getProperty("user.home"),".DaFavorites"));
				jfs.setDialogTitle("Save your current session");
				FileFilter ff = new FssFileFilter();
				jfs.setAcceptAllFileFilterUsed(true);
				jfs.setFileFilter(ff);
				jfs.setDialogType(JFileChooser.SAVE_DIALOG);
				int result = jfs.showSaveDialog(this);
				if(result != JFileChooser.APPROVE_OPTION) {
					return;
				}
				
				File selected = jfs.getSelectedFile();
				if (selected == null) {
					return;
				}
				
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(selected);
					properties.store(fos, selected.getName());
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(this,"A write error occured: " + ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
					return;
				} finally {
					if (fos != null) {
						try {
							fos.close();
						} catch (IOException e1) {}
					}
				}
				
			}
			break;
			
			case DEFAULT: {
				File home = new File(System.getProperty("user.home"),".DaFavorites");
				if (!home.exists()) {
					home.mkdirs();	
				}
				
				File config = new File(home,"default.fss");
				
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(config);
					properties.store(fos, config.getName());
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(this,"A write error occured: " + ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
					return;
				} finally {
					if (fos != null) {
						try {
							fos.close();
						} catch (IOException e1) {}
					}
				}
			}
			break;
			
			case OPEN: {
				JFileChooser jfs = new JFileChooser(new File(System.getProperty("user.home"),".DaFavorites"));
				jfs.setDialogTitle("Load a previons saved session");
				FileFilter ff = new FssFileFilter();
				jfs.setAcceptAllFileFilterUsed(true);
				jfs.setFileFilter(ff);
				
				jfs.setDialogType(JFileChooser.OPEN_DIALOG);
				
				int result = jfs.showOpenDialog(this);
				if(result != JFileChooser.APPROVE_OPTION) {
					return;
				}
				File selected = jfs.getSelectedFile();
				if (selected == null) {
					return;
				}
				try {
					loadConfig(selected);
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(this,"A read error occured: " + ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
				} finally {
					updateGUI();
				}
			}
			break;
			
			case EXIT: {
				this.dispose();
			}
			break;
			
			case HELP: {
				new HelpDialog(this);
			}
			break;
			
			case ABOUT : {
				new AboutDialog(this);
			}
			break;
			
			case WEBSITE: {
				if (!NavigatorLauncher.launch(WEBSITE_URL)) {
					JOptionPane.showMessageDialog(this,"Unable to start web browser.","Error",JOptionPane.ERROR_MESSAGE);	
				}	
			}
			break;
			
			case DEVIANTART: {
				if (!NavigatorLauncher.launch(DEVIANTART_URL)) {
					JOptionPane.showMessageDialog(this,"Unable to start web browser.","Error",JOptionPane.ERROR_MESSAGE);	
				}	
			}
			break;			
			
			default:
				break;
			}
		}
	}
	
	private int getSearchOffset() {
		Search search = getSearcher();
		if (search == null) {
			return -1;
		}
		return search.getOffset();
	}
	
	@SuppressWarnings("unchecked")
	private Search getSearcher() {
		String className = properties.getProperty(Constants.SEARCHER);
		final Class<Search> clazz;
		try {
			clazz = (Class<Search>) Search.class.getClassLoader().loadClass(className);
			return clazz.newInstance();
		} catch (Exception ex) {
			Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), ex);
			return null;
		}
	}
	
	private Properties getDefaults() {
		String defaultLocations = System.getProperty("user.home") + File.separator + "deviantART" + File.separator  + 
		"%user%" + File.separator  + "%artist%" + File.separator  + "%title%" + File.separator + "%filename%";
		
		Properties props = new Properties();
		props.setProperty(Constants.USERNAME, System.getProperty("user.name"));
		props.setProperty(Constants.LOCATION, defaultLocations);
		props.setProperty(Constants.MATURE, defaultLocations);
		props.setProperty(Constants.DOMAIN, "www.deviantart.com");
		props.setProperty(Constants.LNF, UIManager.getSystemLookAndFeelClassName());
		props.setProperty(Constants.THROTTLE, "5");
		props.setProperty(Constants.SEARCHER, com.dragoniade.deviantart.deviation.SearchRss.class.getName());
		props.setProperty(Constants.SEARCH, SEARCH.FAVORITE.toString());
		return props;
	}
	
	private void loadConfig(File config) throws IOException{
		FileInputStream fis = null;
		Properties props = new Properties();
		try {
			fis = new FileInputStream(config);
			props.load(fis);
			
			for (Entry<Object,Object> entry : props.entrySet() ) {
				properties.put(entry.getKey().toString(), entry.getValue().toString());
			}
			
		} catch (IOException ex) {
			throw ex;
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e1) {}
			}
		}
	}
	
	public void savePreferences(Properties p) {
		properties.putAll(p);
		updateGUI();
	}
	
    public class ChangeLookAndFeelAction extends AbstractAction {
		private static final long serialVersionUID = -5627510027903551734L;
		String laf;
        protected ChangeLookAndFeelAction(String laf) {
            super("ChangeTheme");
    	    this.laf = laf;
        }

        public void actionPerformed(ActionEvent e) {
        	if (!laf.equals(properties.getProperty(Constants.LNF))) {
        		properties.setProperty(Constants.LNF,laf);
            	MenuBar bar = (MenuBar) getJMenuBar();
            	bar.selectLnF(e.getSource());
            	try {
					UIManager.setLookAndFeel(laf);
					SwingUtilities.updateComponentTreeUI(instance);
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(instance, "Error loading the Look and Feel.","Error",JOptionPane.ERROR_MESSAGE);
				}
        	}
        }
   }
    /**
     * @author Dragoniade
     *
     */
    private class OffsetSpinnerModel extends SpinnerNumberModel {

    	/**
		 * 
		 */
		private static final long serialVersionUID = 2728826453091046235L;
		int cur;
    	int offset;
    	public OffsetSpinnerModel(int offset) {
        	cur = 0;
        	this.offset = offset;
        	setMaximum((Integer.MAX_VALUE /offset) * offset);
    	}

		public Object getNextValue() {
			if (Integer.MAX_VALUE - offset < cur) {
				cur = (Integer.MAX_VALUE / offset) * offset;
			} else {
				cur += offset;	
			}
			
			return cur;
		}

		public Object getPreviousValue() {
			cur -= offset;
			if (cur < 0) {
				cur = 0;
			}
			return cur;
		}

		public Object getValue() {
			return cur;
		}

		public void setValue(Object value) {
			Integer newCur = (Integer) value;
			cur = (newCur / offset) * offset;
			if (cur < 0) {
				cur = 0;
			}
			 fireStateChanged();
		}
    	public void setOffset(int offset) {
    		this.offset = offset;
    	}
    }
    
    private class FssFileFilter extends FileFilter {
		@Override
		public boolean accept(File f) {
			if (f.getName().toLowerCase().endsWith(".fss")) {
				return true;
			}
			return false;
		}

		@Override
		public String getDescription() {
			return "deviantART Saved Session (*.fss)";
		}
    }
}

