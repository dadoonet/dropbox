package org.elasticsearch.river.dropbox.test;


import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import fr.pilato.elasticsearch.river.dropbox.river.DropBoxRiverUtil;

public class DropboxMatchFilesTest {
	
	@Test
	public void exclude_only() throws Exception {
		Assert.assertFalse(DropBoxRiverUtil.isIndexable("test.doc", new ArrayList<String>(), Arrays.asList("*.doc")));
		Assert.assertTrue(DropBoxRiverUtil.isIndexable("test.xls", new ArrayList<String>(), Arrays.asList("*.doc")));
		Assert.assertTrue(DropBoxRiverUtil.isIndexable("my.doc.xls", new ArrayList<String>(), Arrays.asList("*.doc")));
		Assert.assertFalse(DropBoxRiverUtil.isIndexable("my.doc.xls", new ArrayList<String>(), Arrays.asList("*.doc","*.xls")));
		Assert.assertFalse(DropBoxRiverUtil.isIndexable("my.doc.xls", new ArrayList<String>(), Arrays.asList("my.d?c*.xls")));
		Assert.assertTrue(DropBoxRiverUtil.isIndexable("my.douc.xls", new ArrayList<String>(), Arrays.asList("my.d?c*.xls")));
	}

	@Test
	public void include_only() throws Exception {
		Assert.assertTrue(DropBoxRiverUtil.isIndexable("test.doc", Arrays.asList("*.doc"), new ArrayList<String>()));
		Assert.assertFalse(DropBoxRiverUtil.isIndexable("test.xls", Arrays.asList("*.doc"), new ArrayList<String>()));
		Assert.assertFalse(DropBoxRiverUtil.isIndexable("my.doc.xls", Arrays.asList("*.doc"), new ArrayList<String>()));
		Assert.assertTrue(DropBoxRiverUtil.isIndexable("my.doc.xls", Arrays.asList("my.d?c*.xls"), new ArrayList<String>()));
		Assert.assertFalse(DropBoxRiverUtil.isIndexable("my.douc.xls", Arrays.asList("my.d?c*.xls"), new ArrayList<String>()));
	}

	@Test
	public void include_exclude() throws Exception {
		Assert.assertFalse(DropBoxRiverUtil.isIndexable("test.doc", Arrays.asList("*.xls"), Arrays.asList("*.doc")));
		Assert.assertTrue(DropBoxRiverUtil.isIndexable("test.xls", Arrays.asList("*.xls"), Arrays.asList("*.doc")));
		Assert.assertTrue(DropBoxRiverUtil.isIndexable("my.doc.xls", Arrays.asList("*.xls"), Arrays.asList("*.doc")));
		Assert.assertFalse(DropBoxRiverUtil.isIndexable("my.doc.xls", Arrays.asList("*.xls"), Arrays.asList("my.d?c*.xls")));
		Assert.assertTrue(DropBoxRiverUtil.isIndexable("my.douc.xls", Arrays.asList("*.xls"), Arrays.asList("my.d?c*.xls")));
	}

}
