


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Listening extends Thread {
	// Client Socket 
	 private static HandleClient clientThread = null ;
	 // List Of all Clients 
	 public static final ArrayList<HandleClient> ClientsThread = new  ArrayList<HandleClient>();
	 // List Of all Topics 
	 public static final ArrayList<Topic> topics = new ArrayList<Topic>();
	 
	 ServerSocket listeningSocket;
	public Listening (ServerSocket serverSocket)
	{
		// save the socket we've been provided
		listeningSocket = serverSocket;
	}
	
	@Override
	public void run()
	{
		// start to listen on the socket
		try {
			while (true)
			{
				System.out.println("Waiting ...");
				Socket clientSession = listeningSocket.accept();

				// see if we were interrupted - then stop
				if (this.isInterrupted())
				{
					System.err.println("Stopped listening since we were interrupted.");
					return;
				}
				// create a new handling thread for the client
				if(clientSession != null)
				 {
					 HandleClient clientThread = new HandleClient(clientSession);
					 ClientsThread.add(clientThread);
					 clientThread.start();
				 }		
		   }
		}catch (IOException e) {
			// problem with this connection, show the output and quit
			System.err.println("Error listening for connections: " + e.getMessage());
			return;
		}		
	}
	
	public static int indexTopic(String topicName) {
		
		for(int i = 0;i<topics.size();i++)
		{	
			if(topics.get(i).name.equals(topicName))
				return i;
		}
		return -1;
	}
	public static boolean containsTopic(String topicName) {
		
		for(int i = 0;i<topics.size();i++)
		{
			if(topics.get(i).name.equals(topicName))
				return true;
		}
		return false;
	}		 


}
