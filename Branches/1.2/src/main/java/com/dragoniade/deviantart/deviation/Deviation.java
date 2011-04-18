/**
 *    Deviation - POJO for a deviation
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
package com.dragoniade.deviantart.deviation;

import java.io.Serializable;
import java.util.Date;
public class Deviation implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2899045645356752047L;
	private Long id;
	private String url;
	private String title;
	private String artist;
	private String imageDownloadUrl;
	private String imageFilename;
	private String documentDownloadUrl;
	private String documentFilename;
	private String resolution;
	private Date timestamp;
	private String category;
	private boolean mature;
	
	public boolean isMature() {
		return mature;
	}
	public void setMature(boolean mature) {
		this.mature = mature;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getArtist() {
		return artist;
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}
	public String getResolution() {
		return resolution;
	}
	public void setResolution(String resolution) {
		this.resolution = resolution;
	}
	/**
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	/**
	 * @return the timestamp
	 */
	public Date getTimestamp() {
		return timestamp;
	}
	
	public static String extractFilename(String url) {
		int beginIndex = url.lastIndexOf('/');
		if (beginIndex > -1) {
			String filename = url.substring(beginIndex+1); 
			return (filename.length() > 0) ? filename: null;
		} else {
			return url;
		}
	}
	
	public String toString() {
		return imageFilename + " by " + artist;	
	}
	public String getImageDownloadUrl() {
		return imageDownloadUrl;
	}
	public String getImageFilename() {
		return imageFilename;
	}
	public String getDocumentDownloadUrl() {
		return documentDownloadUrl;
	}
	public String getDocumentFilename() {
		return documentFilename;
	}
	public void setImageDownloadUrl(String imageDownloadUrl) {
		this.imageDownloadUrl = imageDownloadUrl;
	}
	public void setImageFilename(String imageFilename) {
		this.imageFilename = imageFilename;
	}
	public void setDocumentDownloadUrl(String documentDownloadUrl) {
		this.documentDownloadUrl = documentDownloadUrl;
	}
	public void setDocumentFilename(String documentFilename) {
		this.documentFilename = documentFilename;
	}
}
