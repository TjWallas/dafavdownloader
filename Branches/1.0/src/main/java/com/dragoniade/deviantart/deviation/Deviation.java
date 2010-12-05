/**
 *    Deviation - POJO for a deviation
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
	private String primaryDownloadUrl;
	private String secondaryDownloadUrl;
	private String primaryFilename;
	private String secondaryFilename;
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
	public String getPrimaryDownloadUrl() {
		return primaryDownloadUrl;
	}
	public String getSecondaryDownloadUrl() {
		return secondaryDownloadUrl;
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
	public String getPrimaryFilename() {
		return primaryFilename;
	}
	public String getSecondaryFilename() {
		return secondaryFilename;
	}
	public void setPrimaryFilename(String primaryFilename) {
		this.primaryFilename = primaryFilename;
	}
	public void setSecondaryFilename(String secondaryFilename) {
		this.secondaryFilename = secondaryFilename;
	}
	public void setPrimaryDownloadUrl(String primaryDownloadUrl) {
		this.primaryDownloadUrl = primaryDownloadUrl;
	}
	public void setSecondaryDownloadUrl(String secondaryDownloadUrl) {
		this.secondaryDownloadUrl = secondaryDownloadUrl;
	}
	
	public static String extractFilename(String url) {
		int beginIndex = url.lastIndexOf('/');
		if (beginIndex > -1) {
			return url.substring(beginIndex+1);
		} else {
			return url;
		}
	}
	
	public String toString() {
		return primaryFilename + " by " + artist;	
	}
}
