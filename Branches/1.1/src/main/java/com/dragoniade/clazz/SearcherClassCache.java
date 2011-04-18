package com.dragoniade.clazz;

import java.util.HashSet;
import java.util.Set;

import com.dragoniade.deviantart.deviation.Search;

public class SearcherClassCache {

	private static SearcherClassCache instance;
	Set<Class<Search>> classes;
	
	@SuppressWarnings("unchecked")
	private SearcherClassCache () {
		ClassCrawler crawler = new ClassCrawler();
		crawler.setInterfaces(Search.class);

		classes = new HashSet<Class<Search>>();
		Set<Class<?>> list;
		try {
			list = crawler.crawl();
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

		for( Class<?> clazz : list) {
			classes.add((Class<Search>) clazz);
		}
	}
	
	public Set<Class<Search>> getClasses() {
		return classes;
	}
	
	static public synchronized SearcherClassCache getInstance () {
		if (instance == null) {
			instance = new SearcherClassCache();
		}
		return instance;
	}
}
