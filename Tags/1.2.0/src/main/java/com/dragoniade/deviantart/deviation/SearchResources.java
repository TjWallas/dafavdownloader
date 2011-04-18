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

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.commons.httpclient.HttpClient;

import com.dragoniade.deviantart.ui.ProgressDialog;

public class SearchResources implements Search{

	private final int OFFSET = 24;
	
	private SEARCH search;
	private int offset;
	private int total;
	private String user;
	private JFrame owner;
	
	public SearchResources() {
		this.offset = 0;
		this.total = -1;
		this.offset = this.offset * 1;
	}
	
	public List<Deviation> search(ProgressDialog progress, Collection collection) {
		if ( user == null ) {
			throw new IllegalStateException("You must set the user before searching.");
		}
		if ( search == null ) {
			throw new IllegalStateException("You must set the search type before searching.");
		}
		JOptionPane.showMessageDialog(owner, "The resource Searcher hasn't been implemented yet.","Not implemented yet",JOptionPane.WARNING_MESSAGE);
		return null;
	}
	
	public boolean validate() {
		return false;
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
		return "Resources Searcher";
	}
	
	public int priority() {
		return 75;
	}
	
	public void setClient(HttpClient client) {}
	
	public List<Collection> getCollections() {
		return new ArrayList<Collection>();
	}
}
