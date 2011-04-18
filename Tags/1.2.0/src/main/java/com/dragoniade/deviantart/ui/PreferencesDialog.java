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

import javax.swing.Box;
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
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.ProxyHost;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.params.HttpClientParams;

import com.dragoniade.clazz.SearcherClassCache;
import com.dragoniade.deviantart.deviation.Collection;
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
 *  %collection% : The collection or folder this deviation is part of.
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
	private ButtonGroup buttonGroup;
	private SEARCH selectedSearch;
	
	private JCheckBox prxUseBox;
	private JTextField prxHostField;
	private JSpinner prxPortSpinner;
	private JTextField prxUserField;
	private JPasswordField prxPassField;
	
	private final StringBuilder locationString;
	private final StringBuilder locationMatureString;
	private final static String DOWNLOAD_URL = "http://fc09.deviantart.com/fs6/i/2005/098/0/9/Fella_Promo_by_devart.jpg";
	private HttpClient client;
	private boolean proxyChangeState = false;
	
	public PreferencesDialog(final DownloaderGUI owner,Properties config)  {
		super(owner,"Preferences",true);
		
		HttpClientParams params = new HttpClientParams();
		params.setVersion(HttpVersion.HTTP_1_1);
		params.setSoTimeout(30000);
		client = new HttpClient(params);
		setProxy(ProxyCfg.parseConfig(config));
		
		sample = new Deviation();
		sample.setId(15972367L);
		sample.setTitle("Fella Promo");
		sample.setArtist("devart");
		sample.setImageDownloadUrl(DOWNLOAD_URL);
		sample.setImageFilename(Deviation.extractFilename(DOWNLOAD_URL));
		sample.setCollection(new Collection(1L, "MyCollect"));
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
		userLabel.setBorder(new EmptyBorder(0, 5, 0, 5));
		userField.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		userField.setMaximumSize(new Dimension(Integer.MAX_VALUE, userField.getFont().getSize() * 2));
				
		genPanel.add(userLabel);
		genPanel.add(userField);
		
		
		JPanel radioPanel = new JPanel();
		BoxLayout radioLayout = new BoxLayout(radioPanel,BoxLayout.X_AXIS);
		radioPanel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		radioPanel.setBorder(new EmptyBorder(0, 5, 0, 5));
		
		radioPanel.setLayout(radioLayout);
		
		JLabel searchLabel = new JLabel("Search for");
		searchLabel.setToolTipText("Select what you want to download from that user: it favorites or it galleries.");
		searchLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		searchLabel.setBorder(new EmptyBorder(0, 5, 0, 5));
		
		selectedSearch = SEARCH.lookup(config.getProperty(Constants.SEARCH,SEARCH.getDefault().getId()));
		buttonGroup = new ButtonGroup();
		
		for (final SEARCH search :SEARCH.values()) {
			JRadioButton radio = new JRadioButton(search.getLabel());
			radio.setAlignmentX(JLabel.LEFT_ALIGNMENT);
			radio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedSearch = search;
			}});
			
			buttonGroup.add(radio);
			radioPanel.add(radio);
			if (search.equals(selectedSearch)) {
				radio.setSelected(true);
			}
		}
		
		genPanel.add(radioPanel);
		
		final JTextField sampleField = new JTextField("");
		sampleField.setEditable(false);
		
		JLabel locationLabel = new JLabel("Download location");
		locationLabel.setToolTipText("The folder pattern where you want the file to be downloaded in.");
		
		JLabel legendsLabel = new JLabel("<html><body>Field names: %user%, %artist%, %title%, %id%, %filename%, %collection%, %ext%<br></br>Example:</body></html>");
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
		locationLabel.setBorder(new EmptyBorder(0, 5, 0, 5));
		locationField.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		locationField.setMaximumSize(new Dimension(Integer.MAX_VALUE, locationField.getFont().getSize() * 2));
		locationMatureLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		locationMatureLabel.setBorder(new EmptyBorder(0, 5, 0, 5));
		locationMatureField.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		locationMatureField.setMaximumSize(new Dimension(Integer.MAX_VALUE, locationMatureField.getFont().getSize() * 2));
		useSameForMatureBox.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		legendsLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		legendsLabel.setBorder(new EmptyBorder(0, 5, 0, 5));
		legendsLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, legendsLabel.getFont().getSize() * 2));
		sampleField.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		sampleField.setMaximumSize(new Dimension(Integer.MAX_VALUE, sampleField.getFont().getSize() * 2));
		
		genPanel.add(locationLabel);
		genPanel.add(locationField);
		
		
		genPanel.add(locationMatureLabel);
		genPanel.add(locationMatureField);
		genPanel.add(useSameForMatureBox);
		
		genPanel.add(legendsLabel);
		genPanel.add(sampleField);
		genPanel.add(Box.createVerticalBox());
		
		final KeyListener prxChangeListener = new KeyListener() {
			
			public void keyTyped(KeyEvent e) {
				proxyChangeState = true;				
			}
			public void keyPressed(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {}
		};
		
		JPanel prxPanel = new JPanel();
		BoxLayout prxLayout = new BoxLayout(prxPanel,BoxLayout.Y_AXIS); 
		prxPanel.setLayout(prxLayout);
		panes.add("Proxy", prxPanel);
		
		JLabel prxHostLabel = new JLabel("Proxy Host");
		prxHostLabel.setToolTipText("The hostname of the proxy server");
		prxHostField = new JTextField(config.getProperty(Constants.PROXY_HOST));
		prxHostLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		prxHostLabel.setBorder(new EmptyBorder(0, 5, 0, 5));
		prxHostField.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		prxHostField.setMaximumSize(new Dimension(Integer.MAX_VALUE, prxHostField.getFont().getSize() * 2));
		
		JLabel prxPortLabel = new JLabel("Proxy Port");
		prxPortLabel.setToolTipText("The port of the proxy server (Default 80).");

		prxPortSpinner = new JSpinner();
		prxPortSpinner.setModel(new SpinnerNumberModel(Integer.parseInt(config.getProperty(Constants.PROXY_PORT,"80")),1,65535,1));

		prxPortLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		prxPortLabel.setBorder(new EmptyBorder(0, 5, 0, 5));
		prxPortSpinner.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		prxPortSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, prxPortSpinner.getFont().getSize() * 2));
		
		JLabel prxUserLabel = new JLabel("Proxy username");
		prxUserLabel.setToolTipText("The username used for authentication, if applicable.");
		prxUserField = new JTextField(config.getProperty(Constants.PROXY_USERNAME));
		prxUserLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		prxUserLabel.setBorder(new EmptyBorder(0, 5, 0, 5));
		prxUserField.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		prxUserField.setMaximumSize(new Dimension(Integer.MAX_VALUE, prxUserField.getFont().getSize() * 2));
		
		JLabel prxPassLabel = new JLabel("Proxy username");
		prxPassLabel.setToolTipText("The username used for authentication, if applicable.");
		prxPassField = new JPasswordField(config.getProperty(Constants.PROXY_PASSWORD));
		prxPassLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		prxPassLabel.setBorder(new EmptyBorder(0, 5, 0, 5));
		prxPassField.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		prxPassField.setMaximumSize(new Dimension(Integer.MAX_VALUE, prxPassField.getFont().getSize() * 2));
		
		prxUseBox = new JCheckBox("Use a proxy?");
		prxUseBox.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				prxChangeListener.keyTyped(null);
				
				if (prxUseBox.isSelected()) {
					prxHostField.setEditable(true);
					prxPortSpinner.setEnabled(true);
					prxUserField.setEditable(true);
					prxPassField.setEditable(true);

				} else {
					prxHostField.setEditable(false);
					prxPortSpinner.setEnabled(false);
					prxUserField.setEditable(false);
					prxPassField.setEditable(false);
				}
			}
		});
		
		prxUseBox.setSelected(!Boolean.parseBoolean(config.getProperty(Constants.PROXY_USE)));
		prxUseBox.doClick();
		proxyChangeState = false;
		
		prxHostField.addKeyListener(prxChangeListener);
		prxUserField.addKeyListener(prxChangeListener);
		prxPassField.addKeyListener(prxChangeListener);
		prxPortSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				proxyChangeState = true;
			}
		});
		prxPanel.add(prxUseBox);
		
		prxPanel.add(prxHostLabel);
		prxPanel.add(prxHostField);
		
		prxPanel.add(prxPortLabel);
		prxPanel.add(prxPortSpinner);
		
		prxPanel.add(prxUserLabel);
		prxPanel.add(prxUserField);
		
		prxPanel.add(prxPassLabel);
		prxPanel.add(prxPassField);
		prxPanel.add(Box.createVerticalBox());
		
		final JPanel advPanel = new JPanel();
		BoxLayout advLayout = new BoxLayout(advPanel,BoxLayout.Y_AXIS); 
		advPanel.setLayout(advLayout);
		panes.add("Advanced", advPanel);
		panes.addChangeListener(new ChangeListener() {
			
			public void stateChanged(ChangeEvent e) {
		        JTabbedPane pane = (JTabbedPane)e.getSource();

		        if (proxyChangeState && pane.getSelectedComponent() == advPanel) {
		        	Properties properties = new Properties();
		        	properties.setProperty(Constants.PROXY_USERNAME, prxUserField.getText().trim());
		        	properties.setProperty(Constants.PROXY_PASSWORD, new String(prxPassField.getPassword()).trim());
		        	properties.setProperty(Constants.PROXY_HOST, prxHostField.getText().trim());
		        	properties.setProperty(Constants.PROXY_PORT, prxPortSpinner.getValue().toString());
		        	properties.setProperty(Constants.PROXY_USE, Boolean.toString(prxUseBox.isSelected()));
		        	ProxyCfg prx = ProxyCfg.parseConfig(properties);
		        	setProxy(prx);
		        	revalidateSearcher(null);
		        }
			}
		});
		JLabel domainLabel = new JLabel("Deviant Art domain name");
		domainLabel.setToolTipText("The deviantART main domain, should it ever change.");
		
		domainField = new JTextField(config.getProperty(Constants.DOMAIN));
		domainLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		domainLabel.setBorder(new EmptyBorder(0, 5, 0, 5));
		domainField.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		domainField.setMaximumSize(new Dimension(Integer.MAX_VALUE, domainField.getFont().getSize() * 2));
		
		advPanel.add(domainLabel);
		advPanel.add(domainField);
		
		JLabel throttleLabel = new JLabel("Throttle search delay");
		throttleLabel.setToolTipText("Slow down search query by inserting a pause between them. This help prevent abuse when doing a massive download.");
		
		throttleSpinner = new JSpinner();
		throttleSpinner.setModel(new SpinnerNumberModel(Integer.parseInt(config.getProperty(Constants.THROTTLE,"0")),5,60,1));
		
		throttleLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		throttleLabel.setBorder(new EmptyBorder(0, 5, 0, 5));
		throttleSpinner.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		throttleSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, throttleSpinner.getFont().getSize() * 2));
		
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
		
		
		try {
			for (Class<Search> clazz : SearcherClassCache.getInstance().getClasses()) {
				
				Search searcher = clazz.newInstance();
				String name = searcher.getName();
				
				SearchItem item = new SearchItem(name, clazz.getName(), true);
				searcherBox.addItem(item);
			}
			String selectedClazz = config.getProperty(Constants.SEARCHER,com.dragoniade.deviantart.deviation.SearchRss.class.getName());
			revalidateSearcher(selectedClazz);
		} catch (Exception e1) {
			throw new RuntimeException(e1);
		}
		
		searcherLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		searcherLabel.setBorder(new EmptyBorder(0, 5, 0, 5));
		searcherBox.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		searcherBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, searcherBox.getFont().getSize() * 2));
		
		advPanel.add(searcherLabel);
		advPanel.add(searcherBox);
		
		advPanel.add(Box.createVerticalBox());
		
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

				String prxUse = Boolean.toString(prxUseBox.isSelected());
				String prxHost = prxHostField.getText().trim();
				String prxPort = prxPortSpinner.getValue().toString();
				String prxUsername = prxUserField.getText().trim();
				String prxPassword= new String(prxPassField.getPassword()).trim();
					
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
				p.setProperty(Constants.SEARCH, selectedSearch.getId());
				
				p.setProperty(Constants.PROXY_USE, prxUse);
				p.setProperty(Constants.PROXY_HOST, prxHost);
				p.setProperty(Constants.PROXY_PORT, prxPort);
				p.setProperty(Constants.PROXY_USERNAME, prxUsername);
				p.setProperty(Constants.PROXY_PASSWORD, prxPassword);

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
	
	@SuppressWarnings("unchecked")
	private void revalidateSearcher(String selectedClazz) {
		proxyChangeState = false;
		boolean selected = false;
		int weight = -1;
		for (int i = 0; i < searcherBox.getItemCount(); i++ ) {
			SearchItem item = (SearchItem) searcherBox.getItemAt(i);
			try {
				Class<Search> clazz = (Class<Search>) Search.class.getClassLoader().loadClass(item.clazz);
				Search searcher = clazz.newInstance();
				searcher.setClient(client);
				boolean isValid = searcher.validate();
				item.isValid = isValid;
				
				if (item.clazz.equals(selectedClazz)) {
					searcherBox.setSelectedItem(item);
					selected = true;
				}
				if (!selected && isValid && searcher.priority() > weight) {
					searcherBox.setSelectedItem(item);
					weight = searcher.priority();
				}
			} catch (Exception e ) {
				throw new RuntimeException(e);
			}
		}
	
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
	
	private void setProxy(ProxyCfg prx) {
		HostConfiguration hostConfiguration = client.getHostConfiguration();
		if ( prx != null) {
			ProxyHost proxyHost = new ProxyHost(prx.getHost(), prx.getPort());
			hostConfiguration.setProxyHost(proxyHost);	
			if (prx.getUsername() != null) {
				UsernamePasswordCredentials upCred = new UsernamePasswordCredentials(prx.getUsername(), prx.getPassword());
				client.getState().setProxyCredentials(AuthScope.ANY, upCred);
			} else {
				client.getState().clearProxyCredentials();
			}
		} else {
			hostConfiguration.setProxyHost(null);	
			client.getState().clearProxyCredentials();
		}
	}
}
