

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Scanner;


public  class Config {
	 public static int getPort()
	 {		int ServerPort = 0 ;
			Properties prop = new Properties();
			InputStream input = null;
		 
			try {

				input = new FileInputStream("config.properties");
				// load a properties file
				prop.load(input);
				// get the property value and print it out
				ServerPort = Integer.parseInt(prop.getProperty("port"));
						
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return ServerPort;
	 }
	 public static boolean setPort() {
		    int ServerPort ;
			Properties prop = new Properties();
			OutputStream output = null;
			Scanner s = new Scanner(System.in);
			try {
				output = new FileOutputStream("config.properties");
				
				System.out.println("Enter Port Server for Example '4444' ");
				ServerPort = s.nextInt();
				if(ServerPort <= 65000)
				{
					// set the properties value
					prop.setProperty("port", ServerPort+"");
					System.out.print("set the properties Port ="+ ServerPort);
					// save properties to project root folder
					prop.store(output, null);
				}
				else 
					System.out.print("Error \n");

			} catch (IOException io) {
				io.printStackTrace();
			} finally {
				if (output != null) {
					try {
						output.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else return false;

			}
			return true;
		  }
 
}