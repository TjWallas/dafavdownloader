/**
 *    ClassCrawler - Craws the classpath and retrieve class mathching the 
 *    search criteria.
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
package com.dragoniade.clazz;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;



public class ClassCrawler {

	
	private Set<String> prefixes;
	private Set<Class<?>> interfaces;
	private Set<Class<?>> superClasses;
	public ClassCrawler() {
		prefixes = new HashSet<String>();
		interfaces = new HashSet<Class<?>>();
		superClasses = new HashSet<Class<?>>();
	}
	
	
	public void setInterfaces(Class<?> ... interfaces) {
		this.interfaces = new HashSet<Class<?>>();
		for (Class<?> i: interfaces) {
			this.interfaces.add(i);	
		}
		
	}
	
	public void setPrefixes(String ... prefixes) {
		this.prefixes = new HashSet<String>();
		for (String p: prefixes) {
			this.prefixes.add(p);	
		}
	}
	
	public void setSuperClasses(Class<?> ... superClasses) {
		this.superClasses = new HashSet<Class<?>>();
		for (Class<?> s: superClasses) {
			this.superClasses.add(s);	
		}
	}
	
	public Set<Class<?>> crawl() throws ClassNotFoundException {
		// URL [] urls = ((URLClassLoader)Thread.currentThread().getContextClassLoader()).getURLs();
		URL [] urls = ((URLClassLoader)getClass().getClassLoader()).getURLs();
		Set<Class<?>> classes = new HashSet<Class<?>>();
		
		for (URL url: urls) {
			if (url.getProtocol().equals("file")) {
				if (url.getPath().endsWith(".jar") || url.getPath().endsWith(".zip")) {
					loadClassesFromJar(url,classes);
				} else {
					loadClassesFromFile(url,classes);
				}
			}
			if (url.getProtocol().equals("jar")) {
				loadClassesFromJar(url,classes);
			}
		}
		return classes;
	}
	private void loadClassesFromJar(URL url, Set<Class<?>> files ) throws ClassNotFoundException {
		try {
			InputStream stream = url.openStream();
			
			JarInputStream fileNames = new JarInputStream(stream);
			JarEntry entry = null;
			while((entry =fileNames.getNextJarEntry()) != null) {
				if(entry.getName().endsWith(".class")) {
					add(files,entry.getName());
				}
			}
			fileNames.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void loadClassesFromFile(URL url, Set<Class<?>> list ) throws ClassNotFoundException {
		
		LinkedList<File> fileList = new LinkedList<File>();
		
		File urlFile;
		try {
			urlFile = new File(URLDecoder.decode(url.getPath(),"UTF-8"));
		} catch (UnsupportedEncodingException e) {
			urlFile = new File(url.getPath());
		}
		String baseFile = urlFile.getPath();
		fileList.add(urlFile);
		
		while (!fileList.isEmpty()) {
			File pop = fileList.removeFirst();
			if (!pop.exists()) {
				continue;
			}
			File[] files = pop.listFiles();
			
			for (File f: files) {
				if (f.isDirectory()) {
					fileList.add(f);
				} else {
					if (f.getName().endsWith(".class")) {
						add(list,f.getPath().substring(baseFile.length()+1));
					}
				}
			}
		}
	}
	
	private void add(Set<Class<?>> list,String path) throws ClassNotFoundException {
		path = path.substring(0, path.length()-6);
		path = path.replace('/', '.').replaceAll("\\\\", ".");
		boolean isValid = false;
		if (prefixes.size() == 0) {
			isValid = true;
		} else {
			for (String prefix:prefixes) {
				if (path.startsWith(prefix)) {
					isValid = true;
					break;
				}
			}
		}
		if (isValid) {
			try {
				Class<?> c = this.getClass().getClassLoader().loadClass(path);
				if (superClasses.size() > 0 ) {
					Class<?> superC = c.getSuperclass();
					if (superClasses.contains(superC)) {
						list.add(c);
						return;
					}
				}
				if (interfaces.size() > 0) {
					for( Class<?> intC : c.getInterfaces() ) {
						if (interfaces.contains(intC)) {
							list.add(c);
							return;
						}
					}
				}
			} catch (Throwable e ) {}
		}
	}
}
