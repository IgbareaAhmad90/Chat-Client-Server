
public final class msgs {
	
	public static final String userExist = "ERROR 'User exist in this Topic '";
	public static final String userNotExist = "ERROR 'User not exist by this Topic '";
	public static final String topicExist = "";
	public static final String topicNotExist = "ERROR Topic not Exist";
	public static final String OK = "OK";
	public static final String ERRORIN = "ERROR Input";
	public static final String ERRORPORT = "ERROR you dont have a valid Port\nPlease Enter your port for Example 5555 (1023 - 6500) \n :";
	public static final String CONFIGERROR = "Error reading configuration";
	public static final String CONNECTED ="FORWORD Server         Welcome, You Are connected";
	
	
	public static final String InputMessages[] = {
			"Choose one from the list",
			"Listen on all available addresses (0.0.0.0)",
			"Listen on an IP address not in the list "};

	public static void printMsg(String msgError)
	{
		System.out.println(msgError);
	}



}
