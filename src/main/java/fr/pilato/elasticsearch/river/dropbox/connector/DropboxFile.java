package fr.pilato.elasticsearch.river.dropbox.connector;

public class DropboxFile extends DropboxObject {
	private static final long serialVersionUID = 1L;
	
	private DropboxEntry meta;
	private String filename;
	
	/**
	 * Create a DropBox File representation	
	 * @param meta Meta Data for this file
	 * @param filename Filename
	 */
	public DropboxFile(DropboxEntry meta, String filename) {
		super();
		this.meta = meta;
		this.filename = filename;
	}
	
	/**
	 * @return the meta
	 */
	public DropboxEntry getMeta() {
		return meta;
	}
	/**
	 * @param meta the meta to set
	 */
	public void setMeta(DropboxEntry meta) {
		this.meta = meta;
	}
	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}
	/**
	 * @param filename the filename to set
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	

}
