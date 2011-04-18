/**
 *    Location Helper - Render the target location of an image
 *    Copyright (C) 2009-2010  Philippe Busque
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

import java.io.File;

import com.dragoniade.deviantart.deviation.Deviation;

public class LocationHelper {
	/*
	 *  %id% : Deviation Id
	 *  %user% : User being searched
	 *  %artist% : The Deviation's artist username.
	 *  %title% : The Deviations's title, normalized.
	 *  %filename% : The Deviation's original filename.
	 *  %extE% : The Deviation's extension.
	 */
	
	public static  File getFile(String location, String username, Deviation da, String filename) {
		StringBuilder sb = new StringBuilder(location);
				
		replace(sb,"%id%",Long.toString(da.getId()));
		replace(sb,"%user%",username);
		replace(sb,"%artist%",da.getArtist());
		replace(sb,"%title%",normalize(da.getTitle()));
		replace(sb,"%filename%",filename);
		int index = filename.lastIndexOf('.');
		String ext = "";
		if (index > 0) {
			ext = filename.substring(index+1);
		}
		replace(sb,"%ext%",ext);
		
		File f = new File(sb.toString());
		return f;
	}
	
	private static void replace(StringBuilder sb, String search, String replacement) {

		int fromIndex = 0;
		int size = search.length();
		
		while ((fromIndex = sb.indexOf(search,fromIndex)) > -1) {
			sb.replace(fromIndex, fromIndex + size, replacement);
			fromIndex += replacement.length();
		}
	}
	
	private static String normalize(String folder) {
		StringBuffer buffer = new StringBuffer();
		char[] chars = folder.toCharArray();
		
		
		for (int i=0 ; i< chars.length;i++) {
			char c = chars[i];
			
			// Forbidden windows characters
			switch (c) {
			 case '\\' : break;
			 case '/'  : break;
			 case ':'  : break;
			 case '*'  : break;
			 case '?'  : break;
			 case '"'  : break;
			 case '<'  : break;
			 case '>'  : break;
			 case '|'  : break;
			 default: buffer.append(c);
			}
		}
		
		// We trim any ending dot or space.
		while (buffer.length() > 0 && (buffer.charAt(buffer.length()-1) == '.' || buffer.charAt(buffer.length()-1) == ' ')) {
			buffer.setLength(buffer.length()-1);
		}

		String string = buffer.toString().trim();
		if (string.length() == 0) {
			string = "_";
		}
		return string;
	}
}
