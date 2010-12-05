/**
 *    FavoritesDownloader - Download favorites for a specified user
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
package com.dragoniade.deviantart.favorites;

import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;

import com.dragoniade.deviantart.deviation.Deviation;
import com.dragoniade.deviantart.deviation.Search;
import com.dragoniade.deviantart.ui.LocationHelper;
import com.dragoniade.deviantart.ui.ProgressDialog;
import com.dragoniade.deviantart.ui.SwingThread;
import com.dragoniade.deviantart.ui.YesNoAllDialog;

public class FavoritesDownloader {

	private HttpClient client;
	private String userId;
	private String destination;
	private String destinationMature;
	private JTextPane textPane;
	private ProgressDialog progress;
	private JFrame owner;
	private int sleepThrottle = 0;
	
	public FavoritesDownloader(String userId, String destination, String destinationMature) {
		this.destination = destination;
		this.destinationMature = destinationMature;
		this.userId = userId;
		
		HttpClientParams params = new HttpClientParams();
		params.setVersion(HttpVersion.HTTP_1_1);
		params.setSoTimeout(30000);
		client = new HttpClient(params);
	}
	
	public void setGUI(JFrame owner,JTextPane textPane, ProgressDialog progress ) {
		this.owner = owner;
		this.textPane = textPane;
		this.progress = progress;
	}

	private File getFile(Deviation da, boolean primary, AtomicBoolean download, 
			YesNoAllDialog matureMoveDialog, YesNoAllDialog overwriteDialog, YesNoAllDialog overwriteNewerDialog, YesNoAllDialog deleteEmptyDialog) {
		
		String filename = primary? da.getPrimaryFilename(): da.getSecondaryFilename();
		progress.setText("Downloading file '" + filename + "' from " + da.getArtist());
		
		String title = filename + " by " + da.getArtist();
		long timestamp = da.getTimestamp().getTime();
		File artPG = LocationHelper.getFile(destination, userId, da, primary);
		File artMature= LocationHelper.getFile(destinationMature, userId, da, primary);
		File art = null;
		
		if (da.isMature()) {
			if (artPG.exists()) {
				int resMove = matureMoveDialog.displayDialog(owner, title, "This deviation labeled as mature already exists in the main download path.\n Do you want to move the current file to the mature path?");
				if (resMove == YesNoAllDialog.CANCEL) {
					return null;
				}
				if (resMove == YesNoAllDialog.YES) {
					File parent = artMature.getParentFile(); 
					if (!parent.mkdirs()) {
						showMessageDialog(owner,"Unable to create '" + parent.getPath() + "'.","Error",JOptionPane.ERROR_MESSAGE);
						return null;
					}
					
					if (artMature.exists()) {
						int resOv = overwriteDialog.displayDialog(owner,"File already exists","The file '" + artMature.getPath() + "' already exists. Overwrite?");
						if (resOv == YesNoAllDialog.YES) {
							if (!artMature.delete()) {
								showMessageDialog(owner,"Unable to delete '" + artMature.getPath() + "'.","Error",JOptionPane.ERROR_MESSAGE);
								return null;
							}
						} else {
							return null;
						}
					}
					if (!artPG.renameTo(artMature)) {
						showMessageDialog(owner,"Unable to move '" + artPG.getPath() + "' to '" + artMature.getPath() + "'.","Error",JOptionPane.ERROR_MESSAGE);
						return null;
					}
					
					int resEmpty = deleteEmptyDialog.displayDialog(owner,"Delete","Delete empty folders?");
					if (resEmpty == YesNoAllDialog.YES) {
						deleteEmptyFolders(artPG);
					}
					
					if (resEmpty == YesNoAllDialog.CANCEL) {
						return null;
					}
					
					download.set(false);
				} 
				
				if (resMove == YesNoAllDialog.NO) {
					download.set(false);
				}
			}
			art =  artMature;
		} else {
			art = artPG;
		}
		
		if (art.exists() ) {
			if (timestamp > art.lastModified()) {
				int resOver = overwriteNewerDialog.displayDialog(owner, title, "This deviation already exist but a newer version is available. Replace?");
				if (resOver == YesNoAllDialog.CANCEL) {
					return null;
				}
				
				if (resOver == YesNoAllDialog.NO) {
					download.set(false);
				} else {
					download.set(false);
				}
			} else {
				download.set(false);
			}
		}
		
		return art;
		
	}
	public void execute(int offset) {
		int skipped = 0;
		Search searcher = new Search("favby:" + userId);
		searcher.setOffset(offset);
		progress.setTotalMax(Integer.MAX_VALUE);
		progress.setTotalValue(offset);
		
		int total = -1;
		
		YesNoAllDialog matureMoveDialog = new YesNoAllDialog();
		YesNoAllDialog overwriteDialog = new YesNoAllDialog();
		YesNoAllDialog overwriteNewerDialog = new YesNoAllDialog();
		YesNoAllDialog deleteEmptyDialog = new YesNoAllDialog();
		AtomicBoolean download = new AtomicBoolean(true);
		long lastSearch = -1L; 
		while (true) {
			if (progress.isCancelled()) {
				return;
			}
			progress.setText("Fetching results...");
			List<Deviation> results = searcher.search(owner);
			lastSearch = System.currentTimeMillis();
			
			if (results == null) {
				return;
			}
			total = searcher.getTotal();
			progress.setTotalMax(total);
			
			if (results.size() > 0) {
				results:
				for (Deviation da : results) {
					if (progress.isCancelled()) {
						return;
					}
					boolean downloaded = false;
					progress.setUnitMax(1);
					alternateDownload:
					for (int i=0 ; i < 2; i++) {
							
						download.set(true);
						boolean isPrimary = i == 0;
						File art = getFile(da, isPrimary, download,  matureMoveDialog,  overwriteDialog,  overwriteNewerDialog,  deleteEmptyDialog);
						if (art == null) {
							return;
						}
						
						if (download.get() ) {
							File parent = art.getParentFile();
							if (!parent.exists()) {
								if (!parent.mkdirs()) {
									showMessageDialog(owner,"Unable to create '" + parent.getPath() + "'.","Error",JOptionPane.ERROR_MESSAGE);
									return;
								}
							}
							String downloadUrl = isPrimary?da.getPrimaryDownloadUrl(): da.getSecondaryDownloadUrl();
							if (downloadUrl == null) {
								continue;
							}
							
							GetMethod method = new GetMethod(downloadUrl);
							try {
								int sc = -1;
								do {
									sc = client.executeMethod(method);
									if (sc != 200) {
										if (sc == 404 || sc == 403) {
											method.releaseConnection();
											if (i == 0) {
												continue alternateDownload;
											}
											String text = "<br/><a style=\"color:red;\" href=\"" + da.getUrl()+ "\">" + downloadUrl + " was not found" +  "</a>";
											setPaneText(text);
											progress.incremTotal();
											continue results;
										} else {
											File error = new File("./error/" + System.currentTimeMillis() + ".log");
											error.getParentFile().mkdirs();
											FileWriter fw = new FileWriter(error);
											fw.write (method.getResponseBodyAsString());
											fw.close();
											
											int res = showConfirmDialog(owner, "An error has occured when contacting deviantART : error " + sc + ". Try again?","Continue?",JOptionPane.YES_NO_CANCEL_OPTION );
											if (res == JOptionPane.NO_OPTION) {
												String text = "<br/><a style=\"color:red;\" href=\"" + da.getUrl()+ "\">" + downloadUrl + " has an error" +  "</a>";
												setPaneText(text);
												method.releaseConnection();
												progress.incremTotal();
												continue results;
											}
											if (res == JOptionPane.CANCEL_OPTION) {
												return;
											}
											try {
												Thread.sleep(500);
											} catch (InterruptedException e) {}
										}
									}
								} while (sc != 200); 
								
								int length =(int) method.getResponseContentLength();
								int copied = 0;
								progress.setUnitMax(length);
								InputStream is = method.getResponseBodyAsStream();
								File tmpFile = new File(art.getParentFile(),art.getName() + ".tmp");
								FileOutputStream fos = new FileOutputStream(tmpFile,false);
								byte[] buffer = new byte[16184];
								int read = -1;
								
								while ((read = is.read(buffer)) > 0) {
									fos.write(buffer,0,read);
									copied+=read;
									progress.setUnitValue(copied);
									
									if (progress.isCancelled()) {
										fos.close();
										method.releaseConnection();
										tmpFile.delete();
										return;
									}
								}
								fos.close();
								method.releaseConnection();
								
								if (art.exists()) {
									if (!art.delete()) {
										showMessageDialog(owner,"Unable to delete '" + art.getPath() + "'.","Error",JOptionPane.ERROR_MESSAGE);
										return;
									}
								}
								if (!tmpFile.renameTo(art)) {
									showMessageDialog(owner,"Unable to rename '" + tmpFile.getPath() + "' to '" + art.getPath() +"'.","Error",JOptionPane.ERROR_MESSAGE);
									return;
								}
								art.setLastModified(da.getTimestamp().getTime());
								downloaded = true;
								if (da.getPrimaryFilename().equals(da.getSecondaryFilename())) {
									break;
								}
							} catch (HttpException e) {
								showMessageDialog(owner,"Error contacting deviantART: " + e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
								return;
							} catch (IOException e) {
								showMessageDialog(owner,"Error contacting deviantART: " + e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
								return;
							}
						} else {
							progress.setText("Skipping file '" + da.getPrimaryFilename() + "' from " + da.getArtist());
						}
					}
					if (!downloaded) {
						skipped++;
					}
					progress.incremTotal();
					progress.setUnitValue(1);
					progress.setUnitMax(Integer.MAX_VALUE);
					
					if (skipped == 480) {
					
						int res = showConfirmDialog(owner, "480 deviations have been skipped so far. Continue scanning?","Continue?",JOptionPane.YES_NO_OPTION );
						if (res == JOptionPane.NO_OPTION) {
							return;
						}
					}
				}
			long searchDelay =  System.currentTimeMillis() - lastSearch;
			if (sleepThrottle > 0 && searchDelay  < 10000) {
				for (int i=sleepThrottle ; i > 0; i--) {
					progress.setText("Throttling " + i);
					try {
						if (progress.isCancelled()) {
							return;
						}
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						return;
					}
				}
			}

			} else {
				return;
			}
		}
		
	}
	
	public void setThrottle(int throttle) {
		sleepThrottle = throttle;
	}
	
	private void showMessageDialog(final Component parentComponent,
			final Object message, final String title, final int messageType) {
		Runnable command = new Runnable() {
			public void run() {
				JOptionPane.showMessageDialog(parentComponent,message,title,messageType);
			}
		};
		SwingThread.runSync(command);
	}
	
	private void setPaneText(final String text) {
		
		Runnable command = new Runnable() {
			
			public void run() {
				String content = textPane.getText();
				String aTag =  text + "</body>";
				content = content.replace("</body>", aTag);
				textPane.setText(content);
			}
		};
		SwingThread.runASync(command);
	}
	private int showConfirmDialog(Component parentComponent,
	        final Object message, final String title, final int optionType) {
		
		final 
		SwingThread<Integer> command = new SwingThread<Integer>() {
			int result = -1;
			public void run() {
				result = JOptionPane.showConfirmDialog(owner, message,title,optionType );
			}
			public Integer getResult() {
				return result;
			}
		};
		SwingThread.runSync(command);
		
		return command.getResult();
		
	}
	        
	private void deleteEmptyFolders(File file) {

		File current = file;
		do {
			if (current.exists())  {
				if (current.isFile()) {
					return;
				}
				if (current.listFiles().length == 0 ) {
					current.delete();
				} else {
					return;
				}
			} else {
				current = current.getParentFile();
			}
			
		} while (current != null);
	}
}
