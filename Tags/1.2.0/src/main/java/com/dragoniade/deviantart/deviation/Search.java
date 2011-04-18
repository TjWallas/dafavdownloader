/**
 *    Search - Query deviantART for favorites
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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import org.apache.commons.httpclient.HttpClient;

import com.dragoniade.deviantart.ui.ProgressDialog;

public interface Search {

	public enum SEARCH {
		FAVORITE ("favby","favby:%username%", "favourites", "Favorites"),
		GALLERY ("gallery","gallery:%username%", "gallery", "Gallery"),
		SCRAPBOOK ("scraps","gallery:%username%+in:scraps", null, "Scrapbook");
		
		private final String id;
		private final String search;
		private final String label;
		private final String collection;
		
	    private static final Map<String,SEARCH> lookup = new HashMap<String,SEARCH>();
	    static {
	         for(SEARCH s : EnumSet.allOf(SEARCH.class))
	              lookup.put(s.id, s);
	    }
	    static public SEARCH lookup(String name) {
	    	return lookup.get(name);
	    }
	    
		SEARCH(String id, String search, String collection, String label){
			this.search = search;
			this.id = id;
			this.collection = collection;
			this.label = label;
		};
		
		public String getSearch() {
			return search;
		}
		
		public String getLabel() {
			return label;
		}
		
		public String getCollection() {
			return collection;
		}
		
		public String getId() {
			return id;
		}
		public static SEARCH getDefault() {
			return FAVORITE;
		}
	}
	
	/*
	 * Set the frame for the dialog and warning.
	 */
	public void setFrame(JFrame owner);
	
	/*
	 * Set the search type.
	 * 
	 */
	public void setSearch(SEARCH search);

	/*
	 * Set the user
	 * 
	 */
	public void setUser(String user);
	/*
	 * Return how many results the search has. 
	 */
	public int getTotal();
	
	/*
	 * Get a search result iteration or null if there is no more results.
	 */
	public List<Deviation> search(ProgressDialog progress, Collection collection);

	/*
	 * Set the starting point, from the end of the results.
	 */
	public void startAt(int offset);
	
	/*
	 * Get how many results there is per search.
	 */
	public int getOffset();
	
	/*
	 * Validate if this searcher is supported
	 */
	public boolean validate();
	
	/*
	 * Get the priority of this searcher for preference.
	 */
	public int priority();
	/*
	 * Return the display name of this searcher
	 */
	public String getName();
	
	/*
	 * Set the Http Client to be used
	 */
	public void setClient(HttpClient client);
	
	public List<Collection> getCollections();
}
