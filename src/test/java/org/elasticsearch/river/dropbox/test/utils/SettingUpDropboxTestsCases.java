package org.elasticsearch.river.dropbox.test.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.scribe.model.Token;

import fr.pilato.elasticsearch.river.dropbox.connector.DropboxConnector;

public class SettingUpDropboxTestsCases {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("******************************************************");
		System.out.println("*        Dropbox test case settings generator        *");
		System.out.println("*                                                    *");
		System.out.println("* 1) Create your own DropBox APP on                  *");
		System.out.println("*            https://www.dropbox.com/developers/apps *");
		System.out.println("* 2) Note your app key and secret                    *");
		System.out.println("* 3) Follow the following instructions               *");
		System.out.println("*                                                    *");
		System.out.println("* (c) David Pilato aka dadoonet, France, 2012        *");
		System.out.println("******************************************************");
		String appkey = ask("a) Enter your dropbox App Key : ");
		String appsecret = ask("b) Enter your dropbox App Secret : ");
		
		DropboxConnector dbConn = new DropboxConnector(appkey, appsecret);
		String authUrl = dbConn.getAuthUrl();
		
		System.out.println("c) Open your browser and connect to : " + authUrl);
		System.out.println("d) Give authorization to your account");
		ask("e) When done (and only when done), press enter :");
		
		Token accessToken = dbConn.computeAccessToken().getAccessToken();

		System.out.println("");
		System.out.println("You can now edit your /src/test/resources/dropbox.properties file with the following :");

		System.out.println("! Fill you dropbox App Key here");
		System.out.println("! See https://www.dropbox.com/developers/apps - Create An App");
		System.out.println("app.key=" + appkey);
		System.out.println("app.secret=" + appsecret);
		System.out.println("");
		System.out.println("! You also need to authorize your application for your dropbox account");
		System.out.println("token=" + accessToken.getToken());
		System.out.println("secret=" + accessToken.getSecret());

	}
	
	private static String ask(String question) {
	      //  prompt the user to enter their name
	      System.out.print(question);

	      //  open up standard input
	      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

	      String answer = null;

	      //  read the username from the command-line; need to use try/catch with the
	      //  readLine() method
	      try {
	    	  answer = br.readLine();
	      } catch (IOException ioe) {
	         System.err.println("Can not read your answer....");
	         System.exit(1);
	      }

	      return answer;
		
	}

}
