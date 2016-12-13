package simplechatclient;

import java.io.*;
import java.net.*;
import java.util.*;

import simplechatserver.StandardParameters;

public class ClientNetworkHandler extends Observable implements Runnable, StandardParameters {

	private boolean running = true;  // flag to stop thread
    private String message = "";     // Never modify yourself! 
                                     // use receivedMessage
    private String serverAddress = "";
    private int serverPort = 0;
	private ClientView cv = null;
	private static Socket socket = null;
	public static void main(String[] args){
    	ClientNetworkHandler networkHandler = null;
		if (args.length != 2) {
			networkHandler = new ClientNetworkHandler(HOST, PORT);
		} else {
			networkHandler = new ClientNetworkHandler(args[0], Integer.parseInt(args[1]));
		}
		networkHandler.run();
    }
    
    public ClientNetworkHandler(String serverAddress, int serverPort) {
    	this(serverAddress, serverPort, null);
    }
    
    public ClientNetworkHandler(String serverAddress, int serverPort, ClientView cv) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.cv = cv;
    }
    
	public void run() {
	    display("Client Network Handler started");
		while (running){
			if (message != ""){
			    try {
					socket = new Socket(serverAddress, serverPort);
					socket.getLocalAddress().toString();
					//Send the message to the server
					OutputStream os = socket.getOutputStream();
		            OutputStreamWriter osw = new OutputStreamWriter(os);
		            BufferedWriter bw = new BufferedWriter(osw);
		            String messageToSend = message;
		            bw.write(messageToSend + "\n");
		            bw.flush();
		            display("Message sent to server: " + messageToSend + " " + new Date());
		            //Get the return message from the server
		            InputStream is = socket.getInputStream();
		            InputStreamReader isr = new InputStreamReader(is);
		            BufferedReader br = new BufferedReader(isr);
		            String messageFromServer = br.readLine();
		            display("Message received from server: " + messageFromServer + " " + new Date());
		            if (message.contains("quit")){
		            	display("message: " + message);
		            	running = false;
		            	try {
		            		display("quitting...");
		            		Thread.sleep(5000);
		            	} catch (InterruptedException e){
		            		display(e.getMessage());
		            	}
		            	cv.stop();
		            }
					message = "";
				} catch (IOException e) {
					display(e.getMessage());
				}
			    finally {
			    	//Closing the socket
			    	try {
			    		socket.close();
			    	} catch (Exception e){
			    		display(e.getMessage());
			    	}
			    }
			} else {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				//display("no message yet");
			}
		}
	    display("Client Network Handler stopped");
    }

    public void stop() {
        running = false;
    }

    /*
     * This message is called by the GUI to send a message
     */
    public synchronized void sendMessage(String message) throws Exception {
    	receivedMessage(message);
    }

     /* 
     * Call this to send a received message to the GUI
     */
    private synchronized void receivedMessage(String messageString) throws Exception {
        this.message = messageString;
        setChanged();
        notifyObservers();
    }

    /* 
     * This method is called by the GUI-Thread
     */
    public synchronized String getMessage() {
        return message;
    }

    public void display(String str){
    	if (cv == null){
    		System.out.println(str);
    	}
    	else {
    		cv.display(str + "\n");
    	}
    }
}
