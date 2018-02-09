
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.Vector;


/**
* @IgbareaAhmad 
* @AliNaserLoay 
*/

// the Server class
public class SentenceServer {
	

	 // PortServer
	 public static int PORT ;
	 public static void main(String[] args) {
		// get Port 
		PORT= Config.getPort();
		// check port 
		while(PORT<1023 || PORT>65535 ) {
			System.out.print(msgs.ERRORPORT);
			try {
				Config.setPort();
				PORT= Config.getPort();	
			}catch (Exception e) {
				msgs.printMsg(msgs.CONFIGERROR);
			}
		}

		System.out.println("Server Port :"+ PORT );

		///////////////////////////////Have Port///////////////////////////////////////////
		
		
		System.out.println("Choose current IP addresses to listen- :");
		for(int i = 0 ;i<3;i++)
		{
			System.out.println(i +" : "+ msgs.InputMessages[i]);
		}
		Scanner scan = new Scanner(System.in);
		int choice = -1;
		while ( choice < 0 || choice > 2)
		{
			try {				
				choice = scan.nextInt();
			}
			catch (Exception ex) {
				System.out.print("Error parsing choice\n : ");
			}			
		}
		
		InetAddress address = null;;
		switch (choice) {
		case 0:
			// list
			Vector<InetAddress> adds = getListAddresses();
			System.out.print("Choose an IP address to listen on \n :");
			for (int i = 0; i < adds.size(); i++)
			{
				// show it in the list
				System.out.println(i + ": " + adds.elementAt(i).toString());			
			}

		    choice = -1;
			while ( choice < 0 || choice >= adds.size())
			{
				System.out.print(": ");
				try {				
					choice = scan.nextInt();
				}
				catch (Exception ex) {
					System.out.print("Error parsing choice\n :");
				}			
			} 
			address = adds.elementAt(choice);
			break;
		case 1:
			// 0.0.0.0
			try {
				address = InetAddress.getByAddress(new byte[] {0, 0, 0, 0});
				//adds.addElement(InetAddress.getLoopbackAddress());
			} catch (UnknownHostException ex) {
				// something is really weird - this should never fail
				System.out.println("Can't find IP address 0.0.0.0: " + ex.getMessage());
				ex.printStackTrace();
				return;
			}			
			break;
			
		case 2:
			// read line 
				boolean flag =true;
				do {
				 System.out.println("Please enter your VSE IP address:");
				 String ipAddr = scan.nextLine();
					 try {
						 boolean result = validIP(ipAddr);
						 if(!result){
							 System.out.print("Error parsing choice IPAddress\n  Enter For Example 192.0.19.5 :");
							 continue;
						 }else {
							address = InetAddress.getByName(ipAddr);
							flag =false;
						 }
					} catch (UnknownHostException e1) {
						// TODO Auto-generated catch block
						System.out.print("Error parsing choice IPAddress\n  Enter For Example 192.0.19.5 :");
						e1.printStackTrace();
					}
				}while(flag);
			break;
		}
		
		
		String lineIn = "";		
		Boolean quit = false;
		
		// start to listen on the one that the user chose
		ServerSocket listener;
		do {
			try {
			    listener = new ServerSocket(PORT, 50, address);
				Listening listening = new Listening(listener);
				listening.start();

			} catch (IOException e) {
				// fatal error, just quit
				System.out.println("Can't listen on " + address + ":" + PORT);			
				e.printStackTrace();
				return;
			}
			
			BufferedReader brIn = new BufferedReader(new InputStreamReader(System.in));
			// listen for the command to stop listening
			do {
				// we now have a working server socket, we'll use it later
				System.out.println("Listening on " + listener.getLocalSocketAddress().toString());
				System.out.println("Enter 'STOP' to stop listening");

				try {
					lineIn = brIn.readLine();
				} catch (IOException ex) 
				{ 
					System.out.println("Error in reading from console: " + ex.getMessage()); 
				}
			} while (!lineIn.trim().toLowerCase().equals("stop"));
			
			// stop listening
			try {
				listener.close();
			} catch (IOException e) {
				// error while stopping to listen?  weird
				System.out.println("Error stopping listening: " + e.getMessage());
			}

			// now we can resume listening if we want
			System.out.println("Resume listening? [y/n]");
			do {
				System.out.print(": ");
				try {
					lineIn = brIn.readLine();
				} catch (IOException ex) 
				{ 
					System.out.println("Error in reading from console: " + ex.getMessage()); 
				}

			} while ( !lineIn.trim().toLowerCase().equals("y") && !lineIn.trim().toLowerCase().equals("n"));

			// see whether we have an n or a y
			if ( lineIn.trim().equals("Y"))
			{
				quit = false;
				System.out.println("Resuming listening");
			}
			else
			{
				quit = true;	
				// quitting
				System.out.println("Bye!");
			}
		} while (!quit);
		
		
	}

			public static Vector<InetAddress> getListAddresses()
			{
				Vector<InetAddress> adds = new Vector<InetAddress>();
				try {
					// get the local IP addresses from the network interface listing
					Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

					while ( interfaces.hasMoreElements() )
					{
						NetworkInterface ni = interfaces.nextElement();
						// see if it has an IPv4 address
						Enumeration<InetAddress> addresses =  ni.getInetAddresses();
						while ( addresses.hasMoreElements())
						{
							// go over the addresses and add them
							InetAddress add = addresses.nextElement();
							if (!add.isLoopbackAddress())
							{
								adds.addElement(add);
							}				
						}
					}
				}
				catch (SocketException ex)
				{
					// can't get local addresses, something's wrong
					System.out.println("Can't get network interface information: " + ex.getLocalizedMessage());
					return null;
				}
				return adds;
			}
			public static boolean validIP (String ip) {
			    try {
			        if ( ip == null || ip.isEmpty() ) {
			            return false;
			        }

			        String[] parts = ip.split( "\\." );
			        if ( parts.length != 4 ) {
			            return false;
			        }

			        for ( String s : parts ) {
			            int i = Integer.parseInt( s );
			            if ( (i < 0) || (i > 255) ) {
			                return false;
			            }
			        }
			        if ( ip.endsWith(".") ) {
			            return false;
			        }

			        return true;
			    } catch (NumberFormatException nfe) {
			        return false;
			    }
			}
			
}
