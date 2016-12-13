package simplechatserver;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
 
public class Server implements Runnable, StandardParameters {
 
    private static Logger logger = Logger.getLogger("ChatLog");
	private int serverPort = 0;
    private boolean running = true;
	private ArrayList<User> users = new ArrayList<User>();
	private ServerView sv = null;
	
	public static void main(String[] args){
		Server server = null;
		if (args.length != 1) {
			server = new Server(PORT);
        } else {
        	server = new Server(Integer.parseInt(args[0]));
        }
		server.run();
	}
	
	public Server(int serverPort) {
		this(serverPort, null);
	}
	
	public Server(int serverPort, ServerView sv) {
		Handler fh = null;
		try {
			fh = new FileHandler("%t/ChatLog.log");
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.addHandler(fh);
		logger.setLevel(Level.SEVERE);
		this.serverPort = serverPort;
		this.sv = sv;
	}

 	public void run() {
 		ServerSocket serverSocket = null;
		Socket socket = null;
		String messageToReturn = "";
		try {
			display("Server started and listening to port " + serverPort);
			serverSocket = new ServerSocket(serverPort);
			String messageRead = "";
			while(running) {
				try {
					display("Waiting for clients");
					socket = serverSocket.accept();
					display("Client " + socket.getRemoteSocketAddress() + " connected");
					InputStream is = socket.getInputStream();
	                InputStreamReader isr = new InputStreamReader(is);
	                BufferedReader br = new BufferedReader(isr);
	                messageRead = br.readLine();
	                String userRegistered = getUserRegistered(socket.getRemoteSocketAddress().toString());
	                if (userRegistered != ""){
		                display("Message received from client (" + userRegistered + ") is: " + messageRead);
	                } else {
		                display("Message received from client (" + socket.getRemoteSocketAddress() + ") is: " + messageRead);
	                }
					Message messageStored = new Message(messageRead, Sender.Client, users, socket.getRemoteSocketAddress(), sv);
					switch(messageStored.getMessageType()){
				        case ERROR:
				            logger.log(Level.SEVERE, messageStored.getMessage());
				        break;
				        case MESSAGE:
				            logger.log(Level.FINEST, messageStored.toString());
				        break;
				        case COMMAND:
				            logger.log(Level.FINE, messageStored.toString());
			            break;
					}
				} catch (IOException e){
					running = false;
					display(e.getMessage() + " #020");
				}
				try {
					OutputStream os = socket.getOutputStream();
					OutputStreamWriter osw = new OutputStreamWriter(os);
					BufferedWriter bw = new BufferedWriter(osw);
					messageToReturn = "Server: Message \"" + messageRead + "\" received";
	                bw.write(messageToReturn + "\n");
	                bw.flush();
	                display("Server: Message sent to the client is " + messageToReturn + " " + new Date());
				} catch (IOException e){
					display(e.getMessage() + " #030");
				}
			}
			logger.fine("done");
		} catch (IOException e) {
			display(e.getMessage() + " #010");
		}
		finally {
			try {
				//closing the socket/server socket
				socket.close();
				serverSocket.close();
			} catch (IOException e){
				display(e.getMessage());
			}
		}
	}
	
	private String getUserRegistered(String socketAddress) {
    	socketAddress = socketAddress.toString().substring(0, socketAddress.toString().lastIndexOf(":"));
		for (User element: users){
			if (element.getSocketAddress().equals(socketAddress)){
				return element.getUser();
			}
		}
		return "";
	}

	private void display(String str) {
    	if (sv == null){
    		System.out.println(str);
    	}
    	else {
    		sv.display(str + "\n");
    	}
	}

	public void stop(){
		running = false;
	}

	public ArrayList<User> getUsers() {
		return users;
	}

	public void setUsers(ArrayList<User> users) {
		this.users = users;
	}

}
	
