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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.w3c.dom.Element;

import com.dragoniade.deviantart.ui.DialogHelper;
import com.dragoniade.deviantart.ui.ProgressDialog;
import com.dragoniade.exceptions.LoggableException;
import com.dragoniade.xml.XmlToolkit;

public class SearchStream implements Search{

	private final static String DOWNLOAD_URL = "http://www.deviantart.com/download/";
	private final int OFFSET = 24;
	
	private SEARCH search;
	private int offset;
	private int total;
	private String user;
	private HttpClient client;
	private JFrame owner;
	
	public SearchStream() {
		this.offset = 0;
		this.total = -1;
		
		HttpClientParams params = new HttpClientParams();
		params.setVirtualHost("www.deviantart.com");
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
		
		String queryString = "http://www.deviantart.com/global/difi.php?c=Stream;thumbs;" + search.toString() + ":" + user + "," + offset +"," + OFFSET + "&t=xml";
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
			
			StringBuilder stringBuilder = new StringBuilder();
			InputStream inputStream = method.getResponseBodyAsStream();
			int b;
			boolean isOpening = false;
			while ((b = inputStream.read()) > -1) {
				if (b > 127) {
					isOpening = false;
					continue;
				}
				char c = (char) b;
				if (isOpening && Character.isDigit(c)) {
					stringBuilder.append('_');
				}
				if (isOpening && c == '/') {
					stringBuilder.append(c);
					continue;
				}
				isOpening = (c == '<');
				stringBuilder.append(c);
			}
				
				
			Element responses = toolkit.parseDocument(stringBuilder.toString());
			method.releaseConnection();
			
			total = toolkit.getNodeAsInt(responses, "response/calls/response/content/total");
			List<?> deviations =  toolkit.getMultipleNodes(responses, "response/calls/response/content/deviations");
			if (total == 0) {
				return results;
			}
			for (Object obj: deviations) {
				Element deviation = (Element)  obj;
				
				Deviation da = new Deviation();
				da.setId(toolkit.getNodeAsLong(deviation, "id"));
				da.setArtist(toolkit.getNodeAsString(deviation, "artist"));
				da.setCategory(toolkit.getNodeAsString(deviation, "category"));
				da.setTitle(toolkit.getNodeAsString(deviation, "title"));
				da.setUrl(toolkit.getNodeAsString(deviation, "url"));
				da.setTimestamp(new Date(toolkit.getNodeAsLong(deviation, "ts")*1000));
				da.setMature("1".equals(toolkit.getNodeAsString(deviation, "is_mature")));
				
				String filenameStr = toolkit.getNodeAsString(deviation, "filename");
				String imageUrl = toolkit.getNodeAsString(deviation, "image/url");
				
				String primaryFilename = Deviation.extractFilename(filenameStr);
				da.setDocumentDownloadUrl(DOWNLOAD_URL + da.getId() + "/");
				da.setDocumentFilename(primaryFilename); 
				
				if (imageUrl != null) {
					String secondaryFilename = Deviation.extractFilename(imageUrl);
					da.setImageDownloadUrl(imageUrl);
					da.setImageFilename(secondaryFilename);
					da.setResolution(toolkit.getNodeAsString(deviation, "image/width") + "x" + toolkit.getNodeAsString(deviation, "image/height"));
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
	
	public boolean validate() {
		String user = "";
		String offset = "0";
		SEARCH search = SEARCH.FAVORITE;
		
		XmlToolkit toolkit = XmlToolkit.getInstance();
		
		String queryString = "http://www.deviantart.com/global/difi.php?c=Stream;thumbs;" + search.toString() + ":" + user + "," + offset +"," + OFFSET + "&t=xml";
		GetMethod method = new GetMethod(queryString);
		try {
			int sc = client.executeMethod(method);
			if (sc != 200) {
				return false;
			}
			
			Element responses = toolkit.parseDocument(method.getResponseBodyAsStream());
			String status = toolkit.getNodeAsString(responses, "status");
			return "SUCCESS".equalsIgnoreCase(status);
		} catch (IOException e) {
			return false;
		} finally {
			method.releaseConnection();
		}
	}
	
	public String getName() {
		return "Difi Stream Searcher";
	}
	
	public int priority() {
		// Disabled by DA
		return 0;
	}
}
