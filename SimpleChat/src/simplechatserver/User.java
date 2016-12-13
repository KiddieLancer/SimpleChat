package simplechatserver;

public class User {

	private String user = "";
	private String socketAddress = "";
	private boolean registered = true;
	
	public User(String user, String socketAddress){
		this.setUser(user);
		this.setSocketAddress(socketAddress);
		System.out.println("New user registered: " + this.user + "/" + this.socketAddress);
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public boolean isRegistered() {
		return registered;
	}

	public void setRegistered(boolean registered) {
		this.registered = registered;
	}

	public String getSocketAddress() {
		return socketAddress;
	}

	public void setSocketAddress(String socketAddress) {
		this.socketAddress = socketAddress;
	}
	
}
