import java.util.ArrayList;


public class Topic {
	
	public String name;
	public ArrayList<HandleClient> clients;
	
	
	public Topic(String topicName)
	{
		this.name= topicName;
		this.clients = new ArrayList<HandleClient>();
	}
	public Topic(String topicName,ArrayList<HandleClient> clientsInTopic) {
		this.name=topicName;
		this.clients=clientsInTopic;
	}
	

	
		

}
