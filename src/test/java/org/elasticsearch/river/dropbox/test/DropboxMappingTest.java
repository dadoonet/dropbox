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

package org.elasticsearch.river.dropbox.test;


import org.apache.log4j.Logger;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.junit.Test;

import fr.pilato.elasticsearch.river.dropbox.river.DropBoxRiverUtil;

public class DropboxMappingTest {
	
	private Logger logger = Logger.getLogger(DropboxMappingTest.class);

	@Test
	public void fs_mapping_for_files() throws Exception {
		XContentBuilder xb = DropBoxRiverUtil.buildFsFileMapping();
		logger.debug("Mapping used for files : " + xb.string());
	}
	
	@Test
	public void fs_mapping_for_folders() throws Exception {
		XContentBuilder xb = DropBoxRiverUtil.buildFsFolderMapping();
		logger.debug("Mapping used for folders : " + xb.string());
	}

	@Test
	public void fs_mapping_for_meta() throws Exception {
		XContentBuilder xb = DropBoxRiverUtil.buildFsRiverMapping();
		logger.debug("Mapping used for river metadata : " + xb.string());
	}

}
