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
