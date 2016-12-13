package simplechatserver;

import java.net.SocketAddress;
import java.util.ArrayList;

public class Message {
	
	private final String USER_NOT_REGISTERED = "You are not registered!";
	private String message = "";
    private MessageType messageType = null;
    private Sender sender = null;
    private String socketAddress = "";
    private final int maxHeaderLength = 10; 
    private final int maxMessageLength = 10000;
    private final char messageDelimiter = '@';
    private final String messageDelimiters = "@@";
    private ArrayList<User> users = null;
	private ServerView sv = null;
    
    public Message(String message, Sender sender, ArrayList<User> users, SocketAddress socketAddress, ServerView sv) throws IllegalArgumentException {
    	this.setMessage(message);
    	this.setSender(sender);
    	this.socketAddress = socketAddress.toString().substring(0, socketAddress.toString().lastIndexOf(":"));
    	this.users = users;
    	this.sv  = sv;
    	formatMessage();
	}

	private void formatMessage() throws IllegalArgumentException{
		String confirmMessage = "";
		int payLoadLength = message.length();
    	if (payLoadLength > maxMessageLength){
    		messageType = MessageType.ERROR;
    	} else {
    		/* define header */
    		String header = "";
			int countDelimiters = 0;
			for (int i = 0; i < message.length(); i++){
				if (message.charAt(i) != messageDelimiter){
					header += message.charAt(i);
				} else {
					countDelimiters++;
					if (countDelimiters == 2) break;
				}
			}
			/* message already formatted */
			if (countDelimiters == 2){
				/* header probably corrupted */
				if (header.length() > maxHeaderLength) throw new IllegalArgumentException();
				/* Define message type */
				char type = message.charAt(0);
				switch(sender){
		    		case Server: /* Server -> Client */
		    			switch (type){
			    			case 'C':
			    				messageType = MessageType.COMMAND;
			    			break;
			    			case 'E':
			    				messageType = MessageType.ERROR;
			    			break;
			    			case 'M':
			    				messageType = MessageType.MESSAGE;
			    			break;
		    			}
		    		break;
		    		case Client: /* Client -> Server */
		    			switch (type){
		    				case 'C':
		    					messageType = MessageType.COMMAND;
			    				switch (getCommandType(message)){
				    				case REGISTER:
				    					String userName = getUserToRegister(message);
				    					addUser(userName, socketAddress);
				    					confirmMessage = "C" + message.length() + messageDelimiters + message;
				    				break;
				    				case QUIT:
				    					confirmMessage = "C0" + messageDelimiters;
				    				break;
									case NOCOMMAND:
										messageType = MessageType.ERROR;
										if (userNotFound(socketAddress)) confirmMessage = "Ea" + USER_NOT_REGISTERED.length() + messageDelimiters + USER_NOT_REGISTERED;
									break;
									default:
									break;
			    				}
			    			break;
			    			case 'E':
			    				messageType = MessageType.ERROR;
			    			break;
			    			case 'M':
			    				messageType = MessageType.MESSAGE;
			    			break;
		    			}
		    		break;
	    		}
			} else { /* no header defined yet or message corrupted */
				switch (sender){
					case Server: /* Server -> Client */
						messageType = MessageType.COMMAND;
						confirmMessage = "C0" + messageDelimiter;
					break;
					case Client: /* Client -> Server */
						messageType = MessageType.COMMAND;
						switch(getCommandType(message)){
							case REGISTER:
								String userName = getUserToRegister(message);
								addUser(userName, socketAddress);
								confirmMessage = "C" + message.length() + messageDelimiters + message;
							break;
							case QUIT:
								confirmMessage = "C" + message.length() + messageDelimiters + message;
							break;
							case NOCOMMAND:
								messageType = MessageType.ERROR;
								if (userNotFound(socketAddress)) confirmMessage = "Eb" + USER_NOT_REGISTERED.length() + messageDelimiters + USER_NOT_REGISTERED;
							break;
						}
					break;
				}
			}
    	}
    	if (sender == Sender.Client){
    		confirmMessage = "Client -> Server: " + confirmMessage;
    	} else {
    		confirmMessage = "Server -> Client: " + confirmMessage;
    	}
    	display(confirmMessage);
    	setMessageType(messageType);
    }
    
	private void addUser(String userName, String socketAddress) {
		if (userNotFound(socketAddress)){
			User user = new User(userName, socketAddress);
			users.add(user);
		}
	}

	private void display(String str) {
    	if (sv == null){
    		System.out.println(str);
    	}
    	else {
    		sv.display(str + "\n");
    	}
	}
	
	private boolean userNotFound(String socketAddress) {
		for (int i = 0; i < users.size(); i++){
			if (users.get(i).getSocketAddress().equals(socketAddress)) return false;
		}
		return true;
	}

	private String getUserToRegister(String message){
		String[] userString = null;
		if (message.indexOf(messageDelimiters) != -1){
			String[] messageString = message.split(messageDelimiters);
			userString = messageString[1].split(" ");
		} else {
			userString = message.split(" ");
		}
		String userToRegister = "";
		for (int i = 1; i < userString.length; i++){
			userToRegister = userString[i];
		}
		return userToRegister;
	}

	private MessageCommandType getCommandType(String message) {
		if (message.toUpperCase().indexOf("REGISTER") != -1) return MessageCommandType.REGISTER;
		if (message.toUpperCase().indexOf("QUIT") != -1) return MessageCommandType.QUIT;
		return MessageCommandType.NOCOMMAND;	
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public MessageType getMessageType() {
		return messageType;
	}

	private void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

	public Sender getSender() {
		return sender;
	}

	public void setSender(Sender sender) {
		this.sender = sender;
	}

}
