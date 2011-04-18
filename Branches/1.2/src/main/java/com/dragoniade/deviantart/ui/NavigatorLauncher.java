package com.dragoniade.deviantart.ui;

import java.io.IOException;
import java.lang.reflect.Method;

public class NavigatorLauncher {

	private final static String[] LINUX_BROWSERS = { "firefox", "mozilla","opera", "konqueror", "epiphany", "netscape" };
	private final static String os =  System.getProperty("os.name");
	
	public static boolean launch(String url) {

		if (os.startsWith("Mac OS")) {
			try {
				Class<?> fileMager = Class.forName("com.apple.eio.FileManager");
	            Method openURLMethod = fileMager.getDeclaredMethod("openURL", new Class[] {String.class});
	            openURLMethod.invoke(null, new Object[] {url});								
			} catch (Exception e) {
				return false;
			}
			return true;
		}
		
		if (os.startsWith("Windows")) {
			String[] command = new String[3];
			command[0] = "rundll32";
			command[1] = "url.dll,FileProtocolHandler";
			command[2] = url;
			try {
				Runtime.getRuntime().exec(command);
			} catch (IOException e) {
				return false;
			}

			return true;
		}
		
		// We assume the remaining OS 'flavor' are Linux based
        String defaultBrowser = null;
        
        try {
        	for (int i=0; i<LINUX_BROWSERS.length; i++) {
            	String command[] = new String[] {"which",LINUX_BROWSERS[i]};
            	Process process = Runtime.getRuntime().exec(command);
            	int sc = process.waitFor();
            	  if ( sc == 0) {
            		  defaultBrowser = LINUX_BROWSERS[i];
            		  break;
            	  }
            }
             
            if (defaultBrowser != null) {
            	String[] command = new String[] {defaultBrowser,url};
            	Runtime.getRuntime().exec(command);
            	return true;
            } 
        } catch (Exception e) {
        	return false;
        }
        return false;
	}
}
 