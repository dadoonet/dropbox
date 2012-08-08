package fr.pilato.elasticsearch.river.dropbox.connector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DropboxEntry extends DropboxObject {
	private static final long serialVersionUID = 1L;

	/** Size of the file. */
	public long bytes;

	/**
	 * If a directory, the hash is its "current version". If the hash changes
	 * between calls, then one of the directory's immediate children has
	 * changed.
	 */
	public String hash;

	/**
	 * Name of the icon to display for this entry. Corresponds to filenames
	 * (without an extension) in the icon library available at
	 * https://www.dropbox.com/static/images/dropbox-api-icons.zip.
	 */
	public String icon;

	/** True if this entry is a directory, or false if it's a file. */
	public boolean isDir;

	/**
	 * Last modified date, in "EEE, dd MMM yyyy kk:mm:ss ZZZZZ" form (see
	 * {@code RESTUtility#parseDate(String)} for parsing this value.
	 */
	public String modified;

	/**
	 * For a file, this is the modification time set by the client when the file
	 * was added to Dropbox. Since this time is not verified (the Dropbox server
	 * stores whatever the client sends up) this should only be used for display
	 * purposes (such as sorting) and not, for example, to determine if a file
	 * has changed or not.
	 * 
	 * <p>
	 * This is not set for folders.
	 * </p>
	 */
	public String clientMtime;

	/** Path to the file from the root. */
	public String path;

	/**
	 * Name of the root, usually either "dropbox" or "app_folder".
	 */
	public String root;

	/**
	 * Human-readable (and localized, if possible) description of the file size.
	 */
	public String size;

	/** The file's MIME type. */
	public String mimeType;

	/**
	 * Full unique ID for this file's revision. This is a string, and not
	 * equivalent to the old revision integer.
	 */
	public String rev;

	/** Whether a thumbnail for this is available. */
	public boolean thumbExists;

	/**
	 * Whether this entry has been deleted but not removed from the metadata
	 * yet. Most likely you'll only want to show entries with isDeleted ==
	 * false.
	 */
	public boolean isDeleted;

	/** A list of immediate children if this is a directory. */
	public List<DropboxEntry> contents;

	/**
	 * Creates an entry from a map, usually received from the metadata call.
	 * It's unlikely you'll want to create these yourself.
	 * 
	 * @param map
	 *            the map representation of the JSON received from the metadata
	 *            call, which should look like this:
	 * 
	 *            <pre>
	 * {
	 *    "hash": "528dda36e3150ba28040052bbf1bfbd1",
	 *    "thumb_exists": false,
	 *    "bytes": 0,
	 *    "modified": "Sat, 12 Jan 2008 23:10:10 +0000",
	 *    "path": "/Public",
	 *    "is_dir": true,
	 *    "size": "0 bytes",
	 *    "root": "dropbox",
	 *    "contents": [
	 *    {
	 *        "thumb_exists": false,
	 *        "bytes": 0,
	 *        "modified": "Wed, 16 Jan 2008 09:11:59 +0000",
	 *        "path": "/Public/\u2665asdas\u2665",
	 *        "is_dir": true,
	 *        "icon": "folder",
	 *        "size": "0 bytes"
	 *    },
	 *    {
	 *        "thumb_exists": false,
	 *        "bytes": 4392763,
	 *        "modified": "Thu, 15 Jan 2009 02:52:43 +0000",
	 *        "path": "/Public/\u540d\u79f0\u672a\u8a2d\u5b9a\u30d5\u30a9\u30eb\u30c0.zip",
	 *        "is_dir": false,
	 *        "icon": "page_white_compressed",
	 *        "size": "4.2MB"
	 *    }
	 *    ],
	 *    "icon": "folder_public"
	 * }
	 * </pre>
	 */
	public DropboxEntry(Map<String, Object> map) {
		update(map);
	}

	public DropboxEntry() {
	}

	@SuppressWarnings("unchecked")
	public void update(Map<String, Object> map) {
		if (map == null) {
			isDeleted = true;
			return;
		}
		bytes = getFromMapAsLong(map, "bytes");
		hash = (String) map.get("hash");
		icon = (String) map.get("icon");
		isDir = getFromMapAsBoolean(map, "is_dir");
		modified = (String) map.get("modified");
		clientMtime = (String) map.get("client_mtime");
		path = (String) map.get("path");
		root = (String) map.get("root");
		size = (String) map.get("size");
		mimeType = (String) map.get("mime_type");
		rev = (String) map.get("rev");
		thumbExists = getFromMapAsBoolean(map, "thumb_exists");
		isDeleted = getFromMapAsBoolean(map, "is_deleted");

		Object json_contents = map.get("contents");
		if (json_contents != null && json_contents instanceof Collection<?>) {
			contents = new ArrayList<DropboxEntry>();
			Object entry;
			Iterator<?> it = ((Collection<?>) json_contents).iterator();
			while (it.hasNext()) {
				entry = it.next();
				if (entry instanceof Map) {
					contents.add(new DropboxEntry((Map<String, Object>) entry));
				}
			}
		} else {
			contents = null;
		}
	}

	
	/**
	 * Returns the file name if this is a file (the part after the last slash in
	 * the path).
	 */
	public String fileName() {
		int ind = path.lastIndexOf('/');
		return path.substring(ind + 1, path.length());
	}

	/**
	 * Returns the path of the parent directory if this is a file.
	 */
	public String parentPath() {
		if (path.equals("/")) {
			return "";
		} else {
			int ind = path.lastIndexOf('/');
			return path.substring(0, ind + 1);
		}
	}

	/**
	 * @return the bytes
	 */
	public long getBytes() {
		return bytes;
	}

	/**
	 * @param bytes the bytes to set
	 */
	public void setBytes(long bytes) {
		this.bytes = bytes;
	}

	/**
	 * @return the hash
	 */
	public String getHash() {
		return hash;
	}

	/**
	 * @param hash the hash to set
	 */
	public void setHash(String hash) {
		this.hash = hash;
	}

	/**
	 * @return the icon
	 */
	public String getIcon() {
		return icon;
	}

	/**
	 * @param icon the icon to set
	 */
	public void setIcon(String icon) {
		this.icon = icon;
	}

	/**
	 * @return the isDir
	 */
	public boolean isDir() {
		return isDir;
	}

	/**
	 * @param isDir the isDir to set
	 */
	public void setDir(boolean isDir) {
		this.isDir = isDir;
	}

	/**
	 * @return the modified
	 */
	public String getModified() {
		return modified;
	}

	/**
	 * @param modified the modified to set
	 */
	public void setModified(String modified) {
		this.modified = modified;
	}

	/**
	 * @return the clientMtime
	 */
	public String getClientMtime() {
		return clientMtime;
	}

	/**
	 * @param clientMtime the clientMtime to set
	 */
	public void setClientMtime(String clientMtime) {
		this.clientMtime = clientMtime;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return the root
	 */
	public String getRoot() {
		return root;
	}

	/**
	 * @param root the root to set
	 */
	public void setRoot(String root) {
		this.root = root;
	}

	/**
	 * @return the size
	 */
	public String getSize() {
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(String size) {
		this.size = size;
	}

	/**
	 * @return the mimeType
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * @param mimeType the mimeType to set
	 */
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	/**
	 * @return the rev
	 */
	public String getRev() {
		return rev;
	}

	/**
	 * @param rev the rev to set
	 */
	public void setRev(String rev) {
		this.rev = rev;
	}

	/**
	 * @return the thumbExists
	 */
	public boolean isThumbExists() {
		return thumbExists;
	}

	/**
	 * @param thumbExists the thumbExists to set
	 */
	public void setThumbExists(boolean thumbExists) {
		this.thumbExists = thumbExists;
	}

	/**
	 * @return the isDeleted
	 */
	public boolean isDeleted() {
		return isDeleted;
	}

	/**
	 * @param isDeleted the isDeleted to set
	 */
	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	/**
	 * @return the contents
	 */
	public List<DropboxEntry> getContents() {
		return contents;
	}

	/**
	 * @param contents the contents to set
	 */
	public void setContents(List<DropboxEntry> contents) {
		this.contents = contents;
	}

	
	
}
