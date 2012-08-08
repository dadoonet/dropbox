package org.elasticsearch.river.dropbox.test;

import org.junit.Test;

import fr.pilato.elasticsearch.river.dropbox.connector.DropboxConnector;

public class InvalidTokenTest {
	/**
	 * We should get exception with bad values
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidAppKeys() {
		new DropboxConnector(null,  null);
	}
	
	/**
	 * We should get exception with bad values
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidTokens() {
		new DropboxConnector("dummy", "dummy", null, null);
	}
}
