/*
 * Licensed to David Pilato (the "Author") under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Author licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
