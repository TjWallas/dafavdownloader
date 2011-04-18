/**
 *    Search - Query deviantART for favorites
 *    Copyright (C) 2010-2011  Philippe Busque
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
package com.dragoniade.deviantart.deviation;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.jaxen.NamespaceContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.dragoniade.deviantart.ui.DialogHelper;
import com.dragoniade.deviantart.ui.ProgressDialog;
import com.dragoniade.exceptions.LoggableException;
import com.dragoniade.xml.XmlToolkit;

public class SearchRss implements Search{

	private final int OFFSET = 60;
	
	private SEARCH search;
	private int offset;
	private int total;
	private String user;
	private HttpClient client;
	private JFrame owner;
	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EE, dd MMM yyyy HH:mm:ss zz");
	private boolean debug = System.getProperty("debug") != null;;
	
	public SearchRss() {
		this.offset = 0;
		this.total = -1;
		
		HttpClientParams params = new HttpClientParams();
		params.setVirtualHost("backend.deviantart.com");
		params.setVersion(HttpVersion.HTTP_1_1);
		client = new HttpClient(params);
		
	}
	
	public List<Deviation> search(ProgressDialog progress) {
		if ( user == null ) {
			throw new IllegalStateException("You must set the user before searching.");
		}
		if ( search == null ) {
			throw new IllegalStateException("You must set the search type before searching.");
		}
		if (total < 0) {
			progress.setText("Fetching total (0)");
			total = retrieveTotal(progress);
			progress.setText("Total: " + total);
		}
		
		String queryString = "http://backend.deviantart.com/rss.xml?q=" + search.toString() + ":" + user + "&type=deviation&offset=" + offset;
		GetMethod method = new GetMethod(queryString);
		List<Deviation> results = new ArrayList<Deviation>(OFFSET);
		try {
			int sc = -1;
			do {
				sc = client.executeMethod(method);
				if (sc != 200) {
					LoggableException ex = new LoggableException(method.getResponseBodyAsString());
					Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), ex);
					
					int res = DialogHelper.showConfirmDialog(owner, "An error has occured when contacting deviantART : error " + sc + ". Try again?","Continue?",JOptionPane.YES_NO_OPTION );
					if (res == JOptionPane.NO_OPTION) {
						return null;
					}
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {}
				}
			} while (sc != 200); 
			
			XmlToolkit toolkit = XmlToolkit.getInstance();
			
			Element responses = toolkit.parseDocument(method.getResponseBodyAsStream());
			method.releaseConnection();
			
			HashMap<String, String> prefixes = new HashMap<String, String> ();
			prefixes.put("media", responses.getOwnerDocument().lookupNamespaceURI("media"));
			NamespaceContext context = toolkit.getNamespaceContext(prefixes);
			
			List<?> deviations =  toolkit.getMultipleNodes(responses, "channel/item");
			if (deviations.size() == 0) {
				return results;
			}
			
			for (Object obj: deviations) {
				Element deviation = (Element)  obj;
				
				Deviation da = new Deviation();
				da.setId(getId(toolkit.getNodeAsString(deviation, "guid")));
				da.setArtist(toolkit.getNodeAsString(deviation, "media:credit",context));
				da.setCategory(toolkit.getNodeAsString(deviation, "media:category",context));
				da.setTitle(toolkit.getNodeAsString(deviation, "media:title",context));
				da.setUrl(toolkit.getNodeAsString(deviation, "link"));
				da.setTimestamp(parseDate(toolkit.getNodeAsString(deviation, "pubDate")));
				da.setMature(!"nonadult".equals(toolkit.getNodeAsString(deviation, "media:rating",context)));
				
				
				Element documentNode = (Element) toolkit.getSingleNode(deviation, "media:content[@medium='document']",context);
				Element imageNode = (Element) toolkit.getSingleNode(deviation, "media:content[@medium='image']",context);
				Element videoNode = (Element) toolkit.getSingleNode(deviation, "media:content[@medium='video']",context);
				
				if (imageNode != null) {
					String content = imageNode.getAttribute("url");
					String filename = Deviation.extractFilename(content);
					da.setImageDownloadUrl(content);
					da.setImageFilename(filename);
					
					da.setResolution(imageNode.getAttribute("width") + "x" + imageNode.getAttribute("height"));	
				}
				
				if (documentNode != null) {
					String content = documentNode.getAttribute("url");
					String filename = Deviation.extractFilename(content);
					da.setDocumentDownloadUrl(content);
					da.setDocumentFilename(filename);
				}
				
				if (videoNode != null) {
					String content = videoNode.getAttribute("url");
					if (!content.endsWith("/") ) {
						content = content + "/";
					}
					String filename = Deviation.extractFilename(content);
					da.setDocumentDownloadUrl(content);
					da.setDocumentFilename(filename);
				}
				
				results.add(da);
			}
			offset = offset + deviations.size();
			return results;
		} catch (HttpException e) {
			DialogHelper.showMessageDialog(owner,"Error contacting deviantART : " + e.getMessage() + ".","Error",JOptionPane.ERROR_MESSAGE);
			return null;
		} catch (IOException e) {
			DialogHelper.showMessageDialog(owner,"Error contacting deviantART : " + e.getMessage() + ".","Error",JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}
	
	public boolean validate() {
		String user = "";
		SEARCH search = SEARCH.FAVORITE;
		
		String queryString = "http://backend.deviantart.com/rss.xml?q=" + search.toString() + ":" + user + "&type=deviation";
		GetMethod method = new GetMethod(queryString);
		try {
			
			method = new GetMethod(queryString);
			int sc = client.executeMethod(method);
			if (sc != 200) {
				return false;
			}
			String response = method.getResponseBodyAsString();
			if (response == null) {
				return false;
			}
		
			if (response.indexOf("<channel") < 0) {
				return false;
			}
			return true;
		} catch (IOException e) {
			return false;
		} finally {
			method.releaseConnection();
		}
	}
	
	public void setSearch(SEARCH search) {
		this.search = search;
	}
	
	public void setUser(String user) {
		this.user = user;
	}
	
	public int getTotal() {
		return total;
	}
	
	public void startAt(int offset) {
		this.offset = offset;
	}

	public int getOffset() {
		return OFFSET;
	}
	
	public void setFrame(JFrame owner) {
		this.owner = owner;
	}
	
	public String getName() {
		return "RSS Searcher";
	}
	
	private Date parseDate(String pubDate) {
		try {
			return simpleDateFormat.parse(pubDate);
		} catch (ParseException e) {
			return new Date();
		}
	}
	
	private Long getId(String guid) {
		int lastIndex = guid.lastIndexOf('-');
		try {
			return Long.parseLong(guid.substring(lastIndex+1));	
		} catch (NumberFormatException e) {
			return (long) guid.hashCode();
		}
	}
	
	private int retrieveTotal(ProgressDialog progress) {
		int offset = 6000;
		int greaterThan = 0;
		int lessThan = Integer.MAX_VALUE;
		int iteration = 0;
		while (total < 0) {
			progress.setText("Fetching total (" + ++iteration + ")");
			String queryString = "http://backend.deviantart.com/rss.xml?q=" + search.toString() + ":" + user + "&type=deviation&offset=" + offset;
			GetMethod method = new GetMethod(queryString);
			try {
				int sc = -1;
				do {
					sc = client.executeMethod(method);
					if (sc != 200) {
						LoggableException ex = new LoggableException(method.getResponseBodyAsString());
						Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), ex);
						
						int res = DialogHelper.showConfirmDialog(owner, "An error has occured when contacting deviantART : error " + sc + ". Try again?","Continue?",JOptionPane.YES_NO_OPTION );
						if (res == JOptionPane.NO_OPTION) {
							return -1;
						}
					}
				} while (sc != 200); 
				
				XmlToolkit toolkit = XmlToolkit.getInstance();
				
				Element responses = toolkit.parseDocument(method.getResponseBodyAsStream());
				method.releaseConnection();
				
				List<?> deviations =  toolkit.getMultipleNodes(responses, "channel/item");
				
				HashMap<String, String> prefixes = new HashMap<String, String> ();
				prefixes.put("atom", responses.getOwnerDocument().lookupNamespaceURI("atom"));
				Node next = toolkit.getSingleNode(responses, "channel/atom:link[@rel='next']", toolkit.getNamespaceContext(prefixes));
				int size  = deviations.size();
				
				if (debug) {
					System.out.println();
					System.out.println();
					System.out.println("Lesser  Than: " + lessThan);
					System.out.println("Greater Than: " + greaterThan);
					System.out.println("Offset: " + offset);
					System.out.println("Size: " + size);
				}
				
				if (size != OFFSET && size > 0) {
					if (next != null) {
						greaterThan = offset + OFFSET;
					} else {
						if (debug) System.out.println("Total (offset + size) : " + (offset + size));
						return offset + size;
					}
				}
				
				// Page is full, there is more deviations
				if (size == OFFSET) {
					greaterThan = offset + OFFSET;
				}
			
				if (size == 0) {
					lessThan = offset;
				}

				if (greaterThan == lessThan) {
					if (debug) System.out.println("Total (greaterThan) : " + greaterThan);
					return greaterThan;
				}
				
				if (lessThan == Integer.MAX_VALUE) {
					offset = offset * 2;
				} else {
					offset = (greaterThan + lessThan) / 2;
					if (offset % 60 != 0) {
						offset = (offset / 60) * 60;
					}					
				}

				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {}
			} catch (IOException e) {
				
			}
		}
		
		return total;
		
	}

	public int priority() {
		return 100;
	}
}
