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

public class PostOption {
	private String option;
	private String value;
	
	public PostOption() {
	}
	
	public PostOption(String option, String value) {
		super();
		this.option = option;
		this.value = value;
	}

	/**
	 * @return the option
	 */
	public String getOption() {
		return option;
	}
	
	/**
	 * @param option the option to set
	 */
	public void setOption(String option) {
		this.option = option;
	}
	
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
	
	
}
