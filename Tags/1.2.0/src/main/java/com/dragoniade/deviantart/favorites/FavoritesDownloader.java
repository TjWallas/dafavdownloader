/**
 *    FavoritesDownloader - Download favorites for a specified user
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
package com.dragoniade.deviantart.favorites;

import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.ProxyHost;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;

import com.dragoniade.deviantart.deviation.Collection;
import com.dragoniade.deviantart.deviation.Deviation;
import com.dragoniade.deviantart.deviation.Search;
import com.dragoniade.deviantart.ui.LocationHelper;
import com.dragoniade.deviantart.ui.ProgressDialog;
import com.dragoniade.deviantart.ui.ProxyCfg;
import com.dragoniade.deviantart.ui.SwingThread;
import com.dragoniade.deviantart.ui.YesNoAllDialog;
import com.dragoniade.exceptions.LoggableException;

public class FavoritesDownloader {

	private HttpClient client;
	private String userId;
	private String destination;
	private String destinationMature;
	private JTextPane textPane;
	private ProgressDialog progress;
	private JFrame owner;
	private int sleepThrottle = 0;
	private int requestCount = 0;
	private Search searcher;
	private boolean skipCollection = false;
	
	private enum STATUS {
		DOWNLOADED,
		CANCEL,
		SKIP,
		NOTFOUND;
	}
	
	public FavoritesDownloader(String userId, String destination, String destinationMature, Search searcher) {
		this.destination = destination;
		this.destinationMature = destinationMature;
		this.userId = userId;
		this.searcher = searcher;
		
		HttpClientParams params = new HttpClientParams();
		params.setVersion(HttpVersion.HTTP_1_1);
		params.setSoTimeout(30000);
		client = new HttpClient(params);
		searcher.setClient(client);
	}
	
	public void setGUI(JFrame owner,JTextPane textPane, ProgressDialog progress ) {
		this.owner = owner;
		this.textPane = textPane;
		this.progress = progress;
	}

	public void skipCollection(boolean skipCollection) {
		this.skipCollection = skipCollection;
	}
	
	public void setProxy(ProxyCfg prx) {
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
	
	private File getFile(Deviation da, String url, String filename, AtomicBoolean download, 
			YesNoAllDialog matureMoveDialog, YesNoAllDialog overwriteDialog, YesNoAllDialog overwriteNewerDialog, YesNoAllDialog deleteEmptyDialog) {
		
		progress.setText("Downloading file '" + filename + "' from " + da.getArtist());
		
		String title = filename + " by " + da.getArtist();
		long timestamp = da.getTimestamp().getTime();
		File artPG = LocationHelper.getFile(destination, userId, da, filename);
		File artMature= LocationHelper.getFile(destinationMature, userId, da, filename);
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
		requestCount = 0;
		searcher.setFrame(owner);
		searcher.setUser(userId);
		searcher.startAt(offset);
		
		progress.setTotalMax(Integer.MAX_VALUE);
		progress.setTotalValue(offset);
		
		int total = -1;
		
		YesNoAllDialog matureMoveDialog = new YesNoAllDialog();
		YesNoAllDialog overwriteDialog = new YesNoAllDialog();
		YesNoAllDialog overwriteNewerDialog = new YesNoAllDialog();
		YesNoAllDialog deleteEmptyDialog = new YesNoAllDialog();
		
		List<Collection> collections;
		if (skipCollection) {
			collections = new ArrayList<Collection>();
			collections.add(null);
		} else {
			collections = searcher.getCollections();
		}
		 
		for (Collection collection : collections) {
			searcher.startAt(offset);
			progress.setTotalValue(offset);
			
			while (!progress.isCancelled()) {
				
				progress.setText("Fetching results " + (collection != null ? collection.getName() : "..."));
				List<Deviation> results = searcher.search(progress, collection);
				requestCount++;
				
				if (results == null) {
					return;
				}
				total = searcher.getTotal();
				progress.setTotalMax(total);
				
				if (results.size() > 0) {
					for (Deviation da : results) {
						if (progress.isCancelled()) {
							return;
						}
						boolean downloaded = false;
						progress.setUnitMax(1);
							
						if (da.getDocumentDownloadUrl() != null) {
							String url = da.getDocumentDownloadUrl();
							String filename = da.getDocumentFilename();
							
							if (filename == null) {
								AtomicBoolean download = new AtomicBoolean(true);
								url = getDocumentUrl(da, download);
								if (url == null) {
									return;
								}
								if (!download.get()) {
									skipped++; 
									if (!nextDeviation(skipped)) {
										return;
									}
									continue;
								}
							}
							filename = Deviation.extractFilename(url);
							STATUS status = downloadFile(da,url,filename,false,matureMoveDialog,overwriteDialog,overwriteNewerDialog,deleteEmptyDialog);
							
							if (status == null) {
								return;
							}
							
							switch (status) {
								case CANCEL : return;
								case SKIP : 
									skipped++; 
									if (!nextDeviation(skipped)) {
										return;
									} 
									continue;
								case DOWNLOADED : 
									downloaded = true;
									break;
								case NOTFOUND: 
									if (da.getImageDownloadUrl() == null) {
										String text = "<br/><a style=\"color:red;\" href=\"" + da.getUrl()+ "\">" + url + " was not found" +  "</a>";
										setPaneText(text);
										progress.incremTotal();
									}
							}
						}
						
						if (da.getImageDownloadUrl() != null && !downloaded) {
							String url = da.getImageDownloadUrl();
							String filename = da.getImageFilename();
							
							STATUS status = downloadFile(da,url,filename,false,matureMoveDialog,overwriteDialog,overwriteNewerDialog,deleteEmptyDialog);
							
							if (status == null) {
								return;
							}
							
							switch (status) {
								case CANCEL : return;
								case SKIP : 
									skipped++; 
									if (!nextDeviation(skipped)) {
										return;
									} 
									continue;
								case DOWNLOADED : 
									downloaded = true;
									break;
								case NOTFOUND: 
									String text = "<br/><a style=\"color:red;\" href=\"" + da.getUrl()+ "\">" + url + " was not found" +  "</a>";
									setPaneText(text);
									progress.incremTotal();
							}
						}
						if (!nextDeviation(skipped)) {
							return;
						} 
					}
				} else {
					break;
				}
			}
		}
	}
	
	private boolean nextDeviation(int skipped) {
		
		progress.incremTotal();
		progress.setUnitValue(1);
		progress.setUnitMax(Integer.MAX_VALUE);	
		
		if (skipped == 480) {
			int res = showConfirmDialog(owner, "480 deviations have been skipped so far. Continue scanning?","Continue?",JOptionPane.YES_NO_OPTION );
			if (res == JOptionPane.NO_OPTION) {
				return false;
			}
		}
		 
		if (requestCount > 20) {
			throttle();
		}
		return true;
	}
	
	private void throttle() {
		requestCount = 0;
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
	private STATUS downloadFile(Deviation da, String downloadUrl, String filename, boolean reportError,
			YesNoAllDialog matureMoveDialog, YesNoAllDialog overwriteDialog, YesNoAllDialog overwriteNewerDialog, YesNoAllDialog deleteEmptyDialog) {
		
		AtomicBoolean download = new AtomicBoolean(true);
		File art = getFile(da,  downloadUrl, filename, download,  matureMoveDialog,  overwriteDialog,  overwriteNewerDialog,  deleteEmptyDialog);
		if (art == null) {
			return null;
		}
		
		if (download.get() ) {
			File parent = art.getParentFile();
			if (!parent.exists()) {
				if (!parent.mkdirs()) {
					showMessageDialog(owner,"Unable to create '" + parent.getPath() + "'.","Error",JOptionPane.ERROR_MESSAGE);
					return null;
				}
			}
			
			GetMethod method = new GetMethod(downloadUrl);
			try {
				int sc = -1;
				do {
					sc = client.executeMethod(method);
					requestCount++;
					if (sc != 200) {
						if (sc == 404 || sc == 403) {
							method.releaseConnection();
							return STATUS.NOTFOUND;
						} else {
							LoggableException ex = new LoggableException(method.getResponseBodyAsString());
							Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), ex);
							
							int res = showConfirmDialog(owner, "An error has occured when contacting deviantART : error " + sc + ". Try again?","Continue?",JOptionPane.YES_NO_CANCEL_OPTION );
							if (res == JOptionPane.NO_OPTION) {
								String text = "<br/><a style=\"color:red;\" href=\"" + da.getUrl()+ "\">" + downloadUrl + " has an error" +  "</a>";
								setPaneText(text);
								method.releaseConnection();
								progress.incremTotal();
								return STATUS.SKIP;
							}
							if (res == JOptionPane.CANCEL_OPTION) {
								return null;
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
						is.close();
						method.releaseConnection();
						tmpFile.delete();
						return null;
					}
				}
				fos.close();
				method.releaseConnection();
				
				if (art.exists()) {
					if (!art.delete()) {
						showMessageDialog(owner,"Unable to delete '" + art.getPath() + "'.","Error",JOptionPane.ERROR_MESSAGE);
						return null;
					}
				}
				if (!tmpFile.renameTo(art)) {
					showMessageDialog(owner,"Unable to rename '" + tmpFile.getPath() + "' to '" + art.getPath() +"'.","Error",JOptionPane.ERROR_MESSAGE);
					return null;
				}
				art.setLastModified(da.getTimestamp().getTime());
				return STATUS.DOWNLOADED;
			} catch (HttpException e) {
				showMessageDialog(owner,"Error contacting deviantART: " + e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
				return null;
			} catch (IOException e) {
				showMessageDialog(owner,"Error contacting deviantART: " + e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
				return null;
			}
		} else {
			progress.setText("Skipping file '" + filename + "' from " + da.getArtist());
			return STATUS.SKIP;
		}
	}
	
	private String getDocumentUrl (Deviation da, AtomicBoolean download) {
		String downloadUrl = da.getDocumentDownloadUrl(); 
		GetMethod method = new GetMethod(downloadUrl);
		
		try {
			int sc = -1;
			do {
				method.setFollowRedirects(false);
				sc = client.executeMethod(method);
				requestCount++;
				
				if (sc >= 300 && sc <= 399) {
					String location =  method.getResponseHeader("Location").getValue();
					method.releaseConnection();
					return location;
				} else {
						LoggableException ex = new LoggableException(method.getResponseBodyAsString());
						Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), ex);
						
						int res = showConfirmDialog(owner, "An error has occured when contacting deviantART : error " + sc + ". Try again?","Continue?",JOptionPane.YES_NO_CANCEL_OPTION );
						if (res == JOptionPane.NO_OPTION) {
							String text = "<br/><a style=\"color:red;\" href=\"" + da.getUrl()+ "\">" + downloadUrl + " has an error" +  "</a>";
							setPaneText(text);
							method.releaseConnection();
							progress.incremTotal();
							download.set(false);
							return null;
						}
						if (res == JOptionPane.CANCEL_OPTION) {
							return null;
						}
				}
			} while (true); 
		} catch (HttpException e) {
			showMessageDialog(owner,"Error contacting deviantART: " + e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
			return null;
		} catch (IOException e) {
			showMessageDialog(owner,"Error contacting deviantART: " + e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
			return null;
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
					if (!current.delete()) {
						int result = JOptionPane.showConfirmDialog(owner, "Unable to delete '" + current.getAbsolutePath() + "'. Try again?","Error",JOptionPane.ERROR_MESSAGE );
						if (result != JOptionPane.YES_OPTION) {
							return;
						}
					}
				} else {
					return;
				}
			} else {
				current = current.getParentFile();
			}
			
		} while (current != null);
	}
}
