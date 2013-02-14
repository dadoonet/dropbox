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

package org.elasticsearch.river.dropbox.itest;

import fr.pilato.elasticsearch.river.dropbox.connector.DropboxAccount;
import fr.pilato.elasticsearch.river.dropbox.connector.DropboxChanges;
import fr.pilato.elasticsearch.river.dropbox.connector.DropboxConnector;
import fr.pilato.elasticsearch.river.dropbox.connector.DropboxFile;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.util.Properties;

public class UserInfoTest {
	private static String appkey = null;
	private static String appsecret = null;
	private static String token = null;
	private static String secret = null;
	private static DropboxConnector dbConnector = null;

    private static final Logger logger = Logger.getLogger(UserInfoTest.class);
	/**
	 * Load Dropbox properties
	 * @throws Exception
	 */
	@BeforeClass static public void setup() throws Exception {
		Properties prop = new Properties();
		InputStream in = UserInfoTest.class.getResourceAsStream("/itest/dropbox.properties");
		prop.load(in);
		in.close();
		
		appkey = prop.getProperty("app.key", null);
		appsecret = prop.getProperty("app.secret", null);
		token = prop.getProperty("token", null);
		secret = prop.getProperty("secret", null);
		
		Assert.assertTrue("Before running tests, you have to modify dropbox.properties", appkey != null);
		Assert.assertTrue("Before running tests, you have to modify dropbox.properties", appsecret != null);
		Assert.assertTrue("Before running tests, you have to modify dropbox.properties", token != null);
		Assert.assertTrue("Before running tests, you have to modify dropbox.properties", secret != null);
		
		dbConnector = new DropboxConnector(appkey, appsecret, token, secret);
	}
	
	/**
	 * We check if we can retrieve user's data (account, account.email)
	 */
	@Test
	public void testUserInfo() {
		DropboxAccount account = dbConnector.getUserInfo();
		Assert.assertNotNull(account);
		Assert.assertNotNull(account.email);
	}
	
	@Test
	public void testFiles() {
		DropboxChanges changes = dbConnector.getDelta(null);
		
		int nbFiles = 0;
		int nbDir = 0;
		int nbDeleted = 0;
		
		for (DropboxFile dropboxFile : changes.getFiles()) {
			if (dropboxFile.getMeta() != null) {
				if (dropboxFile.getMeta().isDeleted) {
					nbDeleted++;
				} else {
					if (dropboxFile.getMeta().isDir) {
						nbDir++;
					} else {
						nbFiles++;
					}
				}
			}
		}

        logger.info("Dropbox Cursor : " + changes.getCursor());
        logger.info("  - Number of changes " + changes.getFiles().size());
        logger.info("  - dirs " + nbDir);
        logger.info("  - files " + nbFiles);
        logger.info("  - deleted " + nbDeleted);
	}
	
	@Test
	public void getFile() {
		byte[] file = dbConnector.getFiles("dropbox", "/test-es/Premierspas.pdf");
		Assert.assertNotNull(file);
        logger.info("File size : " + file.length);
		// write(file, "premierspas.pdf");
		
		file = dbConnector.getFiles("dropbox", "/ASL Angélique Myrtilles/Comité/CRRéunion-2011-07-04.doc");
		Assert.assertNotNull(file);
        logger.info("File size : " + file.length);
		// write(file, "cr.doc");
		
		
	}
	
	void write(byte[] aInput, String aOutputFileName){
        logger.info("Writing binary file...");
	    try {
	      OutputStream output = null;
	      try {
	        output = new BufferedOutputStream(new FileOutputStream(aOutputFileName));
	        output.write(aInput);
	      }
	      finally {
	        output.close();
	      }
	    }
	    catch(FileNotFoundException ex){
            logger.error("File not found.");
	    }
	    catch(IOException ex){
            logger.error(ex);
	    }
	  }
	  
}
