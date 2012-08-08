package fr.pilato.elasticsearch.river.dropbox.connector;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;

public class DropboxChanges implements Serializable {
	private static final long serialVersionUID = 1L;

	private HashMap<String, DropboxFile> files = new HashMap<String, DropboxFile>();
	
	private String cursor = null;
	
	/**
	 * @return the files
	 */
	public Collection<DropboxFile> getFiles() {
		return files.values();
	}
	
	/**
	 * @return the cursor
	 */
	public String getCursor() {
		return cursor;
	}
	
	/**
	 * @param cursor the cursor to set
	 */
	public void setCursor(String cursor) {
		this.cursor = cursor;
	}
	
	public void add(DropboxFile file) {
		files.put(file.getFilename(), file);
	}
	
	public DropboxFile get(String filename) {
		return files.get(filename);
	}
	
}
