/**
 *    Search - Query deviantART for favorites
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
package com.dragoniade.deviantart.deviation;

import java.awt.Component;
import java.io.File;
import java.io.FileWriter;
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

import com.dragoniade.deviantart.ui.SwingThread;
import com.dragoniade.xml.XmlToolkit;

public class Search {

	private final static String DOWNLOAD_URL = "http://www.deviantart.com/download/";
	
	private String query;
	private int offset;
	private int total;
	private List<Deviation> results;
	private HttpClient client;
	
	public Search(String query) {
		this.query = query;
		this.offset = 0;
		this.total = -1;
		
		HttpClientParams params = new HttpClientParams();
		params.setVirtualHost("www.deviantart.com");
		params.setVersion(HttpVersion.HTTP_1_1);
		client = new HttpClient(params);
		
	}
	
	public List<Deviation> search(JFrame owner) {
		String queryString = "http://www.deviantart.com/global/difi.php?c=Stream;thumbs;" + query + "," + offset +",24&t=xml";
		GetMethod method = new GetMethod(queryString);
		List<Deviation> results = new ArrayList<Deviation>(24);
		try {
			int sc = -1;
			do {
				sc = client.executeMethod(method);
				if (sc != 200) {
					
					File error = new File(System.getProperty("java.io.tmpdir") + File.pathSeparator + "error" + System.currentTimeMillis() + ".log");
					error.getParentFile().mkdirs();
					FileWriter fw = new FileWriter(error);
					fw.write (method.getResponseBodyAsString());
					fw.close();
					
					int res = showConfirmDialog(owner, "An error has occured when contacting deviantART : error " + sc + ". Try again?","Continue?",JOptionPane.YES_NO_OPTION );
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
				
				
//			responseCleaner = responseCleaner.replaceAll("<(/?[0-9]*)>", "<thumb$1W>");
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
				da.setPrimaryDownloadUrl(DOWNLOAD_URL + da.getId() + "/" + primaryFilename);
				da.setPrimaryFilename(primaryFilename); 
				
				if (imageUrl != null) {
					String secondaryFilename = Deviation.extractFilename(imageUrl);
					da.setSecondaryDownloadUrl(imageUrl);
					da.setSecondaryFilename(secondaryFilename);
				}
				
				da.setResolution(toolkit.getNodeAsString(deviation, "image/width") + "x" + toolkit.getNodeAsString(deviation, "image/height"));
				
				results.add(da);
			}
			offset = offset + deviations.size();
			return results;
		} catch (HttpException e) {
			showMessageDialog(owner,"Error contacting deviantART : " + e.getMessage() + ".","Error",JOptionPane.ERROR_MESSAGE);
			return null;
		} catch (IOException e) {
			showMessageDialog(owner,"Error contacting deviantART : " + e.getMessage() + ".","Error",JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}
	
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public int getOffset() {
		return offset;
	}
	public void setOffset(int offset) {
		this.offset = offset;
	}
	public int getTotal() {
		return total;
	}
	public void setTotal(int total) {
		this.total = total;
	}
	public List<Deviation> getResults() {
		return results;
	}
	public void setResults(List<Deviation> results) {
		this.results = results;
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
	
	private int showConfirmDialog(final Component parentComponent,
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
