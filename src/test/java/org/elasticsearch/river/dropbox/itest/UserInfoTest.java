package org.elasticsearch.river.dropbox.itest;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.pilato.elasticsearch.river.dropbox.connector.DropboxAccount;
import fr.pilato.elasticsearch.river.dropbox.connector.DropboxChanges;
import fr.pilato.elasticsearch.river.dropbox.connector.DropboxConnector;
import fr.pilato.elasticsearch.river.dropbox.connector.DropboxFile;

public class UserInfoTest {
	private static String appkey = null;
	private static String appsecret = null;
	private static String token = null;
	private static String secret = null;
	private static DropboxConnector dbConnector = null;

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
		
		// TODO Use a logger
		System.out.println(changes.getCursor() + " - " + changes.getFiles().size() + " : " + 
				nbDir + " dirs, " + nbFiles + " files, " + nbDeleted + " deleted");
	}
	
	@Test
	public void getFile() {
		byte[] file = dbConnector.getFiles("dropbox", "/test-es/premierspas.pdf");
		Assert.assertNotNull(file);
		// write(file, "premierspas.pdf");
		
		file = dbConnector.getFiles("dropbox", "/ASL Angélique Myrtilles/Comité/CRRéunion-2011-07-04.doc");
		Assert.assertNotNull(file);
		// write(file, "cr.doc");
		
		
	}
	
	void write(byte[] aInput, String aOutputFileName){
		System.out.println("Writing binary file...");
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
	      System.err.println("File not found.");
	    }
	    catch(IOException ex){
	    	System.err.println(ex);
	    }
	  }
	  
}
