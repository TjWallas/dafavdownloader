/**
 *    ProxyCfg - Container for the proxy configuration.
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
package com.dragoniade.deviantart.ui;

import java.util.Properties;

public class ProxyCfg {

	private String username;
	private String password;
	private String host;
	private int port;
	
	public String getUsername() {
		return username;
	}
	public String getPassword() {
		return password;
	}
	public String getHost() {
		return host;
	}
	public int getPort() {
		return port;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public void setPort(int port) {
		this.port = port;
	}

	static public ProxyCfg parseConfig (Properties properties) {
		ProxyCfg prxConfig = null;
		if (Boolean.parseBoolean(properties.getProperty(Constants.PROXY_USE)) &&
				properties.getProperty(Constants.PROXY_HOST) != null) {
			
			prxConfig = new ProxyCfg();
			prxConfig.setHost(properties.getProperty(Constants.PROXY_HOST));
			
			int port = 80;
			try {
				port = Integer.parseInt(properties.getProperty(Constants.PROXY_PORT));
			} catch (NumberFormatException e) {
				port = 80;
			}
			prxConfig.setPort(port);
			
			String username = properties.getProperty(Constants.PROXY_USERNAME); 
			if (username != null && username.length() > 0) {
				prxConfig.setUsername(username);	
			}
			
			String password = properties.getProperty(Constants.PROXY_PASSWORD);
			if (password != null && password.length() > 0) {
				prxConfig.setPassword(password);	
			}
		}
		return prxConfig;
	}
	
}
