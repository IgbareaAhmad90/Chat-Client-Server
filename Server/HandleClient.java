
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;



public class HandleClient extends Thread {
	
	
	private static final DateFormat sdf = new SimpleDateFormat("HH:mm:ss");

	//modified Lists

	private Socket socket = null;
	private BufferedReader in = null;
	private PrintStream out = null;
	private ArrayList<Topic> topics;
	
	public HandleClient (Socket client) throws IOException
	{
		super();
		this.socket = client;
		this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
		this.out= new  PrintStream(this.socket.getOutputStream());
		this.topics = new ArrayList<Topic>(); // new array Topics for this User
		
		out.println(msgs.CONNECTED);
	}
	
	private boolean containsTopic(String topicName) {
		
		for(int i = 0;i<this.topics.size();i++)
		{
			if(this.topics.get(i).name.equals(topicName))
				return true;
		}
		return false;
	}
	private int indexTopic(String topicName) {
		
		for(int i = 0;i<topics.size();i++)
		{
			if(topics.get(i).name.equals(topicName))
				return i;
		}
		return -1;
	}	

	
	private String register(String input)
	{
		
		 if( input.isEmpty() || (!input.contains(" ")) || (input.split(" ",2).length != 2))
		 {
			System.out.println(msgs.ERRORIN);
			 return msgs.ERRORIN;
		 }
		
		String line[] = input.split(" ",2);
		String topicName = line[1];
		int indexLocal = this.indexTopic(topicName); // index topic
		int indexGlobal = Listening.indexTopic(topicName);
		
		if(this.containsTopic(topicName)) 
		{	// if topic is exist  my topics?
			if(this.topics.get(indexLocal).clients.contains(this))
			{
				System.out.println(msgs.userExist);
				return msgs.userExist; // User is exist on this topic
			}
				// add User - user not exist
				// add to local array my Topics
				this.topics.get(indexLocal).clients.add(this);
				// add to global array all topics 
				Listening.topics.get(indexGlobal).clients.add(this);
				
				System.out.println("OK - Join agin this Topic "+topicName);
				return msgs.OK;
			
		}else if(Listening.containsTopic(topicName))
			{   // if topic exist on global array 
				Listening.topics.get(indexGlobal).clients.add(this); // add client to Global Topics
				this.topics.add(Listening.topics.get(indexGlobal)); // add Topic to myTopic
				
				System.out.println();
				return msgs.OK;

			}
			else 
			{	// new Topic 
				Topic t = new Topic(topicName);  
				t.clients.add(this);
				this.topics.add(t);	 // add topic to myTopic
				Listening.topics.add(t); // add topic to Global Topic 
				
				System.out.println("OK - add  new Topic "+topicName);
				return msgs.OK;
			}
	}
	private String leave(String input)
	{
		 if( input.isEmpty() || (!input.contains(" ")) || (input.split(" ",2).length != 2))
		 {
			System.out.println(msgs.ERRORIN);
			 return msgs.ERRORIN;
		 }
		 
		 String line[] = input.split(" ",2);
		 String topicName = line[1];
		
		if(this.containsTopic(topicName)) 
		{	// topic exist
			int index = this.indexTopic(topicName); // index topic
				if(this.topics.get(index).clients.contains(this))
				{	// Remove User   
					
					// local array  my topics 
					this.topics.get(index).clients.remove(this);
					this.topics.remove(index);
					
					// global array all the topics 
					index = Listening.indexTopic(topicName);
					Listening.topics.get(index).clients.remove(this);
					if(Listening.topics.get(index).clients.size() == 0 )
					{
						Listening.topics.remove(index);
					}
					System.out.println("LEAVE - user leave this Topic"+ topicName);
					return msgs.OK;
				}else{
					// User not Exist
					System.out.println(msgs.topicNotExist);
					return msgs.userNotExist;
				}
		}else{
			// Topic not Exist
			System.out.println(msgs.topicNotExist);
			return msgs.topicNotExist;
		}
	}
	private String send(String input)
	{
		 if( input.isEmpty() || (!input.contains(" ")) || (input.split(" ",2).length != 2))
		 {
			System.out.println(msgs.ERRORIN);
			 return msgs.ERRORIN; /*"SEND"+*/
		 }
		 
		String line[] = input.split(" ",3);
		String topicName = line[1];
		String msg = line[2];
		
		if(!Listening.containsTopic(topicName))
		{	System.out.println(msgs.topicNotExist);
			return msgs.topicNotExist; /*"SEND"+*/

		}
		
			// topic exist
			int index = Listening.indexTopic(topicName); // index topic
			int ListUsersSize = Listening.topics.get(index).clients.size();
			
			
			for(int i =0;i <ListUsersSize;i++) {	
				if(Listening.topics.get(index).clients.get(i)==this)
					continue;
				String ip =Listening.topics.get(index).clients.get(i).
						socket.getInetAddress().toString();
				String port = Listening.topics.get(index).clients.get(i).
						socket.getPort()+"";
				Listening.topics.get(index).clients.get(i).
				out.println("FORWORD "+topicName+" "
						+ip+":"+port+" "+sdf.format(new java.util.Date()) +" "+msg);
				Listening.topics.get(index).clients.get(i).out.flush();
				//Listening.topics.get(index).clients.get(i).out.println("Finish2");
			}
			System.out.println("send this msg: "+msg+ " to this Topic: "+topicName);
			return msgs.OK;
	}
	private void close() {
		for(int i=0;i<Listening.topics.size();i++)
		{
			if(Listening.topics.get(i).clients.contains(this))
				Listening.topics.get(i).clients.remove(this);
		}
		Listening.ClientsThread.remove(this);
		System.out.println("Client "+ this.socket.getInetAddress()+":"+this.socket.getPort() +"is Closed Connection");
	}
	
	@Override
	public void run()
	{
		String  clientAddress = socket.getRemoteSocketAddress().toString();
		
		System.out.println("Connect Received connection from: " + clientAddress);
		try {
			while (true)
			{
				 String inputString = in.readLine().trim();
				 if(inputString == null)
					 break;
				 // array to Split The First Words
				 if(inputString.contains(" ") && !inputString.isEmpty())
				 {
					  String operation = inputString.split(" ",2)[0];
				 switch (operation.toUpperCase().toString()) {
					case "REGISTER":
						new Thread(new Runnable() {
								public void run() {
									synchronized (this) {
										out.println(register(inputString));
										out.flush();
									}
								}
							}).start();
						
						break;
					case "LEAVE":
						new Thread(new Runnable() {
							public void run() {
								synchronized (this) {
									out.println(leave(inputString));
									out.flush();
								}
							}
						}).start();
					
					break;
					case "SEND":
						new Thread(new Runnable() {
							public void run() {
								synchronized (this) {
									out.println(send(inputString));
									out.flush();
								}
							}
						}).start();
				
					break;
					}			  
				 }else if(inputString.toUpperCase().equals("CLOSE"))
				 {
					 new Thread(new Runnable() {
							public void run() {
								synchronized (this) {
									out.println("CLOSE");
									out.flush();
									close();
								}
							}
						}).start();
					 break;
				 }
			}
		}catch (IOException ex)
		{
			System.out.println("Something went wrong: " + ex.getMessage());
			ex.printStackTrace();
		}
		try {
			 socket.close(); 
		} catch (IOException ex) {}
		System.out.println("Closed connection");
	}
	
	

}
		
