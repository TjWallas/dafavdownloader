/**
 *    PreferencesDialog - Provide a dialog to save a user preferences.
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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.dragoniade.clazz.SearcherClassCache;
import com.dragoniade.deviantart.deviation.Deviation;
import com.dragoniade.deviantart.deviation.Search;
import com.dragoniade.deviantart.deviation.Search.SEARCH;

public class PreferencesDialog extends JDialog {

	private static final long serialVersionUID = -6533829279008497773L;
/*
 *  %id% : Deviation Id
 *  %user% : User being searched
 *  %artist% : The Deviation's artist username.
 *  %title% : The Deviations's title, normalized.
 *  %filename% : The Deviation's original filename.
 *  %extE% : The Deviation's extension.
 */
	
	private JTabbedPane panes;
	private final JTextField userField;
	private final JTextField locationField;
	private final JTextField locationMatureField;
	private final JCheckBox useSameForMatureBox;
	private JTextField domainField;
	private Deviation sample;
	private JSpinner throttleSpinner;
	private JComboBox searcherBox;
	private JRadioButton galleryRadio;
	private JRadioButton favoriteRadio;
	private ButtonGroup buttonGroup;
	
	private final StringBuilder locationString;
	private final StringBuilder locationMatureString;
	private final static String DOWNLOAD_URL = "http://fc09.deviantart.com/fs6/i/2005/098/0/9/Fella_Promo_by_devart.jpg";
	
	public PreferencesDialog(final DownloaderGUI owner,Properties config)  {
		super(owner,"Preferences",true);
		
		sample = new Deviation();
		sample.setId(15972367L);
		sample.setTitle("Fella Promo");
		sample.setArtist("devart");
		sample.setImageDownloadUrl(DOWNLOAD_URL);
		sample.setImageFilename(Deviation.extractFilename(DOWNLOAD_URL));
		
		setLayout(new BorderLayout());
		panes = new JTabbedPane(JTabbedPane.TOP);
		
		
		JPanel genPanel = new JPanel();
		BoxLayout genLayout = new BoxLayout(genPanel,BoxLayout.Y_AXIS); 
		genPanel.setLayout(genLayout);
		panes.add("General", genPanel);
		
		JLabel userLabel = new JLabel("Username");
		
		userLabel.setToolTipText("The username the account you want to download the favorites from.");
		userField = new JTextField(config.getProperty(Constants.USERNAME));
		
		userLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		userField.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		genPanel.add(userLabel);
		genPanel.add(userField);
		
		
		JLabel searchLabel = new JLabel("Search for");
		searchLabel.setToolTipText("Select what you want to download from that user: it favorites or it galleries.");
		
		galleryRadio = new JRadioButton("Gallery");
		favoriteRadio = new JRadioButton("Favorites");
		
		SEARCH search = SEARCH.lookup(config.getProperty(Constants.SEARCH,SEARCH.FAVORITE.toString()));
		
		buttonGroup = new ButtonGroup();
		buttonGroup.add(favoriteRadio);
		buttonGroup.add(galleryRadio);

		switch (search) {
		case FAVORITE:
			favoriteRadio.setSelected(true);break;
		case GALLERY:
			galleryRadio.setSelected(true);break;
		default:
			favoriteRadio.setSelected(true);break;
		}
		JPanel radioPanel = new JPanel();
		BoxLayout radioLayout = new BoxLayout(radioPanel,BoxLayout.X_AXIS);
		radioPanel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		
		radioPanel.setLayout(radioLayout);

		galleryRadio.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		favoriteRadio.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		searchLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		
		radioPanel.add(favoriteRadio);
		radioPanel.add(galleryRadio);
		genPanel.add(radioPanel);
		
		final JTextField sampleField = new JTextField("");
		sampleField.setEditable(false);
		
		JLabel locationLabel = new JLabel("Download location");
		locationLabel.setToolTipText("The folder pattern where you want the file to be downloaded in.");
		
		JLabel legendsLabel = new JLabel("<html><body>Field names: %user%, %artist%, %title%, %id%, %filename%, %ext%<br></br>Example:</body></html>");
		legendsLabel.setToolTipText("An example of where a file will be downloaded to.");
		
		locationString = new StringBuilder();
		locationField = new JTextField(config.getProperty(Constants.LOCATION));
		locationField.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void keyReleased(KeyEvent e) {
				File dest = LocationHelper.getFile(locationField.getText(), userField.getText(),sample,sample.getImageFilename() );
				locationString.setLength(0);
				locationString.append(dest.getAbsolutePath());
				sampleField.setText(locationString.toString());
				if (useSameForMatureBox.isSelected()) {
					locationMatureString.setLength(0);
					locationMatureString.append(sampleField.getText());
					locationMatureField.setText(locationField.getText());
				}
			}

			public void keyTyped(KeyEvent e) {
			}
			
		});
		locationField.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {
			}
			
			public void mousePressed(MouseEvent e) {
			}
			
			public void mouseExited(MouseEvent e) {
				sampleField.setText(locationString.toString());
			}
			
			public void mouseEntered(MouseEvent e) {
				sampleField.setText(locationString.toString());
			}
			
			public void mouseClicked(MouseEvent e) {
			}
		});
		JLabel locationMatureLabel = new JLabel("Mature download location");
		locationMatureLabel.setToolTipText("The folder pattern where you want the file marked as 'Mature' to be downloaded in.");
		
		
		locationMatureString = new StringBuilder();
		locationMatureField = new JTextField(config.getProperty(Constants.MATURE));
		locationMatureField.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void keyReleased(KeyEvent e) {
				File dest = LocationHelper.getFile(locationMatureField.getText(), userField.getText(),sample,sample.getImageFilename() );
				locationMatureString.setLength(0);
				locationMatureString.append(dest.getAbsolutePath());
				sampleField.setText(locationMatureString.toString());
			}

			public void keyTyped(KeyEvent e) {
			}
			
		});
		
		locationMatureField.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {
			}
			
			public void mousePressed(MouseEvent e) {
			}
			
			public void mouseExited(MouseEvent e) {
				sampleField.setText(locationString.toString());
			}
			
			public void mouseEntered(MouseEvent e) {
				sampleField.setText(locationMatureString.toString());
			}
			
			public void mouseClicked(MouseEvent e) {
			}
		});
		
		useSameForMatureBox = new JCheckBox("Use same location for mature deviation?");
		useSameForMatureBox.setSelected(locationLabel.getText().equals(locationMatureField.getText()));
		useSameForMatureBox.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if (useSameForMatureBox.isSelected()) {
					locationMatureField.setEditable(false);
					locationMatureField.setText(locationField.getText());
					locationMatureString.setLength(0);
					locationMatureString.append(locationString);
				} else {
					locationMatureField.setEditable(true);
				}
				
			}
		});
		
		File dest = LocationHelper.getFile(locationField.getText(), userField.getText(),sample,sample.getImageFilename() );
		sampleField.setText(dest.getAbsolutePath());
		locationString.append(sampleField.getText());
		
		dest = LocationHelper.getFile(locationMatureField.getText(), userField.getText(),sample,sample.getImageFilename() );
		locationMatureString.append(dest.getAbsolutePath());
		
		
		locationLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		locationField.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		locationMatureLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		locationMatureField.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		useSameForMatureBox.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		legendsLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		sampleField.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		
		genPanel.add(locationLabel);
		genPanel.add(locationField);
		
		
		genPanel.add(locationMatureLabel);
		genPanel.add(locationMatureField);
		genPanel.add(useSameForMatureBox);
		
		genPanel.add(legendsLabel);
		genPanel.add(sampleField);
		
		
		
		JPanel advPanel = new JPanel();
		BoxLayout advLayout = new BoxLayout(advPanel,BoxLayout.Y_AXIS); 
		advPanel.setLayout(advLayout);
		panes.add("Advanced", advPanel);
		
		JLabel domainLabel = new JLabel("Deviant Art domain name");
		domainLabel.setToolTipText("The deviantART main domain, should it ever change.");
		
		domainField = new JTextField(config.getProperty(Constants.DOMAIN));
		domainLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		domainField.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		advPanel.add(domainLabel);
		advPanel.add(domainField);
		
		JLabel throttleLabel = new JLabel("Throttle search delay");
		throttleLabel.setToolTipText("Slow down search query by inserting a pause between them. This help prevent abuse when doing a massive download.");
		
		throttleSpinner = new JSpinner();
		throttleSpinner.setModel(new SpinnerNumberModel(Integer.parseInt(config.getProperty(Constants.THROTTLE,"0")),5,60,1));
		
		throttleLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		throttleSpinner.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		
		advPanel.add(throttleLabel);
		advPanel.add(throttleSpinner);
		
		JLabel searcherLabel = new JLabel("Searcher");
		searcherLabel.setToolTipText("Select a searcher that will look for your favorites.");

		searcherBox = new JComboBox();
		searcherBox.setRenderer(new TogglingRenderer());
		
		final AtomicInteger index  = new AtomicInteger(0);
		searcherBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox combo = (JComboBox) e.getSource(); 
				Object selectedItem = combo.getSelectedItem();
				if (selectedItem instanceof SearchItem) {
					SearchItem item = (SearchItem) selectedItem;
					if (item.isValid) {
						index.set(combo.getSelectedIndex());
					} else {
						combo.setSelectedIndex(index.get());
					}
				} 
			}
		});
		
		
		String selectedClazz = config.getProperty(Constants.SEARCHER,com.dragoniade.deviantart.deviation.SearchRss.class.getName());
		boolean selected = false;
		try {
			int weight = -1;
			for (Class<Search> clazz : SearcherClassCache.getInstance().getClasses()) {
				
				Search searcher = clazz.newInstance();
				String name = searcher.getName();
				String clazzName = clazz.getName();
				boolean isValid = searcher.validate();
				
				SearchItem item = new SearchItem(name, clazzName, isValid);
				searcherBox.addItem(item);
				if (clazzName.equals(selectedClazz)) {
					searcherBox.setSelectedItem(item);
					selected = true;
				}
				if (!selected && isValid && searcher.priority() > weight) {
					searcherBox.setSelectedItem(item);
					weight = searcher.priority();
				}
			}
		} catch (Exception e1) {
			throw new RuntimeException(e1);
		}
		
		searcherLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		searcherBox.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		
		advPanel.add(searcherLabel);
		advPanel.add(searcherBox);
		
		advPanel.add(new JLabel("<html><body>&nbsp; </body></html>"));
		advPanel.add(new JLabel("<html><body>&nbsp; </body></html>"));
		advPanel.add(new JLabel("<html><body>&nbsp; </body></html>"));
		advPanel.add(new JLabel("<html><body>&nbsp; </body></html>"));
		advPanel.add(new JLabel("<html><body>&nbsp; </body></html>"));
		advPanel.add(new JLabel("<html><body>&nbsp; </body></html>"));
		advPanel.add(new JLabel("<html><body>&nbsp; </body></html>"));
		
		add(panes, BorderLayout.CENTER);
		
		JButton saveBut = new JButton("Save");
		
		userField.setInputVerifier(new InputVerifier() {
			@Override
			public boolean verify(JComponent input) {
				JTextField field = (JTextField) input;
				if (field.getText().trim().length() == 0) {
					JOptionPane.showMessageDialog(input, "The user musn't be empty.","Warning",JOptionPane.WARNING_MESSAGE);
					return false;
				}
				return true;
			}
		});
		
		locationField.setInputVerifier(new InputVerifier() {
			@Override
			public boolean verify(JComponent input) {
				JTextField field = (JTextField) input;
				String content = field.getText().trim();
				if (content.length() == 0) {
					JOptionPane.showMessageDialog(input, "The location musn't be empty.","Warning",JOptionPane.WARNING_MESSAGE);
					return false;
				}
				
				if (!content.contains("%filename%") && !content.contains("%id%")) {
					JOptionPane.showMessageDialog(input, "The location must contains at least a %filename% or an %id% field.","Warning",JOptionPane.WARNING_MESSAGE);
					return false;
				}
				return true;
			}
		});
		
		locationMatureField.setInputVerifier(new InputVerifier() {
			@Override
			public boolean verify(JComponent input) {
				JTextField field = (JTextField) input;
				String content = field.getText().trim();
				if (content.length() == 0) {
					JOptionPane.showMessageDialog(input, "The Mature location musn't be empty.","Warning",JOptionPane.WARNING_MESSAGE);
					return false;
				}
				
				if (!content.contains("%filename%") && !content.contains("%id%")) {
					JOptionPane.showMessageDialog(input, "The Mature location must contains at least a %username% or an %id% field.","Warning",JOptionPane.WARNING_MESSAGE);
					return false;
				}
				return true;
			}
		});
		
		domainField.setInputVerifier(new InputVerifier() {
			@Override
			public boolean verify(JComponent input) {
				JTextField field = (JTextField) input;
				String domain = field.getText().trim();
				if (domain.length() == 0) {
					JOptionPane.showMessageDialog(input, "You must specify the deviantART main domain.","Warning",JOptionPane.WARNING_MESSAGE);
					return false;
				}
				
				if (domain.toLowerCase().startsWith("http://")) {
					JOptionPane.showMessageDialog(input, "You must specify the deviantART main domain, not the full URL (aka www.deviantart.com).","Warning",JOptionPane.WARNING_MESSAGE);
					return false;
				}
				
				return true;
			}
		});
		locationField.setVerifyInputWhenFocusTarget(true);
		
		final JDialog parent = this;
		saveBut.addActionListener(new ActionListener() {

			String errorMsg = "The location is invalid or cannot be written to.";
			public void actionPerformed(ActionEvent e) {
				
				String username = userField.getText().trim();
				String location = locationField.getText().trim();
				String locationMature = locationMatureField.getText().trim();
				String domain = domainField.getText().trim();
				String throttle = throttleSpinner.getValue().toString();
				String searcher = searcherBox.getSelectedItem().toString();
				
				String search = SEARCH.FAVORITE.toString();
				if (favoriteRadio.isSelected()) {
					search = SEARCH.FAVORITE.toString();
				}
				
				if (galleryRadio.isSelected()) {
					search = SEARCH.GALLERY.toString();
				}
				
				if (!testPath(location,username)) {
					JOptionPane.showMessageDialog(parent,errorMsg ,"Error",JOptionPane.ERROR_MESSAGE);	
				}
				if (!testPath(locationMature,username)) {
					JOptionPane.showMessageDialog(parent,errorMsg ,"Error",JOptionPane.ERROR_MESSAGE);	
				}
				
				Properties p = new Properties();
				p.setProperty(Constants.USERNAME, username);
				p.setProperty(Constants.LOCATION,location);
				p.setProperty(Constants.MATURE,locationMature);
				p.setProperty(Constants.DOMAIN, domain);
				p.setProperty(Constants.THROTTLE, throttle);
				p.setProperty(Constants.SEARCHER, searcher);
				p.setProperty(Constants.SEARCH, search);
				
				owner.savePreferences(p);
				parent.dispose();
			}
		});
		
		JButton cancelBut = new JButton("Cancel");
		cancelBut.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				parent.dispose();
			}
		});
		
		JPanel buttonPanel = new JPanel();
		BoxLayout butLayout = new BoxLayout(buttonPanel,BoxLayout.X_AXIS); 
		buttonPanel.setLayout(butLayout);
		
		buttonPanel.add(saveBut);
		buttonPanel.add(cancelBut);
		add(buttonPanel,BorderLayout.SOUTH);
		
		pack();
		setResizable(false);
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((d.width-getWidth())/2, (d.height-getHeight())/2);
		setVisible(true);
	}
	
	private boolean testPath(String location, String username) {
		File dest = LocationHelper.getFile(location, username,sample,sample.getImageFilename() );
		
		Stack<File> stack = new Stack<File>();
		Stack<File> toDelete = new Stack<File>();
		
		stack.push(dest);
		while ((dest = dest.getParentFile()) != null) {
			stack.push(dest);	
		}
		
		try {
			while(!stack.isEmpty()) {
				File file = stack.pop();
				if (file.exists() ) {
					continue;
				} else {
					if (stack.isEmpty()) {
						if (!file.createNewFile()) {
							return false;
						}	
					} else {
						if (!file.mkdir()) {
							return false;
						}
					}
					
					toDelete.add(file);
				}
			}
		} catch (IOException ex) {
			return false;
		} finally {
			while(!toDelete.isEmpty()) {
				toDelete.pop().delete();
			}
		}
		return true;
	}
	
	
	private class TogglingRenderer extends JLabel implements ListCellRenderer {
		private static final long serialVersionUID = -8426795650241831785L;
		public TogglingRenderer() {
	    	super();
	    	setOpaque(true);
	        setBorder(new EmptyBorder(1, 1, 1, 1));

	    }

	    public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
	      if (isSelected) {
	        setBackground(list.getSelectionBackground());
	        setForeground(list.getSelectionForeground());
	      } else {
	        setBackground(list.getBackground());
	        setForeground(list.getForeground());
	      } 
	      if  (value instanceof SearchItem) {
	    	  SearchItem item = (SearchItem) value;
		      if (!item.isValid) {
			        setBackground(list.getBackground());
			        setForeground(UIManager.getColor("Label.disabledForeground"));
		      }
		      setText(item.name);
	      } else {
	    	  setText((value == null) ? "" : value.toString());
	      }
	      setFont(list.getFont());
	      return this;
	    }  
	}
	
	private class SearchItem {
		String name;
		String clazz;
		boolean isValid;
		
		public SearchItem(String name, String clazz, boolean isValid) {
			this.name = name;
			this.clazz = clazz;
			this.isValid = isValid;
		}
		
		public String toString() {
			return clazz;
		}
	}
}
